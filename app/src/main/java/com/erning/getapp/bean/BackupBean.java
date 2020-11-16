package com.erning.getapp.bean;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;

import java.io.File;

/**
 * Created by 二宁 on 2018/5/31.
 */

public class BackupBean {
    private File dir;
    private long size = 0;
    private File apk;
    private String appName;
    private Drawable appIcon;
    private String appPackageName;

    public BackupBean(File dir) {
        calculateSize(dir);
    }

    private void calculateSize(File file){
        for (File f : file.listFiles()){
            if (f.isFile())
                size += f.length();
            else
                calculateSize(f);
        }
        size += 4096;
    }

    @SuppressLint("DefaultLocale")
    public String getAppSizeFormat() {
        double k = size/1024.0;
        if (k<1) return String.format("%dB",(int)size);
        double m = k/1024.0;
        if (m<1) return String.format("%.2fK",k);
        double g = m/1024.0;
        if (g<1) return String.format("%.2fM",m);
        return String.format("%.2fG",g);
    }

    public File getDir() {
        return dir;
    }

    public void setDir(File dir) {
        this.dir = dir;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public File getApk() {
        return apk;
    }

    public void setApk(File apk) {
        this.apk = apk;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }
}
