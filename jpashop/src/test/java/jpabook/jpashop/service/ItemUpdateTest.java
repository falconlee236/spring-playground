package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.item.Book;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ItemUpdateTest {

    @Autowired
    EntityManager em;

    @Test
    public void updateTest() throws Exception {
        // given
        Book book = em.find(Book.class, 1L);

        // when

        // Transaction
        book.setName("asdf");
        /*
        기본적으로 em이 관리하는 entity값을 변경하면 jpa는 수정된 값들을 찾아서
        자동으로 update 쿼리를 날린다음 commit을 한다.
        -> 변경 감지 == dirty checking
         */
        
        // then   
    }
}
