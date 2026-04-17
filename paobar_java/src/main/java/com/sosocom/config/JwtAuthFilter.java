package com.sosocom.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosocom.common.RequestContext;
import com.sosocom.common.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 校验 JWT，将当前用户 ID 放入 RequestContext。
 * 白名单路径不校验，其余需携带有效 token。
 */
// 不加 @Component，由 WebFilterConfig 的 Bean 方法创建，避免双重注册
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final List<String> WHITELIST = List.of(
            "/api/auth/login",
            "/api/auth/register"
    );

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        RequestContext.clear();
        try {
            // 放行 CORS 预检请求（OPTIONS），否则浏览器跨域会失败
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                filterChain.doFilter(request, response);
                return;
            }
            String path = request.getRequestURI();
            if (WHITELIST.stream().anyMatch(path::startsWith)) {
                filterChain.doFilter(request, response);
                return;
            }
            String auth = request.getHeader("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                writeUnauthorized(response);
                return;
            }
            String token = auth.substring(7).trim();
            Long userId = jwtUtil.parseUserId(token);
            if (userId == null) {
                writeUnauthorized(response);
                return;
            }
            RequestContext.setUserId(userId);
            filterChain.doFilter(request, response);
        } finally {
            RequestContext.clear();
        }
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Result<?> result = Result.error(401, "未登录或登录已过期");
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }
}
