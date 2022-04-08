package com.lixinxin.datacleanmanager.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.lixinxin.datacleanmanager.bean.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class AppUnit {

    /**
     * 获取用户安装的Apps
     *
     * @param context
     * @return
     */
    public static List<AppInfo> getAllUserInstalledApps(Context context) {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> installedPackages = pm.getInstalledPackages(0);
        List<AppInfo> appList = new ArrayList<>();
        for (PackageInfo info : installedPackages) {
            ApplicationInfo applicationInfo = info.applicationInfo;
            //系统应用
            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {

            } else {
                AppInfo appInfo = new AppInfo();
                appInfo.setIcon(applicationInfo.loadIcon(pm));
                appInfo.setName(pm.getApplicationLabel(applicationInfo).toString());
                appInfo.setSystem(false);
                appList.add(appInfo);
            }
        }
        return appList;
    }
}
