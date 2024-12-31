package study.querydsl.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;

import java.util.List;

// 이렇게 작성하면 Querydsl 전용 기능인 회원 search 를 작성할 수 없기 때문에 querydsl 을 사용하려면 사용자 정의 리포지토리가 필요하다.
// 3. 스프링 데이터 리포지토리에 사용자 정의 인터페이스 상속
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    List<Member> findByUsername(String username);
}
