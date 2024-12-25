package study.data_jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import study.data_jpa.entity.Member;
import java.util.List;
import java.util.Optional;

@Repository
public class MemberJpaRepository {

    @PersistenceContext // spring 이 알아서 em을 물어오는 어노테이션
    private EntityManager em;

    public Member save(Member member) {
        em.persist(member);
        return member;
    }

    public void delete(Member member) {
        em.remove(member);
    }

    public List<Member> findAll() {
        return em.createQuery("SELECT m FROM Member m", Member.class).getResultList();
    }

    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    public long count() {
        return em.createQuery("SELECT COUNT(m) FROM Member m", Long.class).getSingleResult();
    }

    public Member find(Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findByUsernameAndAgeGreaterThan(String username, int age){
        return em.createQuery("SELECT m FROM Member m where m.username = :username and m.age > :age", Member.class)
                .setParameter("username", username)
                .setParameter("age", age)
                .getResultList();
    }

    public List<Member> findByUsername(String username) {
        return em.createNamedQuery("Member.findByUsername", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

     public List<Member> findByPage(int age, int offset, int limit){
        return em.createQuery("SELECT m FROM Member m where m.age = :age ORDER BY m.username DESC", Member.class)
                .setParameter("age", age)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
     }

     public long totalCount(int age) {
        return em.createQuery("SELECT COUNT(m) FROM Member m where m.age = :age", Long.class)
                .setParameter("age", age)
                .getSingleResult();
     }

     public int bulkAgePlus(int age) {
        return em.createQuery("UPDATE Member m SET m.age = m.age + 1 WHERE m.age >= :age")
                .setParameter("age", age)
                .executeUpdate();
     }

}
