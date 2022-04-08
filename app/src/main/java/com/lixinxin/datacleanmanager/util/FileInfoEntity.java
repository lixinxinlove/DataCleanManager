package com.lixinxin.datacleanmanager.util;

public class FileInfoEntity {
    private String name;
    private long fileSize;
    private long addTime;
    private String fullPath;
    private int fileType;
    private boolean checked;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "name='" + name + '\'' +
                ", fileSize=" + fileSize +
                ", addTime=" + addTime +
                ", fullPath='" + fullPath + '\'' +
                ", fileType=" + fileType +
                ", checked=" + checked +
                '}';
    }
}
