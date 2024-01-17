package com.trace.perf.action;

public class FpsAction implements Action {

    private String pkgName = "";
    private String surfaceName = "";
    private int mode = 1;
    private long lastT = 0;

    public FpsAction(String pkgName) {
        this.pkgName = pkgName;
    }
    @Override
    public void exec(String line) throws Exception {
        switch (mode) {
            case 1:
                parseSurface(line);
                break;
            case 2:
                parseData(line);
                break;
        }
    }

    public String getSurfaceName() {
        return surfaceName;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    private void parseSurface(String line) throws Exception {
        if (line.contains(pkgName) && line.contains("Output Layer")) {
            String[] tmp = line.trim().split("\\(");
            surfaceName = tmp[tmp.length - 1].replace(")", "");
            throw new Exception("");
        }
    }

    private void parseData(String line) {
        String[] info = line.trim().split("\t");
        if (info.length == 3) {
            long t = Long.parseLong(info[1]);
            if (t < 9000000000000000000L) {
                if (t > lastT) {
                    if (lastT != 0) {
                        System.out.printf("frameT: %.2f%n", (float)(t - lastT) / 1000000);
                    }
                    lastT = t;
                }
            }
        }
    }
}
