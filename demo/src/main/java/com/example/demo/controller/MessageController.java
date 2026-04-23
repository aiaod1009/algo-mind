package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.service.MessageService;
import com.example.demo.auth.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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

  // 获取与指定联系人的会话消息（包含双方）
  @GetMapping("/chat/{contactId}")
  public Result<List<Map<String, Object>>> getConversation(@PathVariable Long contactId) {
    try {
      Long userId = currentUserService.getCurrentUserId().orElse(null);
      if (userId == null) {
        return Result.fail(401, "未登录");
      }
      List<Map<String, Object>> messages = messageService.getConversation(userId, contactId);
      return Result.success(messages);
    } catch (Exception e) {
      return Result.fail(500, "获取会话失败: " + e.getMessage());
    }
  }

  // 发送私信
  @PostMapping("/chat/send")
  public Result<Map<String, Object>> sendChatMessage(@RequestBody Map<String, Object> body) {
    try {
      Long senderId = currentUserService.getCurrentUserId().orElse(null);
      if (senderId == null) {
        return Result.fail(401, "未登录");
      }

      Object receiverObj = body.get("receiverId");
      Object contentObj = body.get("content");
      if (receiverObj == null || contentObj == null) {
        return Result.fail(400, "参数不完整");
      }

      Long receiverId = Long.valueOf(String.valueOf(receiverObj));
      String content = String.valueOf(contentObj).trim();
      if (content.isEmpty()) {
        return Result.fail(400, "消息内容不能为空");
      }
      if (content.length() > 1000) {
        return Result.fail(400, "消息内容不能超过1000字符");
      }
      if (senderId.equals(receiverId)) {
        return Result.fail(400, "不能给自己发私信");
      }

      messageService.sendChatMessage(senderId, receiverId, content);

      Map<String, Object> data = new HashMap<>();
      data.put("receiverId", receiverId);
      data.put("content", content);
      return Result.success(data);
    } catch (Exception e) {
      return Result.fail(500, "发送失败: " + e.getMessage());
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
