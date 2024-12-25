package jpabook.jpashop.repository.order.query;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    // 먼저 To One 관계에 있는 값을 다 채운 당므에 각 컬렉션들은 별도의 메소드를 사용해서 조회
    // 이 방식 또한 1 + N 문제가 터지기 때문에 최적화가 필요하다.
    public List<OrderQueryDto> findOrderQueryDtos() {
        List<OrderQueryDto> result = findOrders(); // 1번
        result.forEach(o -> { // N번
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId()); // N번
            o.setOrderItems(orderItems);
        });
        return result;
    }

    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                "SELECT new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        " FROM OrderItem oi" +
                        " JOIN oi.item i" +
                        " WHERE oi.order.id = :orderId ", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
                "SELECT new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                        " FROM Order o" +
                        " JOIN o.member m" +
                        " JOIN o.delivery d",
                OrderQueryDto.class
        ).getResultList();
    }

    public List<OrderQueryDto> findAllByDto_optimization() {
        List<OrderQueryDto> result = findOrders();
        // 주분 id 번호 가져오고
        // 관련된 ITEM 을 한번에 다 가져오기
        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(toOrderIds(result));
        // loop 를 도는 것은 같지만 이제는 loop 안에서 쿼리가 나가지 않는다는 장점이 있다. 즉 메모리에서 매칭
        // 이렇게 하면 쿼리가 2번 나간다.
        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return result;
    }

    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItems = em.createQuery(
                "SELECT new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        " FROM OrderItem oi" +
                        " JOIN oi.item i" +
                        " WHERE oi.order.id in :orderIds ", OrderItemQueryDto.class) // 여기서 동등 비교가 아닌 in 으로 바꾼것이 가장 큰 차이점
                .setParameter("orderIds", orderIds)
                .getResultList();

        // key 가 orderId 이고 value 가 그에 대한 값인 map 을 생성
        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));
        return orderItemMap;
    }

    private static List<Long> toOrderIds(List<OrderQueryDto> result) {
        List<Long> orderIds = result.stream()
                .map(OrderQueryDto::getOrderId)
                .toList();
        return orderIds;
    }

    public List<OrderFlatDto> findAllByDto_flat() {
        return em.createQuery(
                "SELECT new" +
                        " jpabook.jpashop.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)" +
                        " FROM Order o" +
                        " JOIN o.member m" +
                        " JOIN o.delivery d" +
                        " JOIN o.orderItems oi" +
                        " JOIN oi.item i",
                OrderFlatDto.class
        ).getResultList();
    }
}
