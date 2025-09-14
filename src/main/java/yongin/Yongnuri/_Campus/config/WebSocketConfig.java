package yongin.Yongnuri._Campus.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // WebSocket 메시지 브로커를 활성화합니다.
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 클라이언트가 WebSocket 서버에 연결할 엔드포인트를 등록합니다.
     * SockJS를 사용하여 WebSocket을 지원하지 않는 브라우저를 위한 Fallback 옵션을 제공합니다.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // '/ws-stomp' 엔드포인트로 웹소켓 연결을 허용합니다.
        // setAllowedOrigins("*")는 모든 도메인에서 접속을 허용하지만,
        // 실제 운영 환경에서는 보안을 위해 특정 도메인으로 제한하는 것이 좋습니다.
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*") // 모든 오리진 허용 (개발 시 편의, 운영 시에는 특정 도메인으로 변경 권장)
                .withSockJS(); // SockJS 지원을 추가합니다.
    }

    /**
     * 메시지 브로커를 구성합니다.
     * 메시지를 한 클라이언트에서 다른 클라이언트로 라우팅하는 방법을 정의합니다.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지를 구독하는 요청 URL (subscribe)
        registry.enableSimpleBroker("/sub");
        // 메시지를 발행하는 요청 URL (publish)
        registry.setApplicationDestinationPrefixes("/pub");
    }
}