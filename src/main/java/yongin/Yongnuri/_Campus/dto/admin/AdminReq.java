package yongin.Yongnuri._Campus.dto.admin;

import lombok.Getter;
import lombok.Setter;
import yongin.Yongnuri._Campus.domain.Enum;
public class AdminReq {
    @Setter
    @Getter
    public static class reportProcessReq {
        private Long id;
//        private Long userId;
        private Enum.ReportStatus reportStatus;
    }
    @Setter
    @Getter
    public static class reportProcessUserReq {
//        private Long id;
        private Long userId;
        private Enum.ReportStatus reportStatus;
    }
}
