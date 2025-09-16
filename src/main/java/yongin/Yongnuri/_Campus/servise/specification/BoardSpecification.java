// servise/specification/BoardSpecification.java
package yongin.Yongnuri._Campus.servise.specification;

import org.springframework.data.jpa.domain.Specification;
import java.util.List;

public class BoardSpecification {

    /**
     * 공통: 차단한 유저 게시글 제외 (필드명 'userId' 가정)
     */
    public static <T> Specification<T> notBlocked(List<Long> blockedUserIds) {
        return (root, query, cb) -> {
            if (blockedUserIds == null || blockedUserIds.isEmpty()) {
                return cb.conjunction(); 
            }
            return cb.not(root.get("userId").in(blockedUserIds));
        };
    }

    /**
     * 공통: 특정 장소 필터 (필드명 'location' 가정)
     */
    public static <T> Specification<T> hasLocation(String location) {
        return (root, query, cb) -> {
            return cb.equal(root.get("location"), location);
        };
    }
}