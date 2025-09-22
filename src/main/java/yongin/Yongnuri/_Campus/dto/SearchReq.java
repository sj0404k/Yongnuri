package yongin.Yongnuri._Campus.dto;

import lombok.Getter;
import lombok.Setter;
import yongin.Yongnuri._Campus.domain.Reports;

public class SearchReq {
    @Setter
    @Getter
    public static class SearchDto {
        private String query;
    }

}
