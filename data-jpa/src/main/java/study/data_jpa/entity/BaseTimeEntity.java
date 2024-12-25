package study.data_jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


// 보통은 시간이 꼭 필요하고 사람은 안필요한 경우도 많아서 이렇게 사용하기도 함.
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
public class BaseTimeEntity {

    @CreatedDate // 이것만 적으면 끝
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate // 이것만 적으면 끝
    private LocalDateTime lastModifiedDate;
}
