package com.erning.getapp.bean;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by 二宁 on 2018/5/22.
 */

public class AppBean {
    private Drawable appIcon;
    private String appName;
    private long appSize;
    private boolean isSd=false;
    private boolean isSystem=false;
    private String appPackageName;
    private ActivityInfo[] activities = null;

    public String getApkPath() {
        return apkPath;
    }

    public void setApkPath(String apkPath) {
        this.apkPath = apkPath;
    }

    private String apkPath;

    public String getAppPackageName() {
        return appPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public long getAppSize() {
        return appSize;
    }

    @SuppressLint("DefaultLocale")
    public String getAppSizeFormat() {
        double k = appSize/1024.0;
        if (k<1) return String.format("%dB",(int)appSize);
        double m = k/1024.0;
        if (m<1) return String.format("%.2fK",k);
        double g = m/1024.0;
        if (g<1) return String.format("%.2fM",m);
        return String.format("%.2fG",g);
    }

    public void setAppSize(long appSize) {
        this.appSize = appSize;
    }

    public boolean isSd() {
        return isSd;
    }

    public void setSd(boolean sd) {
        isSd = sd;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystem(boolean system) {
        isSystem = system;
    }

    public ActivityInfo[] getActivities() {
        return activities;
    }

    public void setActivities(ActivityInfo[] activities) {
        this.activities = activities;
    }

    @Override
    public String toString() {
        return "AppBean{" +
                "appIcon=" + appIcon +
                ", appName='" + appName + '\'' +
                ", appSize=" + appSize +
                ", isSd=" + isSd +
                ", isSystem=" + isSystem +
                ", appPackageName='" + appPackageName + '\'' +
                ", apkPath='" + apkPath + '\'' +
                '}';
    }
}