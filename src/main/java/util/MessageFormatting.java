package main.java.util;

import java.util.List;

public class MessageFormatting {

    public static String tagUser(String ID) {
        return "<@!" + ID + '>';
    }

    public static String tagChannel(String ID) {
        return "<#" + ID + '>';
    }

    public static String tagRole(String ID) {
        return "<@&" + ID + '>';
    }

    public static String tagUser(List<String> ID) {
        StringBuilder n = new StringBuilder();
        for (String str : ID) {
            n.append("<@&").append(str).append('>');
        }
        return n.toString();
    }

    public static String tagChannel(List<String> ID) {
        StringBuilder n = new StringBuilder();
        for (String str : ID) {
            n.append("<@&").append(str).append('>');
        }
        return n.toString();
    }

    public static String tagRole(List<String> ID) {
        StringBuilder n = new StringBuilder();
        for (String str : ID) {
            n.append("<@&").append(str).append('>');
        }
        return n.toString();
    }


//    User	                    <@USER_ID>	            <@80351110224678912>
//    User (Nickname)	        <@!USER_ID>	            <@!80351110224678912>
//    Channel	                <#CHANNEL_ID>	        <#103735883630395392>
//    Role	                    <@&ROLE_ID>	            <@&165511591545143296>
//    Standard Emoji	        Unicode Characters	💯
//    Custom Emoji	            <:NAME:ID>	            <:mmLol:216154654256398347>
//    Custom Emoji (Animated)	<a:NAME:ID>	            <a:b1nzy:392938283556143104>
//    Unix Timestamp	        <t:TIMESTAMP>	        <t:1618953630>
//    Unix Timestamp (Styled)	<t:TIMESTAMP:STYLE>	    <t:1618953630:d>
}
