package cn.xiaozhou233.juiceloader;

import cn.xiaozhou233.bootstrap.*;

public class JuiceLoader {
    private static JuiceLoaderNative loaderNative;
    public static void init(String juiceLoaderLibPath, String entryJarPath, String entryClass, String entryMethod) {
        try {
            System.load(juiceLoaderLibPath);
            loaderNative = new JuiceLoaderNative();

            // Init Native Library
            boolean result = loaderNative.init();
            if (!result) {
                throw new RuntimeException("JuiceLoader init failed!");
            }

            // Inject Entry Jar
            result = loaderNative.injectJar(entryJarPath);
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

    public static JuiceLoaderNative getLoaderNative() {
        return loaderNative;
    }

}