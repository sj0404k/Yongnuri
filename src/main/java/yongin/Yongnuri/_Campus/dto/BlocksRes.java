package yongin.Yongnuri._Campus.dto;

import lombok.*;
import yongin.Yongnuri._Campus.domain.Block;
import yongin.Yongnuri._Campus.domain.BookMarks;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlocksRes {
    private Long blockedId;
    private String blockedNickName;
}
