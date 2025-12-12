package cn.xiaozhou233.juiceloader;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.WebSocket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class IPCServer extends WebSocketServer {
    private static final int PORT = 58423;
    public IPCServer() {
        super(new InetSocketAddress("0.0.0.0", PORT));
    }

    @Override
    public void onStart() {
        System.out.println("[JuiceLoader IPC] IPC Server started on port " + PORT);
        Class<?>[] loadedClasses = JuiceLoader.getLoadedClasses();
        if (loadedClasses != null) {
            System.out.println("[JuiceLoader IPC] Loaded Classes:" + loadedClasses.length);
        } else {
            System.err.println("[JuiceLoader IPC] Failed to get loaded classes");
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("[JuiceLoader IPC] Client connected: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("[JuiceLoader IPC] Text: " + message);
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        super.onMessage(conn, message);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {}

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

}
