package com.trace.perf;

import com.trace.perf.action.Action;
import com.trace.perf.action.ErrorAction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Util {
    public static void exec(String cmd, Action action) {
        String[] cmds = cmd.split(" ");
        exec(cmds, action, System.out::println);
    }

    public static void exec(String[] cmds, Action action) {
        exec(cmds, action, System.out::println);
    }

    public static void exec(String cmd, Action action, ErrorAction errAction) {
        String[] cmds = cmd.split(" ");
        exec(cmds, action, errAction);
    }

    public static void exec(String[] cmd, Action action, ErrorAction errAction) {
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            // 处理标准输出
            StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), action);
            Thread outputThread = new Thread(outputGobbler);
            outputThread.start();
            // 处理标准错误
            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), errAction);
            Thread errorThread = new Thread(errorGobbler);
            errorThread.start();

            process.waitFor();
            errorThread.join();
            outputThread.join();

        } catch (Exception ignored) {}
    }

    public static void sleep(long startT, int millis) {
        try {
            long currentT = System.currentTimeMillis();
            int t = millis - (int)(currentT - startT);
            if (t > 0) {
                Thread.sleep(t);
            }
        } catch (InterruptedException ignored) {}
    }

    static class StreamGobbler implements Runnable {
        private final InputStream inputStream;
        private final Action action;

        StreamGobbler(InputStream inputStream, Action action) {
            this.inputStream = inputStream;
            this.action = action;
        }

        public void run() {
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        action.exec(line);
                    } catch (Exception ignored) {
                        break;
                    }
                }
            } catch (IOException ignored) {}
        }
    }
}
