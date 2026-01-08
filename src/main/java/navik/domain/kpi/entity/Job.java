package navik.domain.kpi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class Job {

    @Id
    private Long id;
}
