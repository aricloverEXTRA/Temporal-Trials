package com.aric3435.temporaltrials.client;

public final class ClientLoopState {
    private ClientLoopState() {}

    private static volatile boolean active = false;
    private static volatile int day = 1;
    private static volatile long remainingTicks = 0L;
    private static volatile boolean showIntro = false;

    public static void update(boolean newActive, int newDay, long newRemaining, boolean newShowIntro) {
        active = newActive;
        day = newDay;
        remainingTicks = newRemaining;
        showIntro = newShowIntro;
    }

    public static boolean isActive() { return active; }
    public static int getDay() { return day; }
    public static long getRemainingTicks() { return remainingTicks; }
    public static boolean shouldShowIntro() { return showIntro; }
}