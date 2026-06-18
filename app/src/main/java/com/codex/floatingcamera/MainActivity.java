package com.codex.floatingcamera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS = 41;
    private static final int REQUEST_CUSTOM_IMAGE = 42;

    private SeekBar alphaSeek;
    private SeekBar sizeSeek;
    private RadioButton photoButton;
    private RadioButton videoButton;
    private RadioButton circleStyleButton;
    private RadioButton roundedStyleButton;
    private RadioButton outlineStyleButton;
    private RadioButton textOnlyStyleButton;
    private RadioButton greenButton;
    private RadioButton redButton;
    private RadioButton blueButton;
    private RadioButton blackButton;
    private CheckBox successMessagesBox;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Notifications.ensureChannel(this);
        setContentView(buildView());
        loadPrefs();
        updateStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CUSTOM_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            saveCustomImage(data.getData());
        }
    }

    private View buildView() {
        int pad = dp(20);
        ScrollView scrollView = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        scrollView.addView(root);

        TextView title = new TextView(this);
        title.setText("Floating Camera");
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        root.addView(title, matchWrap());

        statusText = new TextView(this);
        statusText.setTextSize(15);
        statusText.setPadding(0, dp(16), 0, dp(16));
        root.addView(statusText, matchWrap());

        TextView alphaLabel = label("悬浮窗透明度");
        root.addView(alphaLabel, matchWrap());
        alphaSeek = new SeekBar(this);
        alphaSeek.setMax(100);
        root.addView(alphaSeek, matchWrap());

        TextView sizeLabel = label("悬浮窗大小");
        root.addView(sizeLabel, matchWrap());
        sizeSeek = new SeekBar(this);
        sizeSeek.setMax(96);
        root.addView(sizeSeek, matchWrap());

        RadioGroup group = new RadioGroup(this);
        group.setOrientation(RadioGroup.HORIZONTAL);
        group.setGravity(Gravity.CENTER);
        photoButton = new RadioButton(this);
        photoButton.setText("拍照");
        videoButton = new RadioButton(this);
        videoButton.setText("录像");
        group.addView(photoButton);
        group.addView(videoButton);
        root.addView(group, matchWrap());

        root.addView(label("悬浮窗样式"), matchWrap());
        RadioGroup styleGroup = new RadioGroup(this);
        styleGroup.setOrientation(RadioGroup.VERTICAL);
        circleStyleButton = radio("圆形");
        roundedStyleButton = radio("圆角方形");
        outlineStyleButton = radio("描边");
        textOnlyStyleButton = radio("仅文字");
        styleGroup.addView(circleStyleButton);
        styleGroup.addView(roundedStyleButton);
        styleGroup.addView(outlineStyleButton);
        styleGroup.addView(textOnlyStyleButton);
        root.addView(styleGroup, matchWrap());

        root.addView(label("悬浮窗颜色"), matchWrap());
        RadioGroup colorGroup = new RadioGroup(this);
        colorGroup.setOrientation(RadioGroup.HORIZONTAL);
        colorGroup.setGravity(Gravity.CENTER);
        greenButton = radio("绿");
        redButton = radio("红");
        blueButton = radio("蓝");
        blackButton = radio("黑");
        colorGroup.addView(greenButton);
        colorGroup.addView(redButton);
        colorGroup.addView(blueButton);
        colorGroup.addView(blackButton);
        root.addView(colorGroup, matchWrap());

        successMessagesBox = new CheckBox(this);
        successMessagesBox.setText("显示“已拍照/录像已保存”提示");
        root.addView(successMessagesBox, matchWrap());

        Button chooseImageButton = button("选择悬浮窗图片");
        chooseImageButton.setOnClickListener(v -> chooseCustomImage());
        root.addView(chooseImageButton, matchWrap());

        Button clearImageButton = button("清除悬浮窗图片");
        clearImageButton.setOnClickListener(v -> {
            AppPrefs.setCustomImagePath(this, "");
            refreshOverlay();
            updateStatus();
        });
        root.addView(clearImageButton, matchWrap());

        Button filesButton = button("查看拍摄文件");
        filesButton.setOnClickListener(v -> startActivity(new Intent(this, FileListActivity.class)));
        root.addView(filesButton, matchWrap());

        Button permissionButton = button("授予相机/录音/通知权限");
        permissionButton.setOnClickListener(v -> requestRuntimePermissions());
        root.addView(permissionButton, matchWrap());

        Button overlayPermissionButton = button("开启悬浮窗权限");
        overlayPermissionButton.setOnClickListener(v -> openOverlaySettings());
        root.addView(overlayPermissionButton, matchWrap());

        Button showButton = button("显示/更新悬浮窗");
        showButton.setOnClickListener(v -> {
            savePrefs();
            startService(new Intent(this, OverlayService.class).setAction(OverlayService.ACTION_START));
            updateStatus();
        });
        root.addView(showButton, matchWrap());

        Button hideButton = button("关闭悬浮窗");
        hideButton.setOnClickListener(v -> {
            stopService(new Intent(this, OverlayService.class));
            updateStatus();
        });
        root.addView(hideButton, matchWrap());

        SeekBar.OnSeekBarChangeListener saveOnChange = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    savePrefs();
                    startService(new Intent(MainActivity.this, OverlayService.class).setAction(OverlayService.ACTION_REFRESH));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };
        alphaSeek.setOnSeekBarChangeListener(saveOnChange);
        sizeSeek.setOnSeekBarChangeListener(saveOnChange);
        group.setOnCheckedChangeListener((g, checkedId) -> savePrefs());
        styleGroup.setOnCheckedChangeListener((g, checkedId) -> {
            savePrefs();
            refreshOverlay();
        });
        colorGroup.setOnCheckedChangeListener((g, checkedId) -> {
            savePrefs();
            refreshOverlay();
        });
        successMessagesBox.setOnCheckedChangeListener((buttonView, isChecked) -> savePrefs());

        return scrollView;
    }

    private void loadPrefs() {
        alphaSeek.setProgress(AppPrefs.alphaPercent(this));
        sizeSeek.setProgress(AppPrefs.sizeDp(this) - 48);
        if (AppPrefs.MODE_VIDEO.equals(AppPrefs.mode(this))) {
            videoButton.setChecked(true);
        } else {
            photoButton.setChecked(true);
        }
        String style = AppPrefs.style(this);
        if (AppPrefs.STYLE_ROUNDED.equals(style)) {
            roundedStyleButton.setChecked(true);
        } else if (AppPrefs.STYLE_OUTLINE.equals(style)) {
            outlineStyleButton.setChecked(true);
        } else if (AppPrefs.STYLE_TEXT_ONLY.equals(style)) {
            textOnlyStyleButton.setChecked(true);
        } else {
            circleStyleButton.setChecked(true);
        }

        String color = AppPrefs.color(this);
        if (AppPrefs.COLOR_RED.equals(color)) {
            redButton.setChecked(true);
        } else if (AppPrefs.COLOR_BLUE.equals(color)) {
            blueButton.setChecked(true);
        } else if (AppPrefs.COLOR_BLACK.equals(color)) {
            blackButton.setChecked(true);
        } else {
            greenButton.setChecked(true);
        }
        successMessagesBox.setChecked(AppPrefs.successMessages(this));
    }

    private void savePrefs() {
        String mode = videoButton.isChecked() ? AppPrefs.MODE_VIDEO : AppPrefs.MODE_PHOTO;
        String style = AppPrefs.STYLE_CIRCLE;
        if (roundedStyleButton.isChecked()) {
            style = AppPrefs.STYLE_ROUNDED;
        } else if (outlineStyleButton.isChecked()) {
            style = AppPrefs.STYLE_OUTLINE;
        } else if (textOnlyStyleButton.isChecked()) {
            style = AppPrefs.STYLE_TEXT_ONLY;
        }

        String color = AppPrefs.COLOR_GREEN;
        if (redButton.isChecked()) {
            color = AppPrefs.COLOR_RED;
        } else if (blueButton.isChecked()) {
            color = AppPrefs.COLOR_BLUE;
        } else if (blackButton.isChecked()) {
            color = AppPrefs.COLOR_BLACK;
        }

        AppPrefs.save(this, alphaSeek.getProgress(), sizeSeek.getProgress() + 48,
                mode, style, color, successMessagesBox.isChecked());
    }

    private void requestRuntimePermissions() {
        if (Build.VERSION.SDK_INT >= 33) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.POST_NOTIFICATIONS
            }, REQUEST_PERMISSIONS);
        } else if (Build.VERSION.SDK_INT < 29) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_PERMISSIONS);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            }, REQUEST_PERMISSIONS);
        }
    }

    private void openOverlaySettings() {
        Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName())
        );
        startActivity(intent);
    }

    private void updateStatus() {
        boolean camera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean audio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        boolean overlay = Settings.canDrawOverlays(this);
        statusText.setText("状态：相机 " + yesNo(camera)
                + " / 录音 " + yesNo(audio)
                + " / 悬浮窗 " + yesNo(overlay)
                + " / 自定义图片 " + yesNo(AppPrefs.hasCustomImage(this))
                + "\n退出本页后，点击悬浮按钮即可拍照或开始/停止录像。");
    }

    private TextView label(String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(16);
        label.setPadding(0, dp(12), 0, 0);
        return label;
    }

    private Button button(String text) {
        Button button = new Button(this);
        button.setText(text);
        return button;
    }

    private RadioButton radio(String text) {
        RadioButton button = new RadioButton(this);
        button.setText(text);
        return button;
    }

    private void refreshOverlay() {
        startService(new Intent(MainActivity.this, OverlayService.class).setAction(OverlayService.ACTION_REFRESH));
    }

    private void chooseCustomImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CUSTOM_IMAGE);
    }

    private void saveCustomImage(Uri uri) {
        try {
            File target = StorageHelper.customIconFile(this);
            try (InputStream input = getContentResolver().openInputStream(uri);
                 OutputStream output = new FileOutputStream(target)) {
                if (input == null) {
                    throw new IllegalStateException("Cannot open selected image");
                }
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
            }
            AppPrefs.setCustomImagePath(this, target.getAbsolutePath());
            refreshOverlay();
            updateStatus();
        } catch (Exception exception) {
            statusText.setText("自定义图片保存失败：" + exception.getMessage());
        }
    }

    private LinearLayout.LayoutParams matchWrap() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(4), 0, dp(4));
        return params;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private String yesNo(boolean value) {
        return value ? "已允许" : "未允许";
    }
}
