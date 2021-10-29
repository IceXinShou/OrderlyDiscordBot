//package main.messenger;
//
//import com.github.messenger4j.Messenger;
//import com.github.messenger4j.exception.MessengerApiException;
//import com.github.messenger4j.exception.MessengerIOException;
//import com.github.messenger4j.send.MessagePayload;
//import com.github.messenger4j.send.MessagingType;
//import com.github.messenger4j.send.message.TextMessage;
//
//import java.util.Optional;
//
//public class Receive {
//    final String payload =
//            "{\n"
//                    + "  \"object\": \"page\",\n"
//                    + "  \"entry\": [{\n"
//                    + "    \"id\": \"1717527131834678\",\n"
//                    + "    \"time\": 1475942721780,\n"
//                    + "    \"messaging\": [{\n"
//                    + "      \"sender\": {\n"
//                    + "        \"id\": \"1256217357730577\"\n"
//                    + "      },\n"
//                    + "      \"recipient\": {\n"
//                    + "        \"id\": \"1717527131834678\"\n"
//                    + "      },\n"
//                    + "      \"timestamp\": 1475942721741,\n"
//                    + "      \"message\": {\n"
//                    + "        \"mid\": \"mid.1475942721728:3b9e3646712f9bed52\",\n"
//                    + "        \"seq\": 123,\n"
//                    + "        \"text\": \"Hello Chatbot\"\n"
//                    + "      }\n"
//                    + "    }]\n"
//                    + "  }]\n"
//                    + "}";
//
//    final Messenger main.messenger = Messenger.create("PAGE_ACCESS_TOKEN", "APP_SECRET", "VERIFY_TOKEN");
//
//main.messenger.onReceiveEvents(
//    payload,
//            Optional.empty(),
//    event -> {
//        final String senderId = event.senderId();
//        if (event.isTextMessageEvent()) {
//            final String text = event.asTextMessageEvent().text();
//
//            final TextMessage textMessage = TextMessage.create(text);
//            final MessagePayload messagePayload =
//                    MessagePayload.create(senderId, MessagingType.RESPONSE, textMessage);
//
//            try {
//                main.messenger.send(messagePayload);
//            } catch (MessengerApiException | MessengerIOException e) {
//                // Oops, something went wrong
//            }
//        }
//    });
//
//}
