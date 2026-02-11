package navik.domain.recruitment.service.position;

import java.util.Base64;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.ability.repository.AbilityRepository;
import navik.domain.job.entity.Job;
import navik.domain.job.repository.JobRepository;
import navik.domain.recruitment.converter.position.PositionConverter;
import navik.domain.recruitment.dto.position.PositionRequestDTO;
import navik.domain.recruitment.dto.position.PositionResponseDTO;
import navik.domain.recruitment.enums.JobType;
import navik.domain.recruitment.repository.position.position.PositionRepository;
import navik.domain.recruitment.repository.position.position.projection.RecommendedPositionProjection;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.exception.code.GeneralErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;
import navik.global.dto.CursorResponseDTO;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PositionQueryService {

	private final PositionRepository positionRepository;
	private final UserRepository userRepository;
	private final JobRepository jobRepository;
	private final AbilityRepository abilityRepository;

	/**
	 * 최대 8가지 검색 필터를 적용하여 사용자에게 적합한 추천 공고를 조회합니다.
	 * 커서 기반 페이징을 사용합니다.
	 */
	public CursorResponseDTO<PositionResponseDTO.RecommendedPosition> getPositions(
		Long userId,
		PositionRequestDTO.SearchCondition searchCondition,
		String cursor,
		Pageable pageable
	) {

		// 1. 유저 및 전공 검색
		User user = userRepository.findByIdWithUserDepartmentAndDepartment(userId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.USER_NOT_FOUND));

		// 2. 직무 검색
		List<String> jobNames = searchCondition.getJobTypes().stream()
			.map(JobType::getLabel)
			.toList();
		List<Job> jobs = jobRepository.findByNameIn(jobNames);

		boolean hasAbilities = abilityRepository.existsByUser(user);

		Slice<RecommendedPositionProjection> result;
		String nextCursor = null;

		if (!hasAbilities) {
			result = positionRepository.findSimpleRecentPositions(jobs, searchCondition, pageable);
		} else {
			PositionRequestDTO.CursorRequest cursorRequest = decodeCursor(cursor);
			result = positionRepository.findRecommendedPositions(
				user, jobs, searchCondition, cursorRequest, pageable);

			if (result.hasNext()) {
				RecommendedPositionProjection lastItem = result.getContent().get(result.getContent().size() - 1);
				nextCursor = encodeCursor(
					lastItem.getMatchScore(),
					lastItem.getMatchCount(),
					lastItem.getPosition().getId()
				);
			}
		}

		// 6. DTO 반환
		return CursorResponseDTO.of(
			result.map(projection -> PositionConverter.toRecommendedPosition(user, projection, searchCondition)),
			nextCursor
		);
	}

	public Long getPositionCount(PositionRequestDTO.SearchCondition searchCondition) {

		// 1. 직무 검색
		List<String> jobNames = searchCondition.getJobTypes().stream()
			.map(JobType::getLabel)
			.toList();
		List<Job> jobs = jobRepository.findByNameIn(jobNames);

		// 2. 필터 적용 Total Count
		return positionRepository.countPositions(jobs, searchCondition);
	}

	private String encodeCursor(Double similarity, Long matchCount, Long id) {
		String original = similarity + "_" + matchCount + "_" + id;
		return Base64.getEncoder().encodeToString(original.getBytes());
	}

	private PositionRequestDTO.CursorRequest decodeCursor(String cursor) {
		if (cursor == null)
			return null;
		try {
			String decoded = new String(Base64.getDecoder().decode(cursor));
			String[] parts = decoded.split("_");

			if (parts.length != 3) {
				throw new GeneralException(GeneralErrorCode.INVALID_INPUT_VALUE);
			}

			Double similarityAvg = Double.parseDouble(parts[0]);
			Long matchCount = Long.parseLong(parts[1]);
			Long id = Long.parseLong(parts[2]);

			return PositionRequestDTO.CursorRequest.builder()
				.lastSimilarity(similarityAvg)
				.lastMatchCount(matchCount)
				.lastId(id)
				.build();

		} catch (Exception e) {
			throw new GeneralException(GeneralErrorCode.INVALID_INPUT_VALUE);
		}
	}
}
