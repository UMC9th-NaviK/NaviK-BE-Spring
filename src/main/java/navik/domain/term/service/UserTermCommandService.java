package navik.domain.term.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.term.dto.TermResponseDTO;
import navik.domain.term.entity.Term;
import navik.domain.term.entity.UserTerm;
import navik.domain.term.repository.UserTermRepository;
import navik.domain.term.service.factory.UserTermFactory;
import navik.domain.users.entity.User;
import navik.domain.users.service.UserQueryService;

@Service
@RequiredArgsConstructor
@Transactional
public class UserTermCommandService {

	private final TermQueryService termQueryService;
	private final UserQueryService userQueryService;
	private final UserTermRepository userTermRepository;

	public TermResponseDTO.AgreementResultDTO agreeTerms(Long userId, List<Long> termIds) {

		User user = userQueryService.getUser(userId);

		List<Long> agreedTermIds = new ArrayList<>();

		for (Long termId : termIds) {
			Term term = termQueryService.getTerm(termId);

			UserTerm userTerm = UserTermFactory.create(user, term);
			userTermRepository.save(userTerm);
			agreedTermIds.add(termId);

		}

		return new TermResponseDTO.AgreementResultDTO(userId, agreedTermIds);
	}
}
