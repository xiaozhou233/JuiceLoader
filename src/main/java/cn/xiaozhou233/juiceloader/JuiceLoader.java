package cn.xiaozhou233.juiceloader;

import cn.xiaozhou233.bootstrap.*;

public class JuiceLoader {
    private static final String homePath = System.getProperty("user.home");
    private static final String defDirectory = ".juiceloader";
    private static JuiceLoaderNative loaderNative;
    public static void init(String juiceLoaderLibPath, String entryJarPath) {
        if (juiceLoaderLibPath == null || juiceLoaderLibPath.isEmpty())
            juiceLoaderLibPath = String.format("%s/%s/libjuiceloader.dll", homePath, defDirectory);
        if (entryJarPath == null || entryJarPath.isEmpty())
            entryJarPath = String.format("%s/%s/Entry.jar", homePath, defDirectory);

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

            Class.forName("cn.xiaozhou233.juiceloader.entry.Entry").getMethod("start").invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static JuiceLoaderNative getLoaderNative() {
        return loaderNative;
    }

    public static void main(String[] args) {
        init(null, "D:\\Development\\JuiceLoader\\build\\libs\\Entry.jar");
    }
}