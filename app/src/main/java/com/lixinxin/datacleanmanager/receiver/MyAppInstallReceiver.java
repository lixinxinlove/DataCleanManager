package com.lixinxin.datacleanmanager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

public class MyAppInstallReceiver extends BroadcastReceiver {
    String TAG = "MyAppInstallReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
            Toast.makeText(context, "安装了---- " + getName(context, intent), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "安装了---- " + getName(context, intent));

        } else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
            Log.e(TAG, "卸载了--- " + intent.getDataString());
            Toast.makeText(context, "卸载了--- " + intent.getDataString().substring(8), Toast.LENGTH_SHORT).show();
        } else if (action.equals(Intent.ACTION_PACKAGE_REPLACED)) {
            Log.e(TAG, getName(context, intent) + " 更新成功---");
            Toast.makeText(context, getName(context, intent) + " 更新成功---", Toast.LENGTH_SHORT).show();
        }
    }

    private String getName(Context context, Intent intent) {
        return getName(context, intent.getDataString().substring(8));
    }

    private String getName(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        String name = packageName;
        try {
            name = pm.getApplicationLabel(
                    pm.getApplicationInfo(packageName,
                            PackageManager.GET_META_DATA)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return name;
    }
}
