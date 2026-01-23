package navik.domain.recruitment.service.recruitment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.recruitment.dto.recruitment.RecruitmentRequestDTO;
import navik.domain.recruitment.repository.position.PositionRepository;
import navik.domain.recruitment.repository.recruitment.RecruitmentRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class RecruitmentCommandService {

	private final RecruitmentRepository recruitmentRepository;
	private final PositionRepository positionRepository;
	private final PositionKpiRepository positionKpiRepository;

	public void saveRecruitment(RecruitmentRequestDTO.Recruitment recruitment) {

	}
}
