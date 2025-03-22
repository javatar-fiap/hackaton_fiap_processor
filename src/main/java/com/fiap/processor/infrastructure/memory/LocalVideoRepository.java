package com.fiap.processor.infrastructure.memory;

import com.fiap.processor.core.domain.VideoModel;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class LocalVideoRepository {

    private final Map<String, VideoModel> videoStore = new HashMap<>();

    public void save(VideoModel video) {
        videoStore.put(video.getPath(), video);
    }

    public VideoModel findByPath(String path) {
        return videoStore.getOrDefault(path, null);
    }
}
