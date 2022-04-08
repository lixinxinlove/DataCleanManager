package com.lixinxin.datacleanmanager.bean

data class StatefulComponent(
    val componentName: String,
    val enabled: Boolean = true,
    val processName: String
)
