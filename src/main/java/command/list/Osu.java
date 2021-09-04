package main.java.command.list;

import main.java.Main;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import static main.java.Main.emoji;
import static main.java.util.EmbedCreator.createEmbed;
import static main.java.util.GuildUtil.guildID;
import static main.java.util.UrlDataGetter.getData;

public class Osu {

    public void search(@NotNull SlashCommandEvent event) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        String result = getData("https://osu.ppy.sh/api/get_user?u=" + event.getOption("name").getAsString().replace(' ', '_') + "&k=b79a9a88c8aa44d689c44b6af2fe3a356e301f48");
        if (new JSONArray(result).length() == 0) {
            event.getHook().editOriginalEmbeds(createEmbed("名字錯誤", 0xFF0000)).queue();
            return;
        }
        JSONObject data = new JSONArray(result).getJSONObject(0);
        if (event.getGuild().getId().equals(guildID))
            event.getHook().editOriginalEmbeds(createEmbed(
                    "",
                    "https://a.ppy.sh/" + data.getString("user_id") + "?476",
                    "https://osu.ppy.sh/users/" + data.getString("user_id"),
                    "" +
                            "▸**排行: **" + '#' + String.format("%,d", Integer.parseInt(data.getString("pp_rank"))) + " (" + data.getString("country") + " #" + String.format("%,d", Integer.parseInt(data.getString("pp_country_rank"))) + ')' + '\n' +
                            "▸**等級: **" + ((int) (Float.parseFloat(data.getString("level")))) + '\n' +
                            "▸**PP: **" + String.format("%,.2f", Float.parseFloat(data.getString("pp_raw"))) + "  **Acc: **" + String.format("%.2f", Float.parseFloat(data.getString("accuracy"))) + "%\n" +
                            "▸**遊玩次數: **" + String.format("%,d", Integer.parseInt(data.getString("playcount"))) + " (" + String.format("%.2f", (Float.parseFloat(data.getString("total_seconds_played")) / 60 / 60)) + " 小時)\n" +
//                        "▸**等級: **" + String.format("%.0f", Float.parseFloat(data.getString("level"))) + '\n' +
                            "▸**成績: **" +
                            emoji.osu_ssh.getAsMention() + '`' + data.getString("count_rank_ssh") + '`' +
                            emoji.osu_sh.getAsMention() + '`' + data.getString("count_rank_sh") + '`' +
                            emoji.osu_ss.getAsMention() + '`' + data.getString("count_rank_ss") + '`' +
                            emoji.osu_s.getAsMention() + '`' + data.getString("count_rank_s") + '`' +
                            emoji.osu_a.getAsMention() + '`' + data.getString("count_rank_a") + '`',
                    "",
                    data.getString("username"),
                    "https://www.countryflags.io/" + data.getString("country") + "/flat/64.png",
                    0x00FFFF
            )).queue();
        else {
            event.getTextChannel().sendMessageEmbeds(createEmbed(
                    "",
                    "https://a.ppy.sh/" + data.getString("user_id") + "?476",
                    "https://osu.ppy.sh/users/" + data.getString("user_id"),
                    "" +
                            "▸**排行: **" + '#' + String.format("%,d", Integer.parseInt(data.getString("pp_rank"))) + " (" + data.getString("country") + " #" + String.format("%,d", Integer.parseInt(data.getString("pp_country_rank"))) + ')' + '\n' +
                            "▸**等級: **" + ((int) (Float.parseFloat(data.getString("level")))) + '\n' +
                            "▸**PP: **" + String.format("%,.2f", Float.parseFloat(data.getString("pp_raw"))) + "  **Acc: **" + String.format("%.2f", Float.parseFloat(data.getString("accuracy"))) + "%\n" +
                            "▸**遊玩次數: **" + String.format("%,d", Integer.parseInt(data.getString("playcount"))) + " (" + String.format("%.2f", (Float.parseFloat(data.getString("total_seconds_played")) / 60 / 60)) + " 小時)\n" +
//                        "▸**等級: **" + String.format("%.0f", Float.parseFloat(data.getString("level"))) + '\n' +
                            "▸**成績: **" +
                            emoji.osu_ssh.getAsMention() + '`' + data.getString("count_rank_ssh") + '`' +
                            emoji.osu_sh.getAsMention() + '`' + data.getString("count_rank_sh") + '`' +
                            emoji.osu_ss.getAsMention() + '`' + data.getString("count_rank_ss") + '`' +
                            emoji.osu_s.getAsMention() + '`' + data.getString("count_rank_s") + '`' +
                            emoji.osu_a.getAsMention() + '`' + data.getString("count_rank_a") + '`',
                    "",
                    data.getString("username"),
                    "https://www.countryflags.io/" + data.getString("country") + "/flat/64.png",
                    0x00FFFF
            )).queue();
            event.getHook().editOriginalEmbeds(createEmbed("完成", 0x00ffff)).queue();
        }

    }

}
