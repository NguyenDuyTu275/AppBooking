package com.nhom2.appbooking.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.nhom2.appbooking.BuildConfig;
import com.nhom2.appbooking.data.model.SupportMessage;

import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Nhận tin chat hỗ trợ ngay khi có: STOMP qua WebSocket /ws của backend, như supportChatSocket.ts của web.
 * Backend chỉ cho nghe hàng đợi riêng /user/queue/support-chat; gửi tin vẫn đi qua REST.
 * Rớt kết nối thì tự nối lại sau vài giây rồi gọi onReconnected để màn hình tải lại tin bị lỡ.
 */
public final class SupportChatSocket {

    /** Gọi trên luồng chính. */
    public interface Listener {
        void onMessage(SupportMessage message);

        /** Đã nối lại sau khi rớt: tin đẩy trong lúc rớt không tới được, nên tải lại. */
        void onReconnected();
    }

    private static final String TAG = "SupportChatSocket";
    private static final String QUEUE = "/user/queue/support-chat";
    private static final long RECONNECT_DELAY_MS = 5000;
    /** Bằng WebSocketConfig.HEARTBEAT_MS của backend: proxy trước Render cắt kết nối im lặng khoảng 100 giây. */
    private static final long SERVER_HEARTBEAT_MS = 10_000;
    /** Ping WebSocket để phát hiện kết nối đã chết (mất mạng) mà không có sự kiện đóng. */
    private static final long PING_INTERVAL_S = 20;

    private static OkHttpClient client;

    private final SessionManager session;
    private final Listener listener;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();
    private final Runnable reconnect = this::open;
    // Chỉ đọc/ghi trên luồng chính
    private WebSocket socket;
    private boolean closed;
    private boolean connectedBefore;

    private SupportChatSocket(Context context, Listener listener) {
        this.session = new SessionManager(context);
        this.listener = listener;
    }

    /** Mở kết nối (trong onResume); nhớ gọi close() trong onPause. */
    public static SupportChatSocket connect(Context context, Listener listener) {
        SupportChatSocket chat = new SupportChatSocket(context, listener);
        chat.open();
        return chat;
    }

    public void close() {
        closed = true;
        main.removeCallbacks(reconnect);
        if (socket != null) {
            socket.close(1000, null);
            socket = null;
        }
    }

    private void open() {
        String token = session.getToken();
        if (closed || token == null) {
            return;
        }
        String host = HttpUrl.get(BuildConfig.API_BASE_URL).host();
        Request request = new Request.Builder().url(StompProtocol.socketUrl(BuildConfig.API_BASE_URL)).build();
        socket = client().newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                webSocket.send(StompProtocol.connect(host, token, SERVER_HEARTBEAT_MS));
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                for (StompProtocol.Frame frame : StompProtocol.parseAll(text)) {
                    handle(webSocket, frame);
                }
            }

            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                retryLater(webSocket);
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
                Log.w(TAG, "Support chat socket dropped", t);
                retryLater(webSocket);
            }
        });
    }

    // Chạy trên luồng của OkHttp
    private void handle(WebSocket webSocket, StompProtocol.Frame frame) {
        switch (frame.command) {
            case "CONNECTED":
                webSocket.send(StompProtocol.subscribe("support-chat", QUEUE));
                onMain(webSocket, () -> {
                    if (connectedBefore) {
                        listener.onReconnected();
                    }
                    connectedBefore = true;
                });
                break;
            case "MESSAGE":
                try {
                    SupportMessage message = gson.fromJson(frame.body, SupportMessage.class);
                    if (message != null) {
                        onMain(webSocket, () -> listener.onMessage(message));
                    }
                } catch (JsonParseException e) {
                    Log.w(TAG, "Unreadable support chat message", e);
                }
                break;
            case "ERROR":
                // Server từ chối (token sai, hết hạn...): nối lại cũng bị từ chối, nên dừng hẳn
                Log.w(TAG, "Support chat refused: " + frame.headers.get("message"));
                onMain(webSocket, this::close);
                break;
            default:
                break;
        }
    }

    /** Chạy trên luồng chính, bỏ qua nếu kết nối này đã bị đóng hoặc thay bằng kết nối mới. */
    private void onMain(WebSocket from, Runnable action) {
        main.post(() -> {
            if (!closed && from == socket) {
                action.run();
            }
        });
    }

    private void retryLater(WebSocket from) {
        onMain(from, () -> {
            socket = null;
            main.postDelayed(reconnect, RECONNECT_DELAY_MS);
        });
    }

    private static synchronized OkHttpClient client() {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    // Kết nối WebSocket mở lâu: không đặt thời hạn đọc, dùng ping để phát hiện kết nối chết
                    .readTimeout(0, TimeUnit.MILLISECONDS)
                    .pingInterval(PING_INTERVAL_S, TimeUnit.SECONDS)
                    .build();
        }
        return client;
    }
}
