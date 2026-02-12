package navik.domain.recruitment.repository.position.position;

import static navik.domain.job.entity.QJob.*;

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

		// 1. pgvector 코사인 쿼리 (역량이 없어도 어쨌든 전체 검색이므로 검색이 되도록, 역량 있으면 매칭 개수 Desc)
		NumberExpression<Double> similarityQuery = new CaseBuilder()
			.when(abilityEmbedding.embedding.isNull())
			.then(0.0)
			.otherwise(
				Expressions.numberTemplate(
					Double.class,
					"1.0 - cast(function('cosine_distance', {0}, {1}) as double)",
					positionKpiEmbedding.embedding,
					abilityEmbedding.embedding
				)
			);

		// 2. 유효 매칭 카운트
		NumberExpression<Long> matchCount = new CaseBuilder()
			.when(similarityQuery.goe(0.42))
			.then(positionKpi.id)
			.otherwise((Long)null) // count 집계 대상 제외
			.countDistinct();

		// 3. 매칭 평균 유사도
		NumberExpression<Double> similarityAvg = new CaseBuilder()
			.when(similarityQuery.goe(0.42))
			.then(similarityQuery)
			.otherwise((Double)null) // 마찬가지, 집계 대상 제외
			.avg()
			.coalesce(0.0);

		// 4. 조건 설정
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

		// 5. 조회
		List<RecommendedPositionProjection> result = jpaQueryFactory
			.select(new QRecommendedPositionProjection(
				position.id,
				position.name,
				position.experienceType,
				position.educationLevel,
				position.majorType,
				position.workPlace,
				position.employmentType,
				recruitment.postId,
				recruitment.link,
				recruitment.companyLogo,
				recruitment.companySize,
				recruitment.companyName,
				recruitment.endDate,
				recruitment.industryType,
				recruitment.title,
				job.name,
				similarityAvg,
				matchCount
			))
			.from(position)
			.join(position.recruitment, recruitment)
			.leftJoin(position.job, job)
			.join(position.positionKpis, positionKpi)
			.join(positionKpi.positionKpiEmbedding, positionKpiEmbedding)
			.leftJoin(ability).on(ability.user.eq(user))
			.leftJoin(ability.abilityEmbedding, abilityEmbedding)
			.where(where)
			.groupBy(position, recruitment, job)
			.having(cursorExpression(cursorRequest, matchCount, similarityAvg))
			.orderBy(
				matchCount.desc(),     // 매칭 개수
				similarityAvg.desc(),  // 평균 매칭 유사도
				position.id.asc()      // PK
			)
			.limit(pageable.getPageSize() + 1)
			.fetch();

		// 5. Slice 반환
		return toSlice(result, pageable);
	}

	@Override
	public Long countPositions(List<Job> jobs, PositionRequestDTO.SearchCondition searchCondition) {

		// 1. 조건 설정
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

		// 2. Total Count Query
		return jpaQueryFactory
			.select(position.countDistinct())
			.from(position)
			.join(position.recruitment, recruitment)
			.where(where)
			.fetchOne();
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
	 * 	 1순위: 매칭 개수 Desc
	 * 	 2순위: 유사도 평균 Desc
	 * 	 3순위: PK Asc
	 */
	private BooleanExpression cursorExpression(PositionRequestDTO.CursorRequest cursorRequest,
		NumberExpression<Long> matchCount,
		NumberExpression<Double> similarityAvg) {
		if (cursorRequest == null || cursorRequest.getLastId() == null
			|| cursorRequest.getLastSimilarity() == null || cursorRequest.getLastMatchCount() == null)
			return null;

		Long lastCount = cursorRequest.getLastMatchCount();
		Double lastAvg = cursorRequest.getLastSimilarity();
		Long lastId = cursorRequest.getLastId();

		// 매칭 개수가 작음
		BooleanExpression condition1 = matchCount.lt(lastCount);

		// 매칭 개수는 같은데, 평균이 작음
		BooleanExpression condition2 = matchCount.eq(lastCount)
			.and(similarityAvg.lt(lastAvg));

		// 매칭 개수와 평균이 같고, PK가 큼
		BooleanExpression condition3 = matchCount.eq(lastCount)
			.and(similarityAvg.eq(lastAvg))
			.and(position.id.gt(lastId));

		return condition1.or(condition2).or(condition3);
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
