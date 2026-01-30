package navik.domain.recruitment.repository.position.position;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.ability.entity.QAbility;
import navik.domain.ability.entity.QAbilityEmbedding;
import navik.domain.job.entity.Job;
import navik.domain.recruitment.dto.position.PositionRequestDTO;
import navik.domain.recruitment.entity.Position;
import navik.domain.recruitment.entity.QPosition;
import navik.domain.recruitment.entity.QPositionKpi;
import navik.domain.recruitment.entity.QPositionKpiEmbedding;
import navik.domain.recruitment.entity.QRecruitment;
import navik.domain.recruitment.enums.AreaType;
import navik.domain.recruitment.enums.CompanySize;
import navik.domain.recruitment.enums.EmploymentType;
import navik.domain.recruitment.enums.ExperienceType;
import navik.domain.recruitment.enums.IndustryType;
import navik.domain.recruitment.repository.position.position.projection.QRecommendedPositionProjection;
import navik.domain.recruitment.repository.position.position.projection.RecommendedPositionProjection;
import navik.domain.users.entity.User;
import navik.domain.users.enums.EducationLevel;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PositionCustomRepositoryImpl implements PositionCustomRepository {

	private final JPAQueryFactory jpaQueryFactory;
	private final JdbcTemplate jdbcTemplate;

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
		PositionRequestDTO.CursorRequest cursorRequest,
		Pageable pageable
	) {

		// 1. pgvector 코사인 쿼리
		NumberTemplate<Double> similarityQuery = Expressions.numberTemplate(
			Double.class,
			"1.0 - cast(function('cosine_distance', {0}, {1}) as double)",
			positionKpiEmbedding.embedding,
			abilityEmbedding.embedding
		);

		/*
		 * 2. 최대한 나에게 적합한 공고 추천을 위해 유사도가 0.3 이상만 summation
		 * 	   but, 유사성 없어도 어쨌든 전체 검색을 위한 창이므로 recruitment를 남기기 위해 where 필터링은 X
		 */
		NumberExpression<Double> similaritySum = new CaseBuilder()
			.when(similarityQuery.gt(0.3))
			.then(similarityQuery)
			.otherwise(0.0)
			.sum();

		// 3. 조건 설정
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
			.filter(Objects::nonNull)
			.reduce(BooleanExpression::and)
			.orElse(null);

		// 4. 조회
		List<RecommendedPositionProjection> result = jpaQueryFactory
			.select(new QRecommendedPositionProjection(
				position,
				similaritySum,
				positionKpi.count()
			))
			.from(position)
			.join(position.recruitment, recruitment)
			.join(position.positionKpis, positionKpi)
			.join(positionKpi.positionKpiEmbedding, positionKpiEmbedding)
			.join(ability).on(ability.user.eq(user))
			.join(ability.abilityEmbedding, abilityEmbedding)
			.where(where)
			.groupBy(position)
			.having(cursorExpression(cursorRequest, similaritySum, positionKpi.count()))
			.orderBy(
				similaritySum.desc(),
				positionKpi.count().desc(),
				position.id.asc()
			)
			.limit(pageable.getPageSize() + 1)
			.fetch();

		// 5. Slice 반환
		return toSlice(result, pageable);
	}

	/**
	 * Position에 대한 Batch Insert를 수행합니다.
	 * PK에 대한 set도 처리합니다.
	 */
	@Override
	public void batchSaveAll(List<Position> positions) {
		String sql = """
			INSERT INTO positions (
			    job_id,
			    recruitment_id,
			    name,
			    employment_type,
			    experience_type,
			    education_level,
			    area_type,
			    major_type,
			    work_place,
			    created_at,
			    updated_at
			)
			VALUES (
			    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
			)
			""";

		jdbcTemplate.execute((ConnectionCallback<Void>)con -> {
			try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
				Timestamp now = Timestamp.valueOf(LocalDateTime.now());

				for (Position position : positions) {
					ps.setLong(1, position.getJob().getId());
					ps.setLong(2, position.getRecruitment().getId());
					ps.setString(3, position.getName());
					ps.setObject(4, position.getEmploymentType() != null ? position.getEmploymentType().name() : null);
					ps.setObject(5, position.getExperienceType() != null ? position.getExperienceType().name() : null);
					ps.setObject(6, position.getEducationLevel() != null ? position.getEducationLevel().name() : null);
					ps.setObject(7, position.getAreaType() != null ? position.getAreaType().name() : null);
					ps.setObject(8, position.getMajorType() != null ? position.getMajorType().name() : null);
					ps.setString(9, position.getWorkPlace());
					ps.setTimestamp(10, now);
					ps.setTimestamp(11, now);
					ps.addBatch();
				}

				// 쿼리 실행
				ps.executeBatch();

				// PK 설정
				try (ResultSet rs = ps.getGeneratedKeys()) {
					int index = 0;
					while (rs.next()) {
						long generatedId = rs.getLong("id");
						positions.get(index).assignId(generatedId);
						index++;
					}
					if (index != positions.size()) {
						throw new IllegalStateException("Position 개수와 PK 개수가 일치하지 않습니다.");
					}
				}
				return null;
			}
		});
	}

	/**
	 * 해당 직무로 지원 가능한 포지션을 선택합니다.
	 */
	private BooleanExpression jobIn(List<Job> jobs) {
		if (jobs == null || jobs.isEmpty())
			return null;
		return position.job.isNull().or(position.job.in(jobs));
	}

	/**
	 * 해당 경력으로 지원 가능한 포지션을 선택합니다.
	 */
	private BooleanExpression experienceTypeIn(List<ExperienceType> experienceTypes) {
		if (experienceTypes == null || experienceTypes.isEmpty())
			return null;
		return position.experienceType.isNull().or(position.experienceType.in(experienceTypes));
	}

	/**
	 * 해당 근무 형태에 해당하는 포지션을 선택합니다.
	 */
	private BooleanExpression employmentTypeIn(List<EmploymentType> employmentTypes) {
		if (employmentTypes == null || employmentTypes.isEmpty())
			return null;
		return position.employmentType.isNull().or(position.employmentType.in(employmentTypes));
	}

	/**
	 * 해당 회사 규모에 해당하는 공고를 선택합니다.
	 */
	private BooleanExpression companySizeIn(List<CompanySize> companySizes) {
		if (companySizes == null || companySizes.isEmpty())
			return null;
		return recruitment.companySize.isNull().or(recruitment.companySize.in(companySizes));
	}

	/**
	 * 해당 학력으로 지원 가능한 포지션을 선택합니다.
	 */
	private BooleanExpression educationLevelIn(List<EducationLevel> educationLevels) {
		if (educationLevels == null || educationLevels.isEmpty())
			return null;
		return position.educationLevel.isNull().or(position.educationLevel.in(educationLevels));
	}

	/**
	 * 해당 지역에 해당하는 포지션을 선택합니다.
	 */
	private BooleanExpression areaTypeIn(List<AreaType> areaTypes) {
		if (areaTypes == null || areaTypes.isEmpty())
			return null;
		return position.areaType.isNull().or(position.areaType.in(areaTypes));
	}

	/**
	 * 해당 산업 업종에 해당하는 공고를 선택합니다.
	 */
	private BooleanExpression industryTypeIn(List<IndustryType> industryTypes) {
		if (industryTypes == null || industryTypes.isEmpty())
			return null;
		return recruitment.industryType.isNull().or(recruitment.industryType.in(industryTypes));
	}

	/**
	 * 아직 모집 중인 공고와 상시 모집 공고를 선택합니다.
	 */
	private BooleanExpression endDate(boolean showEnded) {
		if (!showEnded)
			return recruitment.endDate.isNull().or(recruitment.endDate.goe(LocalDateTime.now()));
		return null;
	}

	/**
	 * 커서에 대한 Where절을 생성합니다.
	 * 	 1순위: 유사도 합산 Desc
	 * 	 2순위: 매칭 개수 Desc
	 * 	 3순위: PK Asc
	 */
	private BooleanExpression cursorExpression(PositionRequestDTO.CursorRequest cursorRequest,
		NumberExpression<Double> scoreSum,
		NumberExpression<Long> matchCount) {
		if (cursorRequest == null || cursorRequest.getLastId() == null
			|| cursorRequest.getLastSimilarity() == null || cursorRequest.getLastMatchCount() == null)
			return null;

		Double lastScore = cursorRequest.getLastSimilarity();
		Long lastCount = cursorRequest.getLastMatchCount();
		Long lastId = cursorRequest.getLastId();

		// Case 1 : 유사도 합산이 다름
		BooleanExpression scoreCondition = scoreSum.lt(lastScore);

		// Case 2 : 유사도 합산이 같은데, 매칭 개수가 다름
		BooleanExpression countCondition = scoreSum.eq(lastScore).and(matchCount.lt(lastCount));

		// Case 3 : 유사도 합산도 같고, 매칭 개수도 같음
		BooleanExpression idCondition = scoreSum.eq(lastScore)
			.and(matchCount.eq(lastCount))
			.and(position.id.gt(lastId));

		return scoreCondition.or(countCondition).or(idCondition);
	}

	/**
	 * 쿼리 결과를 Slice로 변환합니다.
	 */
	private Slice<RecommendedPositionProjection> toSlice(List<RecommendedPositionProjection> result,
		Pageable pageable) {
		boolean hasNext = false;
		if (result.size() > pageable.getPageSize()) {
			hasNext = true;
			result.remove(pageable.getPageSize());
		}
		return new SliceImpl<>(result, pageable, hasNext);
	}
}
