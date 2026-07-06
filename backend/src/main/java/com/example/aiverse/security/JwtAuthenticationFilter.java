package com.example.aiverse.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.aiverse.common.error.ApplicationException;
import com.example.aiverse.util.BearerTokenExtractor;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token;
        try {
            token = BearerTokenExtractor.extract(request.getHeader("Authorization"));
        } catch (ApplicationException exception) {
            // 헤더가 없거나 형식이 틀린 경우: 인가 단계(permitAll 여부)에 판단을 맡긴다.
            filterChain.doFilter(request, response);
            return;
        }

        Long userId;
        try {
            userId = jwtTokenProvider.parseUserId(token);
        } catch (ApplicationException exception) {
            authenticationEntryPoint.commence(
                    request,
                    response,
                    new InvalidTokenAuthenticationException("유효하지 않은 토큰입니다.")
            );
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
