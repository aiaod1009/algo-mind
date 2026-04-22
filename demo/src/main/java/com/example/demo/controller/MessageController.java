package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.service.MessageService;
import com.example.demo.auth.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

  @Autowired
  private MessageService messageService;

  @Autowired
  private CurrentUserService currentUserService;

  // 获取特定类型的通知
  @GetMapping("/{type}")
  public Result<List<Map<String, Object>>> getMessages(@PathVariable String type) {
    try {
      Long userId = currentUserService.getCurrentUserId().orElse(null);
      if (userId == null) {
        return Result.fail(401, "未登录");
      }
      List<Map<String, Object>> messages = messageService.getMessagesByType(userId, type);
      return Result.success(messages);
    } catch (Exception e) {
      return Result.fail(500, "获取消息失败: " + e.getMessage());
    }
  }

  // 获取私信联系人列表
  @GetMapping("/contacts")
  public Result<List<Map<String, Object>>> getContacts() {
    try {
      Long userId = currentUserService.getCurrentUserId().orElse(null);
      if (userId == null) {
        return Result.fail(401, "未登录");
      }
      List<Map<String, Object>> contacts = messageService.getChatContacts(userId);
      return Result.success(contacts);
    } catch (Exception e) {
      return Result.fail(500, "获取联系人失败: " + e.getMessage());
    }
  }

  // 将消息标记为已读
  @PutMapping("/{id}/read")
  public Result<Void> markAsRead(@PathVariable Long id) {
    boolean success = messageService.markAsRead(id);
    if (success) {
      return Result.success(null);
    }
    return Result.fail(400, "失败");
  }
}
