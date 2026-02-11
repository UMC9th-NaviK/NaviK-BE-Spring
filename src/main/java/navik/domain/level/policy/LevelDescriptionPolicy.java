package navik.domain.level.policy;

import navik.domain.level.entity.Level;
import navik.domain.recruitment.enums.JobType;

public interface LevelDescriptionPolicy {

	JobType supports();

	String getDescription(Level level);
}