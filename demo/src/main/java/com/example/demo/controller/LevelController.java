package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.dto.SubmitAnswerRequest;
import com.example.demo.dto.SubmitAnswerResponse;
import com.example.demo.entity.Level;
import com.example.demo.repository.LevelRepository;
import com.example.demo.service.LevelSubmissionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
public class LevelController {

    private final LevelRepository levelRepository;
    private final LevelSubmissionService levelSubmissionService;

    public LevelController(LevelRepository levelRepository, LevelSubmissionService levelSubmissionService) {
        this.levelRepository = levelRepository;
        this.levelSubmissionService = levelSubmissionService;
    }

    @GetMapping("/levels")
    public Result<List<Level>> getLevels(@RequestParam(required = false) String track) {
        List<Level> levelList;
        if (track != null && !track.isBlank()) {
            levelList = levelRepository.findByTrack(track);
        } else {
            levelList = levelRepository.findAll();
        }

        levelList.forEach(level -> {
            if (level.getIsUnlocked() == null) {
                level.setIsUnlocked(false);
            }
            if (level.getRewardPoints() == null) {
                level.setRewardPoints(0);
            }
        });
        return Result.success(levelList == null ? List.of() : levelList);
    }

    @PostMapping("/submit")
    public Result<SubmitAnswerResponse> submitAnswer(@RequestBody SubmitAnswerRequest submitRequest) {
        try {
            return Result.success(levelSubmissionService.submit(submitRequest));
        } catch (IllegalArgumentException e) {
            return Result.fail(40001, e.getMessage());
        } catch (NoSuchElementException e) {
            return Result.fail(40401, e.getMessage());
        }
    }
}
