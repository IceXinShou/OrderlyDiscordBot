package main.java.command.list.Setting;

import main.java.util.file.GuildSettingHelper;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.JsonKeys.*;

public class SettingJoinLeave {

    public void newJoin(@NotNull SlashCommandEvent event) {
        List<MessageEmbed.Field> fields = new ArrayList<>();
        GuildChannel channel = event.getOption("channel").getAsGuildChannel();
        String message = event.getOption("message").getAsString();
        String dm = event.getOption("dm") == null ? null : event.getOption("dm").getAsString();
        List<Role> roles = null;


        if (channel.getType() != ChannelType.TEXT)
            fields.add(new MessageEmbed.Field("找不到文字頻道", "", false));
        if (fields.size() > 0) {
            event.getHook().editOriginalEmbeds(createEmbed("設定失敗", fields, 0xFF0000)).queue();
            return;
        }

        JSONObject data = getOrDefault(settingHelper.getSettingData(event.getGuild(), JL_SETTING), channel.getId());
        data.put(JL_JOIN_MESSAGE, message);

        StringBuilder roleData = null;
        int n = 0;
        for (OptionMapping i : event.getOptions())
            if (i.getName().startsWith("role") && !roles.contains(i.getAsRole())) {
                n++;
                roleData.append(i.getAsRole().getName()).append("`(").append(i.getAsRole().getId()).append(")`\n");
                data.put("R" + n, i.getAsRole().getId());
            }

        fields.add(new MessageEmbed.Field("通知頻道", channel.getAsMention() + "\n`(" + channel.getId() + ")`", false));
        fields.add(new MessageEmbed.Field("通知訊息", message, false));
        if (dm != null) {
            data.put(JL_JOIN_DM, dm);
            fields.add(new MessageEmbed.Field("私聊訊息", dm, false));
        }

        if (roleData != null)
            fields.add(new MessageEmbed.Field("新增身分組", roleData.toString(), false));

        settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
        event.getHook().editOriginalEmbeds(createEmbed("設定成功", fields, 0x00FFFF)).queue();
    }

    public void newLeave(@NotNull SlashCommandEvent event) {
        JSONObject data = settingHelper.getSettingData(event.getGuild(), JL_SETTING);
        List<MessageEmbed.Field> fields = new ArrayList<>();
        GuildChannel channel = event.getOption("channel").getAsGuildChannel();
        String message = event.getOption("message").getAsString();
        if (!channel.getType().equals(ChannelType.TEXT))
            fields.add(new MessageEmbed.Field("找不到此文字頻道", "", false));
        if (fields.size() > 0) {
            event.getHook().editOriginalEmbeds(createEmbed("設定失敗", fields, 0xFF0000)).queue();
            return;
        }
        getOrDefault(data, channel.getId()).put(JL_LEAVE_MESSAGE, message);
        settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
        fields.add(new MessageEmbed.Field("通知頻道", channel.getAsMention() + "\n`(" + channel.getId() + ")`", false));
        fields.add(new MessageEmbed.Field("通知訊息", message, false));
        event.getHook().editOriginalEmbeds(createEmbed("設定成功", fields, 0x00FFFF)).queue();
    }

    public void removeJoin(@NotNull SlashCommandEvent event) {
        JSONObject data = settingHelper.getSettingData(event.getGuild(), JL_SETTING);

        if (!data.has(event.getOption("channel").getAsGuildChannel().getId())) {
            event.getHook().editOriginalEmbeds(createEmbed("此頻道無被設定紀錄", 0xFF0000)).queue();
            return;
        }
        if (!data.getJSONObject(event.getOption("channel").getAsGuildChannel().getId()).has(JL_JOIN_MESSAGE)) {
            event.getHook().editOriginalEmbeds(createEmbed("此頻道無被設定紀錄", 0xFF0000)).queue();
            return;
        }
        data.getJSONObject(event.getOption("channel").getAsGuildChannel().getId()).remove(JL_JOIN_MESSAGE);
        settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
        event.getHook().editOriginalEmbeds(createEmbed("移除成功", 0x00FFFF)).queue();
    }

    public void removeLeave(@NotNull SlashCommandEvent event) {
        JSONObject data = settingHelper.getSettingData(event.getGuild(), JL_SETTING);
        if (!data.has(event.getOption("channel").getAsGuildChannel().getId())) {
            event.getHook().editOriginalEmbeds(createEmbed("此頻道無被設定紀錄", 0xFF0000)).queue();
            return;
        }

        if (!data.getJSONObject(event.getOption("channel").getAsGuildChannel().getId()).has(JL_LEAVE_MESSAGE)) {
            event.getHook().editOriginalEmbeds(createEmbed("此頻道無被設定紀錄", 0xFF0000)).queue();
            return;
        }

        data.getJSONObject(event.getOption("channel").getAsGuildChannel().getId()).remove(JL_LEAVE_MESSAGE);
        settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
        event.getHook().editOriginalEmbeds(createEmbed("移除成功", 0x00FFFF)).queue();
    }

    private JSONObject getOrDefault(@NotNull JSONObject input, String key) {
        if (input.has(key))
            return input.getJSONObject(key);
        else {
            JSONObject data = new JSONObject();
            input.put(key, data);
            return data;
        }
    }
}
