package study.data_jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

// 이것도 추가해야함
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
public class BaseEntity extends BaseTimeEntity{
    @CreatedBy // 이것만 적으면 끝 (등록자)
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy // 이것만 적으면 끝 (수정자) -> 이 둘을 사용하려면 spring bean 을 활용해야 하며, main 에서 auditorAware 을 써야한다.
    private String lastModifiedBy;
    // 등록을 하면 이 값들이 호출될 때 마다 자동으로 spring bean 에 auditorProvider 가 값을 제공해준다.
}
