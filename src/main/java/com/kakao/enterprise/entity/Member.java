package com.kakao.enterprise.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member implements Persistable<Long> {

    @Id
    @Column(nullable = false)
    private Long id;

    private String email;

    private String ageRange;

    private String nickname;

    private String profileImageUrl;

    public Member(Long id, String email, String ageRange, String nickname, String profileImageUrl) {

        this.id = id;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.email = email;
        this.ageRange = ageRange;
    }

    @Transient
    private boolean isNew = true;

    @Override
    public Long getId() {
        return this.id;
    }

    public boolean isNew() {
        return isNew;
    }

    @PrePersist
    @PostLoad
    public void onPrePersist() {
        this.isNew = false;
    }
}