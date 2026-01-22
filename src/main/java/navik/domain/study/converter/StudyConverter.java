package navik.domain.study.converter;

import java.time.LocalDateTime;

import navik.domain.study.dto.StudyCreateDTO;
import navik.domain.study.dto.StudyDTO;
import navik.domain.study.entity.Study;
import navik.domain.study.entity.StudyUser;
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

	public static StudyDTO.MyStudyDTO toMyStudyDTO(StudyUser studyUser, Integer participantCount) {
		Study study = studyUser.getStudy();
		LocalDateTime now = LocalDateTime.now();

		RecruitmentStatus status = study.getStatus(now);
		boolean canEvaluate = study.canEvaluate(now);

		return StudyDTO.MyStudyDTO.builder()
			.studyUserId(studyUser.getId())
			.studyId(study.getId())
			.title(study.getTitle())
			.description(study.getDescription())
			.startDate(study.getStartDate())
			.endDate(study.getEndDate())
			.capacity(study.getCapacity())
			.currentParticipants(participantCount) // 현재 참가인원
			.participationMethod(study.getParticipationMethod()) // 온라인, 오프라인
			.openChatUrl(study.getOpenChatUrl())
			.recruitment_status(status.name()) // 모집중, 진행중, 종료
			.role(studyUser.getRole().name()) // 스터디장, 스터디원
			.canEvaluate(canEvaluate) // 종료된 경우에만 버튼 활성화
			.build();
	}
}
