package com.trace.perf.action;

public class NetAction implements Action {
    private long recv = 0;
    private long send = 0;
    private long timestamp = 0;
    @Override
    public void exec(String line) {
        if (line.contains("wlan0")) {
            String[] info = line.trim().split(":");
            if (info.length == 2) {
                String[] datas = info[1].trim().replaceAll( "\\s+", " " ).split(" ");
                if (datas.length == 16) {

                    long curRecv = Long.parseLong(datas[0]);
                    long curSend = Long.parseLong(datas[8]);
                    long curTime = System.currentTimeMillis();
                    if (send > 0 && recv > 0) {
                        long recvT = curRecv - recv;
                        long sendT = curSend - send;
                        System.out.printf("network: down:%d %.2f up:%d %.2f %n",
                                recvT, (float)recvT / (curTime - timestamp), sendT, (float)sendT / (curTime - timestamp));
                    }
                    send = curSend;
                    recv = curRecv;
                    timestamp = curTime;
                }
            }
        }
    }
}
