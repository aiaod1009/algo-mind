package com.example.demo.repository;

import com.example.demo.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface MessageMapper extends JpaRepository<Message, Long> {

  // 获取包含发送者信息的消息列表
  @Query(value = "SELECT m.*, u.name as sender_name, u.avatar as sender_avatar " +
      "FROM t_message m LEFT JOIN t_user u ON m.sender_id = u.id " +
      "WHERE m.user_id = ?1 AND m.type = ?2 " +
      "ORDER BY m.created_at DESC", nativeQuery = true)
  List<Map<String, Object>> getMessagesWithTypeAndSender(Long userId, String type);

  // 获取有聊天记录的联系人列表
  @Query(value = "SELECT u.id as sender_id, u.name as sender_name, u.avatar as sender_avatar, " +
      "MAX(m.created_at) as last_msg_time, " +
      "(SELECT content FROM t_message m2 WHERE m2.sender_id = u.id AND m2.user_id = ?1 AND m2.type = 'chat' ORDER BY m2.created_at DESC LIMIT 1) as last_msg_content "
      +
      "FROM t_message m JOIN t_user u ON m.sender_id = u.id " +
      "WHERE m.user_id = ?1 AND m.type = 'chat' " +
      "GROUP BY u.id, u.name, u.avatar " +
      "ORDER BY last_msg_time DESC", nativeQuery = true)
  List<Map<String, Object>> getChatContacts(Long userId);
}
