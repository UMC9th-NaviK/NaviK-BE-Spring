package navik.domain.study.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.study.converter.StudyConverter;
import navik.domain.study.dto.StudyCreateDTO;
import navik.domain.study.entity.Study;
import navik.domain.study.entity.StudyUser;
import navik.domain.study.enums.AttendStatus;
import navik.domain.study.enums.StudyRole;
import navik.domain.study.repository.StudyRepository;
import navik.domain.study.repository.StudyUserRepository;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.code.status.GeneralErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Service
@RequiredArgsConstructor
public class StudyCommandService {
	private final StudyUserRepository studyUserRepository;
	private final StudyRepository studyRepository;
	private final UserRepository userRepository;

	@Transactional
	public Long createStudy(StudyCreateDTO.CreateDTO request, Long userId) {
		// 1. 로그한 유저 정보 조회
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.USER_NOT_FOUND));

		// 2. 스터디 방 엔티티 생성 및 저장
		Study study = StudyConverter.toStudy(request);
		Study savedStudy = studyRepository.save(study);

		// 3. 방장을 StudyUser로 등록
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
