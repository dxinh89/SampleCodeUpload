package com.example.samplecode.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SessionBody {

    @SerializedName("md5Checksum")
    @Expose
    private String md5Checksum;

    @SerializedName("totalSize")
    @Expose
    private Long totalSize;

    public SessionBody() {
    }

    public SessionBody(String md5, Long totalSize) {
        this.md5Checksum = md5;
        this.totalSize = totalSize;
    }


    public String getMd5Checksum() {
        return md5Checksum;
    }

    public void setMd5Checksum(String md5Checksum) {
        this.md5Checksum = md5Checksum;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

}