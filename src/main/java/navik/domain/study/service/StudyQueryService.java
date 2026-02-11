package navik.domain.study.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.kpi.dto.res.KpiCardResponseDTO;
import navik.domain.kpi.entity.KpiCard;
import navik.domain.kpi.service.query.KpiScoreQueryService;
import navik.domain.study.converter.StudyConverter;
import navik.domain.study.converter.StudyKpiCardConverter;
import navik.domain.study.converter.StudyRecommendConverter;
import navik.domain.study.dto.StudyApplicationDTO;
import navik.domain.study.dto.StudyDTO;
import navik.domain.study.dto.StudyKpiCardDTO;
import navik.domain.study.dto.StudyRecommendDTO;
import navik.domain.study.entity.Study;
import navik.domain.study.entity.StudyKpi;
import navik.domain.study.entity.StudyUser;
import navik.domain.study.enums.StudyRole;
import navik.domain.study.repository.StudyCustomRepository;
import navik.domain.study.repository.StudyKpiRepository;
import navik.domain.study.repository.StudyRepository;
import navik.domain.study.repository.StudyUserRepository;
import navik.global.dto.CursorResponseDTO;

@Service
@RequiredArgsConstructor
public class StudyQueryService {

	private final StudyUserRepository studyUserRepository;
	private final StudyCustomRepository studyCustomRepository;
	private final StudyKpiRepository studyKpiRepository;
	private final KpiScoreQueryService kpiScoreQueryService;
	private final StudyRepository studyRepository;

	/**
	 * 나의 스터디 조회
	 * @param userId
	 * @param role
	 * @param cursor
	 * @param pageSize
	 * @return
	 */
	@Transactional(readOnly = true)

