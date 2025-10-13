package yongin.Yongnuri._Campus.dto;

import lombok.*;
import yongin.Yongnuri._Campus.domain.Block;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlocksRes {
    private Long id;
    private Long blockedId;
    private String blockedNickName;
}
