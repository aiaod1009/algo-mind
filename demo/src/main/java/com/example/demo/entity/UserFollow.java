package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "t_user_follow", uniqueConstraints = {
    @UniqueConstraint(name = "uk_follower_following", columnNames = { "follower_id", "following_id" })
}, indexes = {
    @Index(name = "idx_follow_following", columnList = "following_id")
})
public class UserFollow {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "follower_id", nullable = false)
  private Long followerId;

  @Column(name = "following_id", nullable = false)
  private Long followingId;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  public void prePersist() {
    if (createdAt == null) {
      createdAt = OffsetDateTime.now();
    }
  }
}
