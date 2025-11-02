package cn.xiaozhou233.juiceloader.entry;

import cn.xiaozhou233.bootstrap.*;
import cn.xiaozhou233.juiceloader.JuiceEventBus;
import cn.xiaozhou233.juiceloader.JuiceLoaderNative;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Entry {
    private static LoaderBridge provider;
    public static void start() {
        System.out.println("Hello, JuiceLoader!");
//        provider = BootstrapBridge.getProvider();
//        if (provider == null) {
//            throw new RuntimeException("No provider registered in BootstrapBridge");
//        }
//        provider.log("Entry starting...");
//        provider.startEntry();
//
//        try {
//            Class<?> loaderClass = Class.forName("cn.xiaozhou233.juiceloader.JuiceLoader");
//            System.out.println("Found JuiceLoader class, Bootstrap loaded!");
//        } catch (ClassNotFoundException e) {
//            System.out.println("WARN: JuiceLoader class Not Found, is BootstrapBridge (bootstrap-api.jar) loaded?");
//        }
//
//        try {
//            cn.xiaozhou233.juiceloader.JuiceLoaderNative.injectJar("D:\\Development\\JuiceTools\\build\\libs\\JuiceTools-1.0-SNAPSHOT-all.jar");
//            Class.forName("cn.xiaozhou233.juicetools.JuiceTools").getMethod("init").invoke(null);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

        System.out.println(JuiceEventBus.class.getClassLoader());
        System.out.println(JuiceEventBus.class.getProtectionDomain().getCodeSource());

//        System.out.println("Start Retransform TEST!");
//        try {
//            byte[] modified = readStream(Files.newInputStream(Paths.get("C:\\Users\\xiaozhou\\Desktop\\loop.class")));
//            JuiceLoaderNative.retransformClassByName("loop", modified, modified.length);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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