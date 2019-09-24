package com.example.samplecode.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SessionInfoResponse {
    @SerializedName("sessionId")
    @Expose
    private String sessionId;
    @SerializedName("md5Checksum")
    @Expose
    private String md5Checksum;
    @SerializedName("totalSize")
    @Expose
    private Integer totalSize;
    @SerializedName("expirationDateTime")
    @Expose
    private String expirationDateTime;
    @SerializedName("uploaded")
    @Expose
    private Integer uploaded;
    @SerializedName("link")
    @Expose
    private String link;

    public String getMd5Checksum() {
        return md5Checksum;
    }

    public void setMd5Checksum(String md5Checksum) {
        this.md5Checksum = md5Checksum;
    }

    public Integer getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Integer totalSize) {
        this.totalSize = totalSize;
    }

    public String getExpirationDateTime() {
        return expirationDateTime;
    }

    public void setExpirationDateTime(String expirationDateTime) {
        this.expirationDateTime = expirationDateTime;
    }

    public Integer getUploaded() {
        return uploaded;
    }

    public void setUploaded(Integer uploaded) {
        this.uploaded = uploaded;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}

