package com.lixinxin.datacleanmanager.bean;

import android.graphics.drawable.Drawable;

public class AppInfo {
    private String name;//app名称
    private String packageName;//包名
    private Drawable icon;//icon
    private boolean installInRom;//是否安装在手机内
    private boolean system; // 是否是系统应用

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public boolean isInstallInRom() {
        return installInRom;
    }

    public void setInstallInRom(boolean installInRom) {
        this.installInRom = installInRom;
    }

    public boolean isSystem() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "name='" + name + '\'' +
                ", packageName='" + packageName + '\'' +
                ", icon=" + icon +
                ", installInRom=" + installInRom +
                ", system=" + system +
                '}';
    }
}
