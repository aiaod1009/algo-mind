package com.example.demo.service;

import com.example.demo.entity.Message;
import com.example.demo.repository.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MessageService {

  @Autowired
  private MessageMapper messageMapper;

  public List<Map<String, Object>> getMessagesByType(Long userId, String type) {
    return messageMapper.getMessagesWithTypeAndSender(userId, type);
  }

  public List<Map<String, Object>> getChatContacts(Long userId) {
    return messageMapper.getChatContacts(userId);
  }

  public List<Map<String, Object>> getConversation(Long userId, Long contactId) {
    return messageMapper.getConversation(userId, contactId);
  }

  public Message sendChatMessage(Long senderId, Long receiverId, String content) {
    Message message = new Message();
    message.setUserId(receiverId);
    message.setSenderId(senderId);
    message.setType("chat");
    message.setContent(content);
    message.setIsRead(0);
    message.setCreatedAt(LocalDateTime.now());
    return messageMapper.save(message);
  }

  public boolean markAsRead(Long messageId) {
    Optional<Message> optMsg = messageMapper.findById(messageId);
    if (optMsg.isPresent()) {
      Message msg = optMsg.get();
      msg.setIsRead(1);
      messageMapper.save(msg);
      return true;
    }
    return false;
  }
}
