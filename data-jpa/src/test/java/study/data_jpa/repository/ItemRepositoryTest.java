package study.data_jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.data_jpa.entity.Item;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ItemRepositoryTest {

    @Autowired ItemRepository itemRepository;

    @Test
    public void save() {
        Item item = new Item("A"); // 지금은 id 값이 null 인데, save 할 때 isNew 로 판단을 수행
        // generatedValue 를 쓰면 persist 할 때 거기서 spring 안에 있는 id 값을 꺼내서 사용
        // 근데 generated Value 를 쓰지 않으면 0이나 null 값이 아니기 때문에 persist 가 아닌 merge 가 호출되어 select query 가 한번 더 나간다
        // 그러면 어떻게 처리하는가? -> 엔티티에 Persistable 인터페이스를 implement + isNew 함수를 재 정의 한다.
        // 강의에서는 createdDate 를 사용해서 이 값이 존재하지 않으면 새로운 값이라고 설정함
        itemRepository.save(item);
    }
}