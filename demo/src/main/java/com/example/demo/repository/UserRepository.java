package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findFirstByName(String username);

    Optional<User> findFirstByNameIgnoreCase(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    @Query("SELECT u FROM User u WHERE locate('@', u.email) > 0 AND lower(substring(u.email, 1, locate('@', u.email) - 1)) = lower(:localPart)")
    Optional<User> findByEmailLocalPart(@Param("localPart") String localPart);

    boolean existsByEmail(String email);

    @Query(value = "SELECT u FROM User u WHERE :track = 'all' OR u.targetTrack = :track ORDER BY u.points DESC")
    List<User> findRankingByTrack(@Param("track") String track);
}
