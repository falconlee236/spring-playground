package jpabook.jpashop;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

    // hello라는 url로 들어오면 해당 함수가 호출되는 방식
    @GetMapping("hello")
    // controller가 model에 데이터를 담아서 뷰에 넘길수가 있다.
    public String hello(Model model) {
        // data는 key, hello는 value
        model.addAttribute("data", "hello!!");
        // 화면 이름을 return에 실어서 보낸다. 이때 뒤에 html 확장자가 자동으로 붙는다.
        // 즉 렌더링 할때 hello.html을 자동으로 이동한다.
        return "hello";
    }
}
