package com.kakao.enterprise.repository;

import com.kakao.enterprise.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberSearchRepository {


}
