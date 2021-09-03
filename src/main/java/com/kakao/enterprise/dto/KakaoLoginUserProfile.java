package com.kakao.enterprise.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoLoginUserProfile {

    private long id;
    private String connectedAt;
    private boolean hasSignedUp;
    private Properties properties;
    private KakaoAccount kakaoAccount;

    @Getter
    @ToString
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Properties {

        private String nickname;
        private String profileImage;
        private String thumbnailImage;
    }

    @Getter
    @ToString
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class KakaoAccount {

        private boolean profileNicknameNeedsAgreement;
        private boolean profileImageNeedsAgreement;

        private Profile profile;

        private boolean profileNeedsAgreement;
        private boolean emailNeedsAgreement;
        private boolean isEmailValid;
        private boolean isEmailVerified;
        private boolean hasEmail;
        private String email;

        private boolean hasAgeRange;
        private boolean ageRangeNeedsAgreement;
        private String ageRange;

        @Getter
        @ToString
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Profile {

            private String nickname;
            private String thumbnailImageUrl;
            private String profileImageUrl;
            private boolean isDefaultImage;
        }
    }
}