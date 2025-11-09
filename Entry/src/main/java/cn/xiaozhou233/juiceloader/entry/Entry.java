package cn.xiaozhou233.juiceloader.entry;

import cn.xiaozhou233.juiceloader.JuiceLoader;
import cn.xiaozhou233.juiceloader.bootstrap.BootstrapBridge;
import cn.xiaozhou233.juiceloader.bootstrap.LoaderBridge;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

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
            Class<?> loaderClass = Class.forName("cn.xiaozhou233.juiceloader.JuiceLoaderBootstrap");
            System.out.println("Found JuiceLoader class, Bootstrap loaded!");
        } catch (ClassNotFoundException e) {
            System.out.println("WARN: JuiceLoader class Not Found, is BootstrapBridge (bootstrap-api.jar) loaded?");
        }

        System.out.println("Entry load.");
        try {
            System.out.println("Find class avo");
            Class<?> target = JuiceLoader.getClassByName("avo");

            System.out.println("Get original bytes");
            byte[] originalBytes = JuiceLoader.getClassBytes(target);

            System.out.println("Use Javassist");
            ClassPool pool = ClassPool.getDefault();
            CtClass ctClass = pool.makeClass(new java.io.ByteArrayInputStream(originalBytes));

            // Important: specify method parameter type (float)
            System.out.println("Get method");
            CtMethod method = ctClass.getDeclaredMethod("a", new CtClass[]{CtClass.floatType});

            System.out.println("Insert print at beginning");
            // Insert your call
            method.insertAfter("{ f().a(\"JuiceOWO!\", 10, 10, 16747520); }");

            System.out.println("Retransform");
            byte[] result = ctClass.toBytecode();
            JuiceLoader.retransformClass(target, result, result.length);

        } catch (Exception e) {
            e.printStackTrace();
        }
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