package navik.domain.recruitment.service.position;

import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.job.entity.Job;
import navik.domain.job.repository.JobRepository;
import navik.domain.recruitment.converter.position.PositionConverter;
import navik.domain.recruitment.dto.position.CursorRequest;
import navik.domain.recruitment.dto.position.PositionRequestDTO;
import navik.domain.recruitment.dto.position.PositionResponseDTO;
import navik.domain.recruitment.enums.JobType;
import navik.domain.recruitment.repository.position.PositionRepository;
import navik.domain.recruitment.repository.position.projection.RecommendedPositionProjection;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.code.status.GeneralErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;
import navik.global.dto.CursorResponseDto;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PositionQueryService {

	private static final Pattern CURSOR_PATTERN = Pattern.compile("^([\\d\\.]+)_(\\d+)_(\\d+)$");

	private final PositionRepository positionRepository;
	private final UserRepository userRepository;
	private final JobRepository jobRepository;

	/**
	 * 최대 8가지 검색 필터를 적용하여 사용자에게 적합한 추천 공고를 조회합니다.
	 * 커서 기반 페이징을 사용합니다.
	 */
	public CursorResponseDto<PositionResponseDTO.RecommendedPosition> getPositions(
		Long userId,
		PositionRequestDTO.SearchCondition searchCondition,
		String cursor,
		Pageable pageable
	) {

		// 1. 유저 및 전공 검색
		User user = userRepository.findByIdWithUserDepartmentAndDepartment(userId)
			.orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.USER_NOT_FOUND));

		// 2. 직무 검색
		List<String> jobNames = searchCondition.getJobTypes().stream()
			.map(JobType::getLabel)
			.toList();
		List<Job> jobs = jobRepository.findByNameIn(jobNames);

		// 3. 커서 디코딩
		CursorRequest cursorRequest = decodeCursor(cursor);

		// 4. 추천 포지션 조회
		Slice<RecommendedPositionProjection> result = positionRepository.findRecommendedPositions(
			user, jobs, searchCondition, cursorRequest, pageable);

		// 5. 커서 인코딩
		String nextCursor = result.hasNext() ?
			encodeCursor(
				result.getContent().getLast().getMatchScore(),
				result.getContent().getLast().getMatchCount(),
				result.getContent().getLast().getPosition().getId()
			)
			: null;

		// 6. DTO 반환
		return CursorResponseDto.of(
			result.map(projection -> PositionConverter.toRecommendedPosition(user, projection, searchCondition)),
			nextCursor
		);
	}

	private String encodeCursor(Double similarity, Long matchCount, Long id) {
		String original = String.format("%f_%d_%d", similarity, matchCount, id);
		return Base64.getEncoder().encodeToString(original.getBytes());
	}

	private CursorRequest decodeCursor(String cursor) {
		if (cursor == null)
			return null;
		String decoded = new String(Base64.getDecoder().decode(cursor));
		Matcher matcher = CURSOR_PATTERN.matcher(decoded);
		if (matcher.find()) {
			Double similarity = Double.parseDouble(matcher.group(1));
			Long matchCount = Long.parseLong(matcher.group(2));
			Long id = Long.parseLong(matcher.group(3));
			return CursorRequest.builder()
				.lastSimilarity(similarity)
				.lastMatchCount(matchCount)
				.lastId(id)
				.build();
		}
		return null;
	}
}
