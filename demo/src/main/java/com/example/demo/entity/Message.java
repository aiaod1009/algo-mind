package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_message")
public class Message {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id")
  private Long userId; // 接收消息的用户

  @Column(name = "sender_id")
  private Long senderId; // 发送消息的用户，可为空(当为系统通知时)

  private String type; // sys-notice, likes, comments, follows, chat
  private String title; // 标题
  private String content; // 内容

  @Column(name = "is_read")
  private Integer isRead; // 0 未读, 1 已读

  @Column(name = "related_id")
  private Long relatedId; // 关联实体ID

  @Column(name = "created_at")
  private LocalDateTime createdAt;
}
