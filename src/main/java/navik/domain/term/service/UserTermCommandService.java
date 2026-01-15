package navik.domain.term.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.term.repository.TermRepository;
import navik.domain.users.service.UserQueryService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserTermCommandService {
	TermRepository termRepository;
	UserQueryService userQueryService;
}
