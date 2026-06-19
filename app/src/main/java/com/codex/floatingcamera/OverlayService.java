package com.codex.floatingcamera;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.io.File;

public class OverlayService extends Service {
    static final String ACTION_START = "com.codex.floatingcamera.action.START_OVERLAY";
    static final String ACTION_REFRESH = "com.codex.floatingcamera.action.REFRESH_OVERLAY";

    private WindowManager windowManager;
    private View floatingButton;
    private WindowManager.LayoutParams layoutParams;
    private boolean usingCustomImage;
    private float downX;
    private float downY;
    private int startX;
    private int startY;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!Settings.canDrawOverlays(this)) {
            AppPrefs.setOverlayEnabled(this, false);
            Toast.makeText(this, "请先授予悬浮窗权限", Toast.LENGTH_SHORT).show();
            stopSelf();
            return START_NOT_STICKY;
        }
        showOrRefresh();
        if (floatingButton != null) {
            AppPrefs.setOverlayEnabled(this, true);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        removeButton();
        AppPrefs.setOverlayEnabled(this, false);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showOrRefresh() {
        int sizePx = dp(AppPrefs.sizeDp(this));
        int alpha = AppPrefs.alphaPercent(this);

        boolean shouldUseImage = hasUsableCustomImage();
        if (floatingButton != null && shouldUseImage != usingCustomImage) {
            removeButton();
        }

        if (floatingButton == null) {
            floatingButton = createFloatingButton(sizePx, shouldUseImage);
            usingCustomImage = shouldUseImage;

            int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                    ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    : WindowManager.LayoutParams.TYPE_PHONE;
            layoutParams = new WindowManager.LayoutParams(
                    sizePx,
                    sizePx,
                    type,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );
            layoutParams.gravity = Gravity.TOP | Gravity.START;
            layoutParams.x = dp(24);
            layoutParams.y = dp(160);
            layoutParams.alpha = alpha / 100f;
            try {
                windowManager.addView(floatingButton, layoutParams);
            } catch (RuntimeException exception) {
                AppPrefs.setOverlayEnabled(this, false);
                Toast.makeText(this, "无法显示悬浮窗，请重新授予悬浮窗权限", Toast.LENGTH_SHORT).show();
                floatingButton = null;
                stopSelf();
            }
        } else {
            layoutParams.width = sizePx;
            layoutParams.height = sizePx;
            layoutParams.alpha = alpha / 100f;
            updateFloatingButton(sizePx);
            try {
                windowManager.updateViewLayout(floatingButton, layoutParams);
            } catch (RuntimeException exception) {
                AppPrefs.setOverlayEnabled(this, false);
                Toast.makeText(this, "无法更新悬浮窗", Toast.LENGTH_SHORT).show();
                stopSelf();
            }
        }
    }

    private boolean handleTouch(android.view.View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getRawX();
                downY = event.getRawY();
                startX = layoutParams.x;
                startY = layoutParams.y;
                return true;
            case MotionEvent.ACTION_MOVE:
                layoutParams.x = startX + Math.round(event.getRawX() - downX);
                layoutParams.y = startY + Math.round(event.getRawY() - downY);
                windowManager.updateViewLayout(floatingButton, layoutParams);
                return true;
            case MotionEvent.ACTION_UP:
                float distance = Math.abs(event.getRawX() - downX) + Math.abs(event.getRawY() - downY);
                if (distance < dp(8)) {
                    triggerCapture();
                }
                return true;
            default:
                return false;
        }
    }

    private void triggerCapture() {
        Intent intent = new Intent(this, CaptureService.class);
        if (AppPrefs.MODE_VIDEO.equals(AppPrefs.mode(this))) {
            intent.setAction(CaptureService.ACTION_TOGGLE_VIDEO);
        } else {
            intent.setAction(CaptureService.ACTION_TAKE_PHOTO);
        }
        ContextCompat.startForegroundService(this, intent);
    }

    private void removeButton() {
        if (floatingButton != null) {
            try {
                windowManager.removeView(floatingButton);
            } catch (RuntimeException ignored) {
                // The view may already be detached after a system-side overlay denial.
            }
            floatingButton = null;
        }
    }

    private String cameraGlyph() {
        return AppPrefs.MODE_VIDEO.equals(AppPrefs.mode(this)) ? "REC" : "CAM";
    }

    private View createFloatingButton(int sizePx, boolean customImage) {
        View view;
        if (customImage) {
            ImageView imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageURI(Uri.fromFile(new File(AppPrefs.customImagePath(this))));
            imageView.setBackgroundColor(Color.TRANSPARENT);
            view = imageView;
        } else {
            TextView textView = new TextView(this);
            textView.setText(cameraGlyph());
            textView.setGravity(Gravity.CENTER);
            view = textView;
        }
        view.setOnTouchListener(this::handleTouch);
        floatingButton = view;
        updateFloatingButton(sizePx);
        return view;
    }

    private void updateFloatingButton(int sizePx) {
        if (floatingButton instanceof ImageView) {
            ImageView imageView = (ImageView) floatingButton;
            imageView.setImageURI(Uri.fromFile(new File(AppPrefs.customImagePath(this))));
            imageView.setBackgroundColor(Color.TRANSPARENT);
        } else if (floatingButton instanceof TextView) {
            TextView textView = (TextView) floatingButton;
            textView.setText(cameraGlyph());
            applyButtonStyle(textView, sizePx);
        }
    }

    private boolean hasUsableCustomImage() {
        String path = AppPrefs.customImagePath(this);
        return path != null && !path.isEmpty() && new File(path).isFile();
    }

    private void applyButtonStyle(TextView textView, int sizePx) {
        String style = AppPrefs.style(this);
        int color = AppPrefs.colorInt(this);
        int stroke = Math.max(dp(2), sizePx / 22);
        int corner = sizePx / 2;

        textView.setTextSize(AppPrefs.STYLE_TEXT_ONLY.equals(style) ? 18 : 20);
        textView.setPadding(0, 0, 0, 0);

        if (AppPrefs.STYLE_TEXT_ONLY.equals(style)) {
            textView.setTextColor(color);
            textView.setBackgroundColor(Color.TRANSPARENT);
            return;
        }

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        if (AppPrefs.STYLE_ROUNDED.equals(style)) {
            corner = Math.max(dp(8), sizePx / 5);
        }
        drawable.setCornerRadius(corner);

        if (AppPrefs.STYLE_OUTLINE.equals(style)) {
            drawable.setColor(Color.TRANSPARENT);
            drawable.setStroke(stroke, color);
            textView.setTextColor(color);
        } else {
            drawable.setColor(color);
            drawable.setStroke(stroke, 0xFFFFFFFF);
            textView.setTextColor(0xFFFFFFFF);
        }
        textView.setBackground(drawable);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
