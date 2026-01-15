package cn.xiaozhou233.juiceloader;

import cn.xiaozhou233.juiceloader.bootstrap.BootstrapBridge;
import cn.xiaozhou233.juiceloader.bootstrap.LoaderBridge;

import java.io.File;
import java.lang.reflect.Method;

public class JuiceLoaderBootstrap {

    /*
     * Invoke by native
     * See: JuiceAgent -> libagent.cpp
     */
    @SuppressWarnings("unused")
    public static void init(String[] args) {

        // -------- argument check --------
        if (args == null || args.length < 5) {
            throw new IllegalArgumentException(
                    "Expected 5 arguments: entryJarPath, entryClass, entryMethod, injectionDir, libjuiceloaderPath"
            );
        }

        String entryJarPath = args[0];
        String entryClass   = args[1];
        String entryMethod  = args[2];
        String injectionDir = args[3];
        String libPath      = args[4];

        System.out.println("[JuiceLoader] =================");
        System.out.println("[JuiceLoader] Entry Jar Path: " + entryJarPath);
        System.out.println("[JuiceLoader] Entry Class: " + entryClass);
        System.out.println("[JuiceLoader] Entry Method: " + entryMethod);
        System.out.println("[JuiceLoader] Injection Dir: " + injectionDir);
        System.out.println("[JuiceLoader] JuiceLoader Library Path: " + libPath);
        System.out.println("[JuiceLoader] =================");
        System.out.println("[JuiceLoader] Juice Loader Bootstrap is initializing...");

        // -------- load native library --------
        File libFile = new File(libPath);
        if (!libFile.exists() || !libFile.isFile()) {
            throw new RuntimeException("JuiceLoader library not found at: " + libPath);
        }
        System.load(libFile.getAbsolutePath());
        System.out.println("[JuiceLoader] Native library loaded successfully.");

        // -------- init JuiceLoader --------
        if (!JuiceLoader.init()) {
            throw new RuntimeException("JuiceLoader initialization failed!");
        }
        System.out.println("[JuiceLoader] JuiceLoader initialized successfully.");

        // -------- inject entry jar --------
        if (!JuiceLoader.AddToSystemClassLoaderSearch(entryJarPath)) {
            throw new RuntimeException("Failed to inject entry jar: " + entryJarPath);
        }
        System.out.println("[JuiceLoader] Entry jar injected successfully.");

        // -------- setup bootstrap bridge --------
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
        System.out.println("[JuiceLoader] Bootstrap bridge set up.");

        // -------- invoke entry --------
        try {
            Class<?> clazz = Class.forName(entryClass);
            Method method = clazz.getMethod(entryMethod);
            method.invoke(null);
            System.out.println("[JuiceLoader] Entry method invoked successfully.");
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to invoke entry method " + entryClass + "." + entryMethod,
                    e
            );
        }
    }
}
