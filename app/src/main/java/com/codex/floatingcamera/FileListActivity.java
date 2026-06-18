package com.codex.floatingcamera;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileListActivity extends AppCompatActivity {
    private LinearLayout listRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildView());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    private View buildView() {
        ScrollView scrollView = new ScrollView(this);
        listRoot = new LinearLayout(this);
        listRoot.setOrientation(LinearLayout.VERTICAL);
        listRoot.setPadding(dp(16), dp(16), dp(16), dp(16));
        scrollView.addView(listRoot);
        return scrollView;
    }

    private void refreshList() {
        listRoot.removeAllViews();

        TextView title = new TextView(this);
        title.setText("拍摄文件");
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        listRoot.addView(title, matchWrap());

        TextView note = new TextView(this);
        note.setText("这里的文件默认保存在隐藏目录，系统相册不会显示。点击“保存到系统”后才会复制到系统可见目录。");
        note.setTextSize(14);
        listRoot.addView(note, matchWrap());

        List<File> files = StorageHelper.captureFiles(this);
        if (files.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("暂无文件");
            empty.setGravity(Gravity.CENTER);
            empty.setTextSize(18);
            empty.setPadding(0, dp(24), 0, 0);
            listRoot.addView(empty, matchWrap());
            return;
        }

        for (File file : files) {
            addFileRow(file);
        }
    }

    private void addFileRow(File file) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, dp(12), 0, dp(12));
        listRoot.addView(row, matchWrap());

        TextView name = new TextView(this);
        name.setText(file.getName());
        name.setTextSize(17);
        row.addView(name, matchWrap());

        TextView meta = new TextView(this);
        meta.setText(fileType(file) + " / " + readableSize(file.length()) + " / " + readableDate(file.lastModified()));
        meta.setTextSize(13);
        row.addView(meta, matchWrap());

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(actions, matchWrap());

        Button view = button("查看");
        view.setOnClickListener(v -> openFile(file));
        actions.addView(view, equalButton());

        Button export = button("保存到系统");
        export.setOnClickListener(v -> exportFile(file));
        actions.addView(export, equalButton());

        Button delete = button("删除");
        delete.setOnClickListener(v -> {
            if (file.delete()) {
                Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
                refreshList();
            } else {
                Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
            }
        });
        actions.addView(delete, equalButton());
    }

    private void openFile(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".files", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mimeType(file));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "打开文件"));
        } catch (Exception exception) {
            Toast.makeText(this, "无法打开文件：" + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void exportFile(File file) {
        try {
            StorageHelper.exportToMediaStore(this, file);
            Toast.makeText(this, "已保存到系统目录", Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            Toast.makeText(this, "保存失败：" + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Button button(String text) {
        Button button = new Button(this);
        button.setText(text);
        return button;
    }

    private String mimeType(File file) {
        return StorageHelper.isPhoto(file) ? "image/*" : "video/*";
    }

    private String fileType(File file) {
        return StorageHelper.isPhoto(file) ? "照片" : "视频";
    }

    private String readableSize(long bytes) {
        if (bytes >= 1024 * 1024) {
            return String.format(Locale.US, "%.1f MB", bytes / 1024f / 1024f);
        }
        if (bytes >= 1024) {
            return String.format(Locale.US, "%.1f KB", bytes / 1024f);
        }
        return bytes + " B";
    }

    private String readableDate(long timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date(timestamp));
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private LinearLayout.LayoutParams equalButton() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        params.setMargins(dp(2), dp(4), dp(2), dp(4));
        return params;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
