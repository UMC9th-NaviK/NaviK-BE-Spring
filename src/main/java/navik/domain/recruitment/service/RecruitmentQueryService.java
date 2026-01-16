package navik.domain.recruitment.service;

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
	 * @param userId
	 * @return 사용자 맞춤형 채용 공고 5건을 반환합니다.
	 */
	public List<RecruitmentResponseDTO.RecommendPost> getRecommendedPosts(Long userId) {

		// 1. 유저 검색
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.USER_NOT_FOUND));

		// 2. 모든 ability <-> 모든 PositionKPI => 종합 유사도 합산이 가장 높은 공고 반환
		List<Recruitment> searchResults = recruitmentRepository.findRecommendedPosts(
			user,
			user.getJob(),
			user.getEducationType(),
			user.getExperienceType(),
			user.getMajorType()
		);

		// 3. DTO 반환
		return searchResults.stream()
			.map(RecruitmentConverter::toRecommendPost)
			.toList();
	}
}
