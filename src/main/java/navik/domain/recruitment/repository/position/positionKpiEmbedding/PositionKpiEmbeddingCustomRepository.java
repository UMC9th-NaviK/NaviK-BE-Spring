package navik.domain.recruitment.repository.position.positionKpiEmbedding;

import java.util.List;

import navik.domain.recruitment.entity.PositionKpiEmbedding;

public interface PositionKpiEmbeddingCustomRepository {
	void batchSaveAll(List<PositionKpiEmbedding> positionKpiEmbeddings);
}
