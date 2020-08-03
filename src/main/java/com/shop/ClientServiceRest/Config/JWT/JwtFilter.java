package com.shop.ClientServiceRest.Config.JWT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Service
public class JwtFilter extends GenericFilterBean {
    private final static Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private JwtUtils jwtUtils;

    public JwtFilter(@Autowired JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        logger.info("DoFilter method called");
        String token = jwtUtils.resolveToken((HttpServletRequest) servletRequest);

        if (token != null && jwtUtils.validateToken(token)) {
            logger.info("Token is not null and token is valid");
            Authentication authentication = jwtUtils.getAuthentication(token);

            if (authentication != null) {
                logger.debug("Authentication is not null");
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                logger.warn("Failed to get authentication");
                SecurityContextHolder.getContext().setAuthentication(null);
            }
        } else {
            logger.warn("Jwt token is null or invalid");
            SecurityContextHolder.getContext().setAuthentication(null);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
