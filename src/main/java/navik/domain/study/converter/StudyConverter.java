package navik.domain.study.converter;

import navik.domain.study.dto.StudyCreateDTO;
import navik.domain.study.entity.Study;
import navik.domain.study.enums.RecruitmentStatus;

public class StudyConverter {
	public static Study toStudy(StudyCreateDTO.CreateDTO request) {
		return Study.builder()
			.title(request.getTitle())
			.capacity(request.getCapacity())
			.description(request.getDescription())
			.gatheringPeriod(request.getGatheringPeriod())
			.participationMethod(request.getParticipationMethod())
			.synergyType(request.getSynergyType())
			.startDate(request.getStartDate())
			.endDate(request.getEndDate())
			.openChatUrl(request.getOpenChatUrl())
			.recruitmentStatus(RecruitmentStatus.RECURRING) // 스터디 초기 상태 '모집중'
			.build();
	}
}
