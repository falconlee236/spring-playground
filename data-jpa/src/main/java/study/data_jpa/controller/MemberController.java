package study.data_jpa.controller;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import study.data_jpa.dto.MemberDto;
import study.data_jpa.entity.Member;
import study.data_jpa.repository.MemberRepository;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Long id) {
        Member member = memberRepository.findById(id).get();
        return member.getUsername();
    }

    // domain class converter -> 객체를 받아도 자동으로 위에 있는 find 하는 과정을 생략해준다. (권장하지는 않음)
    // 조회용으로만 쓰기
    @GetMapping("/members2/{id}")
    public String findMember(@PathVariable("id") Member member) {
        return member.getUsername();
    }

    // 웹에서도 page 가 지원이 된다.
    // /members?page=1&size=3&sort=id,desc&sort=username,desc 이렇게 작성하면 page 가 끝난다!, spring 이 자동으로 객체를 만들어줘서 주입함.
    // default 설정을 바꾸기 위해서는? global 인 경우는 application.yaml 에 넣기
    // local 설정은? PageableDefault 에 넣기 (이것이 더 우선)
    @GetMapping("/members")
    public Page<MemberDto> list(@PageableDefault(size = 5, sort = "username") Pageable pageable) {
        // pageable 인터페이스를 받는 findAll 도 존재한다.
        return memberRepository.findAll(pageable)
                .map(MemberDto::new);
    }

    @PostConstruct
    public void init() {
        for(int i = 0; i < 100; i++) memberRepository.save(new Member("user" + i, i));

    }
}
