package yongin.Yongnuri._Campus.admin;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import yongin.Yongnuri._Campus.domain.User;
import yongin.Yongnuri._Campus.domain.Enum;
@Configuration
@ConfigurationProperties(prefix = "admin")
public class AdminConfig {

    private String email;
    private String password;
    private String nickName;
    private Enum.UserRole role;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }

    public Enum.UserRole getRole() { return role; }
    public void setRole(Enum.UserRole role) { this.role = role; }
}
