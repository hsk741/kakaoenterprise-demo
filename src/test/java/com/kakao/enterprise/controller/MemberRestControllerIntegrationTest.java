package com.kakao.enterprise.controller;

import com.kakao.enterprise.dto.OAuth2AccessToken;
import com.kakao.enterprise.entity.Member;
import com.kakao.enterprise.repository.MemberRepository;
import com.kakao.enterprise.util.SessionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MemberRestControllerIntegrationTest {

    public static final String EMAIL = "a@a.com";
    public static final String AGE_RANGE = "30~39";
    public static final String NICKNAME = "a";
    public static final String PROFILE_IMAGE_URL = "http://a";
    public static final long ID = 1;
    public static final String ACCESS_TOKEN = "6KeSl-qerTtAfQ9Ol7vzQ8dFOT8l4LbtjA7q2go9dNoAAAF7obSfaQ";
    public static final String REFRESH_TOKEN = "4xw301Jq5_MfhjkhuqzpJgkOrYqM7yAVPhHffwo9dNoAAAF7obSfaA";
    public static final int EXPIRES_IN = 21599;
    public static final int REFRESH_TOKEN_EXPIRES_IN = 5183999;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {

        memberRepository.deleteAll();
        memberRepository.save(new Member(ID, EMAIL, AGE_RANGE, NICKNAME, PROFILE_IMAGE_URL));

//        로그인 되었다고 가정하고 Session객체 생성
        session = new MockHttpSession();

        final LocalDateTime now = LocalDateTime.now();
        final OAuth2AccessToken oAuth2AccessToken = OAuth2AccessToken.builder()
                .accessToken(ACCESS_TOKEN)
                .refreshToken(REFRESH_TOKEN)
                .expiresIn(EXPIRES_IN)
                .expiresAt(now.plusSeconds(EXPIRES_IN))
                .refreshTokenExpiresIn(REFRESH_TOKEN_EXPIRES_IN)
                .refreshTokenExpiresAt(now.plusSeconds(REFRESH_TOKEN_EXPIRES_IN))
                .tokenType("Bearer")
                .build();

        session.setAttribute("oauthToken", oAuth2AccessToken);
    }

    @Disabled
    @Test
    @DisplayName("실제 카카오 api서버와 연동하여 access token이 최초 만료되었을 경우 refresh token을 통해 재발급받은 후 전체 회원 조회")
    void reissueAccessTokenUsingRefreshToken() throws Exception {

        final OAuth2AccessToken accessTokenFromSession = SessionUtils.getAccessTokenFromSession(session);
        final LocalDateTime now = LocalDateTime.now();

//      실제 카카오 계정으로 로그인하여 받은 토큰 지정
        final String realAccessToken = "6KeSl-qerTtAfQ9Ol7vzQ8dFOT8l4LbtjA7q2go9dNoAAAF7obSfaQ";
        final String realRefreshToken = "4xw301Jq5_MfhjkhuqzpJgkOrYqM7yAVPhHffwo9dNoAAAF7obSfaA";

//      access_token과 refresh_token만료시간을 현재시간보다 이전으로 설정해 만료된 토큰으로 설정
        session.setAttribute("oauthToken", accessTokenFromSession.toBuilder()
                .refreshTokenExpiresAt(now.minusSeconds(REFRESH_TOKEN_EXPIRES_IN))
                .accessToken(realAccessToken)
                .refreshToken(realRefreshToken)
                .expiresAt(now.minusSeconds(EXPIRES_IN)).build());

        this.mockMvc.perform(get("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + realAccessToken)
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> assertThat(SessionUtils.getAccessTokenFromSession(Objects.requireNonNull(result.getRequest().getSession(false))).getAccessToken()).isNotEqualTo(realAccessToken))  // refresh
                .andExpect(result -> assertThat(SessionUtils.getAccessTokenFromSession(Objects.requireNonNull(result.getRequest().getSession(false))).getRefreshToken()).isNullOrEmpty())
                .andExpect(result -> assertThat(SessionUtils.getAccessTokenFromSession(Objects.requireNonNull(result.getRequest().getSession(false))).getRefreshTokenExpiresIn()).isEqualTo(0))
                .andExpect(jsonPath("$.content[0].id").value(1));
    }
}