package main.java.command.list.Setting;

import main.java.util.file.GuildSettingHelper;
import main.java.util.file.JsonFileManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.JsonKeys.AUTO_VC_NAME;
import static main.java.util.JsonKeys.AUTO_VC_SETTING;

public class SettingVCC {
    public void newVCC(@NotNull SlashCommandEvent event, GuildSettingHelper settingHelper) {
        String detectCategoryID = event.getOption("detectcategory").getAsString();
        String voiceName = event.getOption("voicename").getAsString();

        Guild guild = event.getGuild();

        List<MessageEmbed.Field> fields = new ArrayList<>();

        if (voiceName.length() > 100)
            fields.add(new MessageEmbed.Field("語音頻道名稱長度不能大於 100", "", false));

        if (fields.size() > 0) {
            event.getHook().editOriginalEmbeds(createEmbed("錯誤回報", fields, 0xFF0000)).queue();
            return;
        }

        fields.add(new MessageEmbed.Field("偵測頻道目錄", guild.getCategoryById(detectCategoryID).getName() + "\n`(" + detectCategoryID + ")`", false));
        fields.add(new MessageEmbed.Field("語音頻道名稱", voiceName, false));

        JSONObject VCCSetting = getSettingData(guild, settingHelper);
        VCCSetting.put(detectCategoryID, new JSONObject().put(AUTO_VC_NAME, voiceName));
        settingHelper.getGuildSettingManager(guild.getId()).saveFile();

        event.getHook().editOriginalEmbeds(createEmbed("設定成功", fields, 0x11FF99)).queue();
    }

    public void removeVCC(@NotNull SlashCommandEvent event, GuildSettingHelper settingHelper) {
        String detectID = event.getOption("detectcategory").getAsString();
        Guild guild = event.getGuild();

        getSettingData(guild, settingHelper).remove(detectID);
        event.getHook().editOriginalEmbeds(createEmbed("移除成功", 0x00FFFF)).queue();

        settingHelper.getGuildSettingManager(guild.getId()).saveFile();
    }

    private @Nullable JSONObject getSettingData(@NotNull Guild guild, @NotNull GuildSettingHelper settingHelper) {
        JsonFileManager fileManager = settingHelper.getGuildSettingManager(guild.getId());
        if (fileManager.data.has(AUTO_VC_SETTING))
            return fileManager.data.getJSONObject(AUTO_VC_SETTING);
        else {
            JSONObject data = new JSONObject();
            settingHelper.getGuildSettingManager(guild.getId()).data.put(AUTO_VC_SETTING, data);
            return data;
        }
    }
}

/**
 * {[autoVC:{c:{n:"語音頻道"},c2:{n:"語音頻道"}},room:{}]}
 * {[autoVC:{c:{n:"語音頻道"},c2:{n:"語音頻道"}},room:{},cs:{channelID:"線上成員人數: %server_online_member%",channelID:""}]}
 * <p>
 * {c:{n:"123"}},
 * c:{n:"《🔊》語音頻道"}}
 * <p>
 * categoryID (c)
 * name (n)
 * <p>
 * /setting autovc
 * 偵測目錄ID
 * 頻道名稱
 */