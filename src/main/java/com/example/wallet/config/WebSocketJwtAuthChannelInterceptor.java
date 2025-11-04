package com.example.wallet.config;

import com.example.wallet.security.JwtUtil;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class WebSocketJwtAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    public WebSocketJwtAuthChannelInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Leer Authorization de los headers del frame CONNECT
            String authHeader = getFirstHeader(accessor, "Authorization")
                    .or(() -> getFirstHeader(accessor, "authorization"))
                    .orElse(null);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtUtil.validateToken(token)) {
                    String email = jwtUtil.extractEmail(token);
                    String role = jwtUtil.extractRol(token);
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                    accessor.setUser(auth);
                }
            }
        }
        return message;
    }

    private Optional<String> getFirstHeader(StompHeaderAccessor accessor, String name) {
        List<String> vals = accessor.getNativeHeader(name);
        if (vals != null && !vals.isEmpty()) {
            return Optional.ofNullable(vals.get(0));
        }
        return Optional.empty();
    }
}
