package navik.domain.recruitment.repository.recruitment;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import navik.domain.ability.entity.QAbility;
import navik.domain.ability.entity.QAbilityEmbedding;
import navik.domain.job.entity.Job;
import navik.domain.kpi.entity.KpiCard;
import navik.domain.recruitment.entity.QPosition;
import navik.domain.recruitment.entity.QPositionKpi;
import navik.domain.recruitment.entity.QPositionKpiEmbedding;
import navik.domain.recruitment.entity.QRecruitment;
import navik.domain.recruitment.entity.Recruitment;
import navik.domain.recruitment.enums.ExperienceType;
import navik.domain.recruitment.enums.MajorType;
import navik.domain.users.entity.User;
import navik.domain.users.enums.EducationLevel;

@Repository
@RequiredArgsConstructor
public class RecruitmentCustomRepositoryImpl implements RecruitmentCustomRepository {

	private final JPAQueryFactory jpaQueryFactory;

	private final QRecruitment recruitment = QRecruitment.recruitment;
	private final QPosition position = QPosition.position;
	private final QPositionKpi positionKpi = QPositionKpi.positionKpi;
	private final QPositionKpiEmbedding positionKpiEmbedding = QPositionKpiEmbedding.positionKpiEmbedding;
	private final QAbility ability = QAbility.ability;
	private final QAbilityEmbedding abilityEmbedding = QAbilityEmbedding.abilityEmbedding;

	@Override
	public List<Recruitment> findRecommendedPosts(
		User user,
		Job job,
		EducationLevel educationType,
		ExperienceType experienceType,
		List<MajorType> majorTypes
	) {

		// 1. 조건 설정
		BooleanExpression where = Stream.of(
				jobEqual(job),
				educationTypeSatisfyAll(educationType),
				experienceTypeSatisfyAll(experienceType),
				majorTypeSatisfyAll(majorTypes),
				endDateSatisfyAll(LocalDateTime.now())
			)
			.reduce(BooleanExpression::and)
			.orElse(null);

		// 2. pgvector 코사인 쿼리
		NumberTemplate<Double> similarityScore = Expressions.numberTemplate(
			Double.class,
			"1 - ({0} <=> {1})",
			positionKpiEmbedding.embedding,
			abilityEmbedding.embedding
		);

		// 3. 조회
		return jpaQueryFactory
			.selectFrom(recruitment)
			.join(recruitment.positions, position)                        // Recruitment -> Position
			.join(position.positionKpis, positionKpi)                     // Position → KPI
			.join(positionKpi.positionKpiEmbedding, positionKpiEmbedding) // KPI -> KPI Embedding
			.join(ability).on(ability.user.eq(user))                      // Ability
			.join(ability.abilityEmbedding, abilityEmbedding)             // Ability -> Embedding
			.where(where)
			.groupBy(recruitment.id)
			.orderBy(similarityScore.sum().desc())  // 유사도 합 상위 5개
			.limit(5)
			.fetch();
	}

	@Override
	public List<Recruitment> findRecommendedPostsByCard(KpiCard kpiCard, Job job) {

		// 1. 조건 설정
		BooleanExpression where = jobEqual(job);

		// 2. pgvector 코사인 쿼리
		NumberTemplate<Double> similarityScore = Expressions.numberTemplate(
			Double.class,
			"1 - ({0} <=> cast({1} as vector))",
			positionKpiEmbedding.embedding,
			Arrays.toString(kpiCard.getKpiCardEmbedding().getEmbedding())
		);

		// 3. 조회
		return jpaQueryFactory
			.selectFrom(recruitment)
			.join(recruitment.positions, position)                        // Recruitment -> Position
			.join(position.positionKpis, positionKpi)                     // Position → KPI
			.join(positionKpi.positionKpiEmbedding, positionKpiEmbedding) // KPI -> KPI Embedding
			.where(where)
			.groupBy(recruitment.id)
			.orderBy(similarityScore.sum().desc())  // 유사도 합 상위 5개
			.limit(5)
			.fetch();
	}

	/**
	 * 사용자의 직무만 포함하는 Expression 입니다.
	 */
	private BooleanExpression jobEqual(Job job) {
		return job != null ? position.job.eq(job) : null;
	}

	/**
	 * 사용자의 전공이 만족되는 직무만 포함하는 Expression 입니다.
	 */
	private BooleanExpression majorTypeSatisfyAll(List<MajorType> majorTypes) {
		BooleanExpression expression = position.majorType.isNull();
		if (!majorTypes.isEmpty()) {
			majorTypes.forEach(majorType -> expression.or(position.majorType.eq(majorType)));
		}
		return expression;
	}

	/**
	 * 입력 시간 이후 및 상시 모집을 포함하는 Expression 입니다.
	 */
	private BooleanExpression endDateSatisfyAll(LocalDateTime localDateTime) {
		BooleanExpression expression = position.endDate.isNull();
		if (localDateTime == null) {
			expression.or(position.endDate.goe(localDateTime));
		}
		return localDateTime != null ? position.endDate.goe(localDateTime) : null;
	}

	/**
	 * 사용자의 학력이 만족되는 직무만 선택하는 Expression 입니다.
	 *
	 * 예시)
	 * 고졸 : null or 고졸
	 * 2년제 : null or 고졸 or 2년제
	 * 4년제 : null or 고졸 or 2년제 or 4년제
	 * 석사 : null or 고졸 or 2년제 or 4년제 or 석사
	 * 박사 : null or 고졸 or 2년제 or 4년제 or 석사 or 박사
	 */
	private BooleanExpression educationTypeSatisfyAll(EducationLevel educationLevel) {
		BooleanExpression expression = position.educationLevel.isNull();
		if (educationLevel != null) {
			List<EducationLevel> educationTypes = Arrays.stream(EducationLevel.values())
				.filter(e -> e.getOrder() <= educationLevel.getOrder())
				.toList();
			expression.or(position.educationLevel.in(educationTypes));
		}
		return expression;
	}

	/**
	 * 사용자의 경력이 만족되는 직무만 노출시키는 Expression 입니다.
	 *
	 * 예시)
	 * 신입 : null or 신업
	 * 경력 : null or 신입 or 경력
	 */
	private BooleanExpression experienceTypeSatisfyAll(ExperienceType experienceType) {
		BooleanExpression expression = position.experienceType.isNull();
		if (experienceType != null) {
			List<ExperienceType> experienceTypes = Arrays.stream(ExperienceType.values())
				.filter(e -> e.getOrder() <= experienceType.getOrder())
				.toList();
			expression.or(position.experienceType.in(experienceTypes));
		}
		return expression;
	}
}
