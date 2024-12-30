package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class MemberTeamDto {
    private Long memberId;
    private String username;
    private int age;
    private Long teamId;
    private String teamName;

    // 이러면 DTO 가 queryDSL 을 의존하게 되니 이런 의존이 싫으면 해당 어노테이션 제거하고 다른 방법을 찾자.
    @QueryProjection // 이거 하면 build 는 필수!
    public MemberTeamDto(Long memberId, String username, int age, Long teamId, String teamName) {
        this.memberId = memberId;
        this.username = username;
        this.age = age;
        this.teamId = teamId;
        this.teamName = teamName;
    }
}
