package study.querydsl.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;

import java.util.List;

//1. 사용자 정의 인터페이스 작성
public interface MemberRepositoryCustom {

    List<MemberTeamDto> search(MemberSearchCondition condition);

    // QueryDSL 페이징 연동
    // 단순한 페이징
    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);
    // 복잡한 페이징
    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);
}
