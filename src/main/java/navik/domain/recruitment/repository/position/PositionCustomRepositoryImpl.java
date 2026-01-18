package navik.domain.recruitment.repository.position;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import navik.domain.ability.entity.QAbility;
import navik.domain.ability.entity.QAbilityEmbedding;
import navik.domain.job.entity.Job;
import navik.domain.recruitment.dto.position.CursorRequest;
import navik.domain.recruitment.dto.position.PositionRequestDTO;
import navik.domain.recruitment.entity.QPosition;
import navik.domain.recruitment.entity.QPositionKpi;
import navik.domain.recruitment.entity.QPositionKpiEmbedding;
import navik.domain.recruitment.entity.QRecruitment;
import navik.domain.recruitment.enums.AreaType;
import navik.domain.recruitment.enums.CompanySize;
import navik.domain.recruitment.enums.EmploymentType;
import navik.domain.recruitment.enums.ExperienceType;
import navik.domain.recruitment.enums.IndustryType;
import navik.domain.recruitment.repository.position.projection.QRecommendedPositionProjection;
import navik.domain.recruitment.repository.position.projection.RecommendedPositionProjection;
import navik.domain.users.entity.User;
import navik.domain.users.enums.EducationLevel;

@Repository
@RequiredArgsConstructor
public class PositionCustomRepositoryImpl implements PositionCustomRepository {

	private final JPAQueryFactory jpaQueryFactory;

	private final QRecruitment recruitment = QRecruitment.recruitment;
	private final QPosition position = QPosition.position;
	private final QPositionKpi positionKpi = QPositionKpi.positionKpi;
	private final QPositionKpiEmbedding positionKpiEmbedding = QPositionKpiEmbedding.positionKpiEmbedding;
	private final QAbility ability = QAbility.ability;
	private final QAbilityEmbedding abilityEmbedding = QAbilityEmbedding.abilityEmbedding;

	@Override
	public Slice<RecommendedPositionProjection> findRecommendedPositions(
		User user,
		List<Job> jobs,
		PositionRequestDTO.SearchCondition searchCondition,
		CursorRequest cursorRequest,
		Pageable pageable
	) {

		// 1. pgvector 코사인 쿼리
		NumberTemplate<Double> similarityScore = Expressions.numberTemplate(
			Double.class,
			"1 - ({0} <=> {1})",
			positionKpiEmbedding.embedding,
			abilityEmbedding.embedding
		);

		// 2. 조건 설정
		BooleanExpression where = Stream.of(
				jobIn(jobs),
				experienceTypeIn(searchCondition.getExperienceTypes()),
				employmentTypeIn(searchCondition.getEmploymentTypes()),
				companySizeIn(searchCondition.getCompanySizes()),
				educationLevelIn(searchCondition.getEducationLevels()),
				areaTypeIn(searchCondition.getAreaTypes()),
				industryTypeIn(searchCondition.getIndustryTypes()),
				endDate(searchCondition.isWithEnded())
			)
			.reduce(BooleanExpression::and)
			.orElse(null);

		// 3. 조회
		List<RecommendedPositionProjection> result = jpaQueryFactory
			.select(new QRecommendedPositionProjection(position, similarityScore.sum()))
			.from(position)
			.join(position.recruitment, recruitment)                      // Position -> Recruitment
			.join(position.positionKpis, positionKpi)                     // Position → KPI
			.join(positionKpi.positionKpiEmbedding, positionKpiEmbedding) // KPI -> KPI Embedding
			.join(ability).on(ability.user.eq(user))                      // Ability
			.join(ability.abilityEmbedding, abilityEmbedding)             // Ability -> Embedding
			.where(where)
			.groupBy(position.id)
			.orderBy(similarityScore.sum().desc())
			.limit(pageable.getPageSize() + 1)  // for hasNext
			.fetch();

		// 4. Slice 반환
		return toSlice(result, pageable);
	}

	/**
	 * 찾으려는 직무 유형을 모두 포함합니다.
	 */
	private BooleanExpression jobIn(List<Job> jobs) {
		if (jobs == null || jobs.isEmpty())
			return null;
		return position.job.isNull().or(position.job.in(jobs));
	}

	/**
	 * 찾으려는 경력 유형을 모두 포함합니다.
	 */
	private BooleanExpression experienceTypeIn(List<ExperienceType> experienceTypes) {
		if (experienceTypes == null || experienceTypes.isEmpty())
			return null;
		return position.experienceType.isNull().or(position.experienceType.in(experienceTypes));
	}

	/**
	 * 찾으려는 고용 형태를 모두 포함합니다.
	 */
	private BooleanExpression employmentTypeIn(List<EmploymentType> employmentTypes) {
		if (employmentTypes == null || employmentTypes.isEmpty())
			return null;
		return position.employmentType.isNull().or(position.employmentType.in(employmentTypes));
	}

	/**
	 * 찾으려는 회사 유형을 모두 포함합니다.
	 */
	private BooleanExpression companySizeIn(List<CompanySize> companySizes) {
		if (companySizes == null || companySizes.isEmpty())
			return null;
		return recruitment.companySize.isNull().or(recruitment.companySize.in(companySizes));
	}

	/**
	 * 찾으려는 학력 유형을 모두 포함합니다.
	 */
	private BooleanExpression educationLevelIn(List<EducationLevel> educationLevels) {
		if (educationLevels == null || educationLevels.isEmpty())
			return null;
		return position.educationLevel.isNull().or(position.educationLevel.in(educationLevels));
	}

	/**
	 * 찾으려는 지역 유형을 모두 포함합니다.
	 */
	private BooleanExpression areaTypeIn(List<AreaType> areaTypes) {
		if (areaTypes == null || areaTypes.isEmpty())
			return null;
		return position.areaType.isNull().or(position.areaType.in(areaTypes));
	}

	/**
	 * 찾으려는 산업 업종을 모두 포함합니다.
	 */
	private BooleanExpression industryTypeIn(List<IndustryType> industryTypes) {
		if (industryTypes == null || industryTypes.isEmpty())
			return null;
		return recruitment.industryType.isNull().or(recruitment.industryType.in(industryTypes));
	}

	/**
	 * 이미 지원이 끝난 공고도 선택적으로 포함합니다.
	 * 상시 채용은 항상 포함됩니다.
	 */
	private BooleanExpression endDate(boolean showEnded) {
		if (!showEnded) {
			return position.endDate.isNull().or(recruitment.endDate.goe(LocalDateTime.now()));
		}
		return null;
	}

	/**
	 * 조회 결과를 Slice로 변환하는 convert 메서드입니다.
	 */
	private Slice<RecommendedPositionProjection> toSlice(
		List<RecommendedPositionProjection> result,
		Pageable pageable
	) {
		boolean hasNext = false;
		if (result.size() > pageable.getPageSize()) {
			hasNext = true;
			result.remove(pageable.getPageSize());
		}
		return new SliceImpl<>(result, pageable, hasNext);
	}
}
