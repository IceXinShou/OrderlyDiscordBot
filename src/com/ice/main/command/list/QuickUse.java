package com.ice.main.command.list;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ice.main.util.EmbedCreator.createEmbed;
import static com.ice.main.util.EmbedCreator.isBotOwner;

@SuppressWarnings("ALL")
public class QuickUse {
    Map<String, List<ActionRow>> games = new HashMap<>();
    Map<String, String[]> Ids = new HashMap<>();

    public void onGuildMessageReceived(MessageReceivedEvent event) {
        if (event.getMember() == null || event.getMember().getUser().isBot())
            return;

        Message message = event.getMessage();

        if (message.getContentRaw().startsWith("testCommand") && (isBotOwner(event) /*|| event.getMember().getId().equals(event.getGuild().retrieveOwner().complete().getId())*/)) {

//            event.getChannel().sendMessageEmbeds(createEmbed("原之序 | ORDERLY SERVER", null, "請輸入 `/invite` 來邀請別人為正式成員\nPlease type `/invite` to invite people join this server", "新手 XinShou", "", event.getJDA().getSelfUser().getAvatarUrl(), 0x00ffff)).queue();


//            event.getJDA().getGuildById("864051459121086484").getTextChannelById("864347411165347840").sendMessage("execute at @a run summon creeper ~2 ~ ~2").queue(i -> i.delete().queue());
//            event.getJDA().getGuildById("858672865355890708").getTextChannelById("858672866283356217").sendMessage("pl").queue(i->i.delete().queue());

//            event.getChannel().sendMessageEmbeds(createEmbed("com.ice.Test", "Track cases globally, or in a country, by supplying 2-letter [country codes](https://countrycode.org/).", 0xFF0000)).queue();
            /**
             * Chinese
             */

/*            EmbedBuilder helpBlockChinese = new EmbedBuilder();
            helpBlockChinese.setTitle("原之序 | ORDERLY SERVER");
            helpBlockChinese.setFooter("新手 XinShou");
            helpBlockChinese.addField(emoji.no1.getAsMention() + " 初衷", "以紀律為核心發展的伺服器，希望每位成員可以共同帶動伺服器發展", false);
            helpBlockChinese.addField(emoji.partner.getAsMention() + " 憧憬", "成員們可以在此平等的交流，有任何問題都可以互相幫助", false);
            helpBlockChinese.addField(emoji.discord.getAsMention() + " 加入方式", "" +
                    EmojiUtil.dotEmojis[6].getAsMention() + "" +
                    "必須詳閱並同意規則與注意事項，如有疑慮，隨時可向管理員反應\n\n" +
                    EmojiUtil.dotEmojis[4].getAsMention() + "" +
                    "邀請者要為被邀請的成員之行為負責並持續 1 個月\n\n" +
                    EmojiUtil.dotEmojis[5].getAsMention() + "" +
                    "請邀請者在 <" + tagChannel(authChannelID) + "> 使用指令將被邀請者邀請進來\n", false);
            helpBlockChinese.addBlankField(false);
            helpBlockChinese.addField(emoji.minecraftGrassBlock.getAsMention() + " 此為暫定公告，尚未正式開放！", "目前伺服器尚在籌備中，有意願加入的成員可以在此等待，感謝您", false);
            helpBlockChinese.setColor(0X00FFFF);
            helpBlockChinese.setTimestamp(event.getMessage().getTimeCreated());
*/

            /**
             * English
             */
            /*
            EmbedBuilder helpBlockEnglish = new EmbedBuilder();
            helpBlockEnglish.setTitle("原之序 | ORDERLY SERVER");
            helpBlockEnglish.setFooter("新手 XinShou");
            helpBlockEnglish.addField(emoji.no1.getAsMention() + " Original Goal", "A server with orderly as the core, hope that every member can drive the development of the server together!", false);
            helpBlockEnglish.addField(emoji.partner.getAsMention() + " Longing", "Members can communicate equally here, and they can help each other if they have any questions!", false);
            helpBlockEnglish.addField(emoji.discord.getAsMention() + " How To Join",
                    emoji.dotEmojis[4].getAsMention() + "" +
                            "The inviter is **responsible for the behavior of the invited member** and lasts for 1 month\n" +
                            emoji.dotEmojis[7].getAsMention() + "" +
                            "You have to enter the **correct two-character Chinese nickname **and **Minecraft / English name**\n" +
                            emoji.dot6.getAsMention() + "" +
                            "You must read and agree to the rules. If you have any doubts, you can respond to the developer at any time", false);
            helpBlockEnglish.addBlankField(false);
            helpBlockEnglish.addField(emoji.minecraftGrassBlock.getAsMention() + " This is a tentative announcement and has not yet officially opened", "Currently the server is still in preparation, players who are willing to join can wait here, **thank you so much**", false);
            helpBlockEnglish.setColor(0X00FFFF);
            helpBlockEnglish.setTimestamp(event.getMessage().getTimeCreated());
*/

//            event.getChannel().editMessageEmbedsById("860430606512029716", helpBlockChinese.build()).queue();
//            event.getChannel().sendMessage(helpBlockEnglish.build()).queue();

//            event.getChannel().sendMessageEmbeds(createEmbed("如果有任何問題或是投訴可點擊下方按鈕, 請勿隨意點擊\n" +
//                            "If you have any problems, press the button then we will help you, please do not press it without any reason.", 0x00FFFF))
//
//                    .setActionRows(ActionRow.of(Button.of(ButtonStyle.DANGER, "TicketChannel:newTicket:", "發出請求",
//                            Emoji.fromEmote(emoji.report)))).queue();

/*
            for (String member : memberData.keySet()) {
                if (voiceChannelData.has(member) || member.equals("576747435653595136")) continue;
                Member m;
                try {
                    m = guild.retrieveMemberById(member).complete();
                } catch (Exception e) {
                    continue;
                }
                // 新增私人頻道
                guild.createVoiceChannel(defaultRoomName.replace("%name%", memberData.getJSONObject(member).getString(CHINESE_NICK)), guild.getCategoryById("865145008070852638")).queue(nvc -> {
                    // 創建專屬文字頻道
                    guild.createTextChannel(defaultRoomChatName.replace("%name%", memberData.getJSONObject(member).getString(CHINESE_NICK)), guild.getCategoryById("865145008070852638"))
                            .queue(ntc -> {
                                nvc.createPermissionOverride(m).setAllow(Permission.VIEW_CHANNEL).queue();
                                ntc.createPermissionOverride(m).setAllow(Permission.VIEW_CHANNEL).queue();

                                vcChannelOwner.put(nvc.getId(), member);
                                tcChannelOwner.put(ntc.getId(), member);
                                textChannel.add(ntc.getId());
                                JSONObject vcInfo = new JSONObject();
                                vcInfo.put(VOICE_CHANNEL_ID, nvc.getId());
                                vcInfo.put(TEXT_CHANNEL_ID, ntc.getId());
                                voiceChannelData.put(member, vcInfo);
                                voiceChannelDataFile.saveFile();
                            });
                });
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                }
            }
*/
//            event.getJDA().getGuildById("863234262244786188").moveVoiceMember(event.getJDA().getGuildById("863234262244786188").retrieveMemberById("308721713066147840").complete(), event.getJDA().getGuildById("863234262244786188").getVoiceChannelById("863234263431249978")).queue();

//            for (String member : memberData.keySet()) {
//                try {
//                    VoiceChannel voiceChannel = guild.getVoiceChannelById(voiceChannelData.getJSONObject(member).getString(VOICE_CHANNEL_ID));
//                    TextChannel textChannel = guild.getTextChannelById(voiceChannelData.getJSONObject(member).getString(TEXT_CHANNEL_ID));
//
//                    System.out.println(voiceChannel);
//                    System.out.println(textChannel);
//
//                    voiceChannel.getManager().setBitrate(roomBitrate).queue();
//
//                    System.out.println(voiceChannel.getName() + " Deleting");
//                    voiceChannel.delete().complete();
//                    System.out.println(textChannel.getName() + " Deleting");
//                    textChannel.delete().complete();
//
//                } catch (Exception e) {
//                    System.err.println(e.getMessage());
//                }
//            }

            List<ActionRow> game = gameButtons(3);
            event.getChannel().sendMessageEmbeds(createEmbed("**OOXX大賽!**", 0xFF0000))
                    .setActionRows(game)
                    .queue(m -> {
                        games.put(m.getId(), game);
                        Ids.put(m.getId(), new String[]{event.getMember().getId(), message.getContentRaw().split(" ")[1]});
                    });

//            event.getGuild().getSelfMember().modifyNickname("我叫做序之徒").queue();

//            event.getJDA().getGuildById("847433514248699914").retrieveMemberById("234395307759108106").complete().kick().queue();

//            for (Member m : event.getGuild().getSelfMember().getVoiceState().getChannel().getMembers()) {
//                if (m.getUser().isBot() && !m.getUser().getId().equals(m.getGuild().getSelfMember().getId()))
//                    try {
//                        m.kick().queue();
//                    } catch (Exception e) {
//                        System.err.println(e.getMessage());
//                    }
//
//            }

//            event.getGuild().leave().queue();

//            if (event.getGuild().getId().equals("873558203680567368")) {
//                for (GuildChannel channel : event.getGuild().getChannels()) {
//                    try {
//                        channel.delete().queue();
//                    } catch (Exception e) {
//                        System.out.println(e.getMessage());
//                    }
//                }
//
//                for (Role role : event.getGuild().getRoles()) {
//                    try {
//                        role.delete().queue();
//                    } catch (Exception e) {
//                        System.out.println(e.getMessage());
//                    }
//                }
//
//                for (Category category : event.getGuild().getCategories()) {
//                    try {
//                        category.delete().queue();
//                    } catch (Exception e) {
//                        System.out.println(e.getMessage());
//                    }
//                }
//
//                for (ListedEmote emoji : event.getGuild().retrieveEmotes().complete()) {
//                    try {
//                        emoji.delete().queue();
//                    } catch (Exception e) {
//                        System.out.println(e.getMessage());
//                    }
//                }
//
//                for (ListedEmote emoji : event.getGuild().retrieveEmotes().complete()) {
//                    try {
//                        emoji.delete().queue();
//                    } catch (Exception e) {
//                        System.out.println(e.getMessage());
//                    }
//                }
//
//                List<String> w = new ArrayList<>();
//                String www = "\n" +
//                        "627536018798804993\n" +
//                        "706517019499167776\n" +
//                        "613601779275268115\n" +
//                        "594543799175479306\n" +
//                        "319509734094798848\n" +
//                        "633971675683356673\n" +
//                        "319509734094798848\n" +
//                        "798998311667236924\n" +
//                        "763370805747908640\n" +
//                        "790427407551168565\n" +
//                        "691309675747999754\n" +
//                        "784746730608525313\n" +
//                        "857556148134608916\n" +
//                        "857556148134608916\n" +
//                        "853151455228854273\n" +
//                        "853151455228854273\n" +
//                        "853151455228854273\n" +
//                        "750164699743780915\n" +
//                        "845020305780899840\n" +
//                        "810819325170286613\n" +
//                        "479253812855570433\n" +
//                        "479253812855570433\n" +
//                        "537842010506264606\n" +
//                        "853196348164800552\n" +
//                        "853196348164800552\n" +
//                        "856869221090328586\n" +
//                        "856871041125187654\n" +
//                        "856870655101239306\n" +
//                        "856871401214443531\n" +
//                        "856868892730589185\n" +
//                        "856869929466986496\n" +
//                        "856869775196422144\n" +
//                        "856868289229357066\n" +
//                        "856868354580545546\n" +
//                        "856868354580545546\n" +
//                        "856870926724628550\n" +
//                        "856875757375848448\n" +
//                        "856870145045561344\n" +
//                        "659024834554232842";
//                w.addAll(List.of(www.split("\n")));
//
//                for (String oao:w){
//                    event.getGuild().retrieveMemberById(oao).complete().ban(365).queue();
//                }
//
//
//            }


//            event.getMessage().reply(event.getGuild().retrieveMemberById(event.getMessage().getContentRaw().split(" ")[1]).complete().getActivities().get(0).getName()).queue();
        }
    }

