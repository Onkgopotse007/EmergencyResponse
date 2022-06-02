package com.example.emergencyresponse;

public class Timer {
    private final long nanoSecondsPerMillisecond = 1000000;
    private long stopWatchStartTime = 0;
    private long stopWatchStopTime = 0;
    boolean stopWatchRunning = false;

    public void start() {
        this.stopWatchStartTime = System.nanoTime();
        this.stopWatchRunning = true;
    }


    public void stop() {
        this.stopWatchStopTime = System.nanoTime();
        this.stopWatchRunning = false;
    }


    public long getElapsedMilliseconds() {
        long elapsedTime;

        if (stopWatchRunning)
            elapsedTime = (System.nanoTime() - stopWatchStartTime);
        else
            elapsedTime = (stopWatchStopTime - stopWatchStartTime);

        return elapsedTime / nanoSecondsPerMillisecond;
    }

}
