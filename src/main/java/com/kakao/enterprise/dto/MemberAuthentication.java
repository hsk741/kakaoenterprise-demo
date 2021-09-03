package com.kakao.enterprise.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class MemberAuthentication {

    private Long id;
    private String email;
    private String ageRange;    // 1~9, 10~14
    private String nickname;
    private String profileImageUrl;
}