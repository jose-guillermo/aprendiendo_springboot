package com.jose.curso.springboot.app.springboot_crud.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;

import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    @Value("${jwt.access.secret}")
    private String secretAccess;

     @Value("${jwt.refresh.secret}")
    private String secretRefresh;

    private SecretKey SECRET_ACCESS_KEY;
    private SecretKey SECRET_REFRESH_KEY;
    private String COOKIE_REFRESH_TOKEN = "refreshToken";
    private String COOKIE_ACCESS_TOKEN = "accessToken";
    public static final String CONTENT_TYPE = "application/json";

    @PostConstruct
    public void init(){
        SECRET_ACCESS_KEY = Keys.hmacShaKeyFor(secretAccess.getBytes());
        SECRET_REFRESH_KEY = Keys.hmacShaKeyFor(secretRefresh.getBytes());
    }

    public String createAccessToken(String username, List<String> roles) {
        Claims claims = Jwts.claims()
            .add("authorities", roles)
            .add("username", username)
            .build();

        return Jwts.builder()
            .subject(username)
            .claims(claims)
            .expiration(new Date(System.currentTimeMillis() + (3600000 * 5)))
            .issuedAt(new Date())
            .signWith(SECRET_ACCESS_KEY)
            .compact();
    }

    public String createRefreshToken(String username) {
        return Jwts.builder()
            .subject(username)
            .expiration(new Date(System.currentTimeMillis() + (3600000 * 24 * 7)))
            .issuedAt(new Date())
            .signWith(SECRET_REFRESH_KEY)
            .compact();
    }

    public ResponseCookie createAccessCookie(String accessToken) {
        return ResponseCookie.from(COOKIE_ACCESS_TOKEN, accessToken)
            .httpOnly(true)
            .secure(false)
            .secure(true)
            .path("/")
            .maxAge(3600)
            .sameSite("None")
            .domain("https://aprendiendo-springboot.onrender.com")
            .build();
    }

    public ResponseCookie createRefreshCookie(String refreshToken) {
        return ResponseCookie.from(COOKIE_REFRESH_TOKEN, refreshToken)
            .httpOnly(true)
            .secure(false)
            .secure(true)
            .path("/")
            .maxAge(3600)
            .sameSite("None")
            .domain("https://aprendiendo-springboot.onrender.com")
            .build();
    }

    public String getAccessToken(Cookie[] cookies) {
        String token = null;
        if(cookies != null) {
            for (var cookie : cookies){
                if(COOKIE_ACCESS_TOKEN.equals(cookie.getName())){
                    token = cookie.getValue();
                    break;
                }
            }
        }
        return token;
    }

    public String getRefreshToken(Cookie[] cookies) {
        String token = null;
        if(cookies != null) {
            for (var cookie : cookies){
                if(COOKIE_REFRESH_TOKEN.equals(cookie.getName())){
                    token = cookie.getValue();
                    break;
                }
            }
        }
        return token;
    }

    public Claims verifyAccessToken(String token) {
        return Jwts.parser().verifyWith(SECRET_ACCESS_KEY).build().parseSignedClaims(token).getPayload();
    }

    public Claims verifyRefreshToken(String token) throws IllegalArgumentException{
        return Jwts.parser().verifyWith(SECRET_REFRESH_KEY).build().parseSignedClaims(token).getPayload();
    }
}
