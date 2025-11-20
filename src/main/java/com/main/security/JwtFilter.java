package com.main.security;

import org.springframework.web.filter.OncePerRequestFilter;

import com.main.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
@Component
public class JwtFilter extends OncePerRequestFilter {


	@Autowired
	private JwtUtils jwtUtils;
	
	@Lazy @Autowired
	private UserService userService;
	
	@Autowired
	private TokenBlackList tokenBlacklist;
	
	@Autowired
	private CookieUtil cookieUtil;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		String header = request.getHeader("Authorization");
		String token = null;

		// Require explicit Authorization: Bearer <token> header for authentication.
		// This ensures that, for tools like Swagger UI, APIs only work after the
		// user/admin logs in and pastes the JWT into the "Authorize" dialog.
		if (header != null && header.startsWith("Bearer ")) {
			token = header.substring(7);
		}

		// NOTE: We intentionally no longer fall back to the AUTH_TOKEN cookie here.
		// If you later need cookie-based auth for the browser app, you can re-enable
		// it, but then Swagger UI calls will appear authenticated even without using
		// the "Authorize" button (because the browser automatically sends cookies).

		if (token != null && jwtUtils.validateToken(token) && !tokenBlacklist.isBlacklisted(token)) {
            String email = jwtUtils.extractUsername(token);
            List<String> roles = jwtUtils.extractRoles(token);
            UserDetails userDetails = userService.loadUserByEmail(email);

            List<SimpleGrantedAuthority> authorities = roles.stream()
                                                            .map(SimpleGrantedAuthority::new)
                                                            .collect(Collectors.toList());

			UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null,
					authorities);
			SecurityContextHolder.getContext().setAuthentication(auth);
		}

		chain.doFilter(request, response);
	}
}
