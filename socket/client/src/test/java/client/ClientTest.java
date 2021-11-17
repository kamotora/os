package client;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientTest {

    @Test
    void sendMessageTest() {
        try (var socket = new Client(1234)) {
            String response = socket.sendMessage("client");
            assertEquals(StringUtils.reverse("client"), response);
        }
    }

    @Test
    void sendBlankTest(){
        try (var socket = new Client(1234)) {
            String response = socket.sendMessage("");
            assertEquals("Bad request", response);
        }
    }
}