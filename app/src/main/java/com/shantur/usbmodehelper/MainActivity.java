package com.shantur.usbmodehelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class MainActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "SharedPrefs";
    public static final String FAST_CHARGE_KEY = "FastChargeStatus";
    public static final String POWER_OTG_DEVICES = "PowerOtgDevices";
    public static final String SET_ON_BOOT_KEY = "SetOnBoot";

    private CheckedTextView mFastChargeCheck;
    private CheckedTextView mSetOnBootCheck;
    private TextView mCurrentStatus;
    private CheckedTextView mDisablePowerOtgDevicesCheck;
    private String mFastChargeStatus = "";
    private String mDisablePowerOtgDevicesStatus = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFastChargeCheck = (CheckedTextView) findViewById(R.id.fast_charge_check);

        mFastChargeCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean newState = !mFastChargeCheck.isChecked();
                mFastChargeCheck.setChecked(newState);
                setFastChargeMode(newState);
                saveKey(MainActivity.this, FAST_CHARGE_KEY, newState);
            }
        });


        mDisablePowerOtgDevicesCheck = (CheckedTextView) findViewById(R.id.disable_power_otg_check);

        mDisablePowerOtgDevicesCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean newState = !mDisablePowerOtgDevicesCheck.isChecked();
                mDisablePowerOtgDevicesCheck.setChecked(newState);
                setPowerOtgDevicesMode(newState);
                saveKey(MainActivity.this, POWER_OTG_DEVICES, newState);
            }
        });


        mSetOnBootCheck = (CheckedTextView) findViewById(R.id.set_on_boot_check);

        mSetOnBootCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean newState = !mSetOnBootCheck.isChecked();
                mSetOnBootCheck.setChecked(newState);
                saveKey(MainActivity.this, SET_ON_BOOT_KEY, newState);
            }
        });

        mCurrentStatus = (TextView) findViewById(R.id.current_status);

        mCurrentStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentFastChargeValue();
            }
        });
    }

    public static void saveKey(Context context, String key, boolean enabled) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        preferences.edit().putBoolean(key, enabled).commit();
    }

    public static boolean getSavedKey(Context context, String key) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return preferences.getBoolean(key, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFastChargeStatus = "";
        mDisablePowerOtgDevicesStatus = "";
        getCurrentFastChargeValue();
        getCurrentPowerOtgDevicesMode();
        mSetOnBootCheck.setChecked(getSavedKey(this, SET_ON_BOOT_KEY));
    }

    private void refreshStatus() {
        mCurrentStatus.setText("Status : \n\n" + mFastChargeStatus + "\n\n" + mDisablePowerOtgDevicesStatus);
    }

    private void setPowerOtgDevicesMode(boolean enabled) {

        setPowerOtgDevicesMode(enabled, new Callback() {
            @Override
            public void onComplete(List<String> result) {
                getCurrentPowerOtgDevicesMode();
            }
        });
    }

    private void getCurrentPowerOtgDevicesMode() {
        getCurrentPowerOtgDevicesMode(new Callback() {
            @Override
            public void onComplete(List<String> result) {
                String powerOtg = "";

                if(result != null && result.size() > 0) {
                    powerOtg = result.get(0);
                }

                String status = "Unknown";

                if (powerOtg.equals("1")) {
                    status = "Enabled";
                    mDisablePowerOtgDevicesCheck.setChecked(true);
                } else if (powerOtg.equals("0")){
                    status = "Disabled";
                    mDisablePowerOtgDevicesCheck.setChecked(false);
                }

                mDisablePowerOtgDevicesStatus = "DisablePowerOtgDevices - " + status;
                refreshStatus();
            }
        });
    }

    public static void getCurrentPowerOtgDevicesMode(Callback callback) {
        runCommand("cat /sys/kernel/usbhost/usbhost_fixed_install_mode", callback);
    }


    public static void setPowerOtgDevicesMode(boolean enabled, Callback callback) {
        Log.d("USBModeHelper", "DisablePowerOtgDevices (FixedInstall) Mode : " + enabled);
        String value = enabled ? "1" : "0";

        runCommand("echo " + value + " > /sys/kernel/usbhost/usbhost_fixed_install_mode", callback);
    }

    private void setFastChargeMode(boolean enabled) {
        setFastChargeMode(enabled,  new Callback() {
            @Override
            public void onComplete(List<String> result) {
                getCurrentFastChargeValue();
            }
        });
    }

    public static void setFastChargeMode(boolean enabled, Callback callback) {

        Log.d("USBModeHelper", "SetFastCharge Mode : " + enabled);
        String value = enabled ? "1" : "0";

        runCommand("echo " + value + " > /sys/kernel/usbhost/usbhost_fastcharge_in_host_mode", callback);
    }

    private void getCurrentFastChargeValue() {
        getCurrentFastChargeValue(new Callback() {
            @Override
            public void onComplete(List<String> result) {
                String fastCharge = "";

                if(result != null && result.size() > 0) {
                    fastCharge = result.get(0);
                }

                String status = "Unknown";

                if (fastCharge.equals("1")) {
                    status = "Enabled";
                    mFastChargeCheck.setChecked(true);
                } else if (fastCharge.equals("0")){
                    status = "Disabled";
                    mFastChargeCheck.setChecked(false);
                }

                mFastChargeStatus = "FastChargeHostMode - " + status;
                refreshStatus();
            }
        });
    }

    public static void getCurrentFastChargeValue(Callback callback) {
        runCommand("cat /sys/kernel/usbhost/usbhost_fastcharge_in_host_mode", callback);
    }

    public interface Callback {
        void onComplete(List<String> result);
    }

    private static void runCommand(String command, final Callback callback) {
        AsyncTask<String, Void, List<String>> runTask = new AsyncTask<String, Void, List<String>>() {

            @Override
            protected List<String> doInBackground(String... params) {
                return Shell.SU.run(params[0]);
            }

            @Override
            protected void onPostExecute(List<String> strings) {
                callback.onComplete(strings);
            }
        };
        runTask.execute(command);
    }
}
