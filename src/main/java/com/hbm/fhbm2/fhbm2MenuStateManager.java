package com.hbm.fhbm2;

public class fhbm2MenuStateManager {
    private static boolean isCustomMenuEnabled = true;

    public static boolean isCustomMenuEnabled() {
        return isCustomMenuEnabled;
    }

    public static void setCustomMenuEnabled(boolean enabled) {
        isCustomMenuEnabled = enabled;
    }
}
