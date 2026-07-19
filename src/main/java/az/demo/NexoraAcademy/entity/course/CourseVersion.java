package az.demo.NexoraAcademy.entity.course;

import az.demo.NexoraAcademy.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_versions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CourseVersion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Column(name = "syllabus_data", columnDefinition = "jsonb")
    private String syllabusData;

    @Column(name = "is_current", nullable = false)
    private boolean isCurrent;
}