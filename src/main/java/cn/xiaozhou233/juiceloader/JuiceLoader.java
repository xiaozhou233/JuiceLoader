package cn.xiaozhou233.juiceloader;

import cn.xiaozhou233.bootstrap.*;

public class JuiceLoader {
    public static void init(String entryJarPath, String entryClass, String entryMethod) {
        try {

            // Init Native Library
            boolean result = JuiceLoaderNative.init();
            if (!result) {
                throw new RuntimeException("JuiceLoader init failed!");
            }

            // Inject Entry Jar
            result = JuiceLoaderNative.injectJar(entryJarPath);
            if (!result) {
                throw new RuntimeException("JuiceLoader injectJar failed!");
            }

            BootstrapBridge.setProvider(new LoaderBridge() {
                @Override
                public void startEntry() {
                    System.out.println("[JuiceLoader] startEntry called!");
                }
                @Override
                public void log(String s) {
                    System.out.println("[JuiceLoader] " + s);
                }
            });

            Class.forName(entryClass).getMethod(entryMethod).invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}