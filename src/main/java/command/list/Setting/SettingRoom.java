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

import static main.java.command.list.Room.voiceState;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.JsonKeys.*;

public class SettingRoom {
    private final GuildSettingHelper settingHelper;

    public SettingRoom(GuildSettingHelper settingHelper) {
        this.settingHelper = settingHelper;
    }

    public void newRoom(@NotNull SlashCommandEvent event) {
        GuildChannel detectChannel = event.getOption("detectchannel").getAsGuildChannel();
        String detectID = event.getOption("detectchannel").getAsGuildChannel().getId();
        String voiceName = event.getOption("voicename").getAsString();
        String textName = null;
        Short voiceBitrate;
        String voiceCategoryID;
        String textCategoryID = null;
        Byte memberLimit = null;
        boolean hasTextChannel = false;
        Guild guild = event.getGuild();

        if (event.getOption("textname") != null) {
            textName = event.getOption("textname").getAsString();
            hasTextChannel = true;
            if (event.getOption("textcategory") != null)
                textCategoryID = event.getOption("textcategory").getAsString();
            else
                textCategoryID = detectChannel.getParent().getId();

        }
        if (event.getOption("voicebitrate") != null)
            voiceBitrate = Short.parseShort(event.getOption("voicebitrate").getAsString()); // 8~384
        else
            voiceBitrate = 64;
        if (event.getOption("voicecategory") != null)
            voiceCategoryID = event.getOption("voicecategory").getAsString();
        else
            voiceCategoryID = detectChannel.getParent().getId();

        if (event.getOption("memberlimit") != null)
            if (event.getOption("memberlimit").getAsLong() > 0)
                memberLimit = Byte.parseByte(event.getOption("memberlimit").getAsString());

        List<MessageEmbed.Field> fields = new ArrayList<>();

        if (hasTextChannel && textName.contains(" "))
            fields.add(new MessageEmbed.Field("文字頻道名稱無法包含空格", "", false));

        if (voiceName.length() > 100)
            fields.add(new MessageEmbed.Field("語音頻道名稱長度不能大於 100", "", false));

        if (hasTextChannel && textName.length() > 100)
            fields.add(new MessageEmbed.Field("文字頻道名稱長度不能大於 100", "", false));

        if (voiceBitrate != null)
            if (voiceBitrate * 1000 > guild.getBoostTier().getMaxBitrate())
                fields.add(new MessageEmbed.Field("您的伺服器目前無法達到如此高的音訊位元率", "", false));

        if (memberLimit != null && memberLimit > 99) {
            fields.add(new MessageEmbed.Field("人數限制最大只能達到 99 人", "", false));
        }

        if (fields.size() > 0) {
            event.getHook().editOriginalEmbeds(createEmbed("錯誤回報", fields, 0xFF0000)).queue();
            return;
        }

        fields.add(new MessageEmbed.Field("偵測語音頻道", detectChannel.getName() + "\n`(" + detectID + ")`", false));
        fields.add(new MessageEmbed.Field("語音頻道目錄", guild.getCategoryById(voiceCategoryID).getName() + "\n`(" + voiceCategoryID + ")`", false));
        if (hasTextChannel)
            fields.add(new MessageEmbed.Field("文字頻道目錄", guild.getCategoryById(textCategoryID).getName() + "\n`(" + textCategoryID + ")`", false));
        fields.add(new MessageEmbed.Field("語音頻道名稱", "`" + voiceName + "`", false));
        if (hasTextChannel)
            fields.add(new MessageEmbed.Field("文字頻道名稱", "`" + textName + "`", false));
        fields.add(new MessageEmbed.Field("語音人數限制", memberLimit == null ? "`無`" : "`" + memberLimit + "`", false));
        fields.add(new MessageEmbed.Field("語音位元率", "`" + voiceBitrate + " kbps`", false));

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

        event.getHook().editOriginalEmbeds(createEmbed("設定成功", fields, 0x11FF99)).queue();
    }

    public void removeRoom(@NotNull SlashCommandEvent event) {
        Guild guild = event.getGuild();
        String detectID = event.getOption("detectchannel").getAsGuildChannel().getId();

        JSONObject data = settingHelper.getSettingData(guild, ROOM_SETTING);
        if (voiceState.get(guild.getId()).size() > 0) {
            Map<String, List<String>> memberData = voiceState.get(guild.getId());
            for (String key : memberData.keySet()) {
                for (String channelID : memberData.get(key)) {
                    try {
                        guild.getVoiceChannelById(channelID).delete().queue();
                    } catch (Exception ignored) {
                    }
                }
                memberData.remove(key);
            }
        }

        data.remove(detectID);
        event.getHook().editOriginalEmbeds(createEmbed("移除成功", 0x00FFFF)).queue();

        settingHelper.getGuildSettingManager(guild.getId()).saveFile();
    }
}

// detectID

/**
 * {"autoVC":{"858672865816346637":{"N":"《🔊》語音頻道"}},"room":{},"ticket":{messageID:[{...},{...}]}}
 * {d:{vc:"12345",tc:"12345",vn:"Hello %Name%", tn:"i da %NameTag%", ml:"50", br:"128"},
 * d:{vc:"12345",tc:"12345",vn:"Hello %Name%", tn:"i da %NameTag%", br:"128"}}
 * <p>
 * detectID (d)
 * voiceCategory (vc)
 * textCategory (tc)
 * voiceName (vn)
 * textName (tn)
 * memberLimit (ml) 可選
 * bitrate (br)
 * <p>
 * /setting newroom
 * 偵測頻道 ID
 * 新語音頻道目錄 ID
 * 新文字頻道目錄 ID (-1則無)
 * 新語音名稱(可填空白鍵, %guild_name%, %user%, %user_name%, %user_tag%, or %nickname%)
 * 新文字名稱(不可填空白鍵, %guild_name%, %user%, %user_name%, %user_tag%, or %nickname%)
 * 語音人數限制 (1~99) (0無)
 * 新語音位元率 (kbps)
 */

