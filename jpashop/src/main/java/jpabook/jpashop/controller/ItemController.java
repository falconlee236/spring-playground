package jpabook.jpashop.controller;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.*;

@Controller
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/items/new")
    public String createForm(Model model) {
        model.addAttribute("form", new BookForm());
        return "items/createItemForm";
    }

    @PostMapping("/items/new")
    public String create(BookForm form) {
        // 사실 setter를 전부 날려벼리는게 깔끔한 설계 -> static 함수 사용
        Book book = new Book();
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setIsbn(form.getIsbn());

        itemService.saveItem(book);
        return "redirect:/items";
    }

    @GetMapping("/items")
    public String list(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);
        return "items/itemList";
    }

    @GetMapping("items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {
        Book item = (Book) itemService.findOne(itemId);

        BookForm form = new BookForm();
        form.setId(item.getId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setStockQuantity(item.getStockQuantity());
        form.setAuthor(item.getAuthor());
        form.setIsbn(item.getIsbn());

        model.addAttribute("form", form);
        return "items/updateItemForm";
    }

    @PostMapping("items/{itemId}/edit") // ModelAttribute를 사용해서 model 값을 가져온다.
    public String updateItem(@PathVariable Long itemId, @ModelAttribute("form") BookForm form) {
        // 구린 예제
//        Book book = new Book(); // 임의로 생성되더라도 DB의 식별자를 가지고 있다면 준영속 엔티티
//        book.setId(form.getId()); // 이미 DB를 거쳐서 id값을 받아왔지만 em이 관리하지는 않음 -> 준영속엔티티
//        book.setName(form.getName());
//        book.setPrice(form.getPrice());
//        book.setStockQuantity(form.getStockQuantity());
//        book.setAuthor(form.getAuthor());
//        book.setIsbn(form.getIsbn());

        /*
        준 영속 엔티티를 수정하는 2가지 방법
        1. 실제 em이 관리하는 객체를 사용해서 변경 감지 기능 사용 (이게 더 나은 방법)
        -> itemService.updateItem 함수 참고, 함수가 끝나면 자동으로 변경 감지를 하고 커밋함.
        2. 병합 (merge) 사용
        -> 1번 기능을 JPA에서 자동으로 수행하는 방식
        -> 이 함수에서 saveItem 함수를 호출하는데 그 함수가 사용하는 방식
        -> merge 대상을 DB에서 다 찾고 그 값을 다 바꿔치기 해버린다.
        -> merge 반환값이 em에서 관리하는 객체. 즉 나중에 사용하려변 반환값을 써야함.
        -> 그런데 이걸 왜 쓰지 말라고? 속성 값이 없으면 null로 밀어버리기 때문
        -> 즉 그냥 merge 쓰지 마세요..
         */
        // 좋은 예제 -> DTO를 만드는 것도 좋음
        itemService.updateItem(itemId, form.getName(), form.getPrice(), form.getStockQuantity());
        return "redirect:/items";
    }
}
