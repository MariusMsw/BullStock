package com.mariusmihai.banchelors.BullStock.filters;

import com.mariusmihai.banchelors.BullStock.security.AppUserDetailsService;
import com.mariusmihai.banchelors.BullStock.security.UserPrincipal;
import com.mariusmihai.banchelors.BullStock.services.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final AppUserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Autowired
    public JwtRequestFilter(
            AppUserDetailsService userDetailsService,
            JwtService jwtService
    ) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = httpServletRequest.getHeader("Authorization");

        try {

            String email = null;
            String jwt = null;

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.replace("Bearer ", "");
                email = jwtService.extractClaim(jwt, Claims::getSubject);
            }

            if (email == null || SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(httpServletRequest, httpServletResponse);
                return;
            }

            UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserByUsername(email);

            if (jwtService.validateToken(jwt, userPrincipal.getUser())) {
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                        userPrincipal, null, userPrincipal.getAuthorities());
                token.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                SecurityContextHolder.getContext().setAuthentication(token);
            }

        } catch (ExpiredJwtException ex) {
            String requestUrl = httpServletRequest.getRequestURL().toString();
            if (requestUrl.contains("refreshToken")) {
                allowForRefreshToken(ex, httpServletRequest);
            } else {
                httpServletRequest.setAttribute("exception", ex);
            }
        } catch (BadCredentialsException ex) {
            httpServletRequest.setAttribute("exception", ex);
        } catch (Exception ex) {
            System.out.println(ex);
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    private void allowForRefreshToken(ExpiredJwtException ex, HttpServletRequest httpServletRequest) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                null, null, null);
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        httpServletRequest.setAttribute("claims", ex.getClaims());
    }
}
