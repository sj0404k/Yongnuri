package yongin.Yongnuri._Campus.dto;

import lombok.*;



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
