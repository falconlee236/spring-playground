package study.querydsl.dto;

import lombok.Data;
import java.util.List;

// 회원 검색 조건 클래스
@Data
public class MemberSearchCondition {
    // 회원명, 팀명, 나이 (ageGoe, ageLoe) 조건을 담아둔 검색조건 레포
    private String username;
    private String teamName;
    private Integer ageGoe;
    private Integer ageLoe;
}
