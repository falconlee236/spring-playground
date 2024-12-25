package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class) // 이 2가지가 있어야 스프링을 올려서 통합 테스트를 할 수 있음
@SpringBootTest // 스프링 부트 컨테이너를 띄워서 테스트, 이게 업으면 autowired가 모두 실패
@Transactional //역시 데이터를 변경해야하므로 트랜젝션이 필요함 -> 롤백 가능
public class MemberServiceTest {

    // 테스트 케이스이기 때문에 다른 곳에서 참조할 수 없음.
    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
//    @Autowired EntityManager em;

    @Test // 회원가입 테스트
//    @Rollback(value = false) 이걸 하면 rollback을 안해서 insert commit 함
    public void signupTest() throws Exception {
        //given -> 이게 주어졌을 때
        Member member = new Member();
        member.setName("kim");

        //when -> 이걸 실행하면
        Long savedId = memberService.join(member);
        /*
        join을 하면 em.persist를 하는데, 기본적으로 스프링은 commit을 하지않고 rollback한다.
        그렇기 때문에 test결과를 보면 insert문이 없는 것을 확인할 수 있다.
        혹은 insert하는 것을 보고 싶다면 em에 있는 영속성 컨텍스트를 적용하는 flush라는 메소드를 호출하자.
         */


        //then -> 이렇게 결과가 나와야 한다.
//        em.flush();
        assertEquals(member, memberRepository.findById(savedId).get());
        // 이게 가능한 이유는 같은 트랜젝션에서 같은 멤버는 영속성 안에서 같은 녀석으로 관리가 된다.
    }

    // 중복회원예외 테스트 -> tdd live template 를 쓰자
    @Test(expected = IllegalStateException.class)
    public void duplicateMemberTest() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");

        //when
        memberService.join(member1);
        // 예외가 발생해야한다!! -> 그러면 catch를 안했기 때문에 여기까지 예외가 튀어나옴
        memberService.join(member2);

        //then
        fail("예외가 발생해야한다."); // 여기 온거면 예외가 튀어 나오지 않은 것이기 때문에 테스트 실패
    }
}

// DB연결은 여기 참고 -> https://javanitto.tistory.com/37
/*
test할 때는 완전히 격리된 환경에서 테스트를 해야함.
그럴때 embedded db를 계속 띄울 수 없는데, test 폴더 아래에 resource 디렉토리를 만들고 거기에 application.yml
복붙, 그리고 url을 h2의 in memory 방식인 :mem:test로 바꾸면 완전히 격리된 환경에서 테스트 가능.

즉 test를 할때는 test 디렉토리 안에 있는 resource가 우선권을 갖는다.
 */