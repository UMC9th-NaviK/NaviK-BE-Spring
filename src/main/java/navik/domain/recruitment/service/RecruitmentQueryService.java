package navik.domain.recruitment.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.recruitment.converter.RecruitmentConverter;
import navik.domain.recruitment.dto.RecruitmentResponseDTO;
import navik.domain.recruitment.entity.Recruitment;
import navik.domain.recruitment.repository.RecruitmentRepository;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.code.status.GeneralErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentQueryService {

	private final RecruitmentRepository recruitmentRepository;
	private final UserRepository userRepository;

	/**
	 * 로직설명: 가장 적합한 Position의 채용 공고를 반환합니다.
	 * 	 1. Recruitment(1) + Position(N) + PositionKPI(N) 조인
	 * 	 2. '직무', '학력', '경력', '전공', '진행 중인 공고' 필터 적용
	 * 	 3. 유저의 모든 Ability <-> 모든 PositionKpi에 대해 유사도 계산
	 * 	 4. PositionKpi 기준 sum(유사도) 집계
	 * 	 5. Recruitment 기준 max(sum(유사도)) 집계
	 * 	 6. 유사도 기준 정렬
	 *
	 * @param userId
	 * @return 사용자 맞춤형 채용 공고 5건을 반환합니다.
	 */
	public List<RecruitmentResponseDTO.RecommendPost> getRecommendedPost(Long userId) {

		// 1. 유저 검색
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.USER_NOT_FOUND));

		// 2. 필터 정리
		Long jobId = user.getJob() == null ? null : user.getJob().getId();
		String educationType = user.getEducationType() == null ? null : user.getEducationType().name();
		String experienceType = user.getExperienceType() == null ? null : user.getExperienceType().name();
		String majorType = user.getMajorType() == null ? null : user.getMajorType().name();
		LocalDateTime now = LocalDateTime.now();

		// 3. 모든 ability <-> 모든 PositionKPI Top5 추출
		List<Recruitment> searchResults = recruitmentRepository.searchTop5RecruitmentsByUserId(
			userId, jobId, educationType, experienceType, majorType, now);

		// 4. DTO 변환 및 반환
		return searchResults.stream()
			.map(RecruitmentConverter::toRecommendPost)
			.toList();
	}
}
