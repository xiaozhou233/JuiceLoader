package cn.xiaozhou233.juiceloader.entry;

import cn.xiaozhou233.bootstrap.*;
import cn.xiaozhou233.juiceloader.JuiceLoader;
import cn.xiaozhou233.juiceloader.JuiceLoaderNative;
import cn.xiaozhou233.juicetools.network.HttpServer;
import com.google.gson.Gson;

import java.io.IOException;

public class Entry {
    private static LoaderBridge provider;
    public static void start() {
        System.out.println("Hello, JuiceLoader!");
        provider = BootstrapBridge.getProvider();
        if (provider == null) {
            throw new RuntimeException("No provider registered in BootstrapBridge");
        }
        provider.log("Entry starting...");
        provider.startEntry();

        JuiceLoader.getLoaderNative().injectJar("C:\\Users\\xiaozhou\\.juiceloader\\JuiceTools-1.0-SNAPSHOT-all.jar");
        try {
            new HttpServer(8080);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}