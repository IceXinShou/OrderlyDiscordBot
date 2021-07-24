package multiBot;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import multiBot.music.GuildMusicManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import static main.java.event.Log.logChannel;
import static main.java.util.Funtions.createEmbed;
import static main.java.util.GuildUtil.guildID;

public class MusicBotEvent implements GuildMusicManager.Event {
    @Override
    public void playStart(AudioTrack track, GenericInteractionCreateEvent event, Guild guild, MusicBot musicBot) {
        if (musicBot != null) {
            musicBot.displayQueue(event);
        }
        if (guild.getId().equals(guildID))
            logChannel.sendMessage("開始播放 `" + track.getInfo().title + "`").queue();
    }

    @Override
    public void addToQueue(AudioTrack track, GenericInteractionCreateEvent event) {
        event.replyEmbeds(createEmbed("加入播放清單: `" + track.getInfo().title + "`", 0xBCE9B6)).setEphemeral(true).queue();
        if (!event.getGuild().getId().equals(guildID))
            logChannel.sendMessage("加入播放清單: `" + track.getInfo().title + "` ").queue();
    }

    @Override
    public void addPlayerListToQueue(AudioPlaylist playlist, GenericInteractionCreateEvent event) {
//        event.replyEmbeds(createEmbed("加入 `" + playlist.getName() + "` ", 0xBCE9B6)).setEphemeral(true).queue();
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

}