package main.java.command.list.Setting;

import main.java.util.file.GuildSettingHelper;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static main.java.command.list.Room.voiceState;
import static main.java.lang.LangKey.*;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.JsonKeys.*;

public record SettingRoom(GuildSettingHelper settingHelper) {

    public void newRoom(@NotNull SlashCommandEvent event) {
        GuildChannel detectChannel = Objects.requireNonNull(event.getOption("detectchannel")).getAsGuildChannel();
        String detectID = Objects.requireNonNull(event.getOption("detectchannel")).getAsGuildChannel().getId();
        String voiceName = Objects.requireNonNull(event.getOption("voicename")).getAsString();
        String textName = null;
        Short voiceBitrate;
        String voiceCategoryID;
        String textCategoryID = null;
        Byte memberLimit = null;
        boolean hasTextChannel = false;
        Guild guild = event.getGuild();

        if (event.getOption("textname") != null) {
            textName = Objects.requireNonNull(event.getOption("textname")).getAsString();
            hasTextChannel = true;
            if (event.getOption("textcategory") != null)
                textCategoryID = Objects.requireNonNull(event.getOption("textcategory")).getAsString();
            else
                textCategoryID = Objects.requireNonNull(detectChannel.getParent()).getId();

        }
        if (event.getOption("voicebitrate") != null)
            voiceBitrate = Short.parseShort(Objects.requireNonNull(event.getOption("voicebitrate")).getAsString()); // 8~384
        else
            voiceBitrate = 64;
        if (event.getOption("voicecategory") != null)
            voiceCategoryID = Objects.requireNonNull(event.getOption("voicecategory")).getAsString();
        else
            voiceCategoryID = Objects.requireNonNull(detectChannel.getParent()).getId();

        if (event.getOption("memberlimit") != null)
            if (Objects.requireNonNull(event.getOption("memberlimit")).getAsLong() > 0)
                memberLimit = Byte.parseByte(Objects.requireNonNull(event.getOption("memberlimit")).getAsString());

        List<MessageEmbed.Field> fields = new ArrayList<>();

        if (hasTextChannel && textName.contains(" "))
            fields.add(new MessageEmbed.Field(lang.get(SETTINGROOM_TEXT_CHANNEL_NAME_SPACE_ERROR), "", false));

        if (voiceName.length() > 100)
            fields.add(new MessageEmbed.Field(lang.get(SETTINGROOM_VOICE_CHANNEL_NAME_ERROR), "", false));

        if (hasTextChannel && textName.length() > 100)
            fields.add(new MessageEmbed.Field(lang.get(SETTINGROOM_TEXT_CHANNEL_NAME_ERROR), "", false));

        if (voiceBitrate * 1000 > Objects.requireNonNull(guild).getBoostTier().getMaxBitrate())
            fields.add(new MessageEmbed.Field(lang.get(SETTINGROOM_BITRATE_ERROR), "", false));

        if (memberLimit != null && memberLimit > 99) {
            fields.add(new MessageEmbed.Field(lang.get(SETTINGROOM_MEMBER_LIMIT_ERROR), "", false));
        }

        if (fields.size() > 0) {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(SETTINGROOM_ERROR_REPORT), fields, 0xFF0000)).queue();
            return;
        }

        fields.add(new MessageEmbed.Field(lang.get(SETTINGROOM_DETECT_VOICE_CHANNEL), detectChannel.getName() + "\n`(" + detectID + ")`", false));
        fields.add(new MessageEmbed.Field(lang.get(SETTINGROOM_VOICE_CATEGORY), Objects.requireNonNull(guild.getCategoryById(voiceCategoryID)).getName() + "\n`(" + voiceCategoryID + ")`", false));
        if (hasTextChannel)
            fields.add(new MessageEmbed.Field(lang.get(SETTINGROOM_TEXT_CATEGORY), Objects.requireNonNull(guild.getCategoryById(textCategoryID)).getName() + "\n`(" + textCategoryID + ")`", false));
        fields.add(new MessageEmbed.Field(lang.get(SETTINGROOM_VOICE_NAME), "`" + voiceName + "`", false));
        if (hasTextChannel)
            fields.add(new MessageEmbed.Field(lang.get(SETTINGROOM_TEXT_NAME), "`" + textName + "`", false));
        fields.add(new MessageEmbed.Field(lang.get(SETTINGROOM_MEMBER_LIMIT), memberLimit == null ? lang.get(SETTINGROOM_NONE) : "`" + memberLimit + "`", false));
        fields.add(new MessageEmbed.Field(lang.get(SETTINGROOM_BITRATE), "`" + voiceBitrate + " kbps`", false));

        JSONObject channelData = new JSONObject();
        channelData.put(ROOM_VOICE_CATEGORY_ID, voiceCategoryID);
        if (hasTextChannel) {
            channelData.put(ROOM_TEXT_CATEGORY_ID, textCategoryID);
            channelData.put(ROOM_TEXT_NAME, textName);
        }
        channelData.put(ROOM_VOICE_NAME, voiceName);
        channelData.put(ROOM_VOICE_BITRATE, voiceBitrate);
        if (memberLimit != null) {
            channelData.put(ROOM_VOICE_MEMBER_LIMIT, memberLimit);
        }
        JSONObject roomSetting = settingHelper.getSettingData(guild, ROOM_SETTING);
        roomSetting.put(detectID, channelData);
        settingHelper.getGuildSettingManager(guild.getId()).saveFile();

        event.getHook().editOriginalEmbeds(createEmbed(lang.get(SETTINGROOM_SETTING_SUCCESS), fields, 0x11FF99)).queue();
    }

    public void removeRoom(@NotNull SlashCommandEvent event) {
        Guild guild = event.getGuild();
        String detectID = Objects.requireNonNull(event.getOption("detectchannel")).getAsGuildChannel().getId();

        JSONObject data = settingHelper.getSettingData(Objects.requireNonNull(guild), ROOM_SETTING);
        if (voiceState.get(guild.getId()).size() > 0) {
            Map<String, List<String>> memberData = voiceState.get(guild.getId());
            for (String key : memberData.keySet()) {
                for (String channelID : memberData.get(key)) {
                    try {
                        Objects.requireNonNull(guild.getVoiceChannelById(channelID)).delete().queue();
                    } catch (Exception ignored) {
                    }
                }
                memberData.remove(key);
            }
        }

        data.remove(detectID);
        event.getHook().editOriginalEmbeds(createEmbed(lang.get(SETTINGROOM_REMOVE_SUCCESS), 0x00FFFF)).queue();

        settingHelper.getGuildSettingManager(guild.getId()).saveFile();
    }
}
