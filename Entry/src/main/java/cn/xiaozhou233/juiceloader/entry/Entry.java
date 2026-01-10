package cn.xiaozhou233.juiceloader.entry;

import cn.xiaozhou233.juiceloader.JuiceLoader;
import cn.xiaozhou233.juiceloader.bootstrap.BootstrapBridge;
import cn.xiaozhou233.juiceloader.bootstrap.LoaderBridge;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class Entry {
    private static LoaderBridge provider;
    public static void start() {
        provider = BootstrapBridge.getProvider();
        if (provider == null) {
            throw new RuntimeException("No provider registered in BootstrapBridge");
        }
        provider.log("Entry starting...");
        provider.startEntry();

        try {
            Class<?> loaderClass = Class.forName("cn.xiaozhou233.juiceloader.JuiceLoaderBootstrap");
            System.out.println("Found JuiceLoader class, Bootstrap loaded!");
        } catch (ClassNotFoundException e) {
            System.out.println("WARN: JuiceLoader class Not Found, is BootstrapBridge (bootstrap-api.jar) loaded?");
        }

        System.out.println("Entry load.");
        System.out.println("Loaded class: " + JuiceLoader.getLoadedClasses().length);
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