package yongin.Yongnuri._Campus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Builder
@AllArgsConstructor
public class SearchRes {

        private Long searchId;
        private String query;
}
