package navik.domain.study.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import navik.domain.study.converter.StudyConverter;
import navik.domain.study.dto.StudyCreateDTO;
import navik.domain.study.entity.Study;
import navik.domain.study.repository.StudyRepository;

@Service
@RequiredArgsConstructor
public class StudyCommandService {

	private final StudyRepository studyRepository;

	public Long createStudy(StudyCreateDTO.CreateDTO request) {
		Study study = StudyConverter.toStudy(request);
		return studyRepository.save(study).getId();
	}
}
