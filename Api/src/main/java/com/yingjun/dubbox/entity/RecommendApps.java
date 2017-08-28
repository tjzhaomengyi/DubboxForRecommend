package com.yingjun.dubbox.entity;

import java.io.Serializable;

public class RecommendApps implements Serializable {
    private String applist;
    public RecommendApps(){

    }

    public RecommendApps(String applist){
        this.applist = applist;

    }

    public String getApplist() {
        return applist;
    }

    public void setApplist(String applist) {
        this.applist = applist;
    }
}
