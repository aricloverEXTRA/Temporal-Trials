package com.aric3435.temporaltrials.client;

public class LoopStateClientState {

    public static boolean active = false;
    public static int day = 1;
    public static long remainingTicks = 0;
    public static boolean showIntro = false;

    private LoopStateClientState() {}

    public static void update(boolean newActive, int newDay, long newRemaining, boolean newShowIntro) {
        active = newActive;
        day = newDay;
        remainingTicks = newRemaining;
        showIntro = newShowIntro;
    }
}