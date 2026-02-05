package navik.domain.recruitment.repository.recruitment;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.data.domain.Pageable;
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
import navik.domain.recruitment.enums.ExperienceType;
import navik.domain.recruitment.enums.MajorType;
import navik.domain.recruitment.repository.recruitment.projection.QRecommendedRecruitmentProjection;
import navik.domain.recruitment.repository.recruitment.projection.RecommendedRecruitmentProjection;
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
	public List<RecommendedRecruitmentProjection> findRecommendedPosts(
		User user,
		Job job,
		EducationLevel educationLevel,
		ExperienceType experienceType,
		List<MajorType> majorTypes,
		Pageable pageable
	) {

		// 1. pgvector 코사인 쿼리
		NumberTemplate<Double> similarityQuery = Expressions.numberTemplate(
			Double.class,
			"1.0 - cast(function('cosine_distance', {0}, {1}) as double)",
			positionKpiEmbedding.embedding,
			abilityEmbedding.embedding
		);

		// 2. 조건 설정
		BooleanExpression where = Stream.of(
				jobSatisfy(job),
				educationLevelSatisfy(educationLevel),
				experienceTypeSatisfy(experienceType),
				majorTypeSatisfy(majorTypes),
				endDateSatisfy(),
				similarityQuery.goe(0.4)
			)
			.filter(Objects::nonNull)
			.reduce(BooleanExpression::and)
			.orElse(null);

		// 3. 조회
		return jpaQueryFactory
			.select(new QRecommendedRecruitmentProjection(
				recruitment,
				similarityQuery.sum(),
				positionKpi.count()
			))
			.from(recruitment)
			.join(recruitment.positions, position)                         // Recruitment -> Position
			.join(position.positionKpis, positionKpi)                      // Position → KPI
			.join(positionKpi.positionKpiEmbedding, positionKpiEmbedding)  // KPI -> KPI Embedding
			.join(ability).on(ability.user.eq(user))                       // Ability
			.join(ability.abilityEmbedding, abilityEmbedding)              // Ability -> Embedding
			.where(where)
			.groupBy(recruitment)
			.having(positionKpi.count().goe(3))  // 매칭되는 KPI는 최소 3개 이상
			.orderBy(
				similarityQuery.sum().desc(),  // 1순위: 유사도 합산
				positionKpi.count().desc(),    // 2순위: 매칭 개수
				recruitment.id.asc()           // 3순위: PK
			)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();
	}

	@Override
	public List<RecommendedRecruitmentProjection> findRecommendedPostsByCard(KpiCard kpiCard, Job job) {

		// 1. pgvector 코사인 쿼리
		NumberTemplate<Double> similarityQuery = Expressions.numberTemplate(
			Double.class,
			"1.0 - cast(function('cosine_distance', {0}, {1}) as double)",
			positionKpiEmbedding.embedding,
			kpiCard.getKpiCardEmbedding().getEmbedding()
		);

		// 2. 조건 설정
		BooleanExpression where = Stream.of(
				jobSatisfy(job),
				endDateSatisfy(),
				similarityQuery.goe(0.4)
			)
			.reduce(BooleanExpression::and)
			.filter(Objects::nonNull)
			.orElse(null);

		// 3. 조회
		return jpaQueryFactory
			.select(new QRecommendedRecruitmentProjection(
				recruitment,
				similarityQuery.sum(),
				positionKpi.count()
			))
			.from(recruitment)
			.join(recruitment.positions, position)                        // Recruitment -> Position
			.join(position.positionKpis, positionKpi)                     // Position → KPI
			.join(positionKpi.positionKpiEmbedding, positionKpiEmbedding) // KPI -> KPI Embedding
			.where(where)
			.groupBy(recruitment)
			.having(positionKpi.count().goe(3))  // 매칭되는 KPI는 최소 3개 이상
			.orderBy(
				similarityQuery.sum().desc(),   // 1순위: 유사도 합산
				positionKpi.count().desc(),     // 2순위: 매칭 개수
				recruitment.id.asc()            // 3순위: PK
			)
			.limit(5)  // 유사도 합 상위 5개
			.fetch();
	}

	/**
	 * 해당 직무로 지원 가능한 공고를 선택합니다.
	 */
	private BooleanExpression jobSatisfy(Job job) {
		if (job == null)
			return position.job.isNull();
		return position.job.isNull().or(position.job.eq(job));
	}

	/**
	 * 해당 전공으로 지원 가능한 공고를 선택합니다.
	 */
	private BooleanExpression majorTypeSatisfy(List<MajorType> majorTypes) {
		if (majorTypes == null || majorTypes.isEmpty())
			return position.majorType.isNull();
		return position.majorType.isNull().or(position.majorType.in(majorTypes));
	}

	/**
	 * 아직 모집 중인 공고와 상시 모집 공고를 선택합니다.
	 */
	private BooleanExpression endDateSatisfy() {
		return recruitment.endDate.isNull().or(recruitment.endDate.goe(LocalDateTime.now()));
	}

	/**
	 * 해당 학력으로 지원 가능한 공고를 선택합니다.
	 *
	 * 예시)
	 * 고졸 : null or 고졸
	 * 2년제 : null or 고졸 or 2년제
	 * 4년제 : null or 고졸 or 2년제 or 4년제
	 * 석사 : null or 고졸 or 2년제 or 4년제 or 석사
	 * 박사 : null or 고졸 or 2년제 or 4년제 or 석사 or 박사
	 */
	private BooleanExpression educationLevelSatisfy(EducationLevel educationLevel) {
		if (educationLevel == null)
			return position.educationLevel.isNull();
		List<EducationLevel> educationTypes = Arrays.stream(EducationLevel.values())
			.filter(e -> e.getOrder() <= educationLevel.getOrder())
			.toList();
		return position.educationLevel.isNull().or(position.educationLevel.in(educationTypes));
	}

	/**
	 * 해당 경력으로 지원 가능한 공고를 선택합니다.
	 *
	 * 예시)
	 * 신입 : null or 신업
	 * 경력 : null or 신입 or 경력
	 */
	private BooleanExpression experienceTypeSatisfy(ExperienceType experienceType) {
		if (experienceType == null)
			return position.experienceType.isNull();
		List<ExperienceType> experienceTypes = Arrays.stream(ExperienceType.values())
			.filter(e -> e.getOrder() <= experienceType.getOrder())
			.toList();
		return position.experienceType.isNull().or(position.experienceType.in(experienceTypes));
	}
}
