package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @PersistenceContext
    private EntityManager em;

    @PersistenceUnit
    EntityManagerFactory emf;

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

    // 페이징 예제
    // 조회 건수 제한
    @Test
    public void paging1() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) // querydsl 은 0부터 시작 (zero index)
                .limit(2) // 최대 2건 조회
                .fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    // 전체 조회수가 필요하면?
    @Test
    public void paging2() {
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        System.out.println("result");
        System.out.println(queryResults);
        assertThat(queryResults.getTotal()).isEqualTo(4); // 전체 데이터 수
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getResults().size()).isEqualTo(2);

        // 이 경우는 count query 가 자동으로 실행되니까 성능상 주의
        // 보통 count 는 join 이 필요없는 경우가 많기 때문에 따로 쿼리를 작성하는 것이 효과적
    }

    // 집합 함수 -> sql 이 제공하는 모든 집합 함수를 제공한다.
    @Test
    public void aggregation() {
        List<Tuple> result = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    // groupBy
    @Test
    public void group() {
        // 팀의 이름과 각 팀의 평균 연령 구하기
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member).join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    // Join
    // basic join
    // 첫번째 파라미터에 join 대상을 지정하고 두번째 파라미터에 alias 로 사용할 Q 타입을 지정
    @Test
    public void join() {
        QMember member = QMember.member;
        QTeam team = QTeam.team;

        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team) // inner join
                .where(team.name.eq("teamA"))
                .fetch();
        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    // theta join (카르테시안 곱)
    @Test
    public void theta_join() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        // 연관관계가 없는 필드로 join -> 회원의 이름이 팀 이름과 같은 회원 조회
        List<Member> result = queryFactory
                .select(member)
                .from(member, team) // from 절에 여러 엔티티를 선택해서 외부 조인
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");

        // outer join 은 불가능하지만 join on 을 사용하면 외부 조인이 가능하다
    }

    // Join on statement
    // 1. join 대상 필터링
    // 회원과 팀을 조인하면서 팀 이름이 teamA 인 팀만 조인, 회원은 모두 조회
    /**
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL: SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = 'teamA'
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.TEAM_ID=t.id and
     t.name='teamA'
     */
    @Test
    public void join_on_filtering() {
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();
        // 이 경우는 그냥 join 하고 where 절에 조건을 추가한거랑 동일 즉 내부 조인이면 where 로 쓰는게 좋음
        // outer join 이 필요할 때만 on 을 사용
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    /**
     * 2. 연관관계 없는 엔티티 외부 조인
     * 예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
     * JPQL: SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.username = t.name
     */
    @Test
    public void join_on_no_relation() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name)) // 여기 부분이 다르다! 일반 join 과 다르게 엔티티 하나만 들어감
                .fetch();
        //  이러면 pk 대상으로 join 하는 것이 사라짐

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    // join - fetch join
    // 주로 성능 최적화에 사용, SQL 조인을 사용해서 관련 엔티티를 SQL 한번에 조회하는 기능
    // fetch join 미적용 -> 지연 로딩으로 Member, team sql 각각 실행
    @Test
    public void fetchJoinNo() {
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("패치 조인 미적용").isFalse();
    }

    // 패치 조인 적용
    // 즉시 로딩으로 Member, Team SQL 쿼리 조인으로 한번에 조회
    @Test
    public void fetchJoinUse() {
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin() // 조인 기능 뒤에 fetchJoin 이라고 추가하면된다.
                .where(member.username.eq("member1"))
                .fetchOne();
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("패치 조인 적용").isTrue();
    }

    // sub query
    // com.querydsl.jap.JPAExpressions 사용
    // sub query eq 사용
    @Test
    public void subQuery(){
        // 나이가 가장 많은 회원 조회
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age").containsExactly(40 );
    }

    // subquery goe 사용
    @Test
    public void subQueryGoe() {
        QMember memberSub = new QMember("memberSub");
        // 나이가 평균 나이 이상인 회원 조회
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age").containsExactly(30, 40 );
    }

    /**
     * 서브쿼리 여러 건 처리, in 사용
     */
    @Test
    public void subQueryIn() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();
        assertThat(result).extracting("age")
                .containsExactly(20, 30, 40);

        // select 절에 subQuery
        List<Tuple> fetch = queryFactory
                .select(member.username,
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ).from(member)
                .fetch();
        for (Tuple tuple : fetch) {
            System.out.println("username = " + tuple.get(member.username));
            System.out.println("age = " +
                    tuple.get(JPAExpressions.select(memberSub.age.avg())
                            .from(memberSub)));
        }

        // 참고로 JPA, 하이버네이트를 상요하면 select, where 의 서브쿼리는 지원하지만 from 절의 서브쿼리는 지원안함

        /*
        orderBy 에서 Case 문 함께 사용하기 예제
        예를 들어서 다음과 같은 임의의 순서로 회원을 출력하고 싶다면?
            1. 0 ~ 30살이 아닌 회원을 가장 먼저 출력
            2. 0 ~ 20살 회원 출력
            3. 21 ~ 30살 회원 출력
         */
        NumberExpression<Integer> rankPath = new CaseBuilder()
                .when(member.age.between(0, 20)).then(2)
                .when(member.age.between(21, 30)).then(1)
                .otherwise(3);
        List<Tuple> res = queryFactory
                .select(member.username, member.age, rankPath)
                .from(member)
                .orderBy(rankPath.desc())
                .fetch();
        for (Tuple tuple : res) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            Integer rank = tuple.get(rankPath);
            System.out.println("username = " + username + " age = " + age + " rank = " +
                    rank);
        }

        // 상수 문자 예시
        Tuple rt = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetchFirst();

        // 문자 더하기 concat
        String rs = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        // 결과: member1_10
        // 문자가 아닌 다른 타입은 stringValue() 로  문자로 변환 가능
    }
}
