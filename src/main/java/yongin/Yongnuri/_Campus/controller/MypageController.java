//package yongin.Yongnuri._Campus.controller;
//
//import lombok.AllArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//import yongin.Yongnuri._Campus.domain.User;
//import yongin.Yongnuri._Campus.dto.AuthReq;
//import yongin.Yongnuri._Campus.dto.MypageRes;
//import yongin.Yongnuri._Campus.servise.MailService;
//import yongin.Yongnuri._Campus.servise.MypageService;
//
//@RestController
//@RequestMapping("/mypage")
//@AllArgsConstructor
//public class MypageController {
//    private final MypageService mypageService;
//
//    @GetMapping
//    public ResponseEntity<String> join(@AuthenticationPrincipal User  user) {
//
//        System.out.println(user);
//        return ResponseEntity.ok("mypage 접속");
//    }
//
//
//
//}
