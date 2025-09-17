package yongin.Yongnuri._Campus.service.specification;

import org.springframework.data.jpa.domain.Specification;
import java.util.List;

public class BoardSpecification {

    /*
      차단한 유저 게시글 제외 
     */
    public static <T> Specification<T> notBlocked(List<Long> blockedUserIds) {
        return (root, query, cb) -> {
            if (blockedUserIds == null || blockedUserIds.isEmpty()) {
                return cb.conjunction(); 
            }
            return cb.not(root.get("userId").in(blockedUserIds));
        };
    }

    /*
      장소 필터 
     */
    public static <T> Specification<T> hasLocation(String location) {
        return (root, query, cb) -> {
            return cb.equal(root.get("location"), location);
        };
    }
}