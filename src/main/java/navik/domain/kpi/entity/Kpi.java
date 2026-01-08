package navik.domain.kpi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Kpi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kpi_id")
    private Long kpiId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "strong_title", nullable = false, length = 255)
    private String strongTitle;

    @Column(name = "strong_content", nullable = false, length = 2000)
    private String strongContent;

    @Column(name = "weak_title", nullable = false, length = 255)
    private String weakTitle;

    @Column(name = "weak_content", nullable = false, length = 2000)
    private String weakContent;

    @Transient
    private float[] embedding;

}