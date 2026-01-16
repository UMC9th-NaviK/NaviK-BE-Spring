package navik.domain.term.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.term.dto.TermResponseDTO;
import navik.domain.term.entity.Term;
import navik.domain.term.entity.UserTerm;
import navik.domain.term.repository.TermRepository;
import navik.domain.term.repository.UserTermRepository;
import navik.domain.term.service.factory.UserTermFactory;
import navik.domain.users.entity.User;
import navik.domain.users.service.UserQueryService;

@Service
@RequiredArgsConstructor
@Transactional
public class UserTermCommandService {

	private final UserQueryService userQueryService;
	private final UserTermRepository userTermRepository;
	private final TermRepository termRepository;

	public TermResponseDTO.AgreementResultDTO agreeTerms(Long userId, List<Long> termIds) {

		User user = userQueryService.getUser(userId);

		List<Term> terms = termRepository.findAllById(termIds);

		List<UserTerm> userTerms = terms.stream().map(term -> UserTermFactory.create(user, term)).toList();

		userTermRepository.saveAll(userTerms);

		return new TermResponseDTO.AgreementResultDTO(userId, termIds);
	}
}
