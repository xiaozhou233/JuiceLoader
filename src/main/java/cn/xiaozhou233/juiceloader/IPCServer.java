package cn.xiaozhou233.juiceloader;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.WebSocket;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class IPCServer extends WebSocketServer {

    private static final int PORT = 58423;
    private static final Gson gson = new Gson();

    public IPCServer() {
        super(new InetSocketAddress("0.0.0.0", PORT));
    }

    @Override
    public void onStart() {
        log("IPC Server started on port " + PORT);
        Class<?>[] loadedClasses = JuiceLoader.getLoadedClasses();
        if (loadedClasses != null) {
            log("Loaded Classes: " + loadedClasses.length);
        } else {
            logError("Failed to get loaded classes");
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        log("Client connected: " + conn.getRemoteSocketAddress());
        sendResponse(conn, 200, "Connected to JuiceLoader IPC Server");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        log("Received message: " + message);
        int code = 200;
        String action = null;

        try {
            JsonObject obj = gson.fromJson(message, JsonObject.class);
            if (obj == null || !obj.has("action")) {
                throw new IllegalArgumentException("Invalid JSON or missing 'action'");
            }

            action = obj.get("action").getAsString();
            switch (action) {
                case "injectjar":
                    handleInjectJar(obj);
                    break;
                case "redefine":
                    handleRedefine(obj);
                    break;
                case "retransform":
                    handleRetransform(obj);
                    break;
                case "getclassbytes":
                    handleGetClassBytes(obj);
                    break;
                default:
                    code = 400;
                    logError("Unknown action: " + action);
            }

        } catch (Exception e) {
            code = 500;
            logError("Error handling action " + action + ": " + e.getMessage());
        }

        sendResponse(conn, code, code == 200 ? "done" : "error");
    }

    private void handleInjectJar(JsonObject obj) {
        String path = obj.get("path").getAsString();
        log("Injecting jar: " + path);
        if (!JuiceLoader.injectJar(path)) {
            throw new RuntimeException("Failed to inject jar: " + path);
        }
    }

    private void handleRedefine(JsonObject obj) throws IOException {
        String className = obj.get("name").getAsString();
        String path = obj.get("path").getAsString();
        log("Redefining class " + className + " from " + path);
        byte[] bytes = readFileToBytes(path);
        JuiceLoader.redefineClassByName(className, bytes, bytes.length);
    }

    private void handleRetransform(JsonObject obj) throws IOException {
        String className = obj.get("name").getAsString();
        String path = obj.get("path").getAsString();
        log("Retransforming class " + className);
        byte[] bytes = readFileToBytes(path);
        JuiceLoader.retransformClassByName(className, bytes, bytes.length);
    }

    private void handleGetClassBytes(JsonObject obj) throws IOException {
        String className = obj.get("name").getAsString();
        String path = obj.get("path").getAsString();
        log("Getting class bytes for " + className);
        byte[] bytes = JuiceLoader.getClassBytesByName(className);
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(bytes);
        }
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        log("Received binary message of length " + message.remaining());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        log("Client disconnected: " + conn.getRemoteSocketAddress() + " code=" + code + " reason=" + reason);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        logError("WebSocket error: " + ex.getMessage());
        if (conn != null && conn.isOpen()) {
            sendResponse(conn, 500, "error");
        }
    }

    private static byte[] readFileToBytes(String path) throws IOException {
        try (InputStream in = new FileInputStream(path);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            return out.toByteArray();
        }
    }

    private static void sendResponse(WebSocket conn, int code, String message) {
        if (conn != null && conn.isOpen()) {
            conn.send("{\"code\": " + code + ", \"message\": \"" + message + "\"}");
        }
    }

    private static void log(String msg) {
        System.out.println("[JuiceLoader IPC] " + msg);
    }

    private static void logError(String msg) {
        System.err.println("[JuiceLoader IPC] " + msg);
    }
}
