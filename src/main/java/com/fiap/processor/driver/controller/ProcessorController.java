package com.fiap.processor.driver.controller;

import com.fiap.processor.core.application.usecases.ProcessorVideoUseCase;
import com.fiap.processor.core.domain.VideoModel;
import com.fiap.processor.core.domain.VideoMessageModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/videos")
public class ProcessorController {

    private final ProcessorVideoUseCase processorVideoUseCase;

    public ProcessorController(ProcessorVideoUseCase processorVideoUseCase) {
        this.processorVideoUseCase = processorVideoUseCase;
    }

    @PostMapping("/process")
    public ResponseEntity<Void> processVideo(@RequestBody VideoMessageModel videoMessage) {
        try {
            processorVideoUseCase.execute(videoMessage);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{videoKey}")
    public ResponseEntity<VideoModel> getVideo(@PathVariable String videoKey) {
        try {
            var video = processorVideoUseCase.getVideoByKey(videoKey);
            return ResponseEntity.ok(video);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}