package com.kakao.enterprise.controller;

import com.kakao.enterprise.context.RequestContextHolder;
import com.kakao.enterprise.dto.MemberAuthentication;
import com.kakao.enterprise.dto.MemberSearchConditionRequest;
import com.kakao.enterprise.dto.MemberSignUpRequest;
import com.kakao.enterprise.dto.OAuth2AccessToken;
import com.kakao.enterprise.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberRestController {

    private final MemberService memberService;

    @PostMapping("/member/{id}")
    public MemberAuthentication signUpWithAdditionalInfo(@PathVariable long id, @RequestBody MemberAuthentication memberAuthentication) {

        Objects.requireNonNull(memberAuthentication, "No memberAuthentication parameter");

        return memberService.signUp(
                OAuth2AccessToken.builder().accessToken(RequestContextHolder.get().getToken()).build(),
                id,
                memberAuthentication
        );
    }

    @PutMapping("/member/{id}")
    public MemberAuthentication updateMemberNickname(@PathVariable long id, @RequestBody MemberSignUpRequest memberSignUpRequest) {

        Objects.requireNonNull(memberSignUpRequest, "No memberSignUpRequest parameter");
        Objects.requireNonNull(memberSignUpRequest.nickname(), "No nickname");

        return memberService.updateMemberNickname(id, memberSignUpRequest);
    }

    @DeleteMapping("/member/{id}")
    public MemberAuthentication deleteMemberById(@PathVariable long id) {

        final OAuth2AccessToken oAuth2AccessToken = OAuth2AccessToken.builder().accessToken(RequestContextHolder.get().getToken()).build();

        return memberService.deleteMemberById(id, oAuth2AccessToken);
    }

    @GetMapping("/member/{id}")
    public MemberAuthentication getMemberById(@PathVariable long id) {
        return memberService.getMemberById(id);
    }

    @GetMapping("/members")
    public Page<MemberAuthentication> getSearchMembers(MemberSearchConditionRequest memberSearchConditionRequest, Pageable pageable) {
        return memberService.getMembers(memberSearchConditionRequest, pageable);
    }
}
