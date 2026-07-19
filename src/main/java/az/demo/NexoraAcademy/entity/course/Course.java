package az.demo.NexoraAcademy.entity.course;

import az.demo.NexoraAcademy.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import java.math.BigDecimal;

@Entity
@Table(name = "courses")
@SoftDelete(columnName = "deleted_at")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Course extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String status;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;
}