package main.java;

import main.java.command.Invite;
import main.java.event.Log;
import main.java.util.GuildUtil;
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

import static main.java.event.Log.consoleChannel;

public class BotSetting {
    private final static String settingFileName = "settings.yml";
    public static String botToken,
            helpBlockFooter,
            adminPermissionID, botRoleID,
            defaultRoomName, defaultRoomChatName,
            informationChannelID,
            memberRoleID, noPermissionERROR,
            logRoleID, internalRoleID,
            defaultServiceMessage, newServiceName, boostedRoleID, apiKEY;
    public static boolean debugMode;
    public static Role boostedRole;

    public static int roomBitrate;

    public static List<Role> joinRoleID = new ArrayList<>();
    public static List<Role> confirmRoleID = new ArrayList<>();
    public static List<String> serviceTagRoleID = new ArrayList<>();
    public static List<String> serviceCategoryID = new ArrayList<>();
    public static List<String> roomCategoryID = new ArrayList<>();
    public static List<String> botOwnerID = new ArrayList<>();
    public static List<String> multiMusicBotTokens = new ArrayList<>();
    public static Role memberRole;

    public static List<String[]> activityMessages = new ArrayList<>();
    public static File channelLogFolder;
    public static File levelFolder;
    public static File configFolder;
    public static Map<String, Object> settings;
    public static Map<String, Object> IDSettings;
    public static Map<String, Object> ServiceSettings;
    public static Map<String, Object> RoomSettings;
    public static Map<String, Object> GeneralSettings;
    public static Map<String, Object> MultiBot;
    // console
    public static ByteArrayOutputStream logConsole, errConsole;
    private final String TAG = "[Setting]";

    public BotSetting() {
        setLog();
        loadConfigFile();
        loadVariable();
    }

    private void loadVariable() {
        Map<String, Object> GeneralSettings = (Map<String, Object>) settings.get("GeneralSettings");
        Map<String, Object> RoomSettings = (Map<String, Object>) settings.get("RoomSettings");
        Map<String, Object> ServiceSettings = (Map<String, Object>) settings.get("ServiceSettings");

        /**
         * Bol
         */
        debugMode = (Boolean) GeneralSettings.get("debugMode");

        /**
         * Token
         */
        botToken = (String) GeneralSettings.get("botToken");
        if (multiMusicBotTokens.size() > 0) multiMusicBotTokens.clear();
        multiMusicBotTokens.addAll((List<String>) MultiBot.get("tokens"));

        /**
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

        if (serviceTagRoleID.size() > 0) serviceTagRoleID.clear();
        serviceTagRoleID.addAll((List<String>) ServiceSettings.get("serviceTagRoleID"));
        if (serviceCategoryID.size() > 0) serviceCategoryID.clear();
        serviceCategoryID.addAll((List<String>) ServiceSettings.get("serviceCategoryID"));
        if (roomCategoryID.size() > 0) roomCategoryID.clear();
        roomCategoryID.addAll((List<String>) RoomSettings.get("roomCategoryID"));
        if (botOwnerID.size() > 0) botOwnerID.clear();
        botOwnerID.addAll((List<String>) GeneralSettings.get("botOwnerID"));


        /**
         * Text
         */
        apiKEY = (String) GeneralSettings.get("apiKey");
        helpBlockFooter = (String) GeneralSettings.get("helpBlockFooter");
        noPermissionERROR = (String) GeneralSettings.get("noPermissionERROR");
        if (activityMessages.size() > 0) activityMessages.clear();
        for (String message : (List<String>) GeneralSettings.get("activityMessage")) {
            String[] args = message.split(";");
            if (args[0].equalsIgnoreCase("playing"))
                args[0] = "DEFAULT";
            else {
                args[0] = args[0].toUpperCase();
            }
            if (args[0].equals("STREAMING")) {
                if (args.length < 3) {
                    System.err.println(TAG + " url not found");
                }
            }
            if (args.length < 2) {
                System.err.println(TAG + " parameter not found");
            }
            activityMessages.add(args);
        }


        /**
         * Room
         */
        defaultRoomName = (String) RoomSettings.get("defaultRoomName");
        defaultRoomChatName = (String) RoomSettings.get("defaultRoomChatName");
        roomBitrate = (Integer) RoomSettings.get("defaultRoomBitrate");

        /**
         * Services
         */
        defaultServiceMessage = (String) ServiceSettings.get("defaultServiceMessage");
        newServiceName = (String) ServiceSettings.get("newServiceName");

        /**
         * File
         */
        Map<String, Object> folder = (Map<String, Object>) GeneralSettings.get("folder");

        channelLogFolder = new File((String) folder.get("channelLogFolder"));
        levelFolder = new File((String) folder.get("levelFolder"));
        configFolder = new File((String) folder.get("configFolder"));

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
        System.out.println(TAG + " Settings reloaded");
    }