    public List<ActionRow> gameButtons(int size) {
        List<ActionRow> actionRow = new ArrayList<>();
        List<ItemComponent> components = new ArrayList<>();
        for (int i = 0; i < size * size; i++) {
            components.add(Button.secondary("OOXXgame;" + i, "空"));
            if (i % size == size - 1) {
                actionRow.add(ActionRow.of(components));
                components.clear();
            }
        }
        return actionRow;
    }

    public void onButtonClick(ButtonInteractionEvent event, String[] args) {
        if (!args[0].equals("OOXXgame"))
            return;

        String messageID = event.getMessage().getId();

        List<ActionRow> game = games.get(messageID);
        if (games.get(messageID) == null) {
            event.deferEdit().queue();
            return;
        }
        int size = game.size();
        int y = Integer.parseInt(args[1]) / size;
        int x = Integer.parseInt(args[1]) % size;

        List<ItemComponent> newButtons = new ArrayList<>();
        List<Button> buttons = game.get(y).getButtons();
        for (int i = 0; i < size; i++) {
            if (!(i == x && buttons.get(i).getStyle() == ButtonStyle.SECONDARY))
                newButtons.add(buttons.get(i));
            else if (event.getUser().getId().equals(Ids.get(messageID)[0]))
                newButtons.add(Button.primary("OOXXgame;" + size * y + i + ";BLUE", "BLUE"));
            else if (event.getUser().getId().equals(Ids.get(messageID)[1])) {
                newButtons.add(Button.danger("OOXXgame;" + size * y + i + ";RED", "RED"));
            } else
                newButtons.add(buttons.get(i));
        }
        game.set(y, ActionRow.of(newButtons));
        event.getHook().editOriginalComponents(game).queue();
    }
}