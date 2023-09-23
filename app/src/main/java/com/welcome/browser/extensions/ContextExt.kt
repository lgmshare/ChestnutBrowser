package com.welcome.browser.extensions

import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Process
import android.widget.Toast
import androidx.annotation.StringRes
import com.welcome.browser.BuildConfig
import com.welcome.browser.R
import java.io.File


/** **************************************
 * 加External和不加的比较:
 * 相同点：
 * 1. 都可以做app缓存目录。
 * 2. app卸载后，两个目录下的数据都会被清空。
 * 不同点:
 * 1、目录的路径不同。前者的目录存在外部SD卡上的。后者的目录存在app的内部存储上。
 * 2、前者的路径在手机里可以直接看到。后者的路径需要root以后，用Root Explorer 文件管理器才能看到。
 *******************************************************************/

/**
 * 获取应用缓存文件目录
 * ex: /storage/emulated/0/Android/data/com.lgmshare.mylife/files/test
 */
fun Context.getExternalFilePath(fileDir: String) = getExternalFilesDir(fileDir)?.absolutePath

/**
 * 获取应用缓存文件目录
 * ex: /storage/emulated/0/Android/data/com.lgmshare.mylife/cache/test
 */
fun Context.getExternalCacheFilePath(fileDir: String) =
    externalCacheDir?.absolutePath + File.separator + fileDir

/**
 * 获取应用文件目录
 * ex: /data/user/0/com.lgmshare.mylife/files/test
 */
fun Context.getFilePath(fileDir: String) = filesDir?.absolutePath + File.separator + fileDir

/**
 * 获取应用文件目录
 * ex: /data/user/0/com.lgmshare.mylife/cache/test
 */
fun Context.getCacheFilePath(fileDir: String) = cacheDir?.absolutePath + File.separator + fileDir

fun Context.toast(msg: String?) {
    if (!msg.isNullOrBlank()) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}

fun Context.toast(@StringRes msg: Int) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

/**
 * dp值转换为px
 */
fun Context.dp2px(dp: Float): Int {
    val scale = resources.displayMetrics.density
    return (dp * scale + 0.5f).toInt()
}

/**
 * px值转换成dp
 */
fun Context.px2dp(px: Float): Int {
    val scale = resources.displayMetrics.density
    return (px / scale + 0.5f).toInt()
}

fun Context.px2sp(px: Float): Int {
    val fontScale = resources.displayMetrics.scaledDensity
    return (px / fontScale + 0.5f).toInt()
}

fun Context.sp2px(sp: Float): Int {
    val fontScale = resources.displayMetrics.scaledDensity
    return (sp * fontScale + 0.5f).toInt()
}

/**
 * 获取屏幕宽度
 */
fun Context.getScreenWidth(): Int {
    return resources.displayMetrics.widthPixels
}

/**
 * 获取屏幕高度
 */
fun Context.getScreenHeight(): Int {
    return resources.displayMetrics.heightPixels
}

/**
 * 获得屏幕的高度，包括状态栏和导航栏
 */
fun Context.getScreenRealHeight(): Int {
    val point = Point()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        windowManager?.defaultDisplay?.getRealSize(point)
    } else {
        windowManager?.defaultDisplay?.getSize(point)
    }
    return point.y
}

/**
 * 状态航栏高度
 */
fun Context.getStatusBarHeight(): Int {
    var statusBarHeight = 0
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        statusBarHeight = resources.getDimensionPixelSize(resourceId)
    }
    return statusBarHeight
}

/**
 * 是否有底部导航栏
 */
fun Context.hasNavigationBar(): Boolean {
    val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
    return id > 0 && resources.getBoolean(id)
}

/**
 * 获取底部导航栏高度
 */
fun Context.getNavigationBarHeight(): Int {
    val id = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return if (id > 0 && hasNavigationBar()) {
        resources.getDimensionPixelSize(id)
    } else {
        0
    }
}

fun Context.getCurrentProcessName(context: Context): String? {
    val pid = Process.myPid()
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (appProcess in am.runningAppProcesses) {
        if (appProcess.pid == pid) {
            return appProcess.processName
        }
    }
    return null
}

/**
 * 调用系统分享
 */
fun Context.jumpShare(shareText: String?, shareTitle: String? = getString(R.string.share)) {
    try {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, shareText ?: "")
        startActivity(Intent.createChooser(intent, shareTitle ?: ""))
    } catch (e: Exception) {
    }
}

fun Context.copyToClipboard(text: String) {
    val clip = ClipData.newPlainText(getString(R.string.app_name), text)
    (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
    val toastText = String.format(getString(R.string.copy_successfully), text)
    toast(toastText)
}

fun Context.jumpGooglePlayStore() {
    try {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.data = Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            intent.data = Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}");
            if (intent.resolveActivity(packageManager) == null) {
                startActivity(intent)
            } else {
                toast("You don't have an app market installed, not even a browser!")
            }
        }
    } catch (e: Exception) {
    }
}


