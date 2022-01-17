package com.ice.main;

import com.ice.main.command.list.Invite;
import com.ice.main.event.Log;
import com.ice.main.util.GuildUtil;
import com.ice.multiBot.music.SpotifyToYouTube;
import net.dv8tion.jda.api.entities.Role;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ice.main.event.Log.consoleChannel;

public class BotSetting {
    public static String botToken,
            helpBlockFooter,
            adminPermissionID, botRoleID,
            defaultRoomName, defaultRoomChatName,
            informationChannelID,
            memberRoleID, noPermissionStringERROR,
            logRoleID, internalRoleID,
            defaultServiceMessage, defaultTicketChannelName, newServiceName, boostedRoleID, YT_APIKEY,
            spotify_id, spotify_secret, spotify_refresh;
    public static SpotifyToYouTube spotifyToYouTube;
    public static boolean debugMode;
    public static Role boostedRole;

    public static int roomBitrate;

    public static List<Role> joinRoleID = new ArrayList<>();
    public static List<Role> confirmRoleID = new ArrayList<>();
    public static List<String> botOwnerID = new ArrayList<>();
    public static List<String> multiMusicBotTokens = new ArrayList<>();
    public static Role memberRole;

    public static List<String[]> activityMessages = new ArrayList<>();
    public static File channelLogFolder;
    public static File levelFolder;
    public static File configFolder;
    public static File guildSettingFolder;
    public static Map<String, Object> settings;
    public static Map<String, Object> IDSettings;
    public static Map<String, Object> ServiceSettings;
    public static Map<String, Object> RoomSettings;
    public static Map<String, Object> GeneralSettings;
    public static Map<String, Object> MultiBot;
    // console
    private final String TAG = "[Setting]";

    public BotSetting() {
        setLog();
        loadConfigFile();
        loadVariable();
    }

    @SuppressWarnings("ALL")
    private void loadVariable() {
        Map<String, Object> GeneralSettings = (Map<String, Object>) settings.get("GeneralSettings");
        Map<String, Object> RoomSettings = (Map<String, Object>) settings.get("RoomSettings");
        Map<String, Object> TicketSettings = (Map<String, Object>) settings.get("TicketSettings");

        /*
         * Bol
         */
        debugMode = (Boolean) GeneralSettings.get("debugMode");

        /*
         * Token
         */
        botToken = (String) GeneralSettings.get("botToken");
        if (multiMusicBotTokens.size() > 0) multiMusicBotTokens.clear();
        multiMusicBotTokens.addAll((List<String>) MultiBot.get("tokens"));

        /*
         * ID
         */
        GuildUtil.guildID = (String) IDSettings.get("guildID");
        Log.logChannelID = (String) IDSettings.get("logChannelID");
        Log.consoleChannelID = (String) IDSettings.get("consoleChannelID");
        adminPermissionID = (String) IDSettings.get("adminPermissionID");
        botRoleID = (String) IDSettings.get("botRoleID");
        Invite.authChannelID = (String) IDSettings.get("authChannelID");
        informationChannelID = (String) IDSettings.get("informationChannelID");
        memberRoleID = (String) IDSettings.get("memberRoleID");
        internalRoleID = (String) IDSettings.get("internalRoleID");
        logRoleID = (String) IDSettings.get("logRoleID");
        boostedRoleID = (String) IDSettings.get("boostedRoleID");

        if (botOwnerID.size() > 0) botOwnerID.clear();
        botOwnerID.addAll((List<String>) GeneralSettings.get("botOwnerID"));


        /*
         * Text
         */
        helpBlockFooter = (String) GeneralSettings.get("helpBlockFooter");
        noPermissionStringERROR = (String) GeneralSettings.get("noPermissionERROR");
        if (activityMessages.size() > 0) activityMessages.clear();
        for (String message : (List<String>) GeneralSettings.get("activityMessage")) {
            String[] args = message.split(";");
            if (args[0].equalsIgnoreCase("playing"))
                args[0] = "DEFAULT";
            else
                args[0] = args[0].toUpperCase();

            if (args[0].equals("STREAMING") && args.length < 3)
                System.err.println(TAG + " url not found");

            if (args.length < 2)
                System.err.println(TAG + " parameter not found");

            activityMessages.add(args);
        }


        /*
         * Room
         */
        defaultRoomName = (String) RoomSettings.get("defaultRoomName");
        defaultRoomChatName = (String) RoomSettings.get("defaultRoomChatName");
        roomBitrate = (Integer) RoomSettings.get("defaultRoomBitrate");

        /*
         * Services
         */
        defaultServiceMessage = (String) TicketSettings.get("defaultTicketMessage");
        defaultTicketChannelName = (String) TicketSettings.get("defaultTicketMessage");
        newServiceName = (String) TicketSettings.get("newTicketName");

        /**
         * API keys
         */
        Map<String, Object> keys = (Map<String, Object>) GeneralSettings.get("keys");
        YT_APIKEY = (String) keys.get("youtube");
        Map<String, Object> spotify = (Map<String, Object>) keys.get("spotify");
        spotify_id = (String) spotify.get("id");
        spotify_secret = (String) spotify.get("secret");
        spotify_refresh = (String) spotify.get("refresh");
        spotifyToYouTube = new SpotifyToYouTube();

        /**
         * File
         */
        Map<String, Object> folder = (Map<String, Object>) GeneralSettings.get("folder");

        channelLogFolder = new File((String) folder.get("channelLogFolder"));
        levelFolder = new File((String) folder.get("levelFolder"));
        configFolder = new File((String) folder.get("configFolder"));
        guildSettingFolder = new File((String) folder.get("guildSettingFolder"));

        // create folder
        if (!channelLogFolder.exists())
            channelLogFolder.mkdir();

        if (!configFolder.exists())
            configFolder.mkdir();

        if (!levelFolder.exists())
            levelFolder.mkdir();

        System.out.println(TAG + " Variable loaded");
    }

