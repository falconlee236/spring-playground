package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @PersistenceContext
    private EntityManager em;

    // 동시성 문제와 관련해서는 스프링에서 각 스레드마다 서로 다른 persist context 를 제공하기 때문에 상관 없음
    private JPAQueryFactory queryFactory;

    @BeforeEach // 테스트 실행할 때 항상 먼저 실행하는 메소드
    public void before(){
        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startJPQL(){
        // find member 1
        String qString = "SELECT m FROM Member m WHERE m.username = :username";
        Member findMember = em.createQuery(qString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl(){
        // find member 1
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember m = new QMember("m");

        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1")) //파라미터 바인딩 처리
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test // 보통 queryFactory 는 클래스 전역 변수 안에 둔다.
    public void startQuerydsl2(){
        // find member 1
        QMember m = new QMember("m");

        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1")) //파라미터 바인딩 처리
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test // Q 클래스의 인스턴스는 사용하는 방법이 2가지 있으며 위 경우는 alias 를 직접 지정하는 앙법
    public void startQuerydsl3(){
        // find member 1
//        QMember qMember = QMember.member; // 이 방식을 사용하면 기본 인스턴스를 사용
        // 보통 왼쪽의 Q class 인스턴스를 값에 넣지만, static import 와 함께 사용하면 기본 인스턴스를 그냥 쓸 수 있다.
        // import static study.querydsl.entity.QMember.*;
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
        // 참고로 같은 테이블을 join 해야하는 경우가 아니면 기본 인스턴스를 사용하자 -> static import or QMember.member 등등
    }

    // 검색 조건 쿼리 - 기본 검색 쿼리
    @Test
    public void search(){
        Member findMember = queryFactory
                .selectFrom(member) // select 와 from 이 같으면 selectFrom 으로 합칠 수 있음
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
        // 검색 조건은 where 절 안에 있는 다음과 같이 and or 의 메서드 체인으로 연결 가능
    }

    // and 조건을 method chain 이 아닌, where 문 parameter 로 처리하는 방식
    @Test
    public void searchAndParam() {
        List<Member> result1 = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"),
                        member.age.eq(10))
                .fetch();
        assertThat(result1.size()).isEqualTo(1);
        // where 에 파라미터로 검색 조건을 추가하면 자동으로 AND 조건이 추가
        // 이 경우 null 값은 무시 -> 메서드 추출을 통해서 동적 쿼리를 깔끔하게 만들 수 있다. -> 뒤에서 설명
    }

    // 정렬 예제
    @Test
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();
        // 나이 기준으로 내림차순, 이름 기준으로 오름차순 + null 값은 맨 마지막에 출력 -> nullsLast
        // nullsLast, nullsFirst -> null 데이터 순서 부여

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }


}
