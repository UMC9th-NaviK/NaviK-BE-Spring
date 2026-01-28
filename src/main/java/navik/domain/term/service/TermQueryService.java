package navik.domain.term.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.term.dto.TermResponseDTO;
import navik.domain.term.repository.TermRepository;
import navik.domain.term.repository.projection.TermInfoView;
import navik.global.apiPayload.exception.code.GeneralErrorCode;
import navik.global.apiPayload.exception.exception.GeneralExceptionHandler;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermQueryService {

	private final TermRepository termRepository;

	public TermResponseDTO.TermInfo getTermInfo(Long termId) {
		TermInfoView termInfoView = termRepository.getTermInfo(termId);

		if (termInfoView == null) {
			throw new GeneralExceptionHandler(GeneralErrorCode.ENTITY_NOT_FOUND);
		}
		return new TermResponseDTO.TermInfo(termInfoView.getId(), termInfoView.getContent(),
			termInfoView.getUpdatedAt());
	}
}
