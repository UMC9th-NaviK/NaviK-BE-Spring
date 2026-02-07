package navik.domain.board.service.board;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.board.converter.BoardConverter;
import navik.domain.board.dto.BoardResponseDTO;
import navik.domain.board.entity.Board;
import navik.domain.board.repository.BoardLikeRepository;
import navik.domain.board.repository.BoardRepository;
import navik.domain.board.repository.CommentRepository;
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
	 * @param lastId
	 * @param pageSize
	 * @return
	 */
	@Transactional(readOnly = true)
	public CursorResponseDTO<BoardResponseDTO.BoardDTO> getBoardList(Long lastId, int pageSize) {
		List<Board> boards = boardRepository.findAllByCursor(lastId, pageSize);
		return processCursorPage(boards, pageSize);
	}

	/**
	 * 직무별 게시글 조회
	 * @param jobName
	 * @param lastId
	 * @param pageSize
	 * @return
	 */
	@Transactional(readOnly = true)
	public CursorResponseDTO<BoardResponseDTO.BoardDTO> getBoardListByJob(String jobName, Long lastId, int pageSize) {
		List<Board> boards = boardRepository.findByJobAndCursor(jobName, lastId, pageSize);
		return processCursorPage(boards, pageSize);
	}

	private CursorResponseDTO<BoardResponseDTO.BoardDTO> processCursorPage(List<Board> boards, int pageSize) {
		List<Long> boardIds = boards.stream().map(Board::getId).collect(Collectors.toList());

		Map<Long, Integer> likeCountMap = getLikeCountMap(boardIds);
		Map<Long, Integer> commentCountMap = getCommentCountMap(boardIds);

		List<BoardResponseDTO.BoardDTO> doList = boards.stream()
			.map(board -> BoardConverter.toBoardDTO(
				board,
				likeCountMap.getOrDefault(board.getId(), 0),
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
	@Cacheable(value = "hotBoards", key = "#cursor + #pageable.pageSize", cacheManager = "cacheManager10Sec")
	@Transactional(readOnly = true)
	public BoardResponseDTO.HotBoardListDTO getHotBoardList(String cursor, Pageable pageable) {
		Integer lastScore = null;
		Long lastId = null;

		if (cursor != null && !cursor.isEmpty()) {
			String[] parts = cursor.split("_"); // score_id 형태이기 때문에
			lastScore = Integer.parseInt(parts[0]);
			lastId = Long.parseLong(parts[1]);
		}

		// 1. HOT 게시판 리스트 조회
		List<Board> boards = boardRepository.findHotBoardsByCursor(lastScore, lastId, pageable.getPageSize());
		List<Long> boardIds = boards.stream().map(Board::getId).collect(Collectors.toList());

		// 2. N+1 방지를 위해 Batch 조회 및 Map 변환시킨다
		Map<Long, Integer> likeCountMap = getLikeCountMap(boardIds);
		Map<Long, Integer> commentCountMap = getCommentCountMap(boardIds);

		// 3. 다음 페이지 정보 및 커서를 생성
		boolean hasNext = boards.size() >= pageable.getPageSize();
		String nextCursor = null;

		if (!boards.isEmpty() && hasNext) {
			Board lastBoard = boards.get(boards.size() - 1);
			int score = lastBoard.getArticleLikes() + lastBoard.getArticleViews();
			nextCursor = score + "_" + lastBoard.getId();
		}

		return BoardConverter.toHotBoardListDTO(boards, likeCountMap, commentCountMap, nextCursor, hasNext);

	}

	private Map<Long, Integer> getLikeCountMap(List<Long> boardIds) {
		return boardLikeRepository.countByBoardIdIn(boardIds).stream()
			.collect(Collectors.toMap(
				obj -> (Long)obj[0],
				obj -> ((Long)obj[1]).intValue()
			));
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
	 * @param lastId
	 * @param pageSize
	 * @return
	 */
	@Transactional(readOnly = true)
	public CursorResponseDTO<BoardResponseDTO.BoardDTO> searchBoard(String keyword, Long lastId, int pageSize) {
		// 1. 키워드 및 커서 기반 검색 실행
		List<Board> boards = boardRepository.searchByKeyword(keyword, lastId, pageSize);

		// 2. 검색 결과가 없을 경우 빈 응답 반환
		if (boards.isEmpty()) {
			return CursorResponseDTO.of(Collections.emptyList(), false, null);
		}

		List<Long> boardIds = boards.stream().map(Board::getId).toList();

		// 3. N+1 방지를 위한 Batch 조회 (Map 방식)
		Map<Long, Integer> likeCountMap = getLikeCountMap(boardIds);
		Map<Long, Integer> commentCountMap = getCommentCountMap(boardIds);

		// 4. DTO 변환 및 결과 매핑
		List<BoardResponseDTO.BoardDTO> content = boards.stream()
			.map(board -> BoardConverter.toBoardDTO(board,
				likeCountMap.getOrDefault(board.getId(), 0),
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
		Board board = boardRepository.findById(boardId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.BOARD_NOT_FOUND));

		board.incrementArticleViews(); // 조회수 증가
		boardRepository.save(board); // 변경된 조회수 저장

		return BoardConverter.toBoardDTO(
			board,
			boardLikeRepository.countLikeByBoard(board),
			commentRepository.countCommentByBoard(board)
		);
	}
}
