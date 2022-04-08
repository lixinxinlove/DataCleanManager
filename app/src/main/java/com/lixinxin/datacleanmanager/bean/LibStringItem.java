package com.lixinxin.datacleanmanager.bean;

public class LibStringItem {

    private String name;
    private Long size;
    private String source;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "LibStringItem{" +
                "name='" + name + '\'' +
                ", size=" + size +
                ", source='" + source + '\'' +
                '}';
    }

    public LibStringItem(String name, Long size) {
        this.name = name;
        this.size = size;
    }

    public LibStringItem(String name, Long size, String source) {
        this.name = name;
        this.size = size;
        this.source = source;
    }
}
