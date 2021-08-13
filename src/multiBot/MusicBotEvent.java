package multiBot;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import multiBot.music.GuildMusicManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

import static main.java.util.EmbedCreator.createEmbed;

public class MusicBotEvent implements GuildMusicManager.Event {
    private final MultiMusicBotManager musicBotManager;


    public MusicBotEvent(MultiMusicBotManager musicBotManager) {
        this.musicBotManager = musicBotManager;
    }

    @Override
    public void trackStart(AudioTrack track, GenericInteractionCreateEvent event, Guild guild, MusicBot musicBot, boolean search) {
        if (musicBot != null) {
            musicBot.updateVideoInfo(guild);
            musicBot.displayQueue(event, searchAble);
        }
    }

    @Override
    public void addToQueue(AudioTrack track, GenericInteractionCreateEvent event, boolean search, boolean playNow) {
        MusicInfoData musicInfo = new MusicInfoData(track);
        // 組裝
        MessageEmbed nowPlaying = createEmbed("**" + musicInfo.getTitle() + "**", "https://www.youtube.com/watch?v=" + musicInfo.getVideoID(), playNow ? "已插播" : "已添加至播放清單",
                new StringBuilder()
                        .append(" \uD83D\uDC40 ").append(String.format("%,d", musicInfo.getViewCount()))
                        .append(" | \uD83D\uDC4D ").append(String.format("%,d", musicInfo.getLikeCount()))
                        .append(" | \uD83D\uDC4E ").append(String.format("%,d", musicInfo.getDislikeCount()))
                        .append(" | \uD83D\uDCAC ").append(String.format("%,d", musicInfo.getCommentCount()))
                        .append(" | \uD83D\uDCC5 ").append(musicInfo.getPublishDate().replace(',', '-'))
                        .toString()
                , musicInfo.getChannelName(), musicInfo.getChannelThumbnailUrl(), musicInfo.getThumbnailUrl(),
                0xe5b849);
        if (search) {
            event.replyEmbeds(nowPlaying).setEphemeral(true).queue();
        } else
            event.getHook().editOriginalEmbeds(nowPlaying).queue();
    }

    @Override
    public void addPlayerListToQueue(AudioPlaylist playlist, @NotNull GenericInteractionCreateEvent event) {
//        event.getHook().editOriginalEmbeds(createEmbed("加入 `" + playlist.getName() + "` ", 0xBCE9B6)).setEphemeral(true).queue();
    }

    @Override
    public void skip(AudioTrack lastTrack, SlashCommandEvent event, Guild guild) {
        if (event != null) {
            event.getHook().editOriginalEmbeds(createEmbed("已跳過", 0xD3DAFF)).queue();
        }

    }

    @Override
    public void remove(AudioTrack removedTrack, SlashCommandEvent event) {

        if (removedTrack == null)
            event.getHook().editOriginalEmbeds(createEmbed("移除失敗", 0xFF0000)).queue();
        else
            event.getHook().editOriginalEmbeds(createEmbed(removedTrack.getInfo().title + "已移除", 0x00FFFF)).queue();
    }

    @Override
    public void loop(boolean loopState, SlashCommandEvent event) {
        if (loopState) {
            event.getHook().editOriginalEmbeds(createEmbed("循環播放", 0xf89f65)).queue();
        } else {
            event.getHook().editOriginalEmbeds(createEmbed("正常播放", 0xADACCC)).queue();
        }
    }

    @Override
    public void noMoreTrack(GenericInteractionCreateEvent event, @NotNull Guild guild) {
        if (guild.getAudioManager().isConnected()) {
            // 從頻道移除bot
            musicBotManager.setBotToChannel(guild.getId(), guild.getAudioManager().getConnectedChannel().getId(), null);
            guild.getAudioManager().closeAudioConnection();
        }

        if (event instanceof SlashCommandEvent)
            event.getHook().editOriginalEmbeds(createEmbed("已停止播放", 0xFF3B7D)).queue();
    }

    @Override
    public void repeat(AudioTrack track, boolean repeatState, SlashCommandEvent event) {
        if (repeatState) {
            event.getHook().editOriginalEmbeds(createEmbed("單曲循環", 0x7d95b9)).queue();
        } else {
            event.getHook().editOriginalEmbeds(createEmbed("正常播放", 0xAFACCC)).queue();
        }
    }

    @Override
    public void pauseStateChange(boolean pause, SlashCommandEvent event, Guild guild) {
        if (event != null) {
            event.getHook().editOriginalEmbeds(pause ? createEmbed("已暫停播放", 0xFF3B7D) : createEmbed("已開始播放", 0x75C44C)).queue();
        }
    }

    @Override
    public void volumeChange(int volume, SlashCommandEvent event) {
        if (event != null)
            event.getHook().editOriginalEmbeds(createEmbed("已將音量設定為: " + volume, 0xD9B99B)).queue();
    }

}
