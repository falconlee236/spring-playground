package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * X : 1 경우에 대해서 알아보자 (many to one, one to one)
 * Order
 * Order -> Member
 * Order -> Delivery
 */

@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        // 왜 무한루프가 발생하는가? Order에 가면 member가 있고 member에 가면 order가 있으니까 계속 무한루프가 발생
        // 어떻게 해결하지? -> 양방향 연관관계에 걸리는 친구들을 전부다 JsonIgnore 처리 해줘야한다.
        // 즉 order 입장에서 양방향 연관관계에 걸리는 친구들에 ignore
        // 근데 이렇게만 하니까? 또 에러가 발생하네
        // 왜냐하면 지연로딩이기 때문에 실제 객체를 들고오지 않기 때문 (Order만 가져옴)
        // Order의 친구인 Member는 proxy를 가져오는데, 이 클래스가 bytebuddy 클래스이다.
        // 즉 Order와 Member를 조인해서 뭔가 해보려했지만 Member는 프록시이기때문에 가져올 수 없는 오류 발생
        // 이걸 해결하려면 hibernate5Module 이라는 bin을 스프링에 등록해야한다. -> 지연 로딩을 null로 해버리기
        // 사실 엔티티를 외부에 노출 시키면 안되기 때문에 이짓을 안해도 된다! 사실 DTO로 변환해서 반환하자.
        // module 옵션을 끄고 강제로 지연로딩을 가져오기 위한 방법은?
        for (Order order : all) {
            order.getMember().getName(); // Lazy 강제 초기화 -> 그렇다고 즉시로딩으로 바꾸지는 마세요
            order.getDelivery().getAddress();
        }
        return all;
    }

    @GetMapping("/api/v2/simple-orders")
    public List<OrderSimpleQueryDto> ordersV2 () {
        // ORDER 2개 등장
        // N + 1 문제 = 1번째 Orders + 회원 + 배송 + 회원 + 배송 = 5번
        return orderRepository.findAllByString(new OrderSearch())
                .stream()
                .map(o -> new OrderSimpleQueryDto(
                        o.getId(),
                        o.getMember().getName(),
                        o.getOrderDate(),
                        o.getStatus(),
                        o.getDelivery().getAddress()))
                .toList();
    }

    @GetMapping("/api/v3/simple-orders")
    public List<OrderSimpleQueryDto> ordersV3 () {
        return orderRepository.findAllWithMemberDelivery()
                .stream()
                .map(o -> new OrderSimpleQueryDto(
                        o.getId(),
                        o.getMember().getName(),
                        o.getOrderDate(),
                        o.getStatus(),
                        o.getDelivery().getAddress()))
                .toList();
    }

    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4 () {
        // v4가 원하는 것만 쏙쏙 골라오니까 성능은 v3보다 더 좋지만 이 DTO 가 아니면 쓸 수 없기 때문에 재사용성이 떨어짐
        // 반면 v3은 전체를 가져오지만 이 결과를 가지고 다른 함소에 활용할 수 있기 때문에 재사용성이 높음
        // 이렇게 특정 DTO 를 위한 쿼리를 작성할때는 보통 별도 레포지토리를 만들어서 사용하는 편이다.
        return orderSimpleQueryRepository.findOrderDtos();
    }
}
