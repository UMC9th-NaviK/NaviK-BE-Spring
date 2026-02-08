package navik.domain.board.service.boardLike;

import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.board.converter.boardLike.BoardLikeConverter;
import navik.domain.board.dto.boardLike.BoardLikeDTO;
import navik.domain.board.entity.Board;
import navik.domain.board.entity.BoardLike;
import navik.domain.board.repository.board.BoardRepository;
import navik.domain.board.repository.boardLike.BoardLikeRepository;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;
import navik.domain.users.service.UserQueryService;
import navik.global.apiPayload.exception.code.GeneralErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;

@Service
@RequiredArgsConstructor
public class BoardLikeCommandService {
	private final UserRepository userRepository;
	private final BoardRepository boardRepository;
	private final BoardLikeRepository boardLikeRepository;
	private final UserQueryService userQueryService;

	@Transactional
	@CacheEvict(value = "hotBoards", allEntries = true) // 좋아요 누를 때마다 hotBoards 캐시 전체 삭제
	public BoardLikeDTO.Response toggleBoardLike(BoardLikeDTO.Parameter parameter) {
		// 1. 작성자 조회
		User user = userQueryService.getUser(parameter.getUserId());
		// 2. 게시글 조회
		Board board = boardRepository.findById(parameter.getBoardId())
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.BOARD_NOT_FOUND));

		// 3. 이미 좋아요 눌렀는지 확인
		Optional<BoardLike> boardLikeOpt = boardLikeRepository.findByBoardAndUser(board, user);

		boolean isLiked;
		if (boardLikeOpt.isPresent()) {
			// 이미 좋아요가 있다면 좋아요 취소
			boardLikeRepository.delete(boardLikeOpt.get());
			boardRepository.decrementArticleLikes(board.getId()); // 원자적 쿼리 호출
			isLiked = false;
		} else {
			// 좋아요가 없다면 좋아요 추가
			BoardLike boardLike = BoardLikeConverter.toEntity(user, board);
			boardLikeRepository.save(boardLike);
			boardRepository.incrementArticleLikes(board.getId()); // 원자적 쿼리 호출
			isLiked = true;
		}

		// 4. 최신 좋아요 수 조회
		// 업데이트 쿼리 실행 후 영속성 컨텍스트가 비워졌으므로, 다시 조회하여 최신 값을 가져옵니다.
		Board updatedBoard = boardRepository.findById(board.getId())
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.BOARD_NOT_FOUND));

		return BoardLikeConverter.toResponse(updatedBoard.getId(), updatedBoard.getArticleLikes(), isLiked);
	}
}