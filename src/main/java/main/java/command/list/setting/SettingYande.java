package main.java.command.list.setting;

import main.java.Main;
import main.java.util.file.GuildSettingHelper;
import main.java.util.file.JsonFileManager;
import net.dv8tion.jda.api.JDA;
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

import static main.java.lang.LangKey.*;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.JsonKeys.YANDE_SETTING;
import static main.java.util.UrlDataGetter.getData;

public record SettingYande(GuildSettingHelper settingHelper) {

    private static ScheduledExecutorService threadPool = Executors.newSingleThreadScheduledExecutor();
    static List<String> tags = new ArrayList<>();
    static Map<String, Map<String, List<String>>> map = new HashMap<>();
    static List<Integer> old = new ArrayList<>();

    public void onGuildReady(GuildReadyEvent event) {
        JSONObject data = getSettingData(event.getGuild());
        if (data.length() == 0)
            return;

        for (String i : data.keySet()) {
            List<String> tagList = new ArrayList<>();
            for (Object j : data.getJSONArray(i)) {
                if (!tags.contains(String.valueOf(j)))
                    tags.add(String.valueOf(j));
                tagList.add((String) j);
            }

            Map<String, List<String>> k = new HashMap<>();
            k.put(i, tagList);

            map.put(event.getGuild().getId(), k);
        }
    }


    public void startThread(JDA jda) {
        if (threadPool != null && !threadPool.isShutdown())
            threadPool.shutdown();

        threadPool = Executors.newSingleThreadScheduledExecutor();
        threadPool.scheduleWithFixedDelay(() -> {

            if (tags.size() == 0)
                return;

            for (String tag : tags) {
                JSONArray array = new JSONArray(getData("https://yande.re/post.json?limit=1&tags=" + tag));
                if (array.length() != 0) {
                    int id;
                    boolean success = false;
                    if (!old.contains(id = array.getJSONObject(0).getInt("id")))
                        for (Map.Entry<String, Map<String, List<String>>> i : map.entrySet())
                            for (Map.Entry<String, List<String>> j : i.getValue().entrySet())
                                if (j.getValue().contains(tag)) {
                                    Guild guild;
                                    if ((guild = jda.getGuildById(i.getKey())) != null) {
                                        TextChannel channel;
                                        if ((channel = guild.getTextChannelById(j.getKey())) != null) {
                                            success = true;
                                            if (old.size() > 20)
                                                old.remove(20);
                                            old.add(id);
                                            if (channel.isNSFW())
                                                channel.sendMessage("https://yande.re/post/show/" + id).queue();
                                            else
                                                channel.sendMessage("NSFW is off").queue();
                                        } else
                                            map.get(guild.getId()).remove(j.getKey());
                                    } else
                                        map.remove(i.getKey());
                                }
                    if (!success)
                        tags.remove(tag);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                }
            }
        }, 0, 30000, TimeUnit.MILLISECONDS);
    }

    public void newYande(SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        JSONObject data = getSettingData(event.getGuild());
        TextChannel channel = event.getTextChannel();
        JSONArray tags = new JSONArray();
        List<MessageEmbed.Field> fields = new ArrayList<>();
        for (OptionMapping option : event.getOptions()) {
            if (option.getName().startsWith("t")) {
                String result = getData("https://yande.re/post.json?limit=1&tags=" + option.getAsString());
                JSONArray array = new JSONArray(result);
                if (array.length() == 0)
                    fields.add(new MessageEmbed.Field(lang.get(SETTINGYANDE_WRONG_TAG) + " (" + option.getAsString() + ')', "", false));
                else
                    tags.put(tags.length(), option.getAsString());

            } else if (!option.getAsGuildChannel().getType().equals(ChannelType.TEXT))
                fields.add(new MessageEmbed.Field(lang.get(SETTINGYANDE_WRONG_CHANNEL_TYPE), "", false));
            else
                channel = (TextChannel) option.getAsGuildChannel();

        }
        if (!channel.isNSFW())
            fields.add(new MessageEmbed.Field(lang.get(SETTINGYANDE_NSFW_IS_NOT_OPEN), "", false));
        if (fields.size() > 0) {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(SETTINGYANDE_CREATE_FAILED), fields, 0xFF0000)).queue();
            return;
        }
        if (data.has(channel.getId()))
            data.put(channel.getId(), data.getJSONArray(channel.getId()).putAll(tags));
        else
            data.put(channel.getId(), tags);

        settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
        event.getHook().editOriginalEmbeds(createEmbed(lang.get(SETTINGYANDE_CREATE_SUCCESSFULLY), 0x00FFFF)).queue();
    }

    public void removeYande(SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        JSONObject data = getSettingData(event.getGuild());
        if (!event.getOption("channel").getAsGuildChannel().getType().equals(ChannelType.TEXT)) {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(SETTINGYANDE_WRONG_CHANNEL_TYPE), 0xFF0000)).queue();
            return;
        }
        String channelID = event.getOption("channel").getAsGuildChannel().getId();
        if (!data.has(channelID)) {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(SETTINGYANDE_NO_SET), 0xFF0000)).queue();
            return;
        }
        data.remove(channelID);
        map.get(event.getGuild().getId()).remove(channelID);
        settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
        event.getHook().editOriginalEmbeds(createEmbed(lang.get(SETTINGYANDE_REMOVE_SUCCESSFULLY), 0x00FFFF)).queue();
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