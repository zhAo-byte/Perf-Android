package com.trace.perf.action;

public class MemAction implements Action {
    private boolean trace = false;
    private String result = "memory: ";
    @Override
    public void exec(String line) {
        if (trace) {
            String[] info = line.replaceAll("\\s+", " ").split(":");
            if (info.length > 1) {
                result = result + info[0].trim() + ":" + info[1].trim().split(" ")[0] + ", ";
            }
        }

        if (line.contains("App Summary")) {
            trace = true;
        } else if (line.contains("TOTAL")) {
            if (trace) {
                System.out.println(result);
            }
            result = "memory: ";
            trace = false;
        }
    }
}
