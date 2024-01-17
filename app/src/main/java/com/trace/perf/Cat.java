package com.trace.perf;

import com.trace.perf.action.Action;
import com.trace.perf.action.FpsAction;
import com.trace.perf.action.MemAction;
import com.trace.perf.action.NetAction;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Cat {
    private final String[] traceKeys;
    private final String pkgName;
    private final int interval;
    private final ThreadPoolExecutor executor;

    public Cat(String pkgName, String[] traceKey, int interval) {
        this.pkgName = pkgName;
        this.traceKeys = traceKey;
        this.interval = interval;

        int poolSize = this.traceKeys.length;
        long keepAliveTile = 60000;
        TimeUnit unit = TimeUnit.MILLISECONDS;
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
        this.executor = new ThreadPoolExecutor(poolSize, poolSize, keepAliveTile, unit, workQueue);
    }

    public void start() {
        for (String key : traceKeys) {
            switch (key) {
                case "memory":
                    traceMemory();
                    break;
                case "cpu":
                    traceCPU();
                    break;
                case "fps":
                    traceFps();
                    break;
                case "temperature":
                    traceTemperature();
                    break;
                case "battery":
                    traceBattery();
                    break;
                case "network":
                    traceNetwork();
                    break;
            }
        }
        Util.sleep(System.currentTimeMillis(), 86400000);
    }

    private void traceCPU(){
        int coreCount = Runtime.getRuntime().availableProcessors();
        executor.execute(() -> {
            Util.exec(String.format("top -b -d %.2f -s 1 -o %%CPU,ARGS", (float) this.interval / 1000), line -> {
                if (line.contains(pkgName)) {
                    String[] info = line.trim().split(" ");
                    if (info.length == 2 && info[1].trim().equals(pkgName)) {
                        System.out.printf("cpu: %.2f\n", Float.parseFloat(info[0].trim()) / coreCount);
                    }
                }
            }, errorStr -> {
                int interval = Math.max(this.interval/ 1000, 1);
                Util.exec(String.format("top -b -d %d -s 1 -o %CPU,ARGS", interval), line -> {
                    if (line.contains(pkgName)) {
                        String[] info = line.trim().split(" ");
                        if (info.length == 2 && info[1].trim().equals(pkgName)) {
                            System.out.printf("cpu: %.2f\n", Float.parseFloat(info[0].trim()) / coreCount);
                        }
                    }
                });
            });
        });
    }

    private void traceMemory() {
        executor.execute(() -> {
            MemAction action = new MemAction();
            while (true) {
                long startT = System.currentTimeMillis();
                Util.exec(String.format("dumpsys meminfo %s", pkgName), action);
                Util.sleep(startT, this.interval);
            }
        });
    }

    private void traceFps() {
        executor.execute(() -> {
            Util.exec("dumpsys SurfaceFlinger --latency-clear", line -> {});
            FpsAction fpsAction = new FpsAction(pkgName);
            while (true) {
                long startT = System.currentTimeMillis();
                fpsAction.setMode(1);
                Util.exec("dumpsys SurfaceFlinger", fpsAction);

                fpsAction.setMode(2);
                String[] cmdData = {"dumpsys", "SurfaceFlinger", "--latency", fpsAction.getSurfaceName()};
                Util.exec(cmdData, fpsAction);
                Util.sleep(startT, this.interval);
            }
        });
    }

    private void traceTemperature() {
        executor.execute(() -> {
            Action action = line -> {
                if (line.contains("temperature")) {
                    System.out.println(line.trim());
                }
            };

            while (true) {
                long startT = System.currentTimeMillis();
                Util.exec("dumpsys battery", action);
                Util.sleep(startT, this.interval);
            }
        });
    }

    private void traceBattery() {
        executor.execute(() -> {
            final String[] uuids = new String[1];
            Util.exec("top -n 1", line -> {
                if (line.contains(pkgName)) {
                    uuids[0] = line.trim().split(" ")[1].replace("_", "");
                }
            });
            Util.exec("dumpsys batterystats --enable full-wake-history", line -> {});
            Util.exec("dumpsys batterystats --reset", line -> {});

            Action action = line -> {
                if (line.contains(uuids[0])) {
                    String[] infos = line.trim().split(" ");
                    if (infos[0].equals("Uid")) {
                        System.out.println("battery: " + infos[2].trim());
                    }
                }
            };

            while (true) {
                long startT = System.currentTimeMillis();
                Util.exec("dumpsys batterystats", action);
                Util.sleep(startT, this.interval);
            }
        });
    }

    private void traceNetwork() {
        executor.execute(() -> {
            final String[] uuids = new String[1];
            Util.exec("top -n 1", line -> {
                if (line.contains(pkgName)) {
                    uuids[0] = line.trim().split(" ")[0];
                }
            });
            NetAction action = new NetAction();
            while (true) {
                long startT = System.currentTimeMillis();
                Util.exec(String.format("cat /proc/%s/net/dev", uuids[0]), action);
                Util.sleep(startT, this.interval);
            }
        });
    }
}

