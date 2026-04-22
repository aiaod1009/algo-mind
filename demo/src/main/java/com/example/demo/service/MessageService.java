package com.example.demo.service;

import com.example.demo.entity.Message;
import com.example.demo.repository.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
