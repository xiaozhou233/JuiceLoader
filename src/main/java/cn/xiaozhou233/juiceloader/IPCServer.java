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
        conn.send("{\"code\": 200, \"message\": \"Connected to JuiceLoader IPC Server\"}");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("[JuiceLoader IPC] Text: " + message);
        JsonObject obj = gson.fromJson(message, JsonObject.class);
        int code = 200;
        switch (obj.get("action").getAsString()) {
            case "injectjar": {
                System.out.println("[JuiceLoader IPC] Injecting jar " + obj.get("path").getAsString());
                JuiceLoader.injectJar(obj.get("path").getAsString());
                break;
            }

            // name, path
            case "redefine": {
                System.out.println("[JuiceLoader IPC] Redefining class " + obj.get("name").getAsString() + " from " + obj.get("path").getAsString());
                String className = obj.get("name").getAsString();
                String path = obj.get("path").getAsString();

                try {
                    byte[] bytes = readStream(new FileInputStream(path));
                    JuiceLoader.redefineClassByName(className, bytes, bytes.length);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
            }

            // name, path
            case "retransform": {
                System.out.println("[JuiceLoader IPC] Retransforming class " + obj.get("name").getAsString());
                String className = obj.get("name").getAsString();
                String path = obj.get("path").getAsString();

                try {
                    byte[] bytes = readStream(new FileInputStream(path));
                    JuiceLoader.retransformClassByName(className, bytes, bytes.length);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
            }

            // name, path
            case "getclassbytes": {
                System.out.println("[JuiceLoader IPC] Getting class bytes for " + obj.get("name").getAsString());
                String className = obj.get("name").getAsString();
                String path = obj.get("path").getAsString();

                byte[] bytes = JuiceLoader.getClassBytesByName(className);
                try {
                    new FileOutputStream(path).write(bytes);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            }


            default: {
                code = 400;
            }
        }

        if (code != 200) {
            conn.send("{\"code\": " + code + ", \"message\": \"error\"}");
        } else {
            conn.send("{\"code\": " + code + ", \"message\": \"done\"}");
        }
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
        conn.send("{\"code\": 500, \"message\": \"error\"}");
    }

    public static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inStream.read(buffer)) != -1)
            outStream.write(buffer, 0, len);
        outStream.close();
        return outStream.toByteArray();
    }
}
