package main.java.command.list.Setting;

import main.java.Main;
import main.java.util.file.GuildSettingHelper;
import main.java.util.file.JsonFileManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static main.java.lang.LangKey.*;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.JsonKeys.AUTO_VC_NAME;
import static main.java.util.JsonKeys.AUTO_VC_SETTING;

public record SettingVCC(GuildSettingHelper settingHelper) {

    public void newVCC(SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        String detectCategoryID = event.getOption("detectcategory").getAsString();
        String voiceName = event.getOption("voicename").getAsString();

        Guild guild = event.getGuild();

        List<MessageEmbed.Field> fields = new ArrayList<>();

        if (voiceName.length() > 100)
            fields.add(new MessageEmbed.Field(lang.get(SETTINGVCC_LONG_OVER_100), "", false));

        if (guild.getCategoryById(detectCategoryID) == null)
            fields.add(new MessageEmbed.Field(lang.get(SETTINGVCC_CATEGORY_ERROR), "", false));

        if (fields.size() > 0) {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(SETTINGVCC_ERROR_REPORT), fields, 0xFF0000)).queue();
            return;
        }

        fields.add(new MessageEmbed.Field(lang.get(SETTINGVCC_DETECT_CATEGORY), guild.getCategoryById(detectCategoryID).getName() + "\n`(" + detectCategoryID + ")`", false));
        fields.add(new MessageEmbed.Field(lang.get(SETTINGVCC_DETECT_NAME), voiceName, false));

        JSONObject VCCSetting = getSettingData(guild);
        VCCSetting.put(detectCategoryID, new JSONObject().put(AUTO_VC_NAME, voiceName));
        settingHelper.getGuildSettingManager(guild.getId()).saveFile();

        event.getHook().editOriginalEmbeds(createEmbed(lang.get(SETTINGVCC_SETTING_SUCCESS), fields, 0x11FF99)).queue();
    }

    public void removeVCC(SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        String detectID = event.getOption("detectcategory").getAsString();
        Guild guild = event.getGuild();

        getSettingData(guild).remove(detectID);
        event.getHook().editOriginalEmbeds(createEmbed(lang.get(SETTINGVCC_REMOVE_SUCCESS), 0x00FFFF)).queue();

        settingHelper.getGuildSettingManager(guild.getId()).saveFile();
    }

    JSONObject getSettingData(Guild guild) {
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