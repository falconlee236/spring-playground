package jpabook.jpashop.controller;

import jakarta.validation.Valid;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.*;

@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/members/new")
    public String createForm(Model model) {
        model.addAttribute("memberForm", new MemberForm()); // 이제 이 화면에서 MemberForm 객체에 접근이 가능
        return "members/createMemberForm";
    }

    @PostMapping("/members/new")
    public String create(@Valid MemberForm form, BindingResult result) { // MemberForm에서 @NotEmpty 어노테이션을 검증해주는 부분
    // 이렇게 따로 form Data를 만든 이유는 Member 객체와 MemberForm 객체가 서로 불일치하기 때문 (정제가 필요)
        if (result.hasErrors()) { // 뒤에 BindingResult를 사용하면 에러가 나도 result에 담겨서 쭉 코드를 실행한다.
            return "members/createMemberForm";
        }

        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());

        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);

        memberService.join(member);
        return "redirect:/";
    }

    @GetMapping("/members")
    public String list(Model model) {
        //여기서도 member를 그대로 사용해야 하나 form객체를 추가로 만들어야 하는가? 고민이 있음
        // 보통 entity는 순수하게 유지하고 화면을 건드리는 부분은 form객체를 사용하거나 DTO를 사용하는 것을 권장한다.
        // 여기서도 Member를 그대로 쓰는 것 보다는 화면에 꼭 필요한 데이터를 담은 DTO로 변환해서 뿌리는 것을 권장
        // API를 만들 때는 entity를 절대 넘기면 안된다.
        List<Member> members = memberService.findMembers();
        model.addAttribute("members", members);
        return "members/memberList";
    }
}
