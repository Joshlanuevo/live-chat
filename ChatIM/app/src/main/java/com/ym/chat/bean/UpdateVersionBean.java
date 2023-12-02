package com.ym.chat.bean;

import java.io.Serializable;
import java.util.List;

public class UpdateVersionBean implements Serializable {

    /**
     * id : null
     * versionNum : null
     * versionName : null
     * downloadUrl : null
     * description : null
     * updateType : null
     * clientType : null
     * operatorId : null
     * operator : null
     * createTime : null
     * updateTime : null
     */

    private String id;
    private String versionNum;//版本号
    private String versionName;
    private String downloadUrl;//apk下载地址
    private List<String> summarys;//说明
    private String updateType;//更新类型: 手动更新 Manual,强制更新 Force
    private String clientType;//终端类型: android手机 Android,苹果手机 IOS,电脑端 PC
    private String operatorId;
    private String operator;
    private String createTime;
    private String updateTime;

    public List<String> getSummarys() {
        return summarys;
    }

    public void setSummarys(List<String> summarys) {
        this.summarys = summarys;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(String versionNum) {
        this.versionNum = versionNum;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }


    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
