package com.codex.floatingcamera;

import android.content.Context;
import android.content.SharedPreferences;

final class AppPrefs {
    static final String MODE_PHOTO = "photo";
    static final String MODE_VIDEO = "video";
    static final String STYLE_CIRCLE = "circle";
    static final String STYLE_ROUNDED = "rounded";
    static final String STYLE_OUTLINE = "outline";
    static final String STYLE_TEXT_ONLY = "text_only";
    static final String COLOR_GREEN = "green";
    static final String COLOR_RED = "red";
    static final String COLOR_BLUE = "blue";
    static final String COLOR_BLACK = "black";

    private static final String PREFS = "floating_camera_prefs";
    private static final String KEY_ALPHA = "alpha_percent";
    private static final String KEY_SIZE_DP = "size_dp";
    private static final String KEY_MODE = "mode";
    private static final String KEY_STYLE = "style";
    private static final String KEY_COLOR = "color";
    private static final String KEY_SUCCESS_MESSAGES = "success_messages";
    private static final String KEY_CUSTOM_IMAGE_PATH = "custom_image_path";

    private AppPrefs() {
    }

    static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static int alphaPercent(Context context) {
        return prefs(context).getInt(KEY_ALPHA, 82);
    }

    static int sizeDp(Context context) {
        return prefs(context).getInt(KEY_SIZE_DP, 72);
    }

    static String mode(Context context) {
        return prefs(context).getString(KEY_MODE, MODE_PHOTO);
    }

    static String style(Context context) {
        return prefs(context).getString(KEY_STYLE, STYLE_CIRCLE);
    }

    static String color(Context context) {
        return prefs(context).getString(KEY_COLOR, COLOR_GREEN);
    }

    static boolean successMessages(Context context) {
        return prefs(context).getBoolean(KEY_SUCCESS_MESSAGES, true);
    }

    static String customImagePath(Context context) {
        return prefs(context).getString(KEY_CUSTOM_IMAGE_PATH, "");
    }

    static boolean hasCustomImage(Context context) {
        String path = customImagePath(context);
        return path != null && !path.isEmpty();
    }

    static void setCustomImagePath(Context context, String path) {
        prefs(context).edit().putString(KEY_CUSTOM_IMAGE_PATH, path == null ? "" : path).apply();
    }

    static void save(Context context, int alphaPercent, int sizeDp, String mode,
                     String style, String color, boolean successMessages) {
        prefs(context).edit()
                .putInt(KEY_ALPHA, alphaPercent)
                .putInt(KEY_SIZE_DP, sizeDp)
                .putString(KEY_MODE, mode)
                .putString(KEY_STYLE, style)
                .putString(KEY_COLOR, color)
                .putBoolean(KEY_SUCCESS_MESSAGES, successMessages)
                .apply();
    }

    static int colorInt(Context context) {
        String color = color(context);
        if (COLOR_RED.equals(color)) {
            return 0xFFB3261E;
        }
        if (COLOR_BLUE.equals(color)) {
            return 0xFF1D4ED8;
        }
        if (COLOR_BLACK.equals(color)) {
            return 0xFF202124;
        }
        return 0xFF2F6F5E;
    }
}
