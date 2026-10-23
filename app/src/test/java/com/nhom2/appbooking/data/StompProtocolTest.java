package com.nhom2.appbooking.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

public class StompProtocolTest {

    @Test
    public void socketUrlFollowsTheApiAddress() {
        assertEquals("wss://webbooking-c5iu.onrender.com/ws", StompProtocol.socketUrl("https://webbooking-c5iu.onrender.com/"));
        assertEquals("ws://localhost:8080/ws", StompProtocol.socketUrl("http://localhost:8080/"));
        assertEquals("ws://10.0.2.2:8080/ws", StompProtocol.socketUrl("http://10.0.2.2:8080"));
    }

    @Test
    public void connectSendsTheJwtAndAsksForServerHeartbeats() {
        String frame = StompProtocol.connect("example.com", "abc.def.ghi", 10_000);
        assertTrue(frame.startsWith("CONNECT\n"));
        assertTrue(frame.contains("\nAuthorization:Bearer abc.def.ghi\n"));
        assertTrue(frame.contains("\nheart-beat:0,10000\n"));
        assertTrue(frame.endsWith("\n\n\u0000"));
    }

    @Test
    public void heartbeatsAreNotFrames() {
        assertTrue(StompProtocol.parseAll("\n").isEmpty());
        assertTrue(StompProtocol.parseAll("\r\n\n").isEmpty());
    }

    @Test
    public void readsAMessageWithItsJsonBody() {
        String body = "{\"id\":4,\"roomId\":1,\"fromAdmin\":true,\"content\":\"Dạ có ạ\"}";
        List<StompProtocol.Frame> frames = StompProtocol.parseAll(
                "MESSAGE\ndestination:/user/queue/support-chat\ncontent-type:application/json\n\n" + body + "\u0000");
        assertEquals(1, frames.size());
        assertEquals("MESSAGE", frames.get(0).command);
        assertEquals("/user/queue/support-chat", frames.get(0).headers.get("destination"));
        assertEquals(body, frames.get(0).body);
    }

    @Test
    public void splitsSeveralFramesAndKeepsTheFirstRepeatedHeader() {
        List<StompProtocol.Frame> frames = StompProtocol.parseAll(
                "\nCONNECTED\nversion:1.2\nheart-beat:10000,10000\n\n\u0000ERROR\nmessage:first\nmessage:second\n\n\u0000");
        assertEquals(2, frames.size());
        assertEquals("CONNECTED", frames.get(0).command);
        assertEquals("10000,10000", frames.get(0).headers.get("heart-beat"));
        assertEquals("ERROR", frames.get(1).command);
        assertEquals("first", frames.get(1).headers.get("message"));
    }
}
