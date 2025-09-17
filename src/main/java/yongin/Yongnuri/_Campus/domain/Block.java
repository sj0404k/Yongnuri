package yongin.Yongnuri._Campus.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "blocks") 
public class Block {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blocker_id") // 차단한 유저
    private Long blockerId;

    @Column(name = "blocked_id") // 차단당한 유저
    private Long blockedId;
    
    public Long getBlockedId() {
        return this.blockedId;
    }
    
    protected Block() {}
}