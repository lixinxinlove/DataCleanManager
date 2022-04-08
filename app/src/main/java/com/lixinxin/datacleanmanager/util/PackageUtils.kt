package com.lixinxin.datacleanmanager.util


import android.content.ComponentName
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.ComponentInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.text.format.Formatter
import androidx.core.content.pm.PackageInfoCompat
import com.lixinxin.datacleanmanager.SystemServices
import com.lixinxin.datacleanmanager.bean.LibStringItem
import com.lixinxin.datacleanmanager.bean.StatefulComponent
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile



object PackageUtils {

    /**
     * Get version code of an app
     * @param packageInfo PackageInfo
     * @return version code as Long Integer
     */
    fun getVersionCode(packageInfo: PackageInfo): Long {
        return PackageInfoCompat.getLongVersionCode(packageInfo)
    }

    /**
     * Get version string of an app ( 1.0.0(1) )
     * @param packageInfo PackageInfo
     * @return version code as String
     */
    fun getVersionString(packageInfo: PackageInfo): String {
        return try {
            "${packageInfo.versionName ?: "<unknown>"} (${getVersionCode(packageInfo)})"
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown"
        }
    }

    /**
     * Get version string of an app ( 1.0.0(1) )
     * @param versionName Version name
     * @param versionCode Version code
     * @return version code as String
     */
    fun getVersionString(versionName: String, versionCode: Long): String {
        return "$versionName ($versionCode)"
    }


    fun getTargetApiString(packageInfo: PackageInfo): String {
        return try {
            "targetSdkVersion ${packageInfo.applicationInfo.targetSdkVersion}"
        } catch (e: PackageManager.NameNotFoundException) {
            "targetSdkVersion ?"
        }
    }

    fun getTargetApiString(targetSdkVersion: Short) = "Target API $targetSdkVersion"


    /**
     * Get native libraries of an app from source path
     * @param packageInfo PackageInfo
     * @return List of LibStringItem
     */


    fun getSourceLibs(
        packageInfo: PackageInfo,
        childDir: String,
        source: String? = null
    ): List<LibStringItem> {
        try {
            ZipFile(File(packageInfo.applicationInfo.sourceDir)).use { zipFile ->
                return zipFile.entries()
                    .asSequence()
                    .filter { (it.name.startsWith(childDir)) && it.name.endsWith(".so") }
                    .distinctBy { it.name.split("/").last() }
                    .map { LibStringItem(it.name.split("/").last(), it.size, source) }
                    .toList()
                    .ifEmpty { getSplitLibs(packageInfo) }
            }
        } catch (e: Exception) {
            //  Timber.e(e)
            return emptyList()
        }
    }

    /**
     * Get native libraries of an app from split apk
     * @param packageInfo PackageInfo
     * @return List of LibStringItem
     */
    private fun getSplitLibs(packageInfo: PackageInfo): List<LibStringItem> {
        val libList = mutableListOf<LibStringItem>()
        val splitList = packageInfo.applicationInfo.splitSourceDirs
        if (splitList.isNullOrEmpty()) {
            return listOf()
        }

        splitList.find {
            val fileName = it.split(File.separator).last()
            fileName.startsWith("split_config.arm") || fileName.startsWith("split_config.x86")
        }?.let {
            ZipFile(File(it)).use { zipFile ->
                val entries = zipFile.entries()
                var next: ZipEntry

                while (entries.hasMoreElements()) {
                    next = entries.nextElement()

                    if (next.name.contains("lib/") && !next.isDirectory) {
                        libList.add(LibStringItem(next.name.split("/").last(), next.size))
                    }
                }
            }
        }

        return libList
    }

    /**
     * Judge that whether an app uses split apks
     * @param packageInfo PackageInfo
     * @return true if it uses split apks
     */
    fun isSplitsApk(packageInfo: PackageInfo): Boolean {
        return !packageInfo.applicationInfo.splitSourceDirs.isNullOrEmpty()
    }


