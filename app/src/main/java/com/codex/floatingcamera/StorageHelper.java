package com.codex.floatingcamera;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

final class StorageHelper {
    private StorageHelper() {
    }

    static File hiddenDir(Context context) throws IOException {
        File dir = new File(context.getExternalFilesDir(null), "FloatingCamera");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Cannot create capture folder");
        }
        File noMedia = new File(dir, ".nomedia");
        if (!noMedia.exists()) {
            new FileOutputStream(noMedia).close();
        }
        return dir;
    }

    static File customIconFile(Context context) throws IOException {
        File dir = new File(context.getFilesDir(), "overlay");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Cannot create overlay folder");
        }
        return new File(dir, "custom_overlay_image");
    }

    static File newPhotoFile(Context context) throws IOException {
        return new File(hiddenDir(context), "FC_" + timestamp() + ".jpg");
    }

    static File newVideoFile(Context context) throws IOException {
        return new File(hiddenDir(context), "FC_" + timestamp() + ".mp4");
    }

    static List<File> captureFiles(Context context) {
        try {
            File[] files = hiddenDir(context).listFiles(file ->
                    file.isFile() && !file.getName().equals(".nomedia")
                            && (isPhoto(file) || isVideo(file)));
            if (files == null) {
                return new ArrayList<>();
            }
            Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
            return new ArrayList<>(Arrays.asList(files));
        } catch (IOException exception) {
            return new ArrayList<>();
        }
    }

    static boolean isPhoto(File file) {
        String name = file.getName().toLowerCase(Locale.US);
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
    }

    static boolean isVideo(File file) {
        String name = file.getName().toLowerCase(Locale.US);
        return name.endsWith(".mp4") || name.endsWith(".m4v");
    }

    static Uri exportToMediaStore(Context context, File file) throws IOException {
        boolean photo = isPhoto(file);
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, file.getName());
        values.put(MediaStore.MediaColumns.MIME_TYPE, photo ? "image/jpeg" : "video/mp4");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, photo
                    ? "Pictures/FloatingCamera"
                    : "Movies/FloatingCamera");
            values.put(MediaStore.MediaColumns.IS_PENDING, 1);
        }

        Uri collection = photo
                ? MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                : MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Uri uri = resolver.insert(collection, values);
        if (uri == null) {
            throw new IOException("Cannot create media item");
        }

        try (FileInputStream input = new FileInputStream(file);
             OutputStream output = resolver.openOutputStream(uri)) {
            if (output == null) {
                throw new IOException("Cannot open media output");
            }
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear();
            values.put(MediaStore.MediaColumns.IS_PENDING, 0);
            resolver.update(uri, values, null, null);
        }
        return uri;
    }

    private static String timestamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
    }
}
