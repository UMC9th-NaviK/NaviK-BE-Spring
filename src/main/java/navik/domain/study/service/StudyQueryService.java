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
		// 1. 커서 기반 목록 조회 (리포지토리에서 pageSize + 1개를 조회함)
		List<StudyUser> myStudyUsers = studyCustomRepository.findMyStudyByCursor(userId, role, cursor, pageSize);

		if (myStudyUsers.isEmpty()) {
			return CursorResponseDto.of(Collections.emptyList(), false, null);
		}

		// 2. 다음 페이지 존재 여부 확인 및 리스트 정제
		boolean hasNext = myStudyUsers.size() > pageSize;

		// 다음 페이지 확인용으로 가져온 마지막(pageSize + 1번째) 항목은 실제 데이터에서 제외
		List<StudyUser> pagingList = hasNext ? myStudyUsers.subList(0, pageSize) : myStudyUsers;

		// 3. 정제된 리스트(pagingList)를 바탕으로 ID 추출 및 N+1 방지 조회
		List<Long> studyIds = pagingList.stream()
			.map(su -> su.getStudy().getId())
			.toList();

		Map<Long, Integer> participantCountMap = getParticipantCountMap(studyIds);

		// 4. DTO 변환
		List<StudyDTO.MyStudyDTO> content = pagingList.stream()
			.map(su -> StudyConverter.toMyStudyDTO(su, participantCountMap.getOrDefault(su.getStudy().getId(), 0)))
			.toList();

		// 5. 다음 커서값 생성 (실제 응답에 포함된 마지막 항목의 ID 기준)
		String nextCursor = hasNext ? pagingList.get(pagingList.size() - 1).getId().toString() : null;

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
