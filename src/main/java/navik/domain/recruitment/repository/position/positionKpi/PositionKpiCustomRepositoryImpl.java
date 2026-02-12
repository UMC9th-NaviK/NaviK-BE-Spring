package navik.domain.recruitment.repository.position.positionKpi;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import navik.domain.recruitment.entity.PositionKpi;
import navik.domain.recruitment.entity.QPositionKpi;

@Repository
@RequiredArgsConstructor
public class PositionKpiCustomRepositoryImpl implements PositionKpiCustomRepository {

	private final JdbcTemplate jdbcTemplate;
	private final JPAQueryFactory jpaQueryFactory;

	private final QPositionKpi positionKpi = QPositionKpi.positionKpi;

	/**
	 * PositionKpi에 대한 Batch Insert를 수행합니다.
	 */
	@Override
	public void batchSaveAll(List<PositionKpi> positionKpis) {
		String sql = """
			INSERT INTO position_kpis (
				position_id,
				content,
				created_at,
				updated_at
			)
			VALUES (
				?, ?, ?, ?
			)
			""";

		jdbcTemplate.execute((ConnectionCallback<Void>)con -> {
			try (PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
				Timestamp now = Timestamp.valueOf(LocalDateTime.now());

				for (PositionKpi positionKpi : positionKpis) {
					ps.setLong(1, positionKpi.getPosition().getId());
					ps.setString(2, positionKpi.getContent());
					ps.setTimestamp(3, now);
					ps.setTimestamp(4, now);
					ps.addBatch();
				}

				ps.executeBatch();

				// PK 설정
				try (ResultSet rs = ps.getGeneratedKeys()) {
					int index = 0;
					while (rs.next()) {
						long generatedId = rs.getLong("id");
						positionKpis.get(index).assignId(generatedId);
						index++;
					}
					if (index != positionKpis.size()) {
						throw new IllegalStateException("KPI 개수와 PK 개수가 일치하지 않습니다.");
					}
				}
				return null;
			}
		});
	}

	@Override
	public Map<Long, List<String>> findPositionKpiMapByPositionIds(List<Long> positionIds) {
		if (positionIds == null || positionIds.isEmpty()) {
			return Collections.emptyMap();
		}

		List<Tuple> results = jpaQueryFactory
			.select(
				positionKpi.position.id,
				positionKpi.content
			)
			.from(positionKpi)
			.where(positionKpi.position.id.in(positionIds))
			.fetch();
		
		return results.stream()
			.collect(Collectors.groupingBy(
				tuple -> tuple.get(positionKpi.position.id), // Key
				Collectors.mapping(
					tuple -> tuple.get(positionKpi.content), // Value
					Collectors.toList()
				)
			));
	}
}
