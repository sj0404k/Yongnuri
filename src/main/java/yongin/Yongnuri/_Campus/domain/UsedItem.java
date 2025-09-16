/*// domain/UsedItem.java
package yongin.Yongnuri._Campus.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "중고거래") // (DB의 '중고거래' 테이블과 매핑)
public class UsedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // (공통 Specification을 위해 필드명 통일 'userId')
    @Column(name = "user_id") 
    private Long userId; 

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "status")
    private String status;

    // (공통 Specification을 위해 필드명 통일 'location')
    @Column(name = "location") 
    private String location; 

    @Column(name = "price")
    private Integer price;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // ... (is_images, method 등 나머지 필드들)
    
    // (JPA를 위한 기본 생성자)
    protected UsedItem() {} 
    
    // --- Getters ---
    // (DTO 변환을 위해 Getter가 필요합니다)
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public Integer getPrice() { return price; }
    public String getLocation() { return location; }
    public String getStatus() { return status; }
    // (... 나머지 필드 Getter ...)
}
// domain/UsedItem.java
package yongin.Yongnuri._Campus.domain;

import jakarta.persistence.*;
import lombok.Getter; // <--- 1. Lombok Getter 임포트
import java.time.LocalDateTime;

@Entity
@Getter // <--- 2. 이 어노테이션을 클래스 위에 추가!
@Table(name = "중고거래") 
public class UsedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id") 
    private Long userId; // (@Getter가 getUserId()를 만들어 줌)

    @Column(name = "title")
    private String title;

    @Column(name = "content") // (@Getter가 getContent()를 만들어 줌)
    private String content;

    @Column(name = "status")
    private String status;

    @Column(name = "location") 
    private String location; 

    @Column(name = "price")
    private Integer price;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // ... (is_images, method 등 나머지 필드들)
    
    // (JPA는 기본 생성자가 필요합니다)
    protected UsedItem() {} 
    
    // (Lombok @Getter가 모든 필드의 Getter를 자동으로 만들어주므로
    //  수동으로 만든 public Long getId()... 등은 모두 삭제해도 됩니다.)
}
*/
// domain/UsedItem.java
package yongin.Yongnuri._Campus.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column; // <--- 1. 추가
import jakarta.persistence.Entity; // <--- 2. 추가
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType; // <--- 3. 추가
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder // <--- DTO -> Entity 변환 시 빌더 패턴 사용
@NoArgsConstructor // <--- JPA와 빌더를 위한 기본 생성자
@AllArgsConstructor // <--- 빌더를 위한 모든 필드 생성자
@Table(name = "중고거래") 
public class UsedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id") 
    private Long userId; 

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "is_images") // <--- DTO에서 사용
    private Boolean isImages;

    @Column(name = "method") // <--- DTO에서 사용
    private String method;

    @Column(name = "status")
    private String status;

    @Column(name = "location") 
    private String location; 

    @Column(name = "price")
    private Integer price;

    @Column(name = "created_at") 
    private LocalDateTime createdAt;
    
    // (JPA 기본 생성자 외의 다른 생성자/메서드는 빌더가 처리하므로 삭제)
}