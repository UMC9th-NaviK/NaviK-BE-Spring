package navik.domain.term.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.term.repository.TermRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermQueryService {
	TermRepository termRepository;
}
