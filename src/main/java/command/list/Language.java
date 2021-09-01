package main.java.command.list;

import main.java.Main;
import main.java.command.CommandRegister;
import main.java.util.file.GuildSettingHelper;
import main.java.util.file.JsonFileManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.List;

import static main.java.lang.LangKey.LANG_CHOOSE;
import static main.java.lang.LangKey.LANG_SUCCESS;
import static main.java.util.CountryCodeToEmoji.countryCodeToEmoji;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.JsonKeys.GUILD_LANG;
import static main.java.util.PermissionERROR.hasPermission;

public record Language(GuildSettingHelper settingHelper) {
    public void onCommand(@NotNull SlashCommandEvent event) {
        if (!hasPermission(Permission.ADMINISTRATOR, event, true)) {
            return;
        }

        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        SelectionMenu.Builder builder = SelectionMenu.create("Lang:change:" + event.getUser().getId());

        for (String i : Main.language.languagesName) {
            builder.addOption(i, i, Emoji.fromUnicode(countryCodeToEmoji(i.split("_")[1].toLowerCase())));
        }
        event.getHook().editOriginalComponents().setEmbeds(createEmbed(lang.get(LANG_CHOOSE), 0xa3d7fe)).setActionRow(builder.build()).queue();

    }

    public void onSelect(@NotNull SelectionMenuEvent event, String @NotNull [] args, CommandRegister register) {
        String guildID = event.getGuild().getId();
        if (!args[0].equals("Lang"))
            return;
        if (args[1].equals("change")) {
            JsonFileManager fileManager = settingHelper.getGuildSettingManager(guildID);
            JSONObject data = fileManager.data;
            String langStr;
            switch (event.getValues().get(0)) {
                case "zh_TW", "zh_HK", "zh_SG", "zh_MO" -> langStr = "zh_TW";
                case "zh_CN" -> langStr = "zh_CN";
                default -> langStr = "en_US";
            }
            data.put(GUILD_LANG, langStr);
            List<String> lang = Main.language.setGuildLang(guildID, langStr);
            register.addPublicSlashCommand(event.getGuild());
            fileManager.saveFile();
            event.deferReply(true).addEmbeds(createEmbed(lang.get(LANG_SUCCESS), 0x00FFFF)).queue();
        }
    }
}
