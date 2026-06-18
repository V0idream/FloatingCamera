package com.codex.floatingcamera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.PendingRecording;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleService;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;

public class CaptureService extends LifecycleService {
    static final String ACTION_TAKE_PHOTO = "com.codex.floatingcamera.action.TAKE_PHOTO";
    static final String ACTION_TOGGLE_VIDEO = "com.codex.floatingcamera.action.TOGGLE_VIDEO";

    private ProcessCameraProvider cameraProvider;
    private Recording activeRecording;

    @Override
    public void onCreate() {
        super.onCreate();
        Notifications.ensureChannel(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        String action = intent == null ? "" : intent.getAction();
        if (!hasPermission(Manifest.permission.CAMERA)) {
            toast("缺少相机权限");
            stopSelf();
            return START_NOT_STICKY;
        }

        if (ACTION_TAKE_PHOTO.equals(action)) {
            startCaptureForeground(false);
            takePhoto();
        } else if (ACTION_TOGGLE_VIDEO.equals(action)) {
            if (activeRecording == null && !hasPermission(Manifest.permission.RECORD_AUDIO)) {
                toast("缺少录音权限");
                stopSelf();
                return START_NOT_STICKY;
            }
            startCaptureForeground(true);
            toggleVideo();
        } else {
            stopSelf();
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return null;
    }

    @Override
    public void onDestroy() {
        if (activeRecording != null) {
            activeRecording.stop();
            activeRecording = null;
        }
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        super.onDestroy();
    }

    private void takePhoto() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                cameraProvider = future.get();
                cameraProvider.unbindAll();
                ImageCapture imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();
                cameraProvider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        imageCapture
                );

                File outputFile = StorageHelper.newPhotoFile(this);
                ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(
                        outputFile
                ).build();

                imageCapture.takePicture(options, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                        successToast("已拍照");
                        cleanupAfterPhoto();
                    }

                    @Override
                    public void onError(ImageCaptureException exception) {
                        toast("拍照失败：" + exception.getMessage());
                        cleanupAfterPhoto();
                    }
                });
            } catch (Exception exception) {
                toast("启动相机失败：" + exception.getMessage());
                cleanupAfterPhoto();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void toggleVideo() {
        if (activeRecording != null) {
            activeRecording.stop();
            activeRecording = null;
            return;
        }

        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                cameraProvider = future.get();
                cameraProvider.unbindAll();

                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.HD))
                        .build();
                VideoCapture<Recorder> videoCapture = VideoCapture.withOutput(recorder);
                cameraProvider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        videoCapture
                );

                File outputFile = StorageHelper.newVideoFile(this);
                FileOutputOptions outputOptions = new FileOutputOptions.Builder(outputFile).build();

                PendingRecording pendingRecording = recorder.prepareRecording(this, outputOptions)
                        .withAudioEnabled();
                activeRecording = pendingRecording.start(ContextCompat.getMainExecutor(this), event -> {
                    if (event instanceof VideoRecordEvent.Start) {
                        successToast("开始录像");
                    } else if (event instanceof VideoRecordEvent.Finalize) {
                        VideoRecordEvent.Finalize finalizeEvent = (VideoRecordEvent.Finalize) event;
                        if (finalizeEvent.hasError()) {
                            toast("录像失败：" + finalizeEvent.getError());
                        } else {
                            successToast("录像已保存");
                        }
                        activeRecording = null;
                        cleanupAfterVideo();
                    }
                });
            } catch (Exception exception) {
                toast("启动录像失败：" + exception.getMessage());
                cleanupAfterVideo();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void startCaptureForeground(boolean video) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            int type = ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA;
            if (video) {
                type |= ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE;
            }
            startForeground(
                    Notifications.CAPTURE_NOTIFICATION_ID,
                    Notifications.build(this, video ? "Floating Camera 录像中" : "Floating Camera 拍照中",
                            video ? "再次点击悬浮按钮停止录像" : "正在保存照片", true),
                    type
            );
        } else {
            startForeground(
                    Notifications.CAPTURE_NOTIFICATION_ID,
                    Notifications.build(this, video ? "Floating Camera 录像中" : "Floating Camera 拍照中",
                            video ? "再次点击悬浮按钮停止录像" : "正在保存照片", true)
            );
        }
    }

    private void cleanupAfterPhoto() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        stopSelf();
    }

    private void cleanupAfterVideo() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        stopSelf();
    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void successToast(String message) {
        if (AppPrefs.successMessages(this)) {
            toast(message);
        }
    }

}