    public void reloadConfig() {
        loadConfigFile();
        loadVariable();
        Main.language.loadLanguage();
        System.out.println(TAG + " Settings reloaded");
    }

    private final String commandPr = "> ";
    private final PrintStream originalConsole = System.out;
    private ByteArrayOutputStream logConsole, errConsole;

    public void sendNoneToConsole() {
        originalConsole.print(commandPr);
        logConsole.reset();
    }

    private void setLog() {
        // 原本的console
        PrintStream originalErrConsole = System.err;
        // 新的console
        logConsole = new ByteArrayOutputStream();
        errConsole = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(logConsole, true, StandardCharsets.UTF_8);
        PrintStream err = new PrintStream(errConsole, true, StandardCharsets.UTF_8);
        System.setOut(out);
        System.setErr(err);

        // 暫存 給discord console用的
        consoleBuff = new StringBuilder();

        originalConsole.print(commandPr);
        logConsole.reset();

        // 把log的東西print出來
        new Thread(() -> {
            while (true) {
                String time = ('[' + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] ");
                if (logConsole.size() > 0) {
                    String log = logConsole.toString(StandardCharsets.UTF_8);
                    String[] lines = log.split(System.lineSeparator());
                    StringBuilder builder = new StringBuilder();
                    for (String line : lines) {
                        if (line.length() == 0)
                            builder.append(System.lineSeparator());
                        else
                            builder.append(time).append("[INFO] ").append(line).append(System.lineSeparator());
                    }

                    originalConsole.print('\r' + builder.toString() + commandPr);
                    if (builder.length() > 0)
                        sendToConsole(builder.toString(), false);
                    logConsole.reset();
                }
                if (errConsole.size() > 0) {
                    String log = errConsole.toString(StandardCharsets.UTF_8);
                    String[] lines = log.split(System.lineSeparator());
                    StringBuilder builder = new StringBuilder();
                    for (String line : lines) {
                        if (line.startsWith("[") && !line.contains("ERROR"))
                            out.println(line);
                        else {
                            if (line.startsWith("["))
                                builder.append(time).append("[ERROR] ");
                            builder.append(line).append(System.lineSeparator());
                        }
                    }

                    originalErrConsole.print('\r' + builder.toString());
                    if (builder.length() > 0)
                        sendToConsole(builder.toString(), true);
                    errConsole.reset();
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                }
            }
        }).start();
    }

    private StringBuilder consoleBuff;

    private void sendToConsole(String log, boolean error) {
        consoleBuff.append(log);
        if (consoleChannel == null)
            return;
        for (String msg : textSplit(log))
//            if (msg.contains("<@"))
//                msg.replace("<@", "@");
            if (error)
                consoleChannel.sendMessage("```" + msg + "```").queue();
            else
                consoleChannel.sendMessage(msg).queue();

        consoleBuff.setLength(0);
    }

    private String[] textSplit(String input) {
        int maxLength = 1994;
        String[] split = new String[(input.length() / maxLength) + 1];
        int i;
        for (i = 0; i < split.length - 1; i++)
            split[i] = input.substring(i * maxLength, (i + 1) * maxLength);

        split[i] = input.substring(i * maxLength);
        return split;
    }

    private void loadConfigFile() {
        settings = readYml("settings.yml");
        IDSettings = (Map<String, Object>) settings.get("IDSettings");
        ServiceSettings = (Map<String, Object>) settings.get("ServiceSettings");
        RoomSettings = (Map<String, Object>) settings.get("RoomSettings");
        GeneralSettings = (Map<String, Object>) settings.get("GeneralSettings");
        MultiBot = (Map<String, Object>) settings.get("MultiBot");
        System.out.println(TAG + " Setting file loaded");
    }

    public Map<String, Object> readYml(String name) {
        File settingFile = new File(System.getProperty("user.dir") + '/' + name); // 讀取設定檔
        if (!settingFile.exists()) { // 如果沒有設定檔
            System.err.println(TAG + name + " not found, create default " + name);
            settingFile = exportResource(name); // 用自帶的default設定檔
            if (settingFile == null) {
                System.err.println(TAG + " read " + name + " failed");
//                System.exit(1);
                return null;
            }
        }
        System.out.println(TAG + " load " + settingFile.getPath());

        /*
         * 讀取設定
         */
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            FileInputStream in = new FileInputStream(settingFile);
            int length;
            byte[] buff = new byte[1024];
            while ((length = in.read(buff)) > 0)
                out.write(buff, 0, length);

        } catch (IOException e) {
            System.err.println(TAG + " read " + name + " failed");
//            System.exit(1);
            return null;
        }
        String settingText = out.toString(StandardCharsets.UTF_8); // 使用 UTF_8 讀取設定檔裡的所有字

        Yaml yml = new Yaml();
        return yml.load(settingText);
    }

    public File exportResource(String name) {
        InputStream fileInJar = this.getClass().getClassLoader().getResourceAsStream(name);

        try {
            if (fileInJar == null) {
                System.err.println(TAG + " can not find resource: " + name);
                return null;
            }
            Files.copy(fileInJar, Paths.get(System.getProperty("user.dir") + "/" + name), StandardCopyOption.REPLACE_EXISTING);
            return new File(System.getProperty("user.dir") + "/" + name);
        } catch (IOException e) {
            System.err.println(TAG + " read resource failed");
        }
        return null;
    }

}
