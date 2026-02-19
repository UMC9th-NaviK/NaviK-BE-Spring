package navik.domain.recruitment.service.recruitment;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.ability.repository.AbilityRepository;
import navik.domain.kpi.dto.res.KpiCardResponseDTO;
import navik.domain.kpi.entity.KpiCard;
import navik.domain.kpi.exception.code.KpiCardErrorCode;
import navik.domain.kpi.repository.KpiCardRepository;
import navik.domain.kpi.service.query.KpiScoreQueryService;
import navik.domain.recruitment.converter.recruitment.RecruitmentConverter;
import navik.domain.recruitment.dto.recruitment.RecruitmentResponseDTO;
import navik.domain.recruitment.enums.ExperienceType;
import navik.domain.recruitment.enums.MajorType;
import navik.domain.recruitment.repository.recruitment.RecruitmentRepository;
import navik.domain.recruitment.repository.recruitment.projection.RecommendedRecruitmentProjection;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.exception.code.GeneralErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentQueryService {

	private final RecruitmentRepository recruitmentRepository;
	private final UserRepository userRepository;
	private final KpiCardRepository kpiCardRepository;
	private final AbilityRepository abilityRepository;
	private final KpiScoreQueryService kpiScoreQueryService;

	/**
	 * @param userId
	 * @return 사용자 맞춤형 채용 공고 5건을 반환합니다.
	 */
	public List<RecruitmentResponseDTO.RecommendedPost> getRecommendedPosts(Long userId) {

		// 1. 유저 검색
		User user = userRepository.findByIdWithUserDepartmentAndDepartment(userId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.USER_NOT_FOUND));

		// 2. 역량이 하나도 없으면 상위 KPI 기반 공고 추천
		if (abilityRepository.countByUser(user) == 0) {
			List<KpiCardResponseDTO.GridItem> topKpiCards = kpiScoreQueryService.getTop3KpiCards(userId);
			if (topKpiCards.isEmpty()) {
				return Collections.emptyList();
			}
			int randomIndex = ThreadLocalRandom.current().nextInt(topKpiCards.size());
			KpiCardResponseDTO.GridItem randomCard = topKpiCards.get(randomIndex);
			return getRecommendedPostsByCard(randomCard.kpiCardId());
		}

		// 3. 역량이 존재하면 역량 기반 추천
		List<String> departments = user.getUserDepartments().stream()
			.map(userDepartment -> userDepartment.getDepartment().getName())
			.toList();

		List<MajorType> majorTypes = departments.stream()
			.map(name -> {
				MajorType type = MajorType.fromString(name);
				if (type == null) {
					log.error("[RecruitmentQueryService] 존재하지 않는 학과 타입입니다 : {}", name);
				}
				return type;
			})
			.filter(Objects::nonNull)
			.toList();

		List<RecommendedRecruitmentProjection> results = recruitmentRepository.findRecommendedPosts(
			user,
			user.getJob(),
			user.getEducationLevel(),
			user.getIsEntryLevel() ? ExperienceType.ENTRY : ExperienceType.EXPERIENCED,
			majorTypes,
			PageRequest.of(0, 5)    // 5건
		);

		return results.stream()
			.map(RecruitmentConverter::toRecommendedPost)
			.toList();
	}

	/**
	 * @param kpiCardId
	 * @return KPI 카드와 관련된 채용 공고 5건을 반환합니다.
	 */
	public List<RecruitmentResponseDTO.RecommendedPost> getRecommendedPostsByCard(Long kpiCardId) {

		// 1. 카드 검색
		KpiCard kpiCard = kpiCardRepository.findByIdWithJob(kpiCardId)
			.orElseThrow(() -> new GeneralException(KpiCardErrorCode.KPI_CARD_NOT_FOUND));

		// 2. 검색
		List<RecommendedRecruitmentProjection> results = recruitmentRepository.findRecommendedPostsByCard(
			kpiCard,
			kpiCard.getJob()
		);

		// 3. DTO 반환
		return results.stream()
			.map(RecruitmentConverter::toRecommendedPost)
			.toList();
	}

	/**
	 * @return 전체 채용 공고 개수를 반환합니다.
	 */
	public Long getCountPosts() {
		return recruitmentRepository.count();
	}
}
