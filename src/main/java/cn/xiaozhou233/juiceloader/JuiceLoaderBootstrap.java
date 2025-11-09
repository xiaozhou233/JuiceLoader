package cn.xiaozhou233.juiceloader;

import cn.xiaozhou233.juiceloader.bootstrap.BootstrapBridge;
import cn.xiaozhou233.juiceloader.bootstrap.LoaderBridge;

import java.io.File;

public class JuiceLoaderBootstrap {
    public static void init(String entryJarPath, String entryClass, String entryMethod,  String injectionDir, String libjuiceloaderpath) {
        try {

            File libfile = new File(libjuiceloaderpath);
            System.load(libfile.getAbsolutePath());

            // Init Native Library
            boolean result = JuiceLoader.init();
            if (!result) {
                throw new RuntimeException("JuiceLoader init failed!");
            }

            // Inject Entry Jar
            result = JuiceLoader.injectJar(entryJarPath);
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