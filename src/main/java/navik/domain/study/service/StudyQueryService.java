package navik.domain.study.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.study.converter.StudyConverter;
import navik.domain.study.dto.StudyDTO;
import navik.domain.study.entity.StudyUser;
import navik.domain.study.enums.StudyRole;
import navik.domain.study.repository.StudyCustomRepository;
import navik.domain.study.repository.StudyUserRepository;
import navik.global.dto.CursorResponseDto;

@Service
@RequiredArgsConstructor
public class StudyQueryService {

	private final StudyUserRepository studyUserRepository;
	private final StudyCustomRepository studyCustomRepository;

	@Transactional(readOnly = true)

	public CursorResponseDto<StudyDTO.MyStudyDTO> getMyStudyList(Long userId, StudyRole role, Long cursor,
		int pageSize) {
		// 1. 커서 기반 목록 조회
		List<StudyUser> myStudyUsers = studyCustomRepository.findMyStudyByCursor(userId, role, cursor, pageSize);

		if (myStudyUsers.isEmpty()) {
			return CursorResponseDto.of(Collections.emptyList(), false, null);
		}

		List<Long> studyIds = myStudyUsers.stream()
			.map(su -> su.getStudy().getId())
			.toList();

		// 2. 참여 인원 일괄 조회
		Map<Long, Integer> participantCountMap = getParticipantCountMap(studyIds);

		// 3. DTO 변환
		List<StudyDTO.MyStudyDTO> content = myStudyUsers.stream()
			.map(su -> StudyConverter.toMyStudyDTO(su, participantCountMap.getOrDefault(su.getStudy().getId(), 0)))
			.toList();

		// 4. 다음 페이지 정보 생성
		boolean hasNext = myStudyUsers.size() >= pageSize;
		String nextCursor = hasNext ? myStudyUsers.get(myStudyUsers.size() - 1).getId().toString() : null;

		return CursorResponseDto.of(content, hasNext, nextCursor);
	}

	private Map<Long, Integer> getParticipantCountMap(List<Long> studyIds) {
		return studyUserRepository.countParticipantsByStudyIds(studyIds).stream()
			.collect(Collectors.toMap(
				obj -> (Long)obj[0],
				obj -> ((Long)obj[1]).intValue()
			));
	}
}
