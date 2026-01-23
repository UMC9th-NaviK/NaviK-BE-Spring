package navik.domain.recruitment.repository.position.positionKpi;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import navik.domain.recruitment.entity.PositionKpi;

@Repository
@RequiredArgsConstructor
public class PositionKpiCustomRepositoryImpl implements PositionKpiCustomRepository {

	private final JdbcTemplate jdbcTemplate;

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

		jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				Timestamp now = Timestamp.valueOf(LocalDateTime.now());

				PositionKpi positionKpi = positionKpis.get(i);
				ps.setLong(1, positionKpi.getPosition().getId());
				ps.setString(2, positionKpi.getContent());
				ps.setTimestamp(3, now);
				ps.setTimestamp(4, now);
			}

			@Override
			public int getBatchSize() {
				return positionKpis.size();
			}
		});
	}
}
