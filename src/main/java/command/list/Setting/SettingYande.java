package main.java.command.list.Setting;

import main.java.util.file.GuildSettingHelper;
import main.java.util.file.JsonFileManager;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.JsonKeys.YANDE_SETTING;
import static main.java.util.UrlDataGetter.getData;

public record SettingYande(GuildSettingHelper settingHelper) {

    private static ScheduledExecutorService threadPool = Executors.newSingleThreadScheduledExecutor();
    static Map<String, Map<String, List<String>>> map = new HashMap<>();
    static Map<String, Map<String, List<Integer>>> old = new HashMap<>();

    public void onGuildReady(GuildReadyEvent event) {
        JSONObject data = getSettingData(event.getGuild());
        if (data.length() == 0)
            return;

        for (String i : data.keySet()) {
            List<String> tags = new ArrayList<>();
            for (Object j : data.getJSONArray(i))
                tags.add((String) j);


            Map<String, List<String>> k = new HashMap<>();
            k.put(i, tags);

            map.put(event.getGuild().getId(), k);
        }

        startThread(event.getGuild());
    }

    private void startThread(Guild guild) {

        for (String i : map.get(guild.getId()).keySet()) {
            Map<String, List<Integer>> j = new HashMap<>();
            j.put(i, new ArrayList<>());
            old.put(guild.getId(), j);
        }

        if (threadPool != null && !threadPool.isShutdown())
            threadPool.shutdown();

        threadPool = Executors.newSingleThreadScheduledExecutor();
        threadPool.scheduleWithFixedDelay(() -> {
            if (map.get(guild.getId()).size() == 0)
                return;

            for (String i : map.get(guild.getId()).keySet()) {
                for (String j : map.get(guild.getId()).keySet()) {
                    JSONArray array = new JSONArray(getData("https://yande.re/post.json?limit=1&tags=" + j));

                    if (array.length() != 0) {
                        int id = array.getJSONObject(0).getInt("id");
                        if (!old.get(guild.getId()).get(i).contains(id)) {
                            old.get(guild.getId()).get(i).add(id);
                            TextChannel channel;
                            if ((channel = guild.getTextChannelById(i)) != null)
                                if (channel.isNSFW())
                                    channel.sendMessage("https://yande.re/post/show/" + id).queue();
                                else
                                    channel.sendMessage("請開啟 NSFW").queue();
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.err.println(e.getMessage());
                    }
                }
            }
        }, 0, 30000, TimeUnit.MILLISECONDS);
    }

    public void newYande(SlashCommandEvent event) {
        JSONObject data = getSettingData(event.getGuild());
        TextChannel channel = event.getTextChannel();
        JSONArray tags = new JSONArray();
        List<MessageEmbed.Field> fields = new ArrayList<>();
        for (OptionMapping option : event.getOptions()) {
            if (option.getName().startsWith("t")) {
                String result = getData("https://yande.re/post.json?limit=1&tags=" + option.getAsString());
                JSONArray array = new JSONArray(result);
                if (array.length() == 0)
                    fields.add(new MessageEmbed.Field("錯誤的 Tag (" + option.getAsString() + ')', "", false));
                else
                    tags.put(tags.length(), option.getAsString());

            } else {
                if (!option.getAsGuildChannel().getType().equals(ChannelType.TEXT))
                    fields.add(new MessageEmbed.Field("您選擇的頻道並不是文字頻道", "", false));
                else
                    channel = (TextChannel) option.getAsGuildChannel();

            }
        }
        if (!channel.isNSFW())
            fields.add(new MessageEmbed.Field("尚未開啟 NSFW", "", false));
        if (fields.size() > 0) {
            event.getHook().editOriginalEmbeds(createEmbed("創建失敗", fields, 0xFF0000)).queue();
            return;
        }
        if (data.has(channel.getId()))
            data.put(channel.getId(), data.getJSONArray(channel.getId()).putAll(tags));
        else
            data.put(channel.getId(), tags);

        settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
        event.getHook().editOriginalEmbeds(createEmbed("設定完成", 0x00FFFF)).queue();
        startThread(event.getGuild());
    }

    public void removeYande(SlashCommandEvent event) {
        JSONObject data = getSettingData(event.getGuild());
        if (!event.getOption("channel").getAsGuildChannel().getType().equals(ChannelType.TEXT)) {
            event.getHook().editOriginalEmbeds(createEmbed("您選擇的頻道並不是文字頻道", 0xFF0000)).queue();
            return;
        }
        String channelID = event.getOption("channel").getAsGuildChannel().getId();
        if (!data.has(channelID)) {
            event.getHook().editOriginalEmbeds(createEmbed("此頻道未被設定過", 0xFF0000)).queue();
            return;
        }
        data.remove(channelID);
        map.get(event.getGuild().getId()).remove(channelID);
        old.get(event.getGuild().getId()).remove(channelID);
        settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
        event.getHook().editOriginalEmbeds(createEmbed("移除完成", 0x00FFFF)).queue();
    }

    private JSONObject getSettingData(Guild guild) {
        JsonFileManager fileManager = settingHelper.getGuildSettingManager(guild.getId());
        if (fileManager.data.has(YANDE_SETTING))
            return fileManager.data.getJSONObject(YANDE_SETTING);
        else {
            JSONObject data = new JSONObject();
            settingHelper.getGuildSettingManager(guild.getId()).data.put(YANDE_SETTING, data);
            return data;
        }
    }
}
/*
 * {"yande":{"channelID":[tag1, tag2]}}
 */