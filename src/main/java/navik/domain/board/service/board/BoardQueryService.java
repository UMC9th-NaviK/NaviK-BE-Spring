package navik.domain.board.service.board;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.board.converter.board.BoardConverter;
import navik.domain.board.dto.board.BoardResponseDTO;
import navik.domain.board.entity.Board;
import navik.domain.board.repository.board.BoardRepository;
import navik.domain.board.repository.boardLike.BoardLikeRepository;
import navik.domain.board.repository.comment.CommentRepository;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.exception.code.GeneralErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;
import navik.global.dto.CursorResponseDTO;

@Service
@RequiredArgsConstructor
public class BoardQueryService {
	private final UserRepository userRepository;
	private final BoardRepository boardRepository;
	private final BoardLikeRepository boardLikeRepository;
	private final CommentRepository commentRepository;

	/**
	 * 전체 게시글 조회
	 * @param cursor
	 * @param pageSize
	 * @return
	 */
	@Transactional(readOnly = true)
	public CursorResponseDTO<BoardResponseDTO.BoardDTO> getBoardList(String cursor, int pageSize) {
		LocalDateTime lastCreatedAt = (cursor != null && !cursor.isEmpty()) ? LocalDateTime.parse(cursor) : null;
		List<Board> boards = boardRepository.findAllByCursor(lastCreatedAt, pageSize);

		return processCursorPage(boards, pageSize);
	}

	/**
	 * 직무별 게시글 조회
	 * @param jobName
	 * @param cursor
	 * @param pageSize
	 * @return
	 */
	@Transactional(readOnly = true)
	public CursorResponseDTO<BoardResponseDTO.BoardDTO> getBoardListByJob(String jobName, String cursor, int pageSize) {
		LocalDateTime lastCreatedAt = (cursor != null && !cursor.isEmpty()) ? LocalDateTime.parse(cursor) : null;
		List<Board> boards = boardRepository.findByJobAndCursor(jobName, lastCreatedAt, pageSize);
		return processCursorPage(boards, pageSize);
	}

	private CursorResponseDTO<BoardResponseDTO.BoardDTO> processCursorPage(List<Board> boards, int pageSize) {
		List<Long> boardIds = boards.stream().map(Board::getId).collect(Collectors.toList());

		// 좋아요 Map 조회 제거 (엔티티 필드 사용)
		Map<Long, Integer> commentCountMap = getCommentCountMap(boardIds);

		List<BoardResponseDTO.BoardDTO> doList = boards.stream()
			.map(board -> BoardConverter.toBoardDTO(
				board,
				commentCountMap.getOrDefault(board.getId(), 0)
			))
			.collect(Collectors.toList());

		boolean hasNext = boards.size() >= pageSize;
		String nextCursor = (hasNext && !boards.isEmpty()) ? boards.get(boards.size() - 1).getId().toString() : null;

		return CursorResponseDTO.of(doList, hasNext, nextCursor);
	}

	/**
	 * HOT 게시판 게시글 조회
	 * @param cursor
	 * @param pageable
	 * @return
	 */
	@Cacheable
		(value = "hotBoards",
			key = "(#cursor ?: 'none') + '_' + #pageable.pageSize",
			cacheManager = "cacheManager10Sec")
	@Transactional(readOnly = true)
	public BoardResponseDTO.HotBoardListDTO getHotBoardList(String cursor, Pageable pageable) {
		Integer lastScore = null;
		LocalDateTime lastCreatedAt = null;

		if (cursor != null && !cursor.isEmpty()) {
			String[] parts = cursor.split("_");
			lastScore = Integer.parseInt(parts[0]);
			lastCreatedAt = LocalDateTime.parse(parts[1]);
		}

		List<Board> boards = boardRepository.findHotBoardsByCursor(lastScore, lastCreatedAt, pageable.getPageSize());
		List<Long> boardIds = boards.stream().map(Board::getId).collect(Collectors.toList());

		// 좋아요 Map 생성 로직 제거
		Map<Long, Integer> commentCountMap = getCommentCountMap(boardIds);

		boolean hasNext = boards.size() >= pageable.getPageSize();
		String nextCursor = null;

		if (!boards.isEmpty() && hasNext) {
			Board lastBoard = boards.get(boards.size() - 1);
			// 계산 방식에 따라 엔티티 필드 활용
			int score = lastBoard.getArticleLikes() + lastBoard.getArticleViews();
			nextCursor = score + "_" + lastBoard.getCreatedAt().toString();
		}

		return BoardConverter.toHotBoardListDTO(boards, commentCountMap, nextCursor, hasNext);
	}

	private Map<Long, Integer> getCommentCountMap(List<Long> boardIds) {
		if (boardIds.isEmpty()) {
			return Map.of();
		}
		return commentRepository.countByBoardIds(boardIds).stream()
			.collect(Collectors.toMap(
				obj -> (Long)obj[0],
				obj -> ((Long)obj[1]).intValue()
			));
	}

	/**
	 * 게시글 검색
	 * @param keyword
	 * @param cursor
	 * @param pageSize
	 * @return
	 */
	@Transactional(readOnly = true)
	public CursorResponseDTO<BoardResponseDTO.BoardDTO> searchBoard(String keyword, String cursor, int pageSize) {
		LocalDateTime lastCreatedAt = (cursor != null && !cursor.isEmpty()) ? LocalDateTime.parse(cursor) : null;

		// 1. 키워드 및 커서 기반 검색 실행
		List<Board> boards = boardRepository.searchByKeyword(keyword, lastCreatedAt, pageSize);

		// 2. 검색 결과가 없을 경우 빈 응답 반환
		if (boards.isEmpty()) {
			return CursorResponseDTO.of(Collections.emptyList(), false, null);
		}

		List<Long> boardIds = boards.stream().map(Board::getId).toList();

		// 3. N+1 방지를 위한 Batch 조회 (Map 방식)
		Map<Long, Integer> commentCountMap = getCommentCountMap(boardIds);

		// 4. DTO 변환 및 결과 매핑
		List<BoardResponseDTO.BoardDTO> content = boards.stream()
			.map(board -> BoardConverter.toBoardDTO(board,
				commentCountMap.getOrDefault(board.getId(), 0)))
			.toList();

		// 5. 다음 페이지 여부 및 커서 생성
		boolean hasNext = boards.size() >= pageSize;
		String nextCursor = hasNext ? boards.get(boards.size() - 1).getId().toString() : null;

		return CursorResponseDTO.of(content, hasNext, nextCursor);
	}

	/**
	 * 상세 게시글 조회
	 * @param boardId
	 * @return
	 */
	@Transactional
	public BoardResponseDTO.BoardDTO getBoardDetail(Long boardId) {
		// 1. 조회 수 증가
		int updatedRows = boardRepository.incrementArticleViews(boardId);

		if (updatedRows == 0) {
			throw new GeneralException(GeneralErrorCode.BOARD_NOT_FOUND);
		}

		// 2. 게시글 상세 정보 조회
		// 위에서 clearAutomatically = true를 설정했으므로, DB에서 업데이트된 최신 값을 읽어옵니다.
		Board board = boardRepository.findById(boardId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.BOARD_NOT_FOUND));

		return BoardConverter.toBoardDTO(
			board,
			commentRepository.countCommentByBoard(board)
		);
	}
}
