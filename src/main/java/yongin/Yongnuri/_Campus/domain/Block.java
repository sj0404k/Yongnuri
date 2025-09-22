package yongin.Yongnuri._Campus.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "blocks")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Block {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blocker_id") // 차단한 유저
    private Long blockerId;

    @Column(name = "blocked_id") // 차단당한 유저
    private Long blockedId;

    @Column
    private LocalDateTime createdDate;
}