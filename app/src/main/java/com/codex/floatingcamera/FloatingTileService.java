package com.codex.floatingcamera;

import android.content.Intent;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

public class FloatingTileService extends TileService {
    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();
        if (!Settings.canDrawOverlays(this)) {
            AppPrefs.setOverlayEnabled(this, false);
            Toast.makeText(this, "请先在软件内开启悬浮窗权限", Toast.LENGTH_SHORT).show();
            updateTile();
            return;
        }

        if (AppPrefs.overlayEnabled(this)) {
            stopService(new Intent(this, OverlayService.class));
            AppPrefs.setOverlayEnabled(this, false);
        } else {
            startService(new Intent(this, OverlayService.class).setAction(OverlayService.ACTION_START));
            AppPrefs.setOverlayEnabled(this, true);
        }
        updateTile();
    }

    private void updateTile() {
        Tile tile = getQsTile();
        if (tile == null) {
            return;
        }
        boolean enabled = Settings.canDrawOverlays(this) && AppPrefs.overlayEnabled(this);
        tile.setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.setLabel(enabled ? "关闭悬浮窗" : "开启悬浮窗");
        tile.updateTile();
    }
}
