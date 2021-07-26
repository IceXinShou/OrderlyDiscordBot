package multiBot;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import multiBot.music.GuildMusicManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import static main.java.event.Log.logChannel;
import static main.java.util.Funtions.createEmbed;
import static main.java.util.GuildUtil.guildID;

public class MusicBotEvent implements GuildMusicManager.Event {
    private final MultiMusicBotManager musicBotManager;


    public MusicBotEvent(MultiMusicBotManager musicBotManager) {
        this.musicBotManager = musicBotManager;
    }

    @Override
    public void playStart(AudioTrack track, GenericInteractionCreateEvent event, Guild guild, MusicBot musicBot, boolean searchAble) {
        if (musicBot != null) {
            musicBot.displayQueue(event, searchAble);
        }
        if (guild.getId().equals(guildID))
            logChannel.sendMessage("開始播放 `" + track.getInfo().title + "`").queue();
    }

    @Override
    public void addToQueue(AudioTrack track, GenericInteractionCreateEvent event) {
        event.getHook().editOriginalEmbeds(createEmbed("加入播放清單: `" + track.getInfo().title + "`", 0xBCE9B6)).queue();
        if (!event.getGuild().getId().equals(guildID))
            logChannel.sendMessage("加入播放清單: `" + track.getInfo().title + "` ").queue();
    }

    @Override
    public void addPlayerListToQueue(AudioPlaylist playlist, GenericInteractionCreateEvent event) {
//        event.getHook().editOriginalEmbeds(createEmbed("加入 `" + playlist.getName() + "` ", 0xBCE9B6)).setEphemeral(true).queue();
        if (!event.getGuild().getId().equals(guildID))
            logChannel.sendMessage("加入 `" + playlist.getName() + "` ").queue();
    }

    @Override
    public void skip(AudioTrack lastTrack, SlashCommandEvent event, Guild guild) {
        if (event != null) {
            event.getHook().editOriginalEmbeds(createEmbed("已跳過", 0xD3DAFF)).queue();
            if (guild.getId().equals(guildID))
                logChannel.sendMessage("跳過 `" + lastTrack.getInfo().title + "`").queue();
        }

    }

    @Override
    public void noMoreTrack(GenericInteractionCreateEvent event, Guild guild) {
        if (guild.getAudioManager().isConnected()) {
            // 從頻道移除bot
            musicBotManager.setBotToChannel(guild.getId(), guild.getAudioManager().getConnectedChannel().getId(), null);
            guild.getAudioManager().closeAudioConnection();
        }

        if (event instanceof SlashCommandEvent)
            event.getHook().editOriginalEmbeds(createEmbed("已停止播放", 0xFF3B7D)).queue();
        if (guild.getId().equals(guildID))
            logChannel.sendMessage("停止播放").queue();
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
    public void pause(boolean pause, SlashCommandEvent event, Guild guild) {
        if (event != null) {
            event.getHook().editOriginalEmbeds(pause ? createEmbed("已暫停播放", 0xFF3B7D) : createEmbed("已開始播放", 0x75C44C)).queue();
        }
        if (guild.getId().equals(guildID))
            logChannel.sendMessage(pause ? "暫停音樂播放" : "繼續音樂播放").queue();
    }

    @Override
    public void volumeChange(int volume, SlashCommandEvent event) {
        if (event != null)
            event.getHook().editOriginalEmbeds(createEmbed("已將音量設定為: " + volume, 0xD9B99B)).queue();
    }

}