	public CursorResponseDTO<StudyDTO.MyStudyDTO> getMyStudyList(Long userId, StudyRole role, Long cursor,
		int pageSize) {
		// 1. 커서 기반 목록 조회 (리포지토리에서 pageSize + 1개를 조회함)
		List<StudyUser> myStudyUsers = studyCustomRepository.findMyStudyByCursor(userId, role, cursor, pageSize);

		if (myStudyUsers.isEmpty()) {
			return CursorResponseDTO.of(Collections.emptyList(), false, null);
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
		Map<Long, String> kpiNameMap = getKpiNameMap(studyIds);
		Map<Long, Long> kpiIdMap = getKpiIdMap(studyIds);

		// 4. DTO 변환
		List<StudyDTO.MyStudyDTO> content = pagingList.stream()
			.map(su -> StudyConverter.toMyStudyDTO(
				su,
				participantCountMap.getOrDefault(su.getStudy().getId(), 0),
				kpiNameMap.getOrDefault(su.getStudy().getId(), null),
				kpiIdMap.getOrDefault(su.getStudy().getId(), null)
			))
			.toList();

		// 5. 다음 커서값 생성 (실제 응답에 포함된 마지막 항목의 ID 기준)
		String nextCursor = hasNext ? pagingList.get(pagingList.size() - 1).getId().toString() : null;

		return CursorResponseDTO.of(content, hasNext, nextCursor);
	}

	/**
	 * 직무별 KPI 카드 목록 조회
	 * @param JobName
	 * @param cursor
	 * @param pageSize
	 * @return
	 */
	@Transactional(readOnly = true)
	public CursorResponseDTO<StudyKpiCardDTO.StudyKpiCardNameDTO> getKpiCardListByJob(String JobName, Long cursor,
		int pageSize) {
		// 1. 커서 기반 목록 조회
		List<KpiCard> kpiCards = studyCustomRepository.findByJobNameWithCursor(JobName, cursor, pageSize);

		if (kpiCards.isEmpty()) {
			return CursorResponseDTO.of(Collections.emptyList(), false, null);
		}

		// 2. 다음 페이지 존재 여부 확인 및 리스트 정제
		boolean hasNext = kpiCards.size() > pageSize;
		List<KpiCard> pagingList = hasNext ? kpiCards.subList(0, pageSize) : kpiCards;

		// 3. 다음 커서값 생성
		String nextCursor = hasNext ? pagingList.get(pagingList.size() - 1).getId().toString() : null;

		// 4. Converter를 사용하여 DTO 변환 및 결과 반환
		return StudyKpiCardConverter.toKpiCardNameListDTO(pagingList, hasNext, nextCursor);
	}

	/**
	 * 맞춤형 스터디 추천 목록 조회
	 * @param userId
	 * @param cursor
	 * @param pageSize
	 * @return
	 */
	@Transactional(readOnly = true)
	public CursorResponseDTO<StudyRecommendDTO> getRecommendedStudyList(Long userId, Long cursor, int pageSize) {

		// 1. 유저의 하위 3개 약점 KPI ID 조회
		List<Long> weaknessKpiIds = kpiScoreQueryService.getBottom3KpiCards(userId).stream()
			.map(KpiCardResponseDTO.GridItem::kpiCardId)
			.toList();

		// 2. 이미 참여 중이거나 신청한 스터디는 제외한다
		List<Long> excludeStudyIds = studyUserRepository.findByUserId(userId).stream()
			.map(su -> su.getStudy().getId()).toList();

		if (excludeStudyIds.isEmpty()) {
			excludeStudyIds = List.of(-1L);
		}

		// 3. 약점기반 + 잔여석 있는 스터디 조회
		List<Study> recommendedStudies = studyCustomRepository.findRecommendedStudyByKpi(
			weaknessKpiIds, excludeStudyIds, cursor, pageSize
		);

		if (recommendedStudies.isEmpty()) { // 추천할 수 있는 스터디 없는 경우 빈 리스트 반환
			return CursorResponseDTO.of(Collections.emptyList(), false, null);
		}

		// 4. 정제된 리스트(pagingList)를 바탕으로 ID 추출 및 N+1 방지 조회
		boolean hasNext = recommendedStudies.size() > pageSize;
		List<Study> pagingList = hasNext ? recommendedStudies.subList(0, pageSize) : recommendedStudies;

		List<Long> studyIds = pagingList.stream().map(Study::getId).toList();
		Map<Long, Integer> participantCountMap = getParticipantCountMap(studyIds);
		Map<Long, String> kpiNameMap = getKpiNameMap(studyIds);
		Map<Long, Long> kpiIdMap = getKpiIdMap(studyIds);

		// 5. DTO 변환
		List<StudyRecommendDTO> content = pagingList.stream()
			.map(s -> StudyRecommendConverter.toStudyRecommendDTO(
				s,
				participantCountMap.getOrDefault(s.getId(), 0),
				kpiNameMap.getOrDefault(s.getId(), "KPI 정보 없음"),
				kpiIdMap.getOrDefault(s.getId(), null)
			)).toList();

		String nextCursor = hasNext ? pagingList.get(pagingList.size() - 1).getId().toString() : null;
		return CursorResponseDTO.of(content, hasNext, nextCursor);
	}

	private Map<Long, Integer> getParticipantCountMap(List<Long> studyIds) {
		return studyUserRepository.countParticipantsByStudyIds(studyIds).stream()
			.collect(Collectors.toMap(
				obj -> (Long)obj[0],
				obj -> ((Long)obj[1]).intValue()
			));
	}

	private Map<Long, String> getKpiNameMap(List<Long> studyIds) {
		List<StudyKpi> studyKpis = studyKpiRepository.findByStudyIdIn(studyIds);

		return studyKpis.stream()
			.collect(Collectors.toMap(
				(StudyKpi sk) -> sk.getStudy().getId(),
				(StudyKpi sk) -> sk.getKpiCard().getName()
			));
	}

	// StudyKpiRepository 등을 활용하여 studyId와 kpiCardId 쌍을 가져옵니다.
	private Map<Long, Long> getKpiIdMap(List<Long> studyIds) {
		if (studyIds.isEmpty())
			return Map.of();

		// 이전에 생성했던 study_kpis 연관 테이블을 조회하는 쿼리가 필요합니다.
		return studyKpiRepository.findByStudyIdIn(studyIds).stream()
			.collect(Collectors.toMap(
				sk -> sk.getStudy().getId(),
				sk -> sk.getKpiCard().getId()
			));
	}

	/**
	 * 스터디 신청자 목록
	 * @param studyId
	 * @param cursor
	 * @param size
	 * @return
	 */
	@Transactional(readOnly = true)
	public CursorResponseDTO<StudyApplicationDTO.ApplicationPreviewDTO> getApplicantList(Long studyId, Long cursor,
		int size) {

		// 1. Repository를 통해 신청자 목록 조회 (다음 페이지 확인을 위해 size + 1개를 가져옴)
		List<StudyUser> applicants = studyCustomRepository.findApplicants(studyId, cursor, size);

		// 2. 다음 페이지 존재 여부 확인
		boolean hasNext = applicants.size() > size;

		// 3. 실제 페이지에 포함될 리스트 결정
		List<StudyUser> pagingList = hasNext ? applicants.subList(0, size) : applicants;

		// 4. 다음 커서 값 결정
		String nextCursor = hasNext ? pagingList.get(pagingList.size() - 1).getId().toString() : null;

		// 5. DTO로 변환하여 반환
		return CursorResponseDTO.<StudyApplicationDTO.ApplicationPreviewDTO>builder()
			.content(pagingList.stream()
				.map(StudyConverter::toApplicantPreviewListDTO)
				.toList())
			.pageSize(pagingList.size())
			.nextCursor(nextCursor)
			.hasNext(hasNext)
			.build();
	}
}
