package yongin.Yongnuri._Campus.dto.chat;

import lombok.*;
import yongin.Yongnuri._Campus.domain.User;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatAdminRes {
    private String text;
    private User user;
}
