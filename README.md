<div align="center">

# 📷 Floating Camera

### 合规权限流程下的 Android 悬浮窗相机示例

**Android Overlay · CameraX · Foreground Service · Local Media Management · Bilingual Docs**

<p>
  <strong>语言</strong><br/>
  <strong>简体中文</strong> ·
  <a href="#english">English</a>
</p>

<p>
  <strong>导航</strong><br/>
  <a href="#项目简介">项目简介</a> ·
  <a href="#主要功能">主要功能</a> ·
  <a href="#合规限制">合规限制</a> ·
  <a href="#构建运行">构建运行</a> ·
  <a href="#使用方法">使用方法</a>
</p>

![Platform](https://img.shields.io/badge/Platform-Android-111827)
![Language](https://img.shields.io/badge/Language-Java-0F766E)
![Camera](https://img.shields.io/badge/Camera-CameraX%201.5.0-1D4ED8)
![SDK](https://img.shields.io/badge/compileSdk-35-7C3AED)
![Docs](https://img.shields.io/badge/Docs-ZH%20%2F%20EN-FF5722)
![Release](https://img.shields.io/github/v/release/V0idream/FloatingCamera?include_prereleases)
![Downloads](https://img.shields.io/github/downloads/V0idream/FloatingCamera/total)

</div>

---

<a id="中文"></a>

<a id="项目简介"></a>

## 📌 项目简介

**Floating Camera** 是一个原生 Android 悬浮窗相机示例项目，用于演示如何在合规权限流程下结合悬浮窗、CameraX、前台服务通知和本地文件管理能力。

项目适合用于学习 Android 悬浮窗交互、运行时权限、相机/录像调用、前台服务通知以及应用专用目录文件管理。它不是用于绕过系统相机、麦克风、通知或隐私提示的工具。

<a id="主要功能"></a>

## ✨ 主要功能

* 在 App 内设置悬浮窗透明度、大小、颜色和样式。
* 可选择本机图片作为悬浮窗图标。
* 支持拍照模式与录像模式。
* 开启悬浮窗后可退出设置页，在其他界面继续进行合规的悬浮窗交互。
* 点击悬浮窗拍照；录像模式下第一次点击开始录像，第二次点击停止录像。
* 可关闭“已拍照 / 开始录像 / 录像已保存”等完成提示。
* 拍摄文件默认保存到 App 专用目录，并创建 `.nomedia`，系统相册默认不可见。
* App 内提供“查看拍摄文件”入口，可查看、删除，或复制保存到系统相册 / 视频目录。
* 支持 Android 状态栏快捷设置磁贴，点击磁贴可在不打开软件页面的情况下开启或关闭悬浮窗。
* 低透明度下悬浮窗仍保持可拖动，透明度只影响视觉显示。

<a id="合规限制"></a>

## 🛡️ 合规限制

Android 不允许真正无提示地后台调用相机或麦克风。本工程使用合规实现：

* 首次必须打开设置页并授权相机、录音、通知和悬浮窗权限。
* 拍照或录像时会显示系统要求的前台服务通知。
* 拍照或录像时系统会显示相机 / 麦克风隐私指示。
* 不会打开可见的应用页面，但系统通知和隐私提示不能隐藏。
* 部分国产 ROM 会额外要求允许后台运行、悬浮窗后台显示、后台弹出界面或关闭省电限制。

> 本项目仅供技术交流与合法学习使用，禁止用于非法用途。使用者应自行遵守所在地区的法律法规，以及设备、平台和应用市场规则。

<a id="构建运行"></a>

## 🛠️ 构建运行

1. 用 Android Studio 打开本文件夹。
2. 等待 Gradle 同步完成。
3. 连接安卓手机，运行 `app`。
4. 在手机上授予权限后，点击“显示 / 更新悬浮窗”。

当前工程使用：

* Java
* Android Gradle Plugin 8.7.3
* compileSdk 35
* CameraX 1.5.0

<a id="使用方法"></a>

## 🚀 使用方法

1. 打开 App。
2. 点击“授予相机 / 录音 / 通知权限”。
3. 点击“开启悬浮窗权限”，并在系统页面允许。
4. 调整透明度、大小、颜色和样式，选择“拍照”或“录像”。
5. 可选择一张本机图片作为悬浮窗图标。
6. 点击“显示 / 更新悬浮窗”。
7. 返回桌面或打开其他软件。
8. 点击悬浮窗：
   * 拍照模式：点击一次保存一张照片。
   * 录像模式：点击一次开始录像，再点击一次停止并保存。
9. 回到 App 后点击“查看拍摄文件”，可查看、删除，或保存到系统可见目录。
10. 可在系统快捷设置编辑页添加“悬浮相机”磁贴；之后点击磁贴即可开启悬浮窗，再次点击即可关闭悬浮窗。

---

<a id="english"></a>

## English

**Floating Camera** is a native Android floating-window camera sample project. It demonstrates how to combine overlay windows, CameraX, foreground services, runtime permissions, and local media-file management within Android's normal permission and privacy model.

The project is intended for learning Android overlay interaction, runtime permissions, photo/video capture, foreground service notifications, and app-specific file storage. It is not designed to bypass Android camera, microphone, notification, or privacy indicators.

## Features

* Configure overlay transparency, size, color, and style inside the app.
* Use a local image as the floating-window icon.
* Choose photo mode or video mode.
* After enabling the overlay, leave the settings page and continue compliant floating-window interaction on other screens.
* Tap the floating window to take a photo; in video mode, tap once to start recording and tap again to stop.
* Optional completion prompts such as “photo captured”, “recording started”, and “video saved” can be disabled.
* Captured files are saved to the app-specific directory by default, with `.nomedia` created so they do not appear in the system gallery by default.
* The app provides a “view captured files” entry for viewing, deleting, or copying files to the system photo/video directories.
* Supports an Android Quick Settings tile that toggles the floating window without opening the app page.
* The floating window remains draggable at low opacity; transparency only affects the visual content.

## Important Limitations

Android does not allow truly silent background camera or microphone access. This project follows Android's required behavior:

* The settings page must be opened first to grant camera, audio recording, notification, and overlay permissions.
* Photo or video capture shows the foreground service notification required by the system.
* Camera and microphone privacy indicators remain visible during capture.
* The app page does not need to stay visibly open, but system notifications and privacy indicators cannot be hidden.
* Some Android ROMs may additionally require background running, background overlay display, background pop-up, or battery optimization permissions.

## Build

1. Open this folder in Android Studio.
2. Wait for Gradle sync to finish.
3. Connect an Android phone and run `app`.
4. Grant permissions on the phone, then tap “Show / Update Floating Window”.

This project currently uses Java, Android Gradle Plugin 8.7.3, compileSdk 35, and CameraX 1.5.0.

## Usage

1. Open the app.
2. Tap “Grant camera / audio / notification permissions”.
3. Tap “Enable overlay permission” and allow it in the system settings page.
4. Adjust transparency, size, color, and style, then choose photo or video mode.
5. Optionally select a local image as the floating-window icon.
6. Tap “Show / Update Floating Window”.
7. Return to the home screen or open another app.
8. Tap the floating window:
   * Photo mode: tap once to save one photo.
   * Video mode: tap once to start recording, then tap again to stop and save.
9. Return to the app and tap “View captured files” to view, delete, or save files to system-visible directories.
10. Add the “Floating Camera” tile from the system Quick Settings edit page; tap it to show the floating window, and tap again to hide it.
