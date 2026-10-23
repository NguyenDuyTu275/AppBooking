package com.nhom2.appbooking.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Phần STOMP 1.2 mà chat hỗ trợ cần: địa chỉ /ws, frame CONNECT/SUBSCRIBE, tách frame server gửi về. */
final class StompProtocol {

    private static final String NUL = "\u0000";

    private StompProtocol() {
    }

    /** "https://host/" -> "wss://host/ws", "http://localhost:8080/" -> "ws://localhost:8080/ws". */
    static String socketUrl(String apiBaseUrl) {
        String base = apiBaseUrl.endsWith("/") ? apiBaseUrl : apiBaseUrl + "/";
        return base.replaceFirst("^http", "ws") + "ws";
    }

    /** Đăng nhập bằng JWT như REST; client không gửi nhịp tim, chỉ xin server gửi mỗi serverHeartbeatMs. */
    static String connect(String host, String token, long serverHeartbeatMs) {
        return "CONNECT\naccept-version:1.2\nhost:" + host
                + "\nheart-beat:0," + serverHeartbeatMs
                + "\nAuthorization:Bearer " + token + "\n\n" + NUL;
    }

    static String subscribe(String id, String destination) {
        return "SUBSCRIBE\nid:" + id + "\ndestination:" + destination + "\n\n" + NUL;
    }

    /** Một chuỗi nhận qua WebSocket có thể chứa nhiều frame; dòng trống là nhịp tim, bỏ qua. */
    static List<Frame> parseAll(String text) {
        List<Frame> frames = new ArrayList<>();
        for (String raw : text.split(NUL)) {
            String frame = raw.replaceFirst("^[\r\n]+", "");
            if (!frame.isEmpty()) {
                frames.add(parse(frame));
            }
        }
        return frames;
    }

    private static Frame parse(String frame) {
        int split = frame.indexOf("\n\n");
        int bodyStart = split + 2;
        int crlf = frame.indexOf("\r\n\r\n");
        if (crlf >= 0 && (split < 0 || crlf < split)) {
            split = crlf;
            bodyStart = crlf + 4;
        }
        String head = split < 0 ? frame : frame.substring(0, split);
        String body = split < 0 ? "" : frame.substring(bodyStart);
        String[] lines = head.split("\r?\n");
        Map<String, String> headers = new HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            int colon = lines[i].indexOf(':');
            if (colon > 0) {
                // STOMP 1.2: header lặp lại thì giá trị đầu tiên có hiệu lực
                headers.putIfAbsent(lines[i].substring(0, colon), lines[i].substring(colon + 1));
            }
        }
        return new Frame(lines[0].trim(), Collections.unmodifiableMap(headers), body);
    }

    static final class Frame {
        final String command;
        final Map<String, String> headers;
        final String body;

        Frame(String command, Map<String, String> headers, String body) {
            this.command = command;
            this.headers = headers;
            this.body = body;
        }
    }
}
