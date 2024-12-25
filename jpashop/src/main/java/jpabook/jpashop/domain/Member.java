package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter @Setter
public class Member {
    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long Id;

    @NotEmpty
    private String name;

    @Embedded // 내장타입이라고 표시하는 코드
    private Address address;

    @JsonIgnore
    @OneToMany(mappedBy = "member") // Order class에 있는 member 변수의 거울일뿐 이라고 선언해서 연관관계의 주인이 아니라른 것으 표현 (읽기 전용)
    private List<Order> orders = new ArrayList<>();
}
