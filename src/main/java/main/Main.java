package main;

import main.lang.Lang;
import main.util.EmojiUtil;
import main.util.GuildUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static main.BotSetting.activityMessages;
import static main.BotSetting.botToken;
import static main.util.GuildUtil.guild;

public class Main {
    public static String botID;
    public static String botNickname, botAvatarUrl;
    public static BotSetting setting;
    public static Lang language;
    public static SelfUser self;
    public static EmojiUtil emoji = new EmojiUtil();
    private final String TAG = "[Main]";


    // interval
    private ScheduledExecutorService threadPool;
    private int currentIndex = 0;

    Main() throws LoginException {
        language = new Lang();
        setting = new BotSetting(); // 讀取設定
        language.loadLanguage();

        /*
         * init bot
         */
        JDABuilder builder = JDABuilder.createDefault(botToken)
//                .disableCache(CacheFlag.MEMBER_OVERRIDES) // Disable parts of the cache
                .setBulkDeleteSplittingEnabled(false) // Enable the bulk delete event
//                .setCompression(Compression.ZLIB) // Disable compression (not recommended)
                .setLargeThreshold(250)
                .enableCache(CacheFlag.ONLINE_STATUS, CacheFlag.ACTIVITY)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_PRESENCES);

        JDA jda = builder.build();

        // 註冊event
        ListenerManager listener = new ListenerManager();
        jda.addEventListener(listener);

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
                        System.out.println(TAG + " Stopped");
                        return;
                    case "reload":
                        System.out.println(TAG + " Reloading...");
                        // load new setting
                        setting.reloadConfig();
                        if (guild == null) {
                            System.err.println(TAG + " Cannot found guild: " + GuildUtil.guildID);
                            break;
                        }
                        // get guild variable from setting
                        listener.reload(guild);
                        // 開始自動切換
                        startChangeActivity(jda);
                        System.out.println(TAG + " Reloaded!");
                        break;
                    default:
                        if (command.length() == 0)
                            setting.sendNoneToConsole();
                        else
                            System.out.println(TAG + " Unknown Command");
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
                if (msg[0].equals("STREAMING"))
                    // name, url
                    jda.getPresence().setActivity(Activity.of(Activity.ActivityType.STREAMING, msg[1], msg[2]));
                else {
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