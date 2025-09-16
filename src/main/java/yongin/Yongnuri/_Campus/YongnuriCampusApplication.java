package yongin.Yongnuri._Campus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan; 
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@SpringBootApplication
@ComponentScan(basePackages = "yongin.Yongnuri._Campus") 

@EnableJpaRepositories(basePackages = "yongin.Yongnuri._Campus.repository") 
public class YongnuriCampusApplication {

    public static void main(String[] args) {
        SpringApplication.run(YongnuriCampusApplication.class, args);
    }

	
	@Bean
    public JavaMailSender javaMailSender() {
        // 그냥 빈 껍데기 JavaMailSender 객체를 Bean으로 등록해서 
        // MailService의 오류를 임시로 막습니다.
        return new JavaMailSenderImpl(); 
    }
}