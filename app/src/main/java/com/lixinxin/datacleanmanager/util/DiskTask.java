package com.lixinxin.datacleanmanager.util;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.telephony.mbms.FileInfo;
import android.util.Log;

import com.lixinxin.datacleanmanager.DiskTaskListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * 添加获取目录下的缓存文件
 */
public class DiskTask extends AsyncTask<String, String, List<String>> {

    private final String TAG = "DiskCacheTask";

    private DiskTaskListener listener;


    public DiskTask(DiskTaskListener listener) {
        this.listener = listener;
    }

    @Override
    protected List<String> doInBackground(String... params) {
        File file = new File(Environment.getExternalStorageDirectory().getPath());
        Log.e(TAG, Environment.getExternalStorageDirectory().getAbsolutePath());
        //  List<String> fileInfoList = findCache(file);
        //  List<String> fileInfoList = scanEmptyDirs();

        // logsLogCacheCachesTmfTmpTlog(file);

        //hideDir(file);

        // bigFile(file);

        // uninstallAppDir();


       // findApk(file);

        dirSize();

        List<String> fileInfoList = new ArrayList<>();
        return fileInfoList;
    }

    @Override
    protected void onPostExecute(List<String> fileInfoList) {
        super.onPostExecute(fileInfoList);
        if (listener != null) {
            listener.onFinished(fileInfoList);
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }

    public List<String> findCache(File file) {
        List<String> fileInfoList = new ArrayList<>();
        File[] files = file.listFiles();
        if (files == null) {
            return fileInfoList;
        }
        for (File tempFile : files) {
            if (tempFile.isDirectory()) {
                String path = tempFile.getPath().toLowerCase();
//                if (path.contains("temp") || path.contains("cache") || path.contains("log") || path.contains("download") || path.contains("thum")) {
//                    fileInfoList.add(tempFile.getAbsolutePath());
//                }
                if (path.contains("cache")) {
                    fileInfoList.add(tempFile.getAbsolutePath());
                }
            }
        }
        return fileInfoList;
    }


    private List<String> emptyDirPath = new ArrayList<>();

    /**
     * 扫描外部存储空文件夹
     *
     * @return
     */
    public List<String> scanEmptyDirs() {
        File rootDirPath = new File(Environment.getExternalStorageDirectory().getPath());
        emptyDirPath.clear();
        findEmptyDirs(rootDirPath);
        return emptyDirPath;
    }


    private void findEmptyDirs(File f) {
        // 首先通过f.listFiles()该方法获取到该文件夹下面所有文件
        File[] files = f.listFiles();
        if (files == null || files.length == 0) {
            emptyDirPath.add(f.getAbsolutePath());
        }
        for (File file : files) {
            // 判断文件夹下面是否有文件
            if (file.isDirectory()) {
                findEmptyDirs(file);
            }
        }
    }


    /**
     * 闲杂文件清理
     *
     * @param dir
     * @return
     */
    public List<FileInfoEntity> logsLogCacheCachesTmfTmpTlog(File dir) {

        List<FileInfoEntity> fileInfoList = new ArrayList<>();
        File[] files = dir.listFiles();

        for (File tempFile : files) {
            if (tempFile.isFile()) {
                String path = tempFile.getPath().toLowerCase();
                if (path.endsWith(".logs") ||
                        path.endsWith(".log") ||
                        path.endsWith(".cache") ||
                        path.endsWith(".caches") ||
                        path.endsWith(".tmf") ||
                        path.endsWith(".tlog")) {

                    FileInfoEntity fileInfo = new FileInfoEntity();
                    fileInfo.setName(tempFile.getName());
                    fileInfo.setFileSize(tempFile.getTotalSpace());
                    fileInfo.setFullPath(tempFile.getAbsolutePath());
                    fileInfoList.add(fileInfo);


                    Log.e(TAG, tempFile.getName() + "--" + tempFile.getTotalSpace());

                }
            } else {
                logsLogCacheCachesTmfTmpTlog(tempFile);
            }
        }
        return fileInfoList;
    }


    /**
     * 根目录整理
     *
     * @param dir
     * @return
     */
    public List<String> hideDir(File dir) {

        List<String> fileInfoList = new ArrayList<>();
        File[] files = dir.listFiles();
        for (File tempFile : files) {
            if (tempFile.getName().toLowerCase().startsWith(".")) {
                fileInfoList.add(tempFile.getAbsolutePath());
                Log.e(TAG, tempFile.getName());
                DataCleanManager.deleteFolderFile(tempFile.getAbsolutePath(), true);
            }
        }
        return fileInfoList;
    }


    /**
     * 大文件清理  获取大于20MB的文件
     *
     * @param dir
     * @return
     */
    public List<FileInfoEntity> bigFile(File dir) {
        List<FileInfoEntity> fileInfoList = new ArrayList<>();
        File[] files = dir.listFiles();

        for (File tempFile : files) {
            if (tempFile.isFile()) {
                if (tempFile.length() > 20L * 1024 * 1024) {
                    FileInfoEntity fileInfo = new FileInfoEntity();
                    fileInfo.setName(tempFile.getName());
                    fileInfo.setFileSize(tempFile.getTotalSpace());
                    fileInfo.setFullPath(tempFile.getAbsolutePath());
                    fileInfoList.add(fileInfo);
                    Log.e(TAG, tempFile.getName() + "-----"
                            + DataCleanManager.getFormatSize2(tempFile.length()));
                }
            } else {
                bigFile(tempFile);
            }
        }
        return fileInfoList;
    }


    // 卸载残留
    // 快速扫描残留在你android/data

    public List<String> uninstallAppDir() {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data");

        Log.e(TAG, dir.getAbsolutePath());

        if (!dir.exists()) {
            Log.e(TAG, "文件不存在");
            return null;
        }

        List<String> fileInfoList = new ArrayList<>();
        File[] files = dir.listFiles();

        for (File tempFile : files) {
            if (tempFile.isFile()) {
                Log.e(TAG, tempFile.getName());
            } else {
                // bigFile(tempFile);
                Log.e(TAG, tempFile.getName());
            }
        }
        return fileInfoList;
    }


    private List<String> apks = new ArrayList<>();

    /**
     * APK 清理
     *
     * @param dir
     * @return
     */
    public void findApk(File dir) {
        if (!dir.exists()) {
            Log.e(TAG, "文件不存在");
            return;
        }
        File[] files = dir.listFiles();
        for (File tempFile : files) {
            if (tempFile.isFile()) {
                if (tempFile.getName().toLowerCase().endsWith(".apk")) {
                    Log.e(TAG, tempFile.getName());
                    apks.add(tempFile.getAbsolutePath());
                    DataCleanManager.deleteFile(tempFile.getAbsolutePath());
                }
            } else {
                findApk(tempFile);
            }
        }
    }


    List<File> dirInfoList = new ArrayList<>();

    /**
     * 存储大小分析
     *
     * @return
     */
    public List<File> dirSize() {
        File file = new File(Environment.getExternalStorageDirectory().getPath());
        File[] files = file.listFiles();
        for (File tempFile : files) {
            dirInfoList.add(tempFile);
            Log.e(TAG, tempFile.getName());
            Log.e(TAG, "lastModified==" + tempFile.lastModified());
        }
        return dirInfoList;
    }





}