package main.java.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import static main.java.BotSetting.memberRole;
import static main.java.BotSetting.roomBitrate;
import static main.java.command.VoiceChannelCommand.voiceChannelData;
import static main.java.event.Join.memberData;
import static main.java.util.GuildUtil.guild;
import static main.java.util.JsonKeys.TEXT_CHANNEL_ID;
import static main.java.util.JsonKeys.VOICE_CHANNEL_ID;

public class BlockCommand extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        super.onGuildMessageReceived(event);
        Message message = event.getMessage();

        if (message.getContentRaw().equals("owo") && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {

//
//            /**
//             * Chinese
//             */
//            EmbedBuilder helpBlockChinese = new EmbedBuilder();
//            helpBlockChinese.setTitle("原之序 | ORDERLY SERVER");
//            helpBlockChinese.setFooter("新手 XinShou");
//            helpBlockChinese.addField(emoji.no1.getAsMention() + " 初衷", "以紀律為核心發展的伺服器，希望每位成員可以共同帶動伺服器發展", false);
//            helpBlockChinese.addField(emoji.partner.getAsMention() + " 憧憬", "成員們可以在此平等的交流，有任何問題都可以互相幫助", false);
//            helpBlockChinese.addField(emoji.discord.getAsMention() + " 加入方式",
//                    emoji.dot4.getAsMention() + "" +
//                            "邀請者要為被邀請的成員之行為負責並持續 1 個月\n" +
//                            emoji.dot7.getAsMention() + "" +
//                            "加入時，需輸入正確的**二字中文暱稱**與 **Minecraft / English 名字**\n" +
//                            emoji.dot6.getAsMention() + "" +
//                            "必須詳閱並同意規則與注意事項，如有疑慮，隨時可向管理員反應", false);
//            helpBlockChinese.addBlankField(false);
//            helpBlockChinese.addField(emoji.minecraftGrassBlock.getAsMention() + " 此為暫定公告，尚未正式開放！", "目前伺服器尚在籌備中，有意願加入的成員可以在此等待，感謝您", false);
//            helpBlockChinese.setColor(0X00FFFF);
//            helpBlockChinese.setTimestamp(event.getMessage().getTimeCreated());
//
//
//            /**
//             * English
//             */
//            EmbedBuilder helpBlockEnglish = new EmbedBuilder();
//            helpBlockEnglish.setTitle("原之序 | ORDERLY SERVER");
//            helpBlockEnglish.setFooter("新手 XinShou");
//            helpBlockEnglish.addField(emoji.no1.getAsMention() + " Original Goal", "A server with orderly as the core, hope that every member can drive the development of the server together!", false);
//            helpBlockEnglish.addField(emoji.partner.getAsMention() + " Longing", "Members can communicate equally here, and they can help each other if they have any questions!", false);
//            helpBlockEnglish.addField(emoji.discord.getAsMention() + " How To Join",
//                    emoji.dot4.getAsMention() + "" +
//                            "The inviter is **responsible for the behavior of the invited member** and lasts for 1 month\n" +
//                            emoji.dot7.getAsMention() + "" +
//                            "You have to enter the **correct two-character Chinese nickname **and **Minecraft / English name**\n" +
//                            emoji.dot6.getAsMention() + "" +
//                            "You must read and agree to the rules. If you have any doubts, you can respond to the developer at any time", false);
//            helpBlockEnglish.addBlankField(false);
//            helpBlockEnglish.addField(emoji.minecraftGrassBlock.getAsMention() + " This is a tentative announcement and has not yet officially opened", "Currently the server is still in preparation, players who are willing to join can wait here, **thank you so much**", false);
//            helpBlockEnglish.setColor(0X00FFFF);
//            helpBlockEnglish.setTimestamp(event.getMessage().getTimeCreated());
//
//
//            event.getChannel().sendMessage(helpBlockChinese.build()).queue();
//            event.getChannel().sendMessage(helpBlockEnglish.build()).queue();

//            MessageBuilder builder = new MessageBuilder();
//            builder.setContent("如果有任何問題或是投訴可點擊下方按鈕" + "(亂按的ban掉");
//            builder.setActionRows(ActionRow.of(Button.of(ButtonStyle.DANGER, "newTicket", "發出請求", Emoji.fromEmote(emoji.report))));
//            event.getChannel().sendMessage(builder.build()).queue();
//
//            for (String member : memberData.keySet()) {
//                if (voiceChannelData.has(member) || member.equals("576747435653595136")) continue;
//                Member m;
//                try {
//                    m = guild.retrieveMemberById(member).complete();
//                } catch (Exception e) {
//                    continue;
//                }
//                //新增私人頻道
//                guild.createVoiceChannel(defaultRoomName.replace("%name%", memberData.getJSONObject(member).getString(CHINESE_NICK)), guild.getCategoryById("865145008070852638")).queue(nvc -> {
//                    //創建專屬文字頻道
//                    guild.createTextChannel(defaultRoomChatName.replace("%name%", memberData.getJSONObject(member).getString(CHINESE_NICK)), guild.getCategoryById("865145008070852638"))
//                            .queue(ntc -> {
//                                nvc.createPermissionOverride(m).setAllow(Permission.VIEW_CHANNEL).queue();
//                                ntc.createPermissionOverride(m).setAllow(Permission.VIEW_CHANNEL).queue();
//
//                                vcChannelOwner.put(nvc.getId(), member);
//                                tcChannelOwner.put(ntc.getId(), member);
//                                textChannel.add(ntc.getId());
//                                JSONObject vcInfo = new JSONObject();
//                                vcInfo.put(VOICE_CHANNEL_ID, nvc.getId());
//                                vcInfo.put(TEXT_CHANNEL_ID, ntc.getId());
//                                voiceChannelData.put(member, vcInfo);
//                                voiceChannelDataFile.saveFile();
//                            });
//                });
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }



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
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }






        }
    }
}