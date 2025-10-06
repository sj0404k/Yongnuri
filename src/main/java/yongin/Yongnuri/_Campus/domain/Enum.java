package yongin.Yongnuri._Campus.domain;

public class Enum {

    public enum UserRole {
        USER, ADMIN
    }
    public static enum UsedItemStatus {
        SELLING,    // 판매중
        RESERVED,   // 예약중
        SOLD,       // 판매완료
        DELETED     // 삭제됨
    }
    public enum LostItemPurpose {
        LOST, FOUND
    }

    public enum LostItemStatus {
        REPORTED, // 분실물 신고/게시됨
        RETURNED, // 주인에게 반환됨
        DELETED   // 삭제됨
    }
    public enum GroupBuyStatus {
        RECRUITING, // 모집중
        COMPLETED, //모집완료
        DELETED //삭제됨
    }
    public enum NoticeStatus {
        RECRUITING, // 진행중
        COMPLETED,  // 마감
        DELETED     // 삭제됨
    }

    public enum authStatus {
        ACTIVE,          // 정상 가입 상태
        WITHDRAWN,       // 탈퇴한 사용자
        SUSPENDED;       // 정지된 사용자
    }

    public static enum ReportType {
        SPAM,                   // 도배
        PROMOTION_ADVERTISING, // 홍보_광고행위
        OBSCENE_CONTENT,        // 음란성게시물
        DEFAMATION_HATE,        // 상대방비방및혐오
        IMPERSONATION_FAKE_INFO,// 사칭및거짓정보
        ETC                     // 기타
    }

    public static enum ChatType {
        ALL,        // 전체
        USED_ITEM,  // 중고
        LOST_ITEM,  // 분실
        GROUP_BUY   // 공동구매
    }

    public static enum ReportStatus {
        PENDING,    // 처리 대기
        REJECTED,   // 처리 반려
        APPROVED    // 처리 승인
    }
    public static enum AppointmentStatus {
        SCHEDULED,  // 약속됨
        COMPLETED,  // 거래 완료
        CANCELED    // 약속 취소
    }

}