    /**
     * Get all meta data in an app
     * @param packageInfo PackageInfo
     * @return meta data list
     */
    fun getMetaDataItems(packageInfo: PackageInfo): List<LibStringItem> {
        packageInfo.applicationInfo.metaData?.let {
            return it.keySet().asSequence()
                .map { key -> LibStringItem(key, 0, it.get(key).toString()) }
                .toList()
        } ?: return emptyList()
    }

    /**
     * Get all permissions in an app
     * @param packageInfo PackageInfo
     * @return permissions list
     */
    fun getPermissionsItems(packageInfo: PackageInfo): List<LibStringItem> {
        packageInfo.requestedPermissions?.let {
            return it.asSequence()
                .map { perm -> LibStringItem(perm, 0) }
                .toList()
        } ?: return emptyList()
    }


    /**
     * Get components list of an app
     * @param packageName Package name of the app
     * @param list List of components(can be nullable)
     * @param isSimpleName Whether to show class name as a simple name
     * @return List of String
     */
    private fun getComponentStringList(
        packageName: String,
        list: Array<out ComponentInfo>?,
        isSimpleName: Boolean
    ): List<String> {
        if (list.isNullOrEmpty()) {
            return emptyList()
        }
        return list.asSequence()
            .map {
                if (isSimpleName) {
                    it.name.removePrefix(packageName)
                } else {
                    it.name
                }
            }
            .toList()
    }

    val use32bitAbiString = "use32bitAbi"
     val multiArchString = "multiArch"
     val overlayString = "overlay"

    /**
     * Get ABIs set of an app
     * @param file Application file
     * @param applicationInfo ApplicationInfo
     * @param isApk Whether is an APK file
     * @param overlay Is this app an overlay app
     * @param ignoreArch Ignore arch so you can get all ABIs
     * @return ABI type
     */
    fun getAbiSet(
        file: File,
        applicationInfo: ApplicationInfo,
        isApk: Boolean = false,
        overlay: Boolean,
        ignoreArch: Boolean = false
    ): Set<Int> {
        var elementName: String

        val abiSet = mutableSetOf<Int>()
        var zipFile: ZipFile? = null

        try {
            zipFile = ZipFile(file)
            val entries = zipFile.entries()

            if (overlay) {
                //  abiSet.add(OVERLAY)
                return abiSet
            }

            var entry: ZipEntry

            while (entries.hasMoreElements()) {
                entry = entries.nextElement()

                if (entry.isDirectory) {
                    continue
                }

                elementName = entry.name

                if (abiSet.size == 5) {
                    break
                }


            }

            if (abiSet.isEmpty()) {
                if (!isApk) {
                    abiSet.addAll(getAbiListByNativeDir(applicationInfo.nativeLibraryDir))
                }

                if (abiSet.isEmpty()) {

                }
            }
            return abiSet
        } catch (e: Throwable) {

            abiSet.clear()

            return abiSet
        } finally {
            zipFile?.close()
        }
    }

    /**
     * Get ABI type of an app from native path
     * @param nativePath Native path of the app
     * @return ABI type
     */
    private fun getAbiListByNativeDir(nativePath: String): MutableSet<Int> {
        val file = File(nativePath.substring(0, nativePath.lastIndexOf("/")))
        val abis = mutableSetOf<Int>()

        val fileList = file.listFiles() ?: return mutableSetOf()

        fileList.asSequence()
            .forEach {
                if (it.isDirectory) {
                    when (it.name) {

                    }
                }
            }

        return abis
    }


    /**
     * Format size number to string
     * @param item LibStringItem
     * @return String of size number (100KB)
     */
    fun sizeToString(context: Context, item: LibStringItem): String {
        val source = item.source?.let { ", ${item.source}" }.orEmpty()
        return "(${Formatter.formatFileSize(context, item.size)}$source)"
    }


    private  val minSdkVersion = "minSdkVersion"


    private  val AGP_KEYWORD = "androidGradlePluginVersion"
    private  val AGP_KEYWORD2 = "Created-By: Android Gradle "

