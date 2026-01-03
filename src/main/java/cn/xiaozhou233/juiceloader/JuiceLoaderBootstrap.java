package cn.xiaozhou233.juiceloader;

import cn.xiaozhou233.juiceloader.bootstrap.BootstrapBridge;
import cn.xiaozhou233.juiceloader.bootstrap.LoaderBridge;

import java.io.File;

public class JuiceLoaderBootstrap {
    public static void init(String[] args) {
        try {
            String entryJarPath = args[0];
            String entryClass = args[1];
            String entryMethod = args[2];
            String injectionDir = args[3];
            String libjuiceloaderpath = args[4];

            System.out.printf("Entry Jar Path: %s\n", entryJarPath);
            System.out.printf("Entry Class: %s\n", entryClass);
            System.out.printf("Entry Method: %s\n", entryMethod);
            System.out.printf("Injection Dir: %s\n", injectionDir);
            System.out.printf("JuiceLoader Library Path: %s\n", libjuiceloaderpath);
            System.out.println("Juice Loader Bootstrap is initializing...");

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