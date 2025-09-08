//package yongin.Yongnuri._Campus.security;
//
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.security.Keys;
//
//import java.security.Key;
//import java.util.Base64;
//
//public class JwtSecretGenerator {
//    public static void main(String[] args) {
//        // HS256 알고리즘에 맞는 랜덤 키 생성
//        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
//
//        // Base64 문자열로 변환
//        String secret = Base64.getEncoder().encodeToString(key.getEncoded());
//        System.out.println("Generated JWT Secret:");
//        System.out.println(secret);
//    }
//}