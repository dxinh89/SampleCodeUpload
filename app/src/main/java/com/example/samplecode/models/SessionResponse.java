package com.example.samplecode.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SessionResponse {

    @SerializedName("sessionId")
    @Expose
    private String sessionId;
    @SerializedName("expirationDateTime")
    @Expose
    private String expirationDateTime;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getExpirationDateTime() {
        return expirationDateTime;
    }

    public void setExpirationDateTime(String expirationDateTime) {
        this.expirationDateTime = expirationDateTime;
    }

}
