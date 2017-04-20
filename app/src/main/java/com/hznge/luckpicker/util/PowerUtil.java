package com.hznge.luckpicker.util;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;

/**
 * Created by hznge on 17-4-20.
 */

public class PowerUtil {
    private PowerManager.WakeLock mWakeLock;
    private KeyguardManager.KeyguardLock mKeyguardLock;

    public PowerUtil(Context context) {
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK |
            PowerManager.ACQUIRE_CAUSES_WAKEUP, "HongbaoWakeLock");
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        mKeyguardLock = km.newKeyguardLock("HongbaoKeyguardLock");
    }

    private void acquire() {
        mWakeLock.acquire(1800000);
        mKeyguardLock.disableKeyguard();
    }

    private void release() {
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
            mKeyguardLock.reenableKeyguard();
        }
    }

    public void handleWakeLock(boolean isWake) {
        if (isWake) {
            this.acquire();
        } else {
            this.release();
        }
    }
}
