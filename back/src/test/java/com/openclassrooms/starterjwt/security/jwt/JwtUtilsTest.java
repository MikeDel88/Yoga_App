package com.openclassrooms.starterjwt.security.jwt;

import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    private static final String JWT_SECRET = "cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e";
    private static final int JWT_EXPIRATION_MS = 86400000;

    @InjectMocks
    private JwtUtils jwtUtils;

    private UserDetails userDetails;
    private String token;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", JWT_SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", JWT_EXPIRATION_MS);
        userDetails = UserDetailsImpl.builder()
                .id(1L)
                .username("test@test.com")
                .firstName("test")
                .lastName("test")
                .admin(false)
                .password("encoded-password")
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        token = jwtUtils.generateJwtToken(authentication);
    }

    @Test
    void getUserNameFromJwtToken_success() {

        String userName = jwtUtils.getUserNameFromJwtToken(token);
        assertThat(userName).isEqualTo(userDetails.getUsername());
    }

    @Test
    void validateJwtToken_success() {
        assertThat(jwtUtils.validateJwtToken(token)).isTrue();
    }

    @Test
    void validateJwtToken_malformedToken_returnsFalse() {
        assertThat(jwtUtils.validateJwtToken("toto")).isFalse();
    }

    @Test
    void validateJwtToken_invalidSignature_returnsFalse() {
        String otherSecretToken = Jwts.builder()
                .subject("test@test.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(SignatureAlgorithm.HS512, "1".repeat(100))
                .compact();

        assertThat(jwtUtils.validateJwtToken(otherSecretToken)).isFalse();
    }

    @Test
    void validateJwtToken_expiredToken_returnsFalse() {
        String expiredToken = Jwts.builder()
                .subject("test@test.com")
                .issuedAt(new Date(System.currentTimeMillis() - 20000))
                .expiration(new Date(System.currentTimeMillis() - 10000))
                .signWith(SignatureAlgorithm.HS512, JWT_SECRET)
                .compact();

        assertThat(jwtUtils.validateJwtToken(expiredToken)).isFalse();
    }

    @Test
    void validateJwtToken_unsupportedToken_returnsFalse() {
        String unsignedToken = Jwts.builder()
                .subject("test@test.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .compact();

        assertThat(jwtUtils.validateJwtToken(unsignedToken)).isFalse();
    }

    @Test
    void validateJwtToken_emptyToken_returnsFalse() {
        assertThat(jwtUtils.validateJwtToken("")).isFalse();
    }

}
