package multiBot;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import main.java.Main;
import multiBot.music.GuildMusicManager;
import multiBot.music.MusicInfoData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static main.java.lang.LangKey.*;
import static main.java.util.EmbedCreator.createEmbed;

public record MusicBotEvent(MultiMusicBotManager musicBotManager) implements GuildMusicManager.Event {

    @Override
    public void trackStart(AudioTrack track, GenericInteractionCreateEvent event, Guild guild, MusicBot musicBot, boolean search) {
        if (musicBot != null) {
            musicBot.displayQueue(event, search, event.getGuild());
        }
    }

    @Override
    public void addToQueue(AudioTrack track, @NotNull GenericInteractionCreateEvent event, boolean search, boolean playNow) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        MusicInfoData musicInfo = new MusicInfoData(track);
        // 組裝
        MessageEmbed nowPlaying = createEmbed("**" + musicInfo.getTitle() + "**", "https://www.youtube.com/watch?v=" + musicInfo.getVideoID(), playNow ? lang.get(MUSICBOTEVENT_PLAY_NOW) : lang.get(MUSICBOTEVENT_ADDED_QUEUE),
                " \uD83D\uDC40 " + String.format("%,d", musicInfo.getViewCount()) +
                        " | \uD83D\uDC4D " + String.format("%,d", musicInfo.getLikeCount()) +
                        " | \uD83D\uDC4E " + String.format("%,d", musicInfo.getDislikeCount()) +
                        " | \uD83D\uDCAC " + String.format("%,d", musicInfo.getCommentCount()) +
                        " | \uD83D\uDCC5 " + musicInfo.getPublishDate().replace(',', '-')
                , musicInfo.getChannelName(), musicInfo.getChannelURL(), musicInfo.getChannelThumbnailUrl(), musicInfo.getThumbnailUrl(),
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
            List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOTEVENT_SKIED), 0xD3DAFF)).queue();
        }

    }

    @Override
    public void remove(AudioTrack removedTrack, @NotNull SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        if (removedTrack == null)
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOTEVENT_REMOVED_FAIL), 0xFF0000)).queue();
        else
            event.getHook().editOriginalEmbeds(createEmbed(removedTrack.getInfo().title + lang.get(MUSICBOTEVENT_REMOVED_SUCCESS), 0x00FFFF)).queue();
    }

    @Override
    public void loop(boolean loopState, @NotNull SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        if (loopState) {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOTEVENT_LOOP_PLAY), 0xf89f65)).queue();
        } else {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOTEVENT_NORMAL_PLAY), 0xADACCC)).queue();
        }
    }

    @Override
    public void noMoreTrack(GenericInteractionCreateEvent event, @NotNull Guild guild) {
        if (guild.getAudioManager().isConnected()) {
            // 從頻道移除bot
            musicBotManager.setBotToChannel(guild.getId(), Objects.requireNonNull(guild.getAudioManager().getConnectedChannel()).getId(), null);
            guild.getAudioManager().closeAudioConnection();
        }

        if (event instanceof SlashCommandEvent)
            event.getHook().editOriginalEmbeds(createEmbed("❌", 0xFF3B7D)).queue();
    }

    @Override
    public void repeat(AudioTrack track, boolean repeatState, SlashCommandEvent event) {
        if (repeatState) {
            event.getHook().editOriginalEmbeds(createEmbed("單曲循環", 0x7d95b9)).queue();
        } else {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(MUSICBOTEVENT_NORMAL_PLAY), 0xAFACCC)).queue();
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