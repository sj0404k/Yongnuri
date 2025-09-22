package yongin.Yongnuri._Campus.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "makedeal") // 테이블 이름 가정
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long chatRoomId;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private Long buyerId;

    @Column(nullable = false)
    private LocalDateTime appointmentAt;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String status; // "SCHEDULED", "COMPLETED", "CANCELED"

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}