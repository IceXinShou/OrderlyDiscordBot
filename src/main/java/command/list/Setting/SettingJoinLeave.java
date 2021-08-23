package main.java.command.list.Setting;

import main.java.Main;
import main.java.util.file.GuildSettingHelper;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static main.java.lang.LangKey.*;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.JsonGetter.getOrDefault;
import static main.java.util.JsonKeys.*;
import static main.java.util.Tag.tagRoleID;

public record SettingJoinLeave(GuildSettingHelper settingHelper) {


    @SuppressWarnings("ALL")
    public void newJoin(@NotNull SlashCommandEvent event) {
        List<String> lang = Main.lang.getGuildLang(event.getGuild().getId());
        if (event.getGuild() == null)
            return;

        List<MessageEmbed.Field> fields = new ArrayList<>();
        GuildChannel channel = Objects.requireNonNull(event.getOption("channel")).getAsGuildChannel();
        String message = Objects.requireNonNull(event.getOption("message")).getAsString();
        String dm = event.getOption("dm") == null ? null : Objects.requireNonNull(event.getOption("dm")).getAsString();

        if (channel.getType() != ChannelType.TEXT)
            fields.add(new MessageEmbed.Field(lang.get(SETTINGJOINLEAVE_CAN_NOT_FOUND_TEXT_CHANNEL), "", false));

        if (fields.size() > 0) {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(SETTINGJOINLEAVE_SETTING_FAIL), fields, 0xFF0000)).queue();
            return;
        }


        JSONArray roles = new JSONArray();
        StringBuilder roleData = new StringBuilder();
        int n = 0;
        for (OptionMapping i : event.getOptions())
            if (i.getName().startsWith("role") && !roles.toList().contains(i.getAsRole().getId())) {
                n++;
                roleData.append(tagRoleID(i.getAsRole().getId())).append("`(").append(i.getAsRole().getId()).append(")`\n");
                roles.put(i.getAsRole().getId());
            }

        fields.add(new MessageEmbed.Field(lang.get(SETTINGJOINLEAVE_NOTICE_CHANNEL), channel.getAsMention() + "\n`(" + channel.getId() + ")`", false));
        fields.add(new MessageEmbed.Field(lang.get(SETTINGJOINLEAVE_NOTICE_MESSAGE), message, false));
        JSONObject data = getOrDefault(settingHelper.getSettingData(event.getGuild(), J_SETTING), channel.getId());

        data.put(J_JOIN_MESSAGE, message);
        if (dm != null) {
            data.put(J_JOIN_DM, dm);
            fields.add(new MessageEmbed.Field(lang.get(SETTINGJOINLEAVE_DM_MESSAGE), dm, false));
        }

        if (roleData != null) {
            fields.add(new MessageEmbed.Field(lang.get(SETTINGJOINLEAVE_ROLE_ADD), roleData.toString(), false));
            data.put(J_JOIN_ROLE, roles);
        }
        settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
        event.getHook().editOriginalEmbeds(createEmbed(lang.get(SETTINGJOINLEAVE_SETTING_SUCCESS), fields, 0x00FFFF)).queue();
    }

    public void newLeave(@NotNull SlashCommandEvent event) {
        if (event.getGuild() == null)
            return;
        JSONObject data = settingHelper.getSettingData(event.getGuild(), L_SETTING);
        List<MessageEmbed.Field> fields = new ArrayList<>();
        GuildChannel channel = Objects.requireNonNull(event.getOption("channel")).getAsGuildChannel();
        String message = Objects.requireNonNull(event.getOption("message")).getAsString();
        if (!channel.getType().equals(ChannelType.TEXT))
            fields.add(new MessageEmbed.Field("找不到此文字頻道", "", false));
        if (fields.size() > 0) {
            event.getHook().editOriginalEmbeds(createEmbed("設定失敗", fields, 0xFF0000)).queue();
            return;
        }
        getOrDefault(data, channel.getId()).put(L_LEAVE_MESSAGE, message);
        settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
        fields.add(new MessageEmbed.Field("通知頻道", channel.getAsMention() + "\n`(" + channel.getId() + ")`", false));
        fields.add(new MessageEmbed.Field("通知訊息", message, false));
        event.getHook().editOriginalEmbeds(createEmbed("設定成功", fields, 0x00FFFF)).queue();
    }

    public void removeJoin(@NotNull SlashCommandEvent event) {
        if (event.getGuild() == null)
            return;
        JSONObject data = settingHelper.getSettingData(event.getGuild(), J_SETTING);

        if (!data.has(Objects.requireNonNull(event.getOption("channel")).getAsGuildChannel().getId())) {
            event.getHook().editOriginalEmbeds(createEmbed("此頻道無被設定紀錄", 0xFF0000)).queue();
            return;
        }
        if (!data.getJSONObject(Objects.requireNonNull(event.getOption("channel")).getAsGuildChannel().getId()).has(J_JOIN_MESSAGE)) {
            event.getHook().editOriginalEmbeds(createEmbed("此頻道無被設定紀錄", 0xFF0000)).queue();
            return;
        }
        data.getJSONObject(Objects.requireNonNull(event.getOption("channel")).getAsGuildChannel().getId()).remove(J_JOIN_MESSAGE);
        settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
        event.getHook().editOriginalEmbeds(createEmbed("移除成功", 0x00FFFF)).queue();
    }

    public void removeLeave(@NotNull SlashCommandEvent event) {
        if (event.getGuild() == null)
            return;
        JSONObject data = settingHelper.getSettingData(event.getGuild(), L_SETTING);
        if (!data.has(Objects.requireNonNull(event.getOption("channel")).getAsGuildChannel().getId())) {
            event.getHook().editOriginalEmbeds(createEmbed("此頻道無被設定紀錄", 0xFF0000)).queue();
            return;
        }

        if (!data.getJSONObject(Objects.requireNonNull(event.getOption("channel")).getAsGuildChannel().getId()).has(L_LEAVE_MESSAGE)) {
            event.getHook().editOriginalEmbeds(createEmbed("此頻道無被設定紀錄", 0xFF0000)).queue();
            return;
        }

        data.getJSONObject(Objects.requireNonNull(event.getOption("channel")).getAsGuildChannel().getId()).remove(L_LEAVE_MESSAGE);
        settingHelper.getGuildSettingManager(event.getGuild().getId()).saveFile();
        event.getHook().editOriginalEmbeds(createEmbed("移除成功", 0x00FFFF)).queue();
    }

}
