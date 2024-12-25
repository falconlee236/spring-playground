package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

// spring bin의 자동 등록 대상이 된다.
@Repository
@RequiredArgsConstructor // 스프링이 entity manager를 주입해준다. -> final field에 있는 constructor를 통해서 의존성 주입
public class MemberRepositoryOld {

    // 추가로 스프링 부트에서는 persistencecontext 기능을 autowired가 해주기도 하는데, 그렇기 때문에 위에서 requiredargs constructor로 해결하능 -> 일관성 증가
    private final EntityManager em;

    public void save(Member member) {
        // 영속성 context에 member를 넣어서 나중에 commit이 되면 db에 반영한다. -> db에 insert query가 날라간다.
        em.persist(member);
    }

    public Member findOne(Long id) {
        // 단권 조회라서 뒤에 pk를 넣으면 된다.
        return em.find(Member.class, id);
    }

    public List<Member> findAll(){ // SQL은 table 대상으로 조회하지만 JPQL은 entity를 대상으로 조회한다.
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }
}
