package jpabook.jpashop.domain.item;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@DiscriminatorValue("B") // single table 로 들어갈 때 구분자 값을 설정.
public class Book extends Item{

    private String author;
    private String isbn;
}
