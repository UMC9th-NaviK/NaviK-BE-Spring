package navik.domain.recruitment.service.recruitment;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.kpi.entity.KpiCard;
import navik.domain.kpi.exception.code.KpiCardErrorCode;
import navik.domain.kpi.repository.KpiCardRepository;
import navik.domain.recruitment.converter.recruitment.RecruitmentConverter;
import navik.domain.recruitment.dto.recruitment.RecruitmentResponseDTO;
import navik.domain.recruitment.enums.ExperienceType;
import navik.domain.recruitment.enums.MajorType;
import navik.domain.recruitment.repository.recruitment.RecruitmentRepository;
import navik.domain.recruitment.repository.recruitment.projection.RecommendPostProjection;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserDepartmentRepository;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.code.status.GeneralErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentQueryService {

	private final RecruitmentRepository recruitmentRepository;
	private final UserRepository userRepository;
	private final UserDepartmentRepository userDepartmentRepository;
	private final KpiCardRepository kpiCardRepository;

	/**
	 * @param userId
	 * @return 사용자 맞춤형 채용 공고 5건을 반환합니다.
	 */
	public List<RecruitmentResponseDTO.RecommendPost> getRecommendedPosts(Long userId) {

		// 1. 유저 검색
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.USER_NOT_FOUND));

		// 2. 전공 검색
		List<String> departments = userDepartmentRepository.findDepartmentNamesByUserId(userId);

		// 3. 모든 ability <-> 모든 PositionKPI => 종합 유사도 합산이 가장 높은 공고 반환
		List<RecommendPostProjection> results = recruitmentRepository.findRecommendedPosts(
			user,
			user.getJob(),
			user.getEducationLevel(),
			user.getIsEntryLevel() ? ExperienceType.ENTRY : ExperienceType.EXPERIENCED,
			departments.stream().map(MajorType::valueOf).toList()
		);

		// 4. DTO 반환 (position batchSize)
		return results.stream()
			.map(RecruitmentConverter::toRecommendPost)
			.toList();
	}

	/**
	 * @param kpiCardId
	 * @return KPI 카드와 관련된 채용 공고 5건을 반환합니다.
	 */
	public List<RecruitmentResponseDTO.RecommendPost> getRecommendedPostsByCard(Long kpiCardId) {

		// 1. 카드 검색
		KpiCard kpiCard = kpiCardRepository.findByIdWithJobAndEmbedding(kpiCardId)
			.orElseThrow(() -> new GeneralExceptionHandler(KpiCardErrorCode.KPI_CARD_NOT_FOUND));

		// 2. 검색
		List<RecommendPostProjection> results = recruitmentRepository.findRecommendedPostsByCard(
			kpiCard,
			kpiCard.getJob()
		);

		// 3. DTO 반환
		return results.stream()
			.map(RecruitmentConverter::toRecommendPost)
			.toList();
	}
}
