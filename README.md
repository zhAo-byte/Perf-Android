# Perf-Android
trace performance for Android Apps

1. adb push Perf.jar /data/local/tmp
2. adb shell app_process -Djava.class.path=/data/local/tmp/Perf.jar /data/local/tmp com.trace.perf.Main {pkgId} {traceType} {interval}
