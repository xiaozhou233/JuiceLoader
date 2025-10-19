package cn.xiaozhou233.juiceagent.injector;

import java.io.File;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Injector {
    private static boolean isLoadedLibrary = false;
    public static void main(String[] args) {
        if (!isLoadedLibrary) {
            System.load(ClassLoader.getSystemResource("./libinjector.dll").getPath());
            isLoadedLibrary = true;
        }
        Scanner scanner = new Scanner(System.in);

        // InjectorNative injectorNative = new InjectorNative();
        executeJps();

        System.out.println("Input pid: ");
        int pid = scanner.nextInt();

        File libAgentPath = new File("./libagent.dll");
        if (!libAgentPath.exists()) {
            System.out.println("libagent.dll not found, input libagent.dll path: ");
            String libAgentPathStr = scanner.next();
            libAgentPath = new File(libAgentPathStr);
            if (!libAgentPath.exists()) {
                throw new RuntimeException("libagent.dll not found");
            }
        }

        System.out.println("Input config dir (input !c to use libagent dir): ");
        String configDir = scanner.next();
        if (configDir.equals("!c")) {
            //injectorNative.inject(pid, libAgentPath.getAbsolutePath(), libAgentPath.getParent());
            inject(pid, libAgentPath.getAbsolutePath(), libAgentPath.getParent());
        } else {
            //injectorNative.inject(pid, libAgentPath.getAbsolutePath(), configDir);
            inject(pid, libAgentPath.getAbsolutePath(), configDir);
        }
    }

    public static void inject(int pid, String libAgentPath, String configDir) {
        try {
            if (!isLoadedLibrary) {
                System.load(Injector.class.getClassLoader().getResource("./libinjector.dll").getPath());
                isLoadedLibrary = true;
            }
            InjectorNative injectorNative = new InjectorNative();

            injectorNative.inject(pid, libAgentPath, configDir);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void inject(int pid, String libAgentPath) {
        try {
            if (!isLoadedLibrary) {
                System.load(Injector.class.getClassLoader().getResource("./libinjector.dll").getPath());
                isLoadedLibrary = true;
            }
            InjectorNative injectorNative = new InjectorNative();

            injectorNative.inject(pid, libAgentPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void executeJps() {
        System.out.println("======== Jps ========");
        try {
            ProcessBuilder pb = new ProcessBuilder("jps", "-l");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "GBK"))) { // Windows 控制台一般为GBK编码
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("======== Jps ========");
    }
}