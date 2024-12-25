package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

/*
스프링이 제공하는 REST API 스타일로 제공하는 어노테이션,
이 어노테이션 안에 있는 @ResponseBody 어노테이션은 데이터를 json으로 하자는 뜻
 */
@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        // 이러면 다양한 API를 만들때 애로사항이 생긴다.
        return memberService.findMembers();
    }

    @GetMapping("/api/v2/members")
    public Result<?> memberV2(){
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .toList();
        return new Result<>(collect.size(), collect);
    }

    @Data
    @AllArgsConstructor
    public static class Result<T> { // 이렇게 작성하면 List가 아닌 Object로 한번 감싸서 들어오며, 그 대상은 data이다.
        private int count;
        private T data;
    }

    @Data
    @AllArgsConstructor
    public static class MemberDto {
        private String name;
    }

    @PostMapping("/api/v1/members") //RequestBody는 JSON으로 온 데이터를 Member에 넣어줌
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member){
        // 빈 이름을 막고 싶다면? -> @Valid 어노테이션이 있으니까 Member 도메인에 가서 @NotEmpty 제약조건을 넣으면 된다.
        // 하지만 이렇게 API랑 도메인이랑 1:1로 매핑되어있으면 심각한 문제가 발생 -> 도메인 이름이 바뀌면 그냥 API가 터져버림
        // 이것을 막기 위해서 API와 도메인 사이에 중간 다리 DTO를 만들어야 한다.
        /* API는 항상 엔티티 그대로를 파라미터로 받지 않는 것이 좋다, 엔티티를 외부에 노출하는 것도 안좋음 -> v2로 가보자. */
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    // 회원 등록
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        // 이러면 API의 영향을 받지 않음
        // 엔티티를 변경해도 API의 spec이 영향을 받지 않는다는 장점이 있다.
        // DTO에서 @NotEmpty를 넣을 수 있음
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    // 회원 수정
    @PutMapping("/api/v2/members/{id}") // 수정과 등록은 API 스팩이 다르기 때문에 별도의 응답, 요청 객체를 만든다.
    public UpdateMemberResponse updateMemberV2(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request){

        memberService.update(id, request.getName()); // 개발 스타일: update는 그 시점에 끝내버리기
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @Data
    public static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    public static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    public static class CreateMemberRequest{
        private String name;
    }

    @Data
    public static class CreateMemberResponse {
        private Long id;
        CreateMemberResponse(Long id){
            this.id = id;
        }
    }
}
