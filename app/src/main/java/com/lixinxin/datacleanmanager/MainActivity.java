package com.lixinxin.datacleanmanager;

import android.Manifest;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.lixinxin.datacleanmanager.bean.LibStringItem;
import com.lixinxin.datacleanmanager.bean.StatefulComponent;
import com.lixinxin.datacleanmanager.databinding.ActivityMainBinding;
import com.lixinxin.datacleanmanager.util.DataCleanManager;
import com.lixinxin.datacleanmanager.util.DiskTask;
import com.lixinxin.datacleanmanager.util.PackageUtils;
import com.permissionx.guolindev.PermissionX;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";

    private ActivityMainBinding binding;

    private int REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.permissionBtn.setOnClickListener(view -> permission());
        binding.permissionRBtn.setOnClickListener(view -> permissionR());

        binding.deleteBtn.setOnClickListener(view -> deleteDir());

        binding.allPathBtn.setOnClickListener(view -> getExternalAllCacheDir());

        binding.killAppBtn.setOnClickListener(view -> {
            killRunningAppProcess(this);
            getTopApp(this);
        });

        binding.appLibBtn.setOnClickListener(view -> {
            appLib();
        });

        binding.appServiceBtn.setOnClickListener(view -> {
            //appServiceList();

            try {
                // getAppInfo();

                getAppInfoService();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        });

        binding.unInstallBtn.setOnClickListener(view -> {
            unInstall("com.tap.cleanerd");
        });
    }


    private void appLib() {
        PackageManager pm = getPackageManager();
        List<PackageInfo> installedPackages = pm.getInstalledPackages(0);


        for (PackageInfo packageInfo : installedPackages) {
//packageInfo.
            //  packageInfo.packageName


            List<LibStringItem> list = PackageUtils.INSTANCE.getSourceLibs(packageInfo, "assets/", "/assets");
            if (list.size() > 0) {
                for (LibStringItem item : list) {
                    Log.e(TAG, item.toString());
                }
            }

        }
    }

    private void appServiceList() {
        PackageManager pm = getPackageManager();
        List<PackageInfo> installedPackages = pm.getInstalledPackages(0);

        for (PackageInfo packageInfo : installedPackages) {
            Log.e(TAG, packageInfo.packageName);
            ServiceInfo[] serviceInfos = packageInfo.services;
            ActivityInfo[] activities = packageInfo.activities;

            if (serviceInfos != null && serviceInfos.length > 0) {
                for (ServiceInfo item : serviceInfos) {
                    Log.e(TAG, item.name);
                }
            }

            List<StatefulComponent> list = PackageUtils.INSTANCE.getComponentList(packageInfo.packageName, packageInfo.services, false);
            if (list.size() > 0) {
                for (StatefulComponent item : list) {
                    Log.e(TAG, item.toString());
                }
            }

        }
    }


    private void permission() {
        PermissionX.init(this).permissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.KILL_BACKGROUND_PROCESSES,
                Manifest.permission.PACKAGE_USAGE_STATS
        )
                .onExplainRequestReason((scope, deniedList) -> scope.showRequestReasonDialog(deniedList, "????????????????????????????????????", "OK", "Cancel"))
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        Toast.makeText(this, "??????????????????", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "?????????????????????: $deniedList", Toast.LENGTH_LONG).show();
                        deleteDir();
                    }
                });

    }


    private void permissionR() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // ????????????????????????
            if (Environment.isExternalStorageManager()) {
                //writeFile();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            }
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                Toast.makeText(this, "??????????????????", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "????????????????????????", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void getExternalAllCacheDir() {
        DiskTask diskTask = new DiskTask(filePathList -> {

            if (filePathList.size() > 0) {
//                for (String path : filePathList) {
//                    Log.e("MainActivity", path);
//                }
            }
        });
        diskTask.execute("");
    }


    private void deleteDir() {
        String path = Environment.getExternalStorageDirectory().getPath() + "/tap";
        DataCleanManager.deleteFolderFile(path, true);
    }

    private void deleteExternalStorageFile() {
        //??????sd?????????  temp  log cache  download thum ????????????
        //DataCleanManager.deleteFolderFile(path, true);

        //TODO
    }


    private void killRunningAppProcess(Context context) {

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infoList = am.getRunningAppProcesses();
        List<ActivityManager.RunningServiceInfo> serviceInfos = am.getRunningServices(100);

        long beforeMem = getAvailMemory(context);
        Log.d(TAG, "-----------before memory info : " + beforeMem);
        int count = 0;
        if (infoList != null) {
            for (int i = 0; i < infoList.size(); ++i) {
                ActivityManager.RunningAppProcessInfo appProcessInfo = infoList.get(i);
                Log.d(TAG, "process name : " + appProcessInfo.processName);
                //importance ????????????????????????  ????????????????????????????????????????????????
                Log.d(TAG, "importance : " + appProcessInfo.importance);

                // ??????????????????RunningAppProcessInfo.IMPORTANCE_SERVICE?????????????????????????????????????????????
                // ??????????????????RunningAppProcessInfo.IMPORTANCE_VISIBLE????????????????????????????????????????????????????????????
                if (appProcessInfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                    String[] pkgList = appProcessInfo.pkgList;
                    for (int j = 0; j < pkgList.length; ++j) {//pkgList ?????????????????????????????????
                        Log.d(TAG, "It will be killed, package name : " + pkgList[j]);
                        am.killBackgroundProcesses(pkgList[j]);
                        count++;
                    }
                }
            }
        }

        long afterMem = getAvailMemory(context);
        Log.d(TAG, "----------- after memory info : " + afterMem);

    }


    //????????????????????????
    private long getAvailMemory(Context context) {
        // ??????android????????????????????????
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        //mi.availMem; ???????????????????????????
        //return Formatter.formatFileSize(context, mi.availMem);// ?????????????????????????????????
        Log.d("", "????????????---->>>" + mi.availMem / (1024 * 1024));
        return mi.availMem / (1024 * 1024);
    }


    private void getTopApp(Context context) {

        UsageStatsManager m = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (m != null) {
            long now = System.currentTimeMillis();
            //??????60????????????????????????
            List<UsageStats> stats = m.queryUsageStats(UsageStatsManager.INTERVAL_BEST, now - 60 * 1000, now);
            Log.e(TAG, "Running app number in last 60 seconds : " + stats.size());

            String topActivity = "";

            //???????????????????????????app?????????????????????app
            if ((stats != null) && (!stats.isEmpty())) {
                int j = 0;
                for (int i = 0; i < stats.size(); i++) {
                    if (stats.get(i).getLastTimeUsed() > stats.get(j).getLastTimeUsed()) {
                        j = i;
                    }
                }
                topActivity = stats.get(j).getPackageName();
            }
            Log.e(TAG, "top running app is : " + topActivity);
        }
    }


    /**
     * ??????????????????
     *
     * @throws PackageManager.NameNotFoundException
     */
    private void getAppInfo() throws PackageManager.NameNotFoundException {

        // ??????PackageManager
//        PackageManager pm = getPackageManager();
//        // ??????PackageInfo???PackageManager.GET_UNINSTALLED_PACKAGES???PackageManager.GET_ACTIVITIES???????????????????????????????????????
//        List<PackageInfo> pkgs=pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
//        // ?????????????????????????????????ActivityInfo,actInfo??????null,
//        //ActivityInfo[] actInfo = pkgs.get(0).activities;
//        //??????????????????ActivityInfo????????????????????????Activity???name
//        ActivityInfo[] actInfo = getPackageManager().getPackageInfo(pkgs.get(0).packageName, PackageManager.GET_ACTIVITIES).activities;
//
        // ??????PackageManager
        PackageManager pm2 = getPackageManager();
        // ??????PackageInfo???PackageManager.GET_UNINSTALLED_PACKAGES???PackageManager.GET_ACTIVITIES???????????????????????????????????????
        //TODO
        //List<PackageInfo> pkgs2 = pm2.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        List<PackageInfo> pkgs2 = pm2.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES);
        // ?????????????????????????????????ActivityInfo,actInfo??????null,
        //ActivityInfo[] actInfo = pkgs.get(0).activities;

        //??????????????????ActivityInfo????????????????????????Activity???name
        // ActivityInfo[] actInfo2 = getPackageManager().getPackageInfo(pkgs2.get(0).packageName, PackageManager.GET_ACTIVITIES).activities;


        for (PackageInfo info : pkgs2) {

            ApplicationInfo applicationInfo = info.applicationInfo;
            //????????????
            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {

            } else {
                ActivityInfo[] actInfo2 = getPackageManager().getPackageInfo(info.packageName, PackageManager.GET_ACTIVITIES).activities;
                Log.e(TAG, info.packageName + "--------------------");
                if (actInfo2 != null) {
                    for (ActivityInfo activityInfo : actInfo2) {
                        Log.e(TAG, activityInfo.name);
                    }
                }
            }
        }

    }


    private void getAppInfoService() throws PackageManager.NameNotFoundException {

        // ??????PackageManager
        PackageManager pm2 = getPackageManager();
        // ??????PackageInfo???PackageManager.GET_UNINSTALLED_PACKAGES???PackageManager.GET_ACTIVITIES???????????????????????????????????????
        //TODO
        //List<PackageInfo> pkgs2 = pm2.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        List<PackageInfo> pkgs2 = pm2.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES);


        for (PackageInfo info : pkgs2) {
            ApplicationInfo applicationInfo = info.applicationInfo;
            //????????????
            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {

            } else {
                ServiceInfo[] serInfo2 = getPackageManager().getPackageInfo(info.packageName, PackageManager.GET_SERVICES).services;
                Log.e(TAG, info.packageName + "--------------------");
                if (serInfo2 != null) {
                    for (ServiceInfo serInfo : serInfo2) {
                        Log.e(TAG, serInfo.name);
                    }
                }
            }
        }
    }


    private void unInstall(String pageName) {
        Uri uri = Uri.fromParts("package", pageName, null);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        startActivity(intent);
    }
}