package com.example.music_chenyujie.utils;

import com.tencent.mmkv.MMKV;

public class PrivacyUtils {
    private static final String KEY_PRIVACY_ACCEPTED = "privacy_accepted";
    private static final MMKV mmkv = MMKV.defaultMMKV();

    public static boolean isPrivacyAccepted() {
        return mmkv.getBoolean(KEY_PRIVACY_ACCEPTED, false);
    }

    public static void setPrivacyAccepted(boolean accepted) {
        mmkv.encode(KEY_PRIVACY_ACCEPTED, accepted);
    }
}
