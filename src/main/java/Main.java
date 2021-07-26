package main.java;

import main.java.automatic.InformationReaction;
import main.java.automatic.TicketChannel;
import main.java.command.QuickUse;
import main.java.event.GeneralReplay;
import main.java.event.Join;
import main.java.event.Log;
import main.java.util.EmojiUtil;
import main.java.util.GuildUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static main.java.BotSetting.activityMessages;
import static main.java.BotSetting.botToken;


public class Main {
    public static String botID;
    public static String botNickname, botAvatarUrl;
    public static BotSetting setting;
    public static SelfUser self;
    public static EmojiUtil emoji = new EmojiUtil();
    private final String TAG = "[Main]";

    // interval
    private ScheduledExecutorService threadPool;
    private int currentIndex = 0;

    Main() throws LoginException {
        setting = new BotSetting(); // 讀取設定

        /**
         * init bot
         */
        JDABuilder builder = JDABuilder.createDefault(botToken)
//                .disableCache(CacheFlag.MEMBER_OVERRIDES) // Disable parts of the cache
                .setBulkDeleteSplittingEnabled(false) // Enable the bulk delete event
                .setCompression(Compression.ZLIB) // Disable compression (not recommended)
                .setLargeThreshold(250)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES);

        JDA jda = builder.build();

        // 註冊event
        jda.addEventListener(new Log());
        jda.addEventListener(new Join());
        jda.addEventListener(new GeneralReplay());
        jda.addEventListener(new TicketChannel());
        jda.addEventListener(new InformationReaction());
        jda.addEventListener(new QuickUse());
        SlashCommandManager commandManager = new SlashCommandManager();
        jda.addEventListener(commandManager);


        // bot自己的資料
        botID = jda.getSelfUser().getId();
        botAvatarUrl = jda.getSelfUser().getAvatarUrl();
        self = jda.getSelfUser();
        // 開始切換狀態
        startChangeActivity(jda);

        // 啟動完畢
        System.out.println(TAG + " 已啟動");
        // 平行處理 不然會卡住
        new Thread(() -> {
            while (true) {
                Scanner scanner = new Scanner(System.in);
                String command = scanner.nextLine();
                switch (command.toLowerCase()) {
                    case "stop":
                        jda.shutdown();
                        threadPool.shutdown();
                        System.out.println(TAG + " 已停止");
                        return;
                    case "reload":
                        // load new setting
                        setting.reloadConfig();
                        Guild guild = jda.getGuildById(GuildUtil.guildID);
                        if (guild == null) {
                            System.err.println(TAG + " 無法找到公會: " + GuildUtil.guildID);
                            break;
                        }
                        // get guild variable from setting
                        commandManager.getGuildVariable(guild);
                        // 開始自動切換
                        startChangeActivity(jda);
                        System.out.println(TAG + " 重新載入完成");
                        break;
                    default:
                        if (command.length() == 0)
                            setting.sendNoneToConsole();
                        else
                            System.out.println(TAG + " 不知名的指令");
                        break;
                }
            }
        }).start();
    }

    private void startChangeActivity(JDA jda) {
        if (threadPool != null && !threadPool.isShutdown())
            threadPool.shutdown();

        threadPool = Executors.newSingleThreadScheduledExecutor();
        // run thread
        threadPool.scheduleWithFixedDelay(() -> {
            String[] msg = activityMessages.get(currentIndex);
            try {
                if (msg[0].equals("STREAMING")) {
                    // name, url
                    jda.getPresence().setActivity(Activity.of(Activity.ActivityType.STREAMING, msg[1], msg[2]));
                } else {
                    Activity.ActivityType type = Activity.ActivityType.valueOf(msg[0]);
                    jda.getPresence().setActivity(Activity.of(type, msg[1]));
                }
            } catch (IllegalArgumentException e) {
                System.err.println(TAG + " can not find type: " + msg[0]);
                threadPool.shutdown();
                return;
            }
            currentIndex = (currentIndex + 1) % activityMessages.size();
        }, 0, 5000, TimeUnit.MILLISECONDS);
    }

    public static void main(String[] args) throws LoginException {
        new Main();
    }
}