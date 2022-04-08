package com.lixinxin.datacleanmanager;

import java.util.List;

public interface DiskTaskListener {
    void onFinished(List<String> filePathList);
}
