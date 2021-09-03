package com.kakao.enterprise.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakao.enterprise.dto.MemberAuthentication;
import com.kakao.enterprise.dto.MemberSignUpRequest;
import com.kakao.enterprise.dto.OAuth2AccessToken;
import com.kakao.enterprise.entity.Member;
import com.kakao.enterprise.repository.MemberRepository;
import com.kakao.enterprise.service.OAuth2RestService;
import com.kakao.enterprise.util.SessionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MemberRestControllerTest {

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

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private OAuth2RestService oAuth2RestService;

    private MockHttpSession session;

    private Member member;

    @BeforeEach
    void setUp() {

        memberRepository.deleteAll();

        member = new Member(ID, EMAIL, AGE_RANGE, NICKNAME, PROFILE_IMAGE_URL);
        memberRepository.saveAll(
                Arrays.asList(
                        member,
                        new Member(2L, "b@b.com", "40~49", "b", "http://b"),
                        new Member(3L, "c@c.com", "50~59", "c", "http://c"),
                        new Member(4L, "d@ab.com", "20~29", "d", "http://d"),
                        new Member(5L, "e@ab.com", "20~29", "e", "http://e"),
                        new Member(6L, "f@ab.com", "20~29", "f", "http://f"),
                        new Member(7L, "g@ab.com", "20~29", "g", "http://g"),
                        new Member(8L, "h@ab.com", "20~29", "h", "http://h"),
                        new Member(9L, "i@ab.com", "20~29", "i", "http://i"),
                        new Member(10L, "j@ab.com", "20~29", "j", "http://j"),
                        new Member(11L, "hsk741@gmail.com", "40~49", "hsk741", "http://hsk741")
                )
        );

//      로그인 되었다고 가정하고 Session객체 생성
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

    @Test
    @DisplayName("개인 회원 ID로 정보 조회")
    void getMemberById() throws Exception {

        mapper = new ObjectMapper();

        final MvcResult mvcResult = this.mockMvc.perform(get("/api/member/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN)
                        .session(session))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(ID))
                .andReturn();

        final Member responseMember = mapper.readValue(mvcResult.getResponse().getContentAsString(), Member.class);

        assertThat(responseMember.getId()).isEqualTo(member.getId());
        assertThat(responseMember.getEmail()).isEqualTo(member.getEmail());
        assertThat(responseMember.getNickname()).isEqualTo(member.getNickname());
        assertThat(responseMember.getAgeRange()).isEqualTo(member.getAgeRange());
        assertThat(responseMember.getProfileImageUrl()).isEqualTo(member.getProfileImageUrl());
    }

    @Test
    @DisplayName("전체 회원 조회")
    void getAllMembers() throws Exception {

        this.mockMvc.perform(get("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN)
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> result.getRequest().getSession(false))
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    @DisplayName("전체 회원에 대한 2페이지 4개 목록을 연령대 내림차순 및 이메일 오름차순으로 정렬하여 조회")
    void getAllMembersSortByAgeRangeDescAndEmailDesc() throws Exception {

        this.mockMvc.perform(get("/api/members")
                        .param("sort", "ageRange,desc")
                        .param("sort", "email,asc")
                        .param("page", "1")
                        .param("size", "4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN)
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].email").value("d@ab.com"))
                .andExpect(jsonPath("$.content[1].email").value("e@ab.com"));
    }

    @Test
    @DisplayName("전체 회원에 대한 1페이지 3개 목록을 연령대 내림차순으로 정렬하여 조회")
    void getAgeRangeSearchMembersOnePageByAgeRangeDesc() throws Exception {

        this.mockMvc.perform(get("/api/members")
                        .param("sort", "ageRange,desc")
                        .param("page", "0")
                        .param("size", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN)
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].nickname").value("c"));
    }

    @Test
    @DisplayName("전체 회원에 대한 2페이지 3개 목록을 연령대 내림차순으로 정렬하여 조회")
    void getAgeRangeSearchMembersTwoPageByAgeRangeDesc() throws Exception {

        this.mockMvc.perform(get("/api/members")
                        .param("sort", "ageRange,desc")
                        .param("page", "1")
                        .param("size", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN)
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].nickname").value("a"));
    }

    @Test
    @DisplayName("이메일이 ab.com으로 끝나는 회원에 대한 1페이지 3개 목록을 이메일 내림차순으로 정렬하여 조회")
    void getEmailSearchMembersOnePageByEmailDesc() throws Exception {

        this.mockMvc.perform(get("/api/members")
                        .param("sort", "email,desc")
                        .param("email", "ab.com")
                        .param("page", "0")
                        .param("size", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN)
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].email").value("j@ab.com"));
    }

    @Test
    @DisplayName("이메일이 ab.com으로 끝나는 회원에 대한 2페이지 3개 목록을 이메일 내림차순으로 정렬하여 조회")
    void getEmailSearchMembersTwoPageByEmailDesc() throws Exception {

        this.mockMvc.perform(get("/api/members")
                        .param("sort", "email,desc")
                        .param("email", "ab.com")
                        .param("page", "1")
                        .param("size", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN)
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].email").value("g@ab.com"));
    }

    @Test
    @DisplayName("이메일이 ab.com으로 끝나고 연령대가 20대인 회원에 대한 1페이지 3개 목록을 이메일 내림차순으로 정렬하여 조회")
    void getEmailAndAgeRangeSearchMembersOnePageByEmailDesc() throws Exception {

        this.mockMvc.perform(get("/api/members")
                        .param("sort", "email,desc")
                        .param("email", "ab.com")
                        .param("ageRange", "20~29")
                        .param("page", "0")
                        .param("size", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN)
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].email").value("j@ab.com"));
    }

    @Test
    @DisplayName("이메일이 ab.com으로 끝나고 연령대가 20대인 회원에 대한 2페이지 3개 목록을 이메일 내림차순으로 정렬하여 조회")
    void getEmailAndAgeRangeSearchMembersTwoPageByEmailDesc() throws Exception {

        this.mockMvc.perform(get("/api/members")
                        .param("sort", "email,desc")
                        .param("email", "ab.com")
                        .param("ageRange", "20~29")
                        .param("page", "1")
                        .param("size", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN)
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].email").value("g@ab.com"));
    }

    @Test
    @DisplayName("개인 회원 정보 등록 => 카카오 로그인시 조회한 프로필 정보를 등록")
    void signUp() throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        this.mockMvc.perform(post("/api/member/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN)
                        .session(session)
                        .content(mapper.writeValueAsString(MemberAuthentication.builder().email(EMAIL)
                                .ageRange(AGE_RANGE)
                                .nickname(NICKNAME)
                                .profileImageUrl(PROFILE_IMAGE_URL)
                                .build())))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nickname").value(NICKNAME));
    }

    @Test
    @DisplayName("개인 회원 ID로 정보 수정")
    void updateMemberNickname() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        final String updatedNickname = "updatedNickname";

        this.mockMvc.perform(put("/api/member/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN)
                        .session(session)
                        .content(mapper.writeValueAsString(new MemberSignUpRequest(updatedNickname))))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nickname").value(updatedNickname));
    }

    @Test
    @DisplayName("개인 회원 ID로 정보 삭제")
    void deleteMemberById() throws Exception {

        doNothing().when(oAuth2RestService).unlink(any(OAuth2AccessToken.class));

        this.mockMvc.perform(delete("/api/member/{id}", ID)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(ID));

        verify(oAuth2RestService, times(1)).unlink(any(OAuth2AccessToken.class));
    }

    @Test
    @DisplayName("실제 카카오 api서버가 아닌 mock객체의 메소드 호출을 통해 access token이 최초 만료되었을 경우 refresh token을 통해 재발급받은 후 전체 회원 조회")
    void reissueAccessTokenUsingRefreshToken() throws Exception {

        final OAuth2AccessToken accessTokenFromSession = SessionUtils.getAccessTokenFromSession(session);
        final LocalDateTime now = LocalDateTime.now();

//      access_token과 refresh_token만료시간을 현재시간보다 이전으로 설정해 만료된 토큰으로 설정
        session.setAttribute("oauthToken", accessTokenFromSession.toBuilder()
                .expiresAt(now.minusSeconds(2 * EXPIRES_IN)).build());

        final String updatedAccessToken = "Ia1t4bMGWoJQJFfJzaPjmqWSzex7AonMs6fyMwopyNkAAAF7obWqhg";
        when(oAuth2RestService.reissueAccessTokenFromTokenEndpoint(SessionUtils.getAccessTokenFromSession(session)))
                .thenReturn(OAuth2AccessToken.builder()
                        .accessToken(updatedAccessToken)
                        .expiresIn(EXPIRES_IN)
                        .expiresAt(LocalDateTime.now().plusSeconds(EXPIRES_IN))
                        .tokenType("Bearer")
                        .build());

        this.mockMvc.perform(get("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN)
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> assertThat(SessionUtils.getAccessTokenFromSession(Objects.requireNonNull(result.getRequest().getSession(false))).getAccessToken()).isEqualTo(updatedAccessToken))  // refresh
                .andExpect(result -> assertThat(SessionUtils.getAccessTokenFromSession(Objects.requireNonNull(result.getRequest().getSession(false))).getRefreshToken()).isNullOrEmpty())
                .andExpect(result -> assertThat(SessionUtils.getAccessTokenFromSession(Objects.requireNonNull(result.getRequest().getSession(false))).getRefreshTokenExpiresIn()).isEqualTo(0))
                .andExpect(jsonPath("$.content[0].id").value(1));

        verify(oAuth2RestService, times(1)).reissueAccessTokenFromTokenEndpoint(any(OAuth2AccessToken.class));
    }
}