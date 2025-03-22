package com.fiap.processor.core.domain;

import java.time.Duration;

public class VideoModel {
    private String path;
    private Duration duration;

    public VideoModel(String path, Duration duration) {
        this.path = path;
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public Duration getDuration() {
        return duration;
    }
}
