package study.data_jpa.repository;

import study.data_jpa.entity.Member;

import java.util.List;


// custom 인테페이스를 만들고 싶다면 인터페이스와 구현체를 각각 준비하고 이 인터페이스를 data jpa 레포지토리에 다중상속 시키면 된다.
// java 에서 해주는 것은 아니고 스프링 data jpa 에서 해주는 것
// 보통 queryDSL 을 사용할 때 쓴다.
/*
중요 - 규칙이 한가지 있음
이 인터페이스를 구현한 클래스의 이름은 SpringDataJPARepo + Impl 형태를 따라야 한다.
 */
public interface MemberRepositoryCustom {
    List<Member> findMemberCustom();
}