    private final String commandPr = "> ";
    private final PrintStream originalConsole = System.out;

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
        errBuff = new StringBuilder();

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
                        printConsole(builder.toString());
                    logConsole.reset();
                }
                if (errConsole.size() > 0) {
                    String log = errConsole.toString(StandardCharsets.UTF_8);
                    String[] lines = log.split(System.lineSeparator());
                    StringBuilder builder = new StringBuilder();
                    for (String line : lines) {
                        if (line.startsWith("[") && !line.contains("ERROR")) {
                            out.println(line);
                        } else {
                            if (line.startsWith("["))
                                builder.append(time).append("[ERROR] ");
                            builder.append(line).append(System.lineSeparator());
                        }
                    }

                    originalErrConsole.print('\r' + builder.toString());
                    if (builder.length() > 0)
                        printError(builder.toString());
                    errConsole.reset();
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private StringBuilder consoleBuff, errBuff;

    private void printConsole(String log) {
        consoleBuff.append(log);
        if (consoleChannel == null)
            return;
        consoleChannel.sendMessage(consoleBuff + System.lineSeparator()).queue();
        consoleBuff.setLength(0);
    }

    private void printError(String log) {
        errBuff.append(log);
        if (consoleChannel == null)
            return;
        consoleChannel.sendMessage(errBuff).queue();
        errBuff.setLength(0);
    }

    private void loadConfigFile() {
        File settingFile = new File(System.getProperty("user.dir") + "/" + settingFileName); // 讀取設定檔
        if (!settingFile.exists()) { // 如果沒有設定檔
            System.err.println(TAG + " setting not found, create default settings");
            settingFile = exportResource(settingFileName); // 用自帶的default設定檔
        }
        System.out.println(TAG + " load setting from: " + settingFile.getPath());


        /**
         * 讀取設定
         */

        byte[] reader = new byte[0];
        try {
            reader = Files.readAllBytes(settingFile.toPath());
        } catch (IOException e) {
            System.err.println(TAG + " read setting failed");
        }

        String settingText = new String(reader, StandardCharsets.UTF_8); // 使用 UTF_8 讀取設定檔裡的所有字

        Yaml yml = new Yaml();
        settings = yml.load(settingText);
        IDSettings = (Map<String, Object>) settings.get("IDSettings");
        ServiceSettings = (Map<String, Object>) settings.get("ServiceSettings");
        RoomSettings = (Map<String, Object>) settings.get("RoomSettings");
        GeneralSettings = (Map<String, Object>) settings.get("GeneralSettings");
        MultiBot = (Map<String, Object>) settings.get("MultiBot");
        System.out.println(TAG + " Setting file loaded");
    }

    private File exportResource(String resourceName) {
        InputStream fileInJar = this.getClass().getClassLoader().getResourceAsStream(resourceName);

        try {
            if (fileInJar == null) {
                System.err.println(TAG + " can not find resource: " + resourceName);
                return null;
            }
            Files.copy(fileInJar, Paths.get(System.getProperty("user.dir") + "/" + resourceName), StandardCopyOption.REPLACE_EXISTING);
            return new File(System.getProperty("user.dir") + "/" + resourceName);
        } catch (IOException e) {
            System.err.println(TAG + " read resource failed");
        }
        return null;
    }

}
