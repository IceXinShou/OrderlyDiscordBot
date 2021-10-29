package main.command.list;

import main.Main;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static main.lang.LangKey.*;
import static main.util.EmbedCreator.createEmbed;
import static main.util.UrlDataGetter.getDataAuthorization;
import static main.util.UrlDataGetter.postDataAuthorization;

public class FileConvert {
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public void onCommand(SlashCommandEvent event, URLShort urlShort) {
        List<String> lang = Main.language.getGuildLang(event.getGuild().getId());
        // run thread
        executor.submit(() -> {
            event.getHook().editOriginalEmbeds(createEmbed(lang.get(FILECONVERT_CONVERTING), lang.get(FILECONVERT_WAIT_TIME), 0xFFA500)).queue();
            String url = event.getOption("url").getAsString();
            String outputName = ((event.getOption("outputname") == null) ? "Converted" : event.getOption("outputname").getAsString()) + ".gif";
            String fps = (event.getOption("fps") == null) ? "30" : event.getOption("fps").getAsString();

            String payload = "{\"tasks\":{\"import\":{\"operation\":\"import/url\",\"url\":\"" + url + "\"},\"task\":{\"operation\":\"convert\",\"input_format\":\"mp4\",\"output_format\":\"gif\",\"engine\":\"ffmpeg\",\"input\":[\"import\"],\"video_codec\":\"gif\",\"fps\":" + fps + ",\"filename\":\"" + outputName + "\"},\"export\":{\"operation\":\"export/url\",\"input\":[\"task\"],\"inline\":false,\"archive_multiple_files\":false}}}";
            String authorization = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxIiwianRpIjoiOWFkNjM5NDMwNjMyYTE1YmYwOTIyYmU3NGY3MmI1OGIzYTc2NjViYTE0OTI1ZDYwYjVmOWI0MjQwOTQ1ZTY3MTBmZTQ5NWQ0MjY2OTczODYiLCJpYXQiOjE2Mjg5NTQwNTEuMjEzNDUzLCJuYmYiOjE2Mjg5NTQwNTEuMjEzNDU2LCJleHAiOjQ3ODQ2Mjc2NTEuMTc2OTkzLCJzdWIiOiI1MjgyNTg2OSIsInNjb3BlcyI6WyJ0YXNrLnJlYWQiLCJ0YXNrLndyaXRlIl19.ogdiXCvwvc213UqMvldYDiRcrZPiRnqkDdDaSz9CO71bnUbyQLjaPXYwKKXWwKu84qb6ix_A766fGtDLnRbrwbKeD3ZMC8_zFnNzrN1kN-P644xBkD3l8IBALCn1Y0I1aoZJ1qcXkTO1Cobyptozefj9NAGh8tZriEnRR4-b1jB6vY-xGw5bnk8p_tAFDlz4nwmGH_RQWyNYDWqY0_wtHtUmdjJCnwhGd-DaVQJX4tgUvhOdNYHr4i1IAIEWsSOsFtqwoycuihyQ9ugbreJJGdKSROKOndsc4xEgP3h9xkwR2fyeF-tk980eORkywPwbqKFTx31aBtr6lhJbvErwE3v7o-63sO4glIRhliDwntE5Reu1_kwXVc4TCQmW439krFn-eeYubBF0QcoV73YobAaT3SUXbc6Nav_EH1wsIAD6P6KQ3_cykx2AxEJ_gUdubZSEc8X7lTZLZIeOAHd4bnhu4xCZrKA3pOojJp7dkAj_vxUn9Ejc6HsgvHaLi9OOpB4YfFcAfxYwsehJKhOT-8Kg8fGlbtEP0eksdXbrBctfuQ2vVNsiF78K7GzrmzlupYHbLCM2L2TkVYvJFZHngNA-G72rlGy2cPMYWRmCX864gJcGiHhgMBlzGjgD4XoVNDIGbXXRvkH8aOP7BfV53BkwX4-5lPgzOxA9IjqIz7s";
            String result = postDataAuthorization("https://api.cloudconvert.com/v2/jobs", payload, authorization);
            if (result == null) {
                event.getHook().editOriginalEmbeds(createEmbed(lang.get(FILECONVERT_FAIL), 0xFF0000)).queue();
                return;
            }
            String urlID = new JSONObject(result).getJSONObject("data").getJSONArray("tasks").getJSONObject(2).getString("id");
            JSONObject expertData = new JSONObject(getDataAuthorization("https://api.cloudconvert.com/v2/tasks/" + urlID, authorization)).getJSONObject("data");
            while (!expertData.getString("status").equals("finished")) {
                try {
                    expertData = new JSONObject(getDataAuthorization("https://api.cloudconvert.com/v2/tasks/" + urlID, authorization)).getJSONObject("data");
                    if (expertData.getString("status").equals("finished")) {
                        urlShort.onCommand(event, true,
                                expertData.getJSONObject("result").getJSONArray("files").getJSONObject(0).getString("url")
                                , lang);
                        return;
                    }
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                }
            }
        });
    }
}