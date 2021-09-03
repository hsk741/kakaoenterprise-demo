package com.kakao.enterprise.repository;

import com.kakao.enterprise.dto.MemberAuthentication;
import com.kakao.enterprise.dto.MemberSearchConditionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberSearchRepository {
    Page<MemberAuthentication> getMembers(MemberSearchConditionRequest memberSearchConditionRequest, Pageable pageable);
}