    fun getAGPVersion(packageInfo: PackageInfo): String? {
        runCatching {
            ZipFile(File(packageInfo.applicationInfo.sourceDir)).use { zipFile ->
                zipFile.getEntry("META-INF/com/android/build/gradle/app-metadata.properties")
                    ?.let { ze ->
                        Properties().apply {
                            load(zipFile.getInputStream(ze))
                            getProperty(AGP_KEYWORD)?.let {
                                return it
                            }
                        }
                    }
                zipFile.getEntry("META-INF/MANIFEST.MF")?.let { ze ->
                    BufferedReader(InputStreamReader(zipFile.getInputStream(ze))).useLines { seq ->
                        seq.find { it.contains(AGP_KEYWORD2) }?.let {
                            return it.removePrefix(AGP_KEYWORD2)
                        }
                    }
                }
                arrayOf(
                    "META-INF/androidx.databinding_viewbinding.version",
                    "META-INF/androidx.databinding_databindingKtx.version",
                    "META-INF/androidx.databinding_library.version"
                ).forEach { entry ->
                    zipFile.getEntry(entry)?.let { ze ->
                        BufferedReader(InputStreamReader(zipFile.getInputStream(ze))).use { seq ->
                            val version = seq.readLine()
                            if (version.isNotBlank()) {
                                return version
                            }
                        }
                    }
                }
            }
        }

        return null
    }

    private val runtime by lazy { Runtime.getRuntime() }

    private fun getAppListByShell(): List<String> {
        try {
            val pmList = mutableListOf<String>()
            val process = runtime.exec("pm list packages")
            InputStreamReader(process.inputStream, StandardCharsets.UTF_8).use { isr ->
                BufferedReader(isr).use { br ->
                    br.forEachLine { line ->
                        line.trim().let { trimLine ->
                            if (trimLine.startsWith("package:")) {
                                trimLine.removePrefix("package:").let {
                                    if (it.isNotBlank()) {
                                        pmList.add(it)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return pmList
        } catch (t: Throwable) {
            return emptyList()
        }
    }


    fun getPackageSize(packageInfo: PackageInfo, includeSplits: Boolean): Long {
        var size: Long = File(packageInfo.applicationInfo.sourceDir).length()

        if (!includeSplits) {
            return size
        }

        packageInfo.applicationInfo.splitSourceDirs?.forEach {
            runCatching {
                size += File(it).length()
            }
        }
        return size
    }

    fun PackageInfo.isXposedModule(): Boolean {
        return applicationInfo.metaData?.getBoolean("xposedmodule") == true ||
                applicationInfo.metaData?.containsKey("xposedminversion") == true
    }

    fun PackageInfo.isPlayAppSigning(): Boolean {
        return applicationInfo.metaData?.getString("com.android.stamp.type") == "STAMP_TYPE_DISTRIBUTION_APK" &&
                applicationInfo.metaData?.getString("com.android.stamp.source") == "https://play.google.com/store"
    }

    fun PackageInfo.isPWA(): Boolean {
        return applicationInfo.metaData?.keySet()
            ?.any { it.startsWith("org.chromium.webapk.shell_apk") } == true
    }




    fun getComponentList(
        packageName: String,
        list: Array<out ComponentInfo>?,
        isSimpleName: Boolean
    ): List<StatefulComponent> {
        if (list.isNullOrEmpty()) {
            return emptyList()
        }
        var state: Int
        var isEnabled: Boolean
        return list.asSequence()
            .map {
                state = try {
                    SystemServices.packageManager.getComponentEnabledSetting(
                        ComponentName(
                            packageName,
                            it.name
                        )
                    )
                } catch (e: IllegalArgumentException) {
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
                }
                isEnabled = when (state) {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED -> false
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> it.enabled
                    else -> false
                }

                val name = if (isSimpleName) {
                    it.name.orEmpty().removePrefix(packageName)
                } else {
                    it.name.orEmpty()
                }
                StatefulComponent(name, isEnabled, it.processName.removePrefix(it.packageName))
            }
            .toList()
    }


}