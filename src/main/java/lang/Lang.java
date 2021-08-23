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

public class Lang {
//    private final Map<String, String> zh_TW = new HashMap<>();
//    private final Map<String, String> zh_CN = new HashMap<>();
//    private final Map<String, String> en_US = new HashMap<>();

    private final Map<String, List<String>> guildSetting = new HashMap<>();

    private final List<String> zh_TW = new ArrayList<>();
    private final List<String> zh_CN = new ArrayList<>();
    private final List<String> en_US = new ArrayList<>();

    public List<String> getGuildLang(String guildID) {
        return guildSetting.get(guildID);
    }

    private List<String> getLang(@NotNull String lang) {
        switch (lang) {
            case "zh_TW", "zh_HK", "ja_JP" -> {
                return zh_TW;
            }
//            case "zh_CN" -> {
//                return zh_CN;
//            }
            default -> {
                return en_US;
            }
        }
    }

    public void loadGuildSetting(@NotNull Guild guild){
        guildSetting.put(guild.getId(), getLang(guild.getLocale().toString()));
    }

    private final String[] languagesName = new String[]{"zh_TW", "en_US"};

    public void loadLanguage() {
        File languageFolder = new File("languages/");
        if (!languageFolder.exists())
            languageFolder.mkdir();
        for (String languageName : languagesName) {
            Map<String, Object> language = Main.setting.readYml("languages/" + languageName + ".yml");
            for (Field field : LangKey.class.getFields()) {
                Object message = language.get(field.getName());
                List<String> setting = getLang(languageName);
                if (message == null)
                    setting.add(null);
                else
                    setting.add((String) message);
            }
        }
    }
}