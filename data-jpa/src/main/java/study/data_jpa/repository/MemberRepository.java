package study.data_jpa.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.data_jpa.dto.MemberDto;
import study.data_jpa.entity.Member;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

// 어떻게 인터페이스인데 메소드를 실행할 수 있죠? -> 처음에 생성할때 스프링 Data JPA 가 알아서 (proxy 형태로) 구현체를 만들어주고 갈아끼워줌
// Repository 어노테이션이 필요 없음
// 스프링 어노테이션의 2가지 기능 1. 컴포넌트 스캔 2. JPA 의 예외를 스프링에서 공통적으로 처리할 수 있는 예외로 변환
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    // 그냥 username 이면 equal 조건, age greater than 이면 > 방식으로
    // 1. Method name 으로 쿼리 생성
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    // JPA Named Query 예제
    // query="SELECT m FROM Member m where m.username = :username" 이런식으로 named parameter 로 넘어갈때 매개변수에 param 을 사용
    // 2. NamedQuery 예제 -> 실무에서 잘 쓰지 않음...
    @Query(name = "Member.findByUsername")
    List<Member> findByUsername(@Param("username") String username);

    // 3. 메소드에 바로 쿼리 정의하기 (실무에서 자주 사용)
    // 어플리케이션 로딩 시점에서 바로 SQL 구문을 파싱처리 해버린다. 그래서 컴파일 시점에 오류를 찾을 수 있음
    @Query("SELECT m FROM Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    // 사용자 이름의 배열을 가져오고 싶은 경우
    @Query("SELECT m.username FROM Member m")
    List<String> findUsernameList();

    @Query("SELECT new study.data_jpa.dto.MemberDto(m.id, m.username, t.name) FROM Member m join m.team t")
    List<MemberDto> findMemberDto();

    // collection parameter binding
    @Query("SELECT m FROM Member m WHERE m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);

    List<Member> findListByUsername(String username); // collection 으로 반환
    Member findMemberByUsername(String username); // 단건 반환
    Optional<Member> findOptionalByUsername(String username); // 단건 + optional

    // join 할 때 1:N 이면 count query 를 날릴 때, 다시 join 을 할필요 없기 때문에 (개수가 어짜피 일치함)
    // paging 할 때 query 랑 count query 를 분리하는 방법을 제공한다.
    // 주의 - page 는 0 부터 시작한다!!!
    @Query(value = "SELECT m FROM Member m left join m.team t",
            countQuery = "SELECT COUNT(m.username) FROM Member m")
    Page<Member> findByAge(int age, Pageable pageable);

    // 이 어노테이션이 있어야 executeQuery 함수를 실행한다.
    // clearAutometically 를 쓰면 자동으로 영속성 컨텍스트를 날려버림
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Member m SET m.age = m.age + 1 WHERE m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.team")
    List<Member> findMemberFetchJoin();

    @Override
    @EntityGraph(attributePaths = {"team"}) // spring data jpa 에서 fetch join 을 적용하는 방법
    List<Member> findAll();

    // override 뿐만 아니라 다른 방식으로도 사용가능
    @EntityGraph(attributePaths = {"team"})
    @Query("SELECT m FROM Member m")
    List<Member> findMemberEntityGraph();

    @EntityGraph(attributePaths = {"team"})
    List<Member> findEntityGraphByUsername(@Param("username") String username);

    // readonly 라는 것을 미리 제공하는 방법 -> 이러면 snapshot 을 JPA 내부에서 만들지 않는다.
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);

    // jpa 에서 제공하는 locking method
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String username);
}
