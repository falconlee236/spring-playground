package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class QuerydslApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuerydslApplication.class, args);
	}

	// 다음과 같이 JPAQueryFactory 를 생성 시점에 spring bean 으로 등록해서 주입받아 사용해도 된다.
	// 그러면 레포지토리 생성자에서 this.queryFactory = new JPAQueryFactory(em); 이런 코드가 필요 없을듯
	// bean 으로 등록하면 레포지토리 생성자 파라미터로 주입받는듯
	/*
	EntityManager 와 같은 객체들은 스프링이 자동으로 @Bean 으로 등록해서 관리합니다. 그러나 JPAQueryFactory 는 스프링에서 자동으로 등록되지 않습니다
	 */
	@Bean
	JPAQueryFactory jpaQueryFactory(EntityManager em) {
		return new JPAQueryFactory(em);
	}
}
