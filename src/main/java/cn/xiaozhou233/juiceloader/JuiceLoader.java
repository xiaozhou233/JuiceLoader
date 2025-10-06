package cn.xiaozhou233.juiceloader;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JuiceLoader {
    private static JuiceLoaderNative loaderNative;
    private static String loaderPath;

    public static void init(String path){
        System.out.println("JuiceLoader init!");
        loaderPath = path;
        System.load(path + "/libjuiceloader.dll");
        loaderNative = new JuiceLoaderNative();
        loaderNative.init();

        File entry = new File(path + "/entry.jar");
        if(!entry.exists()){
            System.out.println("entry.jar not found in " + path);
            String selectedPath = selectFile("entry.jar");
            if (selectedPath == null) {
                System.out.println("No entry.jar selected, exiting.");
                return;
            }
            entry = new File(selectedPath);
            if(!entry.exists()) {
                System.out.println("Selected file does not exist, exiting.");
                return;
            }
        }

        loaderNative.injectJar(entry.getAbsolutePath());

        HashMap<String, String> info = parseInfo(entry);
        if(!info.containsKey("mainclass") || !info.containsKey("method")){
            System.out.println("mainclass.txt missing required keys, exiting.");
            return;
        }

        try (URLClassLoader classLoader = new URLClassLoader(
                new URL[]{entry.toURI().toURL()},
                JuiceLoader.class.getClassLoader())) {

            Class<?> mainClass = Class.forName(info.get("mainclass"), true, classLoader);
            Method mainMethod = mainClass.getMethod(info.get("method"));
            mainMethod.setAccessible(true);
            mainMethod.invoke(null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String selectFile(String filenameTips) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select the " + filenameTips + " file");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    private static HashMap<String, String> parseInfo(File jar) {
        HashMap<String, String> info = new HashMap<>();

        try (JarFile jarFile = new JarFile(jar)) {
            JarEntry classinfo = jarFile.getJarEntry("mainclass.txt");
            InputStream is;

            if (classinfo != null) {
                is = jarFile.getInputStream(classinfo);
            } else {
                System.out.println("mainclass.txt not found in jar.");
                String selectedPath = selectFile("mainclass.txt");
                if (selectedPath == null) {
                    System.out.println("No mainclass.txt selected, returning empty info.");
                    return info;
                }
                is = new FileInputStream(selectedPath);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                reader.lines().forEach(line -> {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) return;
                    String[] split = line.split("=", 2);
                    if (split.length == 2) {
                        info.put(split[0].trim(), split[1].trim());
                        System.out.println(split[0].trim() + " : " + split[1].trim());
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return info;
    }

    public static JuiceLoaderNative getLoaderNative(){
        return loaderNative;
    }

    public static String getLoaderPath() {
        return loaderPath;
    }
}
