package cn.xiaozhou233.juiceloader.entry;

import cn.xiaozhou233.bootstrap.*;

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
            Class<?> loaderClass = Class.forName("cn.xiaozhou233.juiceloader.JuiceLoader");
            System.out.println("Found JuiceLoader class, Bootstrap loaded!");
        } catch (ClassNotFoundException e) {
            System.out.println("WARN: JuiceLoader class Not Found, is BootstrapBridge (bootstrap-api.jar) loaded?");
        }
    }
}