package com.erning.getapp.bean;

import android.graphics.drawable.Drawable;

/**
 * Created by 二宁 on 2018/5/31.
 */

public class AppInfoData {
    private Drawable appicon;
    private String appname;
    private String apppackage;
    private String appversion;
    private String appversionCode;

    public void setAppicon(Drawable appicon) {
        this.appicon = appicon;
    }

    public void setAppname(String appname) {
        this.appname = appname;
    }

    public void setApppackage(String apppackage) {
        this.apppackage = apppackage;
    }

    public void setAppversion(String appversion) {
        this.appversion = appversion;
    }

    public void setAppversionCode(String appversionCode) {
        this.appversionCode = appversionCode;
    }

    public Drawable getAppicon() {
        return appicon;
    }

    public String getAppname() {
        return appname;
    }

    public String getApppackage() {
        return apppackage;
    }

    public String getAppversion() {
        return appversion;
    }

    public String getAppversionCode() {
        return appversionCode;
    }
}
