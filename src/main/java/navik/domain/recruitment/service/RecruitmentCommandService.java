package navik.domain.recruitment.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.job.entity.Job;
import navik.domain.job.exception.code.JobErrorCode;
import navik.domain.job.repository.JobRepository;
import navik.domain.recruitment.converter.PositionConverter;
import navik.domain.recruitment.converter.PositionKpiConverter;
import navik.domain.recruitment.converter.RecruitmentConverter;
import navik.domain.recruitment.entity.Position;
import navik.domain.recruitment.entity.PositionKpi;
import navik.domain.recruitment.entity.Recruitment;
import navik.domain.recruitment.exception.code.RecruitmentErrorCode;
import navik.domain.recruitment.repository.PositionKpiRepository;
import navik.domain.recruitment.repository.PositionRepository;
import navik.domain.recruitment.repository.RecruitmentRepository;
import navik.global.ai.dto.LLMResponseDTO;
import navik.global.ai.service.EmbeddingService;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RecruitmentCommandService {

	private final RecruitmentRepository recruitmentRepository;
	private final PositionRepository positionRepository;
	private final PositionKpiRepository positionKpiRepository;
	private final JobRepository jobRepository;
	private final EmbeddingService embeddingService;

	/**
	 * 채용 공고 한 건을 입력받아 KPI 임베딩 후 DB에 적재합니다.
	 *
	 * @param request
	 */
	public void createRecruitment(LLMResponseDTO.Recruitment request) {

		// 1. 이미 등록된 채용 공고인지 확인
		String postId = request.getPostId();
		if (recruitmentRepository.existsByPostId(postId))
			throw new GeneralExceptionHandler(RecruitmentErrorCode.DUPLICATE_POST_ID);

		// 2. 채용 공고 생성 및 저장
		Recruitment recruitment = RecruitmentConverter.toEntity(request);
		recruitmentRepository.save(recruitment);

		// 3. 공고 별 모집 분야 생성
		request.getPositions().forEach(positionDTO -> {
			// 직무 탐색
			Job job = jobRepository.findByJobType(positionDTO.getJobType())
				.orElseThrow(() -> new GeneralExceptionHandler(JobErrorCode.NOT_FOUND_JOB));

			// 모집 분야 생성 및 저장
			Position position = PositionConverter.toEntity(positionDTO, recruitment, job);
			positionRepository.save(position);

			// 모집 분야 별 KPI 목록 생성
			List<PositionKpi> kpis = positionDTO.getKpis().stream()
				.map(content -> {
					float[] embedding = embeddingService.embed(content);  // 의미 임베딩
					return PositionKpiConverter.toEntity(position, content, embedding);
				})
				.toList();

			// KPI saveAll
			positionKpiRepository.saveAll(kpis);
		});

		log.info("채용 공고 적재 완료 - {}", request.getLink());
	}
}
