package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service // component scan의 대상이 되서 자동 등록함
@Transactional(readOnly = true) // 기본적으로 데이터 변경은 반드시 트랜젝션 안에서 이루어져야 한다.
@RequiredArgsConstructor // final 필드만 가진 녀석들에한에허 생성자를 만들어 줌. - lombook
public class MemberService {

    // 최신 스프링에서는 autowired 없어서 자동으로 생성자를 통해서 의존성을 주입해준다.
    private final MemberRepository memberRepository;

    /*
    회원 가입
     */
    @Transactional // default로는 맨 위에 있는 true를 따르지만 값을 변경해야 할 때는 추가로 적어줘서 false로 만든다.
    public Long join(Member member) {
        validateDuplicateMember(member); // 중복 회원 검증
        memberRepository.save(member);
        return member.getId();
    }

    // 읽기 할때 트랜젝션을 true로 하면 성능이 더 빨라진다고 함.
    private void validateDuplicateMember(Member member) {
        // exception
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    // 회원 전체 조회
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    public Member findOne(Long memberId) {
        return memberRepository.findById(memberId).get();
    }

    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepository.findById(id).get();
        member.setName(name);
    }
}
