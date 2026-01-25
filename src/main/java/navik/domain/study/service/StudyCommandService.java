package navik.domain.study.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.kpi.entity.KpiCard;
import navik.domain.kpi.repository.KpiCardRepository;
import navik.domain.study.converter.StudyConverter;
import navik.domain.study.dto.StudyCreateDTO;
import navik.domain.study.entity.Study;
import navik.domain.study.entity.StudyKpi;
import navik.domain.study.entity.StudyUser;
import navik.domain.study.enums.AttendStatus;
import navik.domain.study.enums.StudyRole;
import navik.domain.study.repository.StudyKpiRepository;
import navik.domain.study.repository.StudyRepository;
import navik.domain.study.repository.StudyUserRepository;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.exception.code.GeneralErrorCode;
import navik.global.apiPayload.exception.exception.GeneralExceptionHandler;

@Service
@RequiredArgsConstructor
public class StudyCommandService {
	private final StudyUserRepository studyUserRepository;
	private final StudyKpiRepository studyKpiRepository;
	private final KpiCardRepository kpiCardRepository;
	private final StudyRepository studyRepository;
	private final UserRepository userRepository;

	/**
	 * 스터디 생성
	 * @param request
	 * @param userId
	 * @return
	 */
	@Transactional
	public Long createStudy(StudyCreateDTO.CreateDTO request, Long userId) {
		// 1. 로그한 유저 정보 조회
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.USER_NOT_FOUND));

		// 2. 선택한 KPI 카드 존재 여부 확인
		KpiCard kpiCard = kpiCardRepository.findById(request.getKpiId())
			.orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.KPI_NOT_FOUND));

		// 3. 스터디 방 엔티티 생성 및 저장
		Study study = StudyConverter.toStudy(request);
		Study savedStudy = studyRepository.save(study);

		// 4. 스터디 추천을 위한 스터디 - KPI 매핑 데이터 저장
		StudyKpi studyKpi = StudyKpi.builder()
			.study(savedStudy)
			.kpiCard(kpiCard)
			.build();
		studyKpiRepository.save(studyKpi);

		// 5. 방장을 StudyUser로 등록
		StudyUser leader = StudyUser.builder()
			.study(savedStudy)
			.user(user)
			.role(StudyRole.STUDY_LEADER)
			.attend(AttendStatus.ACCEPTANCE)
			.isActive(true)
			.memberStartDate(LocalDateTime.now())
			.build();

		studyUserRepository.save(leader);
		return savedStudy.getId();
	}
}
