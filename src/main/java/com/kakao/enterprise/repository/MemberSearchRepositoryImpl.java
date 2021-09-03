package com.kakao.enterprise.repository;

import com.kakao.enterprise.dto.MemberAuthentication;
import com.kakao.enterprise.dto.MemberSearchConditionRequest;
import com.kakao.enterprise.entity.Member;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.kakao.enterprise.entity.QMember.member;

@RequiredArgsConstructor
public class MemberSearchRepositoryImpl implements MemberSearchRepository {

    private final JPQLQueryFactory queryFactory;

    @Override
    public Page<MemberAuthentication> getMembers(MemberSearchConditionRequest memberSearchConditionRequest, Pageable pageable) {

        final String ageRange = memberSearchConditionRequest.ageRange();
        final String email = memberSearchConditionRequest.email();

        final QueryResults<MemberAuthentication> queryResults = queryFactory
                .select(Projections.fields(MemberAuthentication.class,
                        member.id,
                        member.nickname,
                        member.profileImageUrl,
                        member.email,
                        member.ageRange))
                .from(member)
                .where(ageRangeEq(ageRange),
                        emailEq(email))
                .orderBy(getOrderSpecifier(pageable.getSort()).toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        return PageableExecutionUtils.getPage(queryResults.getResults(), pageable, queryResults::getTotal);
    }

    private BooleanExpression emailEq(String email) {
        return Objects.isNull(email) ? null : member.email.endsWith(email);
    }

    private BooleanExpression ageRangeEq(String ageRange) {
        return Objects.isNull(ageRange) ? null : member.ageRange.eq(ageRange);
    }

    private List<OrderSpecifier> getOrderSpecifier(Sort sort) {

        List<OrderSpecifier> orders = new ArrayList<>();

        // Sort
        sort.stream().forEach(order -> {

            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            String prop = order.getProperty();
            PathBuilder orderByExpression = new PathBuilder(Member.class, "member1");
            orders.add(new OrderSpecifier(direction, orderByExpression.get(prop)));
        });

        return orders;
    }
}
