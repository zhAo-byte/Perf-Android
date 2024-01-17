package com.trace.perf;

public class Perf {
    public static void main(String[] args) {
        if (args.length == 3) {
            try {
                String pkgName = args[0];
                String[] traceKeys = args[1].split(",");
                int interval = Integer.parseInt(args[2]);
                Cat cat = new Cat(pkgName, traceKeys, interval);
                cat.start();
            } catch (Exception ignored) {
                System.out.println("please use com.trace.perf.Main [pkgName] [traceKey1,traceKey2,...] [interval]");
            }
        } else  {
            System.out.println("please use com.trace.perf.Main [pkgName] [traceKey1,traceKey2,...] [interval]");
        }
    }
}
