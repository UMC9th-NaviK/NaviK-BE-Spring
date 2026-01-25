package navik.domain.recruitment.service.recruitment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.job.entity.Job;
import navik.domain.job.repository.JobRepository;
import navik.domain.recruitment.converter.position.PositionConverter;
import navik.domain.recruitment.converter.position.PositionKpiConverter;
import navik.domain.recruitment.converter.position.PositionKpiEmbeddingConverter;
import navik.domain.recruitment.converter.recruitment.RecruitmentConverter;
import navik.domain.recruitment.dto.recruitment.RecruitmentRequestDTO;
import navik.domain.recruitment.entity.Position;
import navik.domain.recruitment.entity.PositionKpi;
import navik.domain.recruitment.entity.PositionKpiEmbedding;
import navik.domain.recruitment.entity.Recruitment;
import navik.domain.recruitment.enums.JobType;
import navik.domain.recruitment.repository.position.position.PositionRepository;
import navik.domain.recruitment.repository.position.positionKpi.PositionKpiRepository;
import navik.domain.recruitment.repository.position.positionKpiEmbedding.PositionKpiEmbeddingRepository;
import navik.domain.recruitment.repository.recruitment.RecruitmentRepository;
import navik.global.ai.EmbeddingClient;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RecruitmentCommandService {

	private final RecruitmentRepository recruitmentRepository;
	private final PositionRepository positionRepository;
	private final PositionKpiRepository positionKpiRepository;
	private final PositionKpiEmbeddingRepository positionKpiEmbeddingRepository;
	private final JobRepository jobRepository;
	private final EmbeddingClient embeddingClient;

	public void saveRecruitment(RecruitmentRequestDTO.Recruitment recruitmentDTO) {

		if (recruitmentRepository.existsByPostId(recruitmentDTO.getPostId())) {
			log.info("[RecruitmentCommandService] 이미 등록된 채용 공고입니다.");
			return;
		}

		// 1. 공고 저장
		Recruitment recruitment = RecruitmentConverter.toEntity(recruitmentDTO);
		recruitmentRepository.save(recruitment);

		// 2. Job 탐색
		List<String> jobNames = new ArrayList<>();
		recruitmentDTO.getPositions().forEach(
			position -> jobNames.add(position.getJobType().getLabel())
		);
		List<Job> jobs = jobRepository.findByNameIn(jobNames);
		Map<JobType, Job> jobMap = jobs.stream()
			.map(job -> Map.entry(JobType.getByLabel(job.getName()), job))
			.filter(entry -> entry.getKey() != null)
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				Map.Entry::getValue
			));

		// 3. List 저장
		List<Position> positions = new ArrayList<>();
		List<PositionKpi> positionKpis = new ArrayList<>();
		List<PositionKpiEmbedding> positionKpiEmbeddings = new ArrayList<>();
		recruitmentDTO.getPositions().forEach(
			positionDTO -> {
				Job job = jobMap.get(positionDTO.getJobType());
				Position position = PositionConverter.toEntity(positionDTO, recruitment, job);
				positions.add(position);
				positionDTO.getKpis().forEach(
					content -> {
						PositionKpi positionKpi = PositionKpiConverter.toEntity(position, content);
						float[] embedding = embeddingClient.embed(content);    // 문장 의미 임베딩
						PositionKpiEmbedding positionKpiEmbedding = PositionKpiEmbeddingConverter.toEntity(positionKpi,
							embedding);
						positionKpis.add(positionKpi);
						positionKpiEmbeddings.add(positionKpiEmbedding);
					}
				);
			}
		);

		// 4. Batch insert
		positionRepository.batchSaveAll(positions);
		positionKpiRepository.batchSaveAll(positionKpis);
		positionKpiEmbeddingRepository.batchSaveAll(positionKpiEmbeddings);
	}
}
