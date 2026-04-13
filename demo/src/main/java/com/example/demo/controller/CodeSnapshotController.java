package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.auth.CurrentUserService;
import com.example.demo.entity.CodeSnapshot;
import com.example.demo.service.CodeSnapshotService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
public class CodeSnapshotController {

    @Resource
    private CodeSnapshotService codeSnapshotService;

    @Resource
    private CurrentUserService currentUserService;

    @GetMapping("/code-snapshots")
    public Result<List<CodeSnapshot>> getMySnapshots(
            @RequestParam(required = false) Long levelId,
            @RequestParam(required = false, defaultValue = "false") boolean passedOnly) {
        Long userId = currentUserService.requireCurrentUserId();

        if (levelId != null) {
            return Result.success(codeSnapshotService.getLevelSnapshots(userId, levelId));
        }

        if (passedOnly) {
            return Result.success(codeSnapshotService.getUserPassedSnapshots(userId));
        }

        return Result.success(codeSnapshotService.getUserSnapshots(userId));
    }

    @GetMapping("/code-snapshots/best")
    public Result<CodeSnapshot> getBestSnapshot(@RequestParam Long levelId) {
        Long userId = currentUserService.requireCurrentUserId();
        Optional<CodeSnapshot> best = codeSnapshotService.getBestSnapshot(userId, levelId);
        return best.map(Result::success)
                .orElseGet(() -> Result.fail(40401, "该题暂无通过的历史代码"));
    }

    @GetMapping("/code-snapshots/stats")
    public Result<Map<String, Object>> getMyStats() {
        Long userId = currentUserService.requireCurrentUserId();
        return Result.success(codeSnapshotService.getUserStats(userId));
    }

    @PostMapping("/code-snapshots")
    public Result<CodeSnapshot> saveSnapshot(@RequestBody CodeSnapshot snapshot) {
        Long userId = currentUserService.requireCurrentUserId();
        snapshot.setUserId(userId);
        try {
            return Result.success(codeSnapshotService.saveSnapshot(snapshot));
        } catch (IllegalArgumentException exception) {
            return Result.fail(40001, exception.getMessage());
        }
    }

    @DeleteMapping("/code-snapshots/{id}")
    public Result<Map<String, Object>> deleteSnapshot(@PathVariable Long id) {
        Long userId = currentUserService.requireCurrentUserId();
        boolean deleted = codeSnapshotService.deleteSnapshot(id, userId);
        if (!deleted) {
            return Result.fail(40401, "历史代码不存在");
        }
        return Result.success(Map.of("id", id, "deleted", true));
    }
}
