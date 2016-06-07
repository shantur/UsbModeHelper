package com.shantur.usbmodehelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

public class BootCompleteReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        boolean setOnBoot = MainActivity.getSavedKey(context, MainActivity.SET_ON_BOOT_KEY);
        Log.d("USBModeHelper", "OnBoot Receiver Set On Boot : " + setOnBoot);

        if (setOnBoot) {
            MainActivity.setFastChargeMode(MainActivity.getSavedKey(context, MainActivity.FAST_CHARGE_KEY), new MainActivity.Callback() {
                @Override
                public void onComplete(List<String> result) {
                    Log.d("USBModeHelper", "OnBoot SetFastCharge Result : " + result.toString());
                    MainActivity.getCurrentFastChargeValue(new MainActivity.Callback() {
                        @Override
                        public void onComplete(List<String> result) {
                            Log.d("USBModeHelper", "OnBoot FastChargeStatus : " + result.toString());
                        }
                    });
                }
            });
        }
    }
}
