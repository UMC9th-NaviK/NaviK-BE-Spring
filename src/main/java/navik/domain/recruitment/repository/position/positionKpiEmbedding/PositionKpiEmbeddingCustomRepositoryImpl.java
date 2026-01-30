package navik.domain.recruitment.repository.position.positionKpiEmbedding;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.StringJoiner;

import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import navik.domain.recruitment.entity.PositionKpiEmbedding;

@Repository
@RequiredArgsConstructor
public class PositionKpiEmbeddingCustomRepositoryImpl implements PositionKpiEmbeddingCustomRepository {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public void batchSaveAll(List<PositionKpiEmbedding> positionKpiEmbeddings) {
		String sql = """
			INSERT INTO position_kpi_embeddings (
			    position_kpi_id,
			    embedding,
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
				PositionKpiEmbedding positionKpiEmbedding = positionKpiEmbeddings.get(i);

				StringJoiner joiner = new StringJoiner(",", "[", "]");
				for (float value : positionKpiEmbedding.getEmbedding()) {
					joiner.add(String.valueOf(value));
				}

				PGobject pgObject = new PGobject();
				pgObject.setType("vector");
				pgObject.setValue(joiner.toString());

				ps.setLong(1, positionKpiEmbedding.getPositionKpi().getId());
				ps.setObject(2, pgObject);
				ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
				ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
			}

			@Override
			public int getBatchSize() {
				return positionKpiEmbeddings.size();
			}
		});
	}
}
