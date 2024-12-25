package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/*
이제는 1:N, N:N 경우에 대해서 알아보자.
즉 컬렉션에 있는 정보를 어떻게 성능 최적화 할지가 고민인 것이다.
이 예제에서는 OrderItem 에 대해서 알아볼 예정
 */

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems(); // 이친구가 이번 API 의 핵심
            orderItems.forEach(orderItem -> orderItem.getItem().getName()); // LAZY Loading 할 때 강제 초기화 하는 코드
        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        return orderRepository.findAllByString(new OrderSearch()).stream()
                .map(OrderDto::new)
                .toList();
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        return orderRepository.findAllWithItem().stream()
                .map(OrderDto::new)
                .toList();
    }

    // 페이징의 핵심은 application.yml 파일에 default_batch_fetch_size 속성을 넣어주는 것
    // 이러면 쿼리에 in 연산자가 들어가서 fetch_size 개수만큼 미리 N값을 가져오는 놀라운 마법을 보여준다.
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit
    ) {
        return orderRepository.findAllWithMemberDelivery(offset, limit).stream()
                .map(OrderDto::new)
                .toList();
    }

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    // 쿼리 1회로 가능 하지만 페이징을 못함
    // 어플리케이션에서 추가 작업이 매우매우 크다
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        // 여기에 query DTO 스팩을 맞추려면? 노가다 뛰면 된다.
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

        // OrderQueryDTO 에서 @EqualsAndHashCode(of = "orderId") 를 넣어서 그룹화 할때 기준을 만들어야함, 이거 안쓰면 객체가 기준이라 이상함
        return flats.stream()
                .collect(Collectors.groupingBy(o -> new OrderQueryDto(
                                o.getOrderId(),
                                o.getName(),
                                o.getOrderDate(),
                                o.getOrderStatus(),
                                o.getAddress()),
                        Collectors.mapping(o -> new OrderItemQueryDto(
                                o.getOrderId(),
                                o.getItemName(),
                                o.getOrderPrice(),
                                o.getCount()), Collectors.toList())))
                .entrySet().stream()
                .map(e -> new OrderQueryDto(
                        e.getKey().getOrderId(),
                        e.getKey().getName(),
                        e.getKey().getOrderDate(),
                        e.getKey().getOrderStatus(),
                        e.getKey().getAddress(),
                        e.getValue()))
                .collect(Collectors.toList());
    }

    @Data
    public static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        // 하지만 DTO 란 API 엔티티의 연관관계를 다 끊어버리라는 이야기
        // 즉 이 OrderItem 또한 엔티티라서 이것도 Dto 로 변환해야한다.
        // 속에 있는 엔티티 또한 모두 벗겨야한다.
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .toList();
//            orderItems = order.getOrderItems(); 이렇게 했을때 안나오는 이유는? LAZY + 엔티티이기 때문 (엔티티는 값이 아니다)
//            order.getOrderItems().forEach(orderItem -> orderItem.getItem().getName()); // 이렇게 하면 되겠네? -> 이것도 아님
        }
    }

    @Data
    public static class OrderItemDto {

        private String itemName; // 상품 명
        private int orderPrice; // 주문 가격
        private int count; // 주문 수량

        // 여기서 노출하고 싶은것을 적어두면 된다
        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
