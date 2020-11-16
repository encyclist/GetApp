package com.erning.getapp.util

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.erning.getapp.bean.AppBean
import org.jetbrains.anko.toast
import java.io.File

/**
 * @author 二宁
 * @date 2020/11/16 17:50
 * @des
 */
class AppUtil {
    companion object{
        fun getAllActivity(activity:Activity,packageName: String): List<ActivityInfo> {
            val packageManager = activity.packageManager
            val packInfo = packageManager.getPackageInfo(packageName,PackageManager.GET_ACTIVITIES)
            if (packInfo != null){
                return packInfo.activities.sortedBy { it.exported.not() }
            }
            return ArrayList(0)
        }

        fun openApp(activity:Activity,packageName: String) {
            try {
                val packageManager = activity.packageManager
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                activity.startActivity(launchIntent)
            }catch (e:Exception){
                activity.toast("哔了哈士奇了")
            }
        }

        fun getAllApk(packageManager:PackageManager): List<AppBean> {
            val appBeanList = ArrayList<AppBean>()
            var bean: AppBean?

            val list = packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES)
            for (p in list) {
                bean = AppBean()
                bean.activities = p.activities
                bean.appIcon = p.applicationInfo.loadIcon(packageManager)
                bean.appName = packageManager.getApplicationLabel(p.applicationInfo).toString()
                bean.appPackageName = p.applicationInfo.packageName
                bean.apkPath = p.applicationInfo.sourceDir
                val file = File(p.applicationInfo.sourceDir)
                bean.appSize = file.length()
                val flags = p.applicationInfo.flags
                //判断是否是属于系统的apk
                if (flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                    bean.isSystem = true
                } else {
                    bean.isSd = true
                }
                appBeanList.add(bean)
                Log.d("程序信息",bean.toString())
            }
            return appBeanList
        }
    }
}