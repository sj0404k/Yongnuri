package yongin.Yongnuri._Campus.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "block") // (DB에 'block' 테이블이 있다고 가정)
public class Block {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blocker_id") // 차단한 유저
    private Long blockerId;

    @Column(name = "blocked_id") // 차단당한 유저
    private Long blockedId;
    
    // --- Getter ---
    // (BlockService에서 .map(Block::getBlockedId)를 사용하기 위해 필요)
    public Long getBlockedId() {
        return this.blockedId;
    }
    
    // (JPA를 위한 기본 생성자)
    protected Block() {}
}