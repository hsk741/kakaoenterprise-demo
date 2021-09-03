package com.kakao.enterprise.service;

import com.kakao.enterprise.dto.MemberAuthentication;
import com.kakao.enterprise.dto.MemberSearchConditionRequest;
import com.kakao.enterprise.dto.MemberSignUpRequest;
import com.kakao.enterprise.dto.OAuth2AccessToken;
import com.kakao.enterprise.entity.Member;
import com.kakao.enterprise.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final OAuth2RestService oAuth2AuthorizationService;

    public MemberAuthentication signUp(OAuth2AccessToken accessToken, Long id, MemberAuthentication memberAuthentication) {

//      개인 정보 동의에 대한 변경사항을 반영해야 한다면 소셜 인증 서버로 사용자 정보 조회
//        final KakaoLoginUserProfile userProfile = oAuth2AuthorizationService.getUserProfileFromProfileResourceServer(accessToken);
//        final Profile profile = userProfile.getKakaoAccount().getProfile();

//      미반영한다면 이전에 로그인 정보를 가지고 처리
        memberRepository.findById(id)
                .ifPresentOrElse(
                        member -> member.setNickname(memberAuthentication.getNickname()),
                        () ->
                                memberRepository.save(
                                        new Member(id,
                                                memberAuthentication.getEmail(),
                                                memberAuthentication.getAgeRange(),
                                                memberAuthentication.getNickname(),
                                                memberAuthentication.getProfileImageUrl()
                                        )));

        return makeMemberResponse(memberRepository.findById(id).get());
    }

    public MemberAuthentication updateMemberNickname(long id, MemberSignUpRequest memberSignUpRequest) {

        Member member = memberRepository.findById(id).orElseThrow(() -> new EmptyResultDataAccessException("No member id : " + id, 1));
        member.setNickname(memberSignUpRequest.nickname());

        return makeMemberResponse(member);
    }

    public MemberAuthentication deleteMemberById(long id, OAuth2AccessToken accessToken) {

        oAuth2AuthorizationService.unlink(accessToken);
        memberRepository.deleteById(id);

        return MemberAuthentication.builder().id(id).build();
    }

    public MemberAuthentication getMemberById(long id) {
        return memberRepository.findById(id).map(this::makeMemberResponse).orElseGet(MemberAuthentication::new);
    }

    public Page<MemberAuthentication> getMembers(MemberSearchConditionRequest memberSearchConditionRequest, Pageable pageable) {
        return memberRepository.getMembers(memberSearchConditionRequest, pageable);
    }

    private MemberAuthentication makeMemberResponse(Member member) {

        return MemberAuthentication.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .ageRange(member.getAgeRange())
                .build();
    }
}
