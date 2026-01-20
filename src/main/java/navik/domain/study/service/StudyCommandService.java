package navik.domain.study.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.study.converter.StudyConverter;
import navik.domain.study.dto.StudyCreateDTO;
import navik.domain.study.entity.Study;
import navik.domain.study.repository.StudyRepository;

@Service
@RequiredArgsConstructor
public class StudyCommandService {

	private final StudyRepository studyRepository;

	@Transactional
	public Long createStudy(StudyCreateDTO.CreateDTO request, Long userId) {
		Study study = StudyConverter.toStudy(request);
		return studyRepository.save(study).getId();
	}
}
