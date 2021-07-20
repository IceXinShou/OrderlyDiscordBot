package main.java.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static main.java.SlashCommandOption.COUNT;
import static main.java.SlashCommandOption.URL;
import static main.java.event.Log.logChannel;
import static main.java.util.EmbedUtil.createEmbed;
import static main.java.util.GuildUtil.guildID;

public class MusicCommands implements GuildMusicManager.Event {
    private final AudioPlayerManager playerManager;
    public Map<String, GuildMusicManager> musicManagers;

    private int commandState;

    public MusicCommands() {
        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public int onCommand(SlashCommandEvent event) {
        commandState = 1;

        switch (event.getName()) {
            // 全域頻道
            case "play": //
                if (checkVcState(event)) {
                    if (event.getOption(URL) == null) {
                        event.replyEmbeds(createEmbed("未知的參數", 0xFF0000)).setEphemeral(true).queue();
                        break;
                    }
                    String url = event.getOption(URL).getAsString();
                    loadAndPlay(event, getGuildAudioPlayer(event.getGuild()), url);
                }
                break;
            case "skip":
                if (checkVcState(event))
                    getGuildAudioPlayer(event.getGuild()).scheduler.nextTrack(event);
                break;
            case "previous":
                if (checkVcState(event))
                    playPrevious(event);
                break;
            case "repeat":
                if (checkVcState(event))
                    getGuildAudioPlayer(event.getGuild()).scheduler.toggleRepeat(event);
                break;
            case "pause":
                if (checkVcState(event))
                    getGuildAudioPlayer(event.getGuild()).scheduler.pause(event);
                break;
            case "queue":
                if (checkVcState(event))
                    queue(event);
                break;
            case "volume":
                if (checkVcState(event)) {
                    Integer volume = null;
                    if (event.getOption(COUNT) != null)
                        volume = (int) event.getOption(COUNT).getAsLong();
                    getGuildAudioPlayer(event.getGuild()).scheduler.changeVolume(volume, event);
                }
                break;
            default:
                commandState = 0;
        }
        return commandState;
    }

    public MessageEmbed[] playStatus(Member member, TrackScheduler scheduler) {
        //憲政撥放資料
        StringBuilder progress = new StringBuilder();
        MessageEmbed nowPlaying;
        AudioTrackInfo trackInfo = null;
        //有歌曲正在撥放
        if (scheduler.playingTrack != null) {
            //進度顯示
            trackInfo = scheduler.playingTrack.getInfo();
            int nowPlayingTime = (int) ((System.currentTimeMillis() - scheduler.startPlayTime) / 1000);
            int playPercent = (int) ((nowPlayingTime / (float) (trackInfo.length / 1000)) * 15);
            progress.append("\n\n**[")
                    .append(timeCalculator(nowPlayingTime))
                    .append("] **").append("━".repeat(playPercent))
                    .append("❚")
                    .append("─".repeat(15 - playPercent))
                    .append("** [").append(timeCalculator((int) (trackInfo.length / 1000)))
                    .append("]**\n");
        }
        //音量顯示
        int volumePercent = (int) (getGuildAudioPlayer(member.getGuild()).player.getVolume() / 5f);
        progress.append("\n")
                .append("**音量: **")
                .append("◆".repeat(volumePercent))
                .append("◇".repeat(20 - volumePercent))
                .append(scheduler.loopStatus == 0 ? " <順序撥放>" : (scheduler.loopStatus == 1 ? " <循環撥放>" : " <單曲循環>"));
        //組裝
        if (scheduler.playingTrack != null)
            nowPlaying = createEmbed("**" + trackInfo.title + "**", trackInfo.uri,
                    progress.toString(), "",
                    null, 0xe5b849);
        else
            nowPlaying = createEmbed("**[沒有歌曲正在被撥放]**",
                    "", "",
                    null, 0xe5b849);

        //歌曲列表
        List<MessageEmbed.Field> fields = new ArrayList<>();
        if (scheduler.getQueue().size() == 0)
            fields.add(new MessageEmbed.Field("無", "", false));
        else
            scheduler.getQueue().forEach((track) -> {
                long songLength = track.getInfo().length / 1000;
                fields.add(new MessageEmbed.Field(track.getInfo().title, timeCalculator(songLength), false));
            });

        return new MessageEmbed[]{createEmbed("歌曲列表", "",
                "",
                fields,
                null,
                0x7fc89a),
                nowPlaying};
    }

    public ActionRow controlButtons(String senderID, Boolean pauseStatus, int loopStatus) {
        return ActionRow.of(
                Button.of(ButtonStyle.SECONDARY, senderID + ":musicLoopChange", "",
                        loopStatus == 0 ? Emoji.fromUnicode("➡️") : (loopStatus == 1 ? Emoji.fromUnicode("\uD83D\uDD01") : Emoji.fromUnicode("\uD83D\uDD02"))),
                Button.of(ButtonStyle.SECONDARY, senderID + ":musicPause", "",
                        pauseStatus ? Emoji.fromUnicode("▶️") : Emoji.fromUnicode("⏸️")),
                Button.of(ButtonStyle.SECONDARY, senderID + ":nextToPlay", "", Emoji.fromUnicode("⏭️")),
                Button.of(ButtonStyle.SECONDARY, senderID + ":musicVolumeDown", "", Emoji.fromUnicode("\uD83D\uDD09")),
                Button.of(ButtonStyle.SECONDARY, senderID + ":musicVolumeUp", "", Emoji.fromUnicode("\uD83D\uDD0A")));
    }

    public void queue(GenericInteractionCreateEvent event) {
        TrackScheduler scheduler = getGuildAudioPlayer(event.getGuild()).scheduler;
        if (scheduler.playingTrack == null) {
            event.replyEmbeds(createEmbed("目前無音樂播放", 0xFF0000)).setEphemeral(true).queue();
            return;
        }

        MessageEmbed[] embed = playStatus(event.getMember(), scheduler);

        event.replyEmbeds(embed[0], embed[1])
                .addActionRows(controlButtons(event.getMember().getId(), scheduler.musicPause, scheduler.loopStatus))
                .setEphemeral(true).queue();

    }


    public void onButton(ButtonClickEvent event, String[] args) {
        if (!checkVcState(event))
            return;

        AudioPlayer player = getGuildAudioPlayer(event.getGuild()).player;
        TrackScheduler scheduler = getGuildAudioPlayer(event.getGuild()).scheduler;
        int volume;
        boolean editButton = false;
        switch (args[1]) {
            case "musicLoopChange": {
                switch ((scheduler.loopStatus = (scheduler.loopStatus + 1) % 3)) {
                    case 0:
                        scheduler.repeat = false;
                        scheduler.loop = false;
                        break;
                    case 1:
                        scheduler.repeat = false;
                        scheduler.loop = true;
                        break;
                    case 2:
                        scheduler.repeat = true;
                        scheduler.loop = false;
                        break;
                    default:
                        return;
                }
                editButton = true;
                break;
            }
            case "musicPause":
                scheduler.pause(null);
                editButton = true;
                break;
            case "nextToPlay":
                scheduler.nextTrack(null);
                break;
            case "musicVolumeUp":
                volume = player.getVolume() + 5;
                player.setVolume(Math.min(volume, 100));
                break;
            case "musicVolumeDown":
                volume = player.getVolume() - 5;
                player.setVolume(Math.max(volume, 0));
                break;
            default:
                return;
        }

        MessageEmbed[] embed = playStatus(event.getMember(), scheduler);
        WebhookMessageUpdateAction<Message> message = event.getHook().editOriginalEmbeds(embed[0], embed[1]);
        if (editButton)
            message.setActionRows(controlButtons(args[0], scheduler.musicPause, scheduler.loopStatus));
        message.queue();

    }

    private String timeCalculator(long songLength) {

        StringBuilder builder = new StringBuilder();

        int hr = (int) songLength / 3600;
        int min = (int) ((songLength / 60) % 60);
        int sec = (int) songLength % 60;

        if (hr > 0)
            builder.append(hr < 10 ? "0" + hr : hr).append(':');
        builder.append(min < 10 ? "0" + min : min).append(':');
        builder.append(sec < 10 ? "0" + sec : sec);

        return builder.toString();
    }

    private boolean checkVcState(GenericInteractionCreateEvent event) {
        if (!event.getMember().getVoiceState().inVoiceChannel()) {
            event.replyEmbeds(createEmbed("請在語音頻道內使用此指令", 0xFF0000)).setEphemeral(true).queue();
            return false;
        } else if (event.getGuild().getSelfMember().getVoiceState().inVoiceChannel() && !event.getMember().getVoiceState().getChannel().equals(event.getGuild().getSelfMember().getVoiceState().getChannel())) {
            event.replyEmbeds(createEmbed("機器人已被其他頻道所使用", 0xFF0000)).setEphemeral(true).queue();
            return false;
        } else return true;
    }

    public void loadAndPlay(final SlashCommandEvent event, final GuildMusicManager manager, final String trackUrl) {
        VoiceChannel vc = event.getMember().getVoiceState().getChannel();

        //取得音樂
        playerManager.loadItemOrdered(musicManagers, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                play(track, vc, manager, event);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                play(playlist, vc, manager, event);
            }

            @Override
            public void noMatches() {
                event.replyEmbeds(createEmbed("查無此網址: " + trackUrl, 0xFF0000)).setEphemeral(true).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                event.replyEmbeds(createEmbed("無法播放此網址: " + exception.getMessage(), 0xFF0000)).setEphemeral(true).queue();
            }
        });

    }


    /**
     * event
     */
    @Override
    public void playStart(AudioTrack track, GenericInteractionCreateEvent event, Guild guild) {
        if (event != null) {
            queue(event);
        }
        if (guild.getId().equals(guildID))
            logChannel.sendMessage("開始播放 `" + track.getInfo().title + "`").queue();
    }

    @Override
    public void addToQueue(AudioTrack track, SlashCommandEvent event) {
        event.replyEmbeds(createEmbed("加入播放清單: `" + track.getInfo().title + "`", 0xBCE9B6)).setEphemeral(true).queue();
        if (!event.getGuild().getId().equals(guildID))
            logChannel.sendMessage("加入播放清單: `" + track.getInfo().title + "` ").queue();
    }

    @Override
    public void addPlayerListToQueue(AudioPlaylist playlist, SlashCommandEvent event) {
        event.replyEmbeds(createEmbed("加入 `" + playlist.getName() + "` ", 0xBCE9B6)).setEphemeral(true).queue();
        if (!event.getGuild().getId().equals(guildID))
            logChannel.sendMessage("加入 `" + playlist.getName() + "` ").queue();
    }

    @Override
    public void skip(AudioTrack lastTrack, SlashCommandEvent event, Guild guild) {
        if (event != null) {
            event.replyEmbeds(createEmbed("已跳過", 0xD3DAFF)).setEphemeral(true).queue();
        }
        if (guild.getId().equals(guildID))
            logChannel.sendMessage("跳過 `" + lastTrack.getInfo().title + "`").queue();
    }

    @Override
    public void noMoreTrack(GenericInteractionCreateEvent event, Guild guild) {
        if (guild.getAudioManager().isConnected())
            guild.getAudioManager().closeAudioConnection();

        MessageEmbed embed = createEmbed("已停止播放", 0xFF3B7D);
        if (event instanceof SlashCommandEvent)
            event.replyEmbeds(embed).setEphemeral(true).queue();
        if (guild.getId().equals(guildID))
            logChannel.sendMessage("停止播放").queue();
    }

    @Override
    public void repeat(AudioTrack track, boolean repeatState, SlashCommandEvent event) {
        if (repeatState) {
            event.replyEmbeds(createEmbed("單曲循環", 0x7d95b9)).setEphemeral(true).queue();
        } else {
            event.replyEmbeds(createEmbed("正常播放", 0xAFACCC)).setEphemeral(true).queue();
        }
    }

    @Override
    public void pause(boolean pause, SlashCommandEvent event, Guild guild) {
        if (event != null) {
            event.replyEmbeds(pause ? createEmbed("已暫停播放", 0xFF3B7D) : createEmbed("已開始播放", 0x75C44C)).setEphemeral(true).queue();
        }
        if (guild.getId().equals(guildID))
            logChannel.sendMessage(pause ? "暫停音樂播放" : "繼續音樂播放").queue();
    }

    @Override
    public void volumeChange(int volume, SlashCommandEvent event) {
        event.replyEmbeds(createEmbed("已將音量設定為: " + volume, 0xD9B99B)).setEphemeral(true).queue();
    }

    /**
     * method
     */
    private void play(AudioPlaylist playlist, VoiceChannel vc, GuildMusicManager manager, SlashCommandEvent event) {
        if (!event.getGuild().getAudioManager().isConnected())
            event.getGuild().getAudioManager().openAudioConnection(vc);

        manager.scheduler.addPlayListToQueue(playlist, event);
    }

    private void play(AudioTrack track, VoiceChannel vc, GuildMusicManager manager, SlashCommandEvent event) {
        if (!event.getGuild().getAudioManager().isConnected())
            event.getGuild().getAudioManager().openAudioConnection(vc);

        manager.scheduler.queue(track, event);
    }

    private void playPrevious(SlashCommandEvent event) {
        if (!event.getGuild().getAudioManager().isConnected())
            event.getGuild().getAudioManager().openAudioConnection(event.getMember().getVoiceState().getChannel());

        getGuildAudioPlayer(event.getGuild()).scheduler.previousTrack(event);
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        GuildMusicManager musicManager = musicManagers.get(guild.getId());

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager, guild);
            musicManagers.put(guild.getId(), musicManager);
        }

        musicManager.scheduler.setManagerEvent(this);
        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }


}
