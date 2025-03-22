package com.fiap.processor.core.domain;

import org.json.JSONObject;

public class VideoMessageModel {

    private String id;
    private String username;
    private String email;
    private String keyS3;
    private Integer intervalSeconds;

    public VideoMessageModel() {
    }

    public VideoMessageModel(JSONObject messageFormatted) {
        this.id = messageFormatted.getString("id");
        this.email = messageFormatted.getString("email");
        this.username = messageFormatted.getString("user");
        this.keyS3 = messageFormatted.getString("videoKeyS3");
        this.intervalSeconds = messageFormatted.getInt("intervalSeconds");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getIntervalSeconds() {
        return intervalSeconds;
    }

    public String getKeyS3() {
        return keyS3;
    }

    public void setKeyS3(String keyS3) {
        this.keyS3 = keyS3;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
