package main.java.lang;

import main.java.Main;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static main.java.lang.LangKey.loadKey;
import static main.java.util.JsonKeys.GUILD_LANG;

public class Lang {
    private final String TAG = "[Lang]";
    private final Map<String, List<String>> guildSetting = new HashMap<>();

    private final List<String> zh_TW = new ArrayList<>();
    private final List<String> zh_CN = new ArrayList<>();
    private final List<String> en_US = new ArrayList<>();

    public List<String> getGuildLang(String guildID) {
        return guildSetting.get(guildID);
    }

    private List<String> getLang(@NotNull String lang) {
        switch (lang) {
            case "zh_TW", "zh_HK", "zh_SG", "zh_MO" -> {
                return zh_TW;
            }
            case "zh_CN" -> {
                return zh_CN;
            }
            default -> {
                return en_US;
            }
        }
    }

    public void loadGuildSetting(@NotNull Guild guild){
        guildSetting.put(guild.getId(), getLang(guild.getLocale().toString()));
    }

    public final String[] languagesName = new String[]{"zh_TW", "zh_CN", "en_US"};

    public void loadLanguage() {
        File languageFolder = new File("languages/");
        if (!languageFolder.exists())
            languageFolder.mkdir();
        for (String languageName : languagesName) {
            Map<String, Object> language = Main.setting.readYml("languages/" + languageName + ".yml");
            for (Field field : LangKey.class.getFields()) {
                Object message = language.get(field.getName());
                List<String> setting = getLang(languageName);
                if (message == null) {
                    setting.add(null);
                    System.err.println(TAG + " missing key: " + field.getName());
                } else
                    setting.add((String) message);
            }
        }
        loadKey();
    }
}