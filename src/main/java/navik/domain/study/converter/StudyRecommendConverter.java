package navik.domain.study.converter;

import navik.domain.study.dto.StudyRecommendDTO;
import navik.domain.study.entity.Study;

public class StudyRecommendConverter {

	public static StudyRecommendDTO toStudyRecommendDTO(Study study, int participantCount, String kpiName, Long kpiId) {
		return StudyRecommendDTO.builder()
			.studyId(study.getId())
			.title(study.getTitle())
			.description(study.getDescription())
			.kpiName(kpiName)
			.kpiId(kpiId)
			.participationMethod(study.getParticipationMethod())
			.participantCount(participantCount)
			.startDate(study.getStartDate())
			.endDate(study.getEndDate())
			.capacity(study.getCapacity())
			.build();
	}
}
