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
        if (args == null || args.length < 5) {
            throw new IllegalArgumentException("Expected 5 arguments: entryJarPath, entryClass, entryMethod, injectionDir, libjuiceloaderPath");
        }

        String entryJarPath = args[0];
        String entryClass = args[1];
        String entryMethod = args[2];
        String injectionDir = args[3];
        String libPath = args[4];

        log("Entry Jar Path: " + entryJarPath);
        log("Entry Class: " + entryClass);
        log("Entry Method: " + entryMethod);
        log("Injection Dir: " + injectionDir);
        log("JuiceLoader Library Path: " + libPath);
        log("Juice Loader Bootstrap is initializing...");

        loadNativeLibrary(libPath);
        initJuiceLoader();
        injectEntryJar(entryJarPath);
        setupBootstrapBridge();
        invokeEntry(entryClass, entryMethod);
    }

    private static void log(String message) {
        System.out.println("[JuiceLoader] " + message);
    }

    private static void loadNativeLibrary(String path) {
        File libFile = new File(path);
        if (!libFile.exists() || !libFile.isFile()) {
            throw new RuntimeException("JuiceLoader library not found at: " + path);
        }
        System.load(libFile.getAbsolutePath());
        log("Native library loaded successfully.");
    }

    private static void initJuiceLoader() {
        if (!JuiceLoader.init()) {
            throw new RuntimeException("JuiceLoader initialization failed!");
        }
        log("JuiceLoader initialized successfully.");
    }

    private static void injectEntryJar(String jarPath) {
        if (!JuiceLoader.injectJar(jarPath)) {
            throw new RuntimeException("Failed to inject entry jar: " + jarPath);
        }
        log("Entry jar injected successfully.");
    }

    private static void setupBootstrapBridge() {
        BootstrapBridge.setProvider(new LoaderBridge() {
            @Override
            public void startEntry() {
                log("startEntry called!");
            }
            @Override
            public void log(String s) {
                System.out.println("[JuiceLoader] " + s);
            }
        });
        log("Bootstrap bridge set up.");
    }

    private static void invokeEntry(String className, String methodName) {
        try {
            Class<?> clazz = Class.forName(className);
            Method method = clazz.getMethod(methodName);
            method.invoke(null);
            log("Entry method invoked successfully.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke entry method " + className + "." + methodName, e);
        }
    }
}
