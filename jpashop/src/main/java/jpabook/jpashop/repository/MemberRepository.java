package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository  extends JpaRepository<Member, Long> {

    // 이렇게만 작성하면 findBy* 이렇게 만들면 JPA 가 다음 JPQL 쿼리를 자동으로 만들어준다.
    // select m from Member m where m.name = ?
    List<Member> findByName(String name);
}
