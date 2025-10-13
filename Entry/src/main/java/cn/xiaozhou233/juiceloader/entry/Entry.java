package cn.xiaozhou233.juiceloader.entry;

import cn.xiaozhou233.bootstrap.*;
import cn.xiaozhou233.juiceloader.JuiceLoader;
import cn.xiaozhou233.juiceloader.JuiceLoaderNative;
import com.google.gson.Gson;

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

        try {
            Class.forName("cn.xiaozhou233.juiceloader.JuiceLoader");
            System.out.println("Found!");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        //Gson gson = new Gson();
    }
}