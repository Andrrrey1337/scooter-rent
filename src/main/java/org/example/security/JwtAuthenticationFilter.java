package org.example.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, 
            HttpServletResponse response, 
            FilterChain filterChain) throws ServletException, IOException {
        try {
            final String authorizationHeader = request.getHeader("Authorization");

            if (isNull(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
                log.info("Заголовок Authorization отсутствует или имеет неверный формат, пропуск JWT фильтра");
                return;
            }

            String jwt = authorizationHeader.substring(7);
            String username = jwtService.extractUsername(jwt);

            // если пользователя нет в контексте (не добавили туда в предыдущих фильтрах)
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (isNotBlank(username) && isNull(auth)) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // создаем объект авторизации
                if (userDetails.isEnabled()) { // если пользователь не забанен
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    // добавляем детали (IP адрес, сессию) к нашему токену
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("Пользователь {} успешно аутентифицирован", username);
                } else {
                    log.info("Пользователь {} заблокирован, аутентификация пропущена", username);
                }
            } else if (isBlank(username)) {
                log.info("Не удалось извлечь имя пользователя из JWT токена");
            }
        } catch (Exception e) {
            log.error("Не удалось установить аутентификацию пользователя в фильтре: {}", e.getMessage());
        } finally {
            filterChain.doFilter(request, response);
        }
    }
}
