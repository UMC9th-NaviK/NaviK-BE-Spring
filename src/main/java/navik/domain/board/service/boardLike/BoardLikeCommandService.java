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
			board.decrementArticleLikes();
			isLiked = false;
		} else {
			// 좋아요가 없다면 좋아요 추가
			BoardLike boardLike = BoardLikeConverter.toEntity(user, board);
			boardLikeRepository.save(boardLike);
			board.incrementArticleLikes();
			isLiked = true;
		}

		// 4. 좋아요 총합 조회
		int totalLikeCount = board.getArticleLikes();

		return BoardLikeConverter.toResponse(board.getId(), totalLikeCount, isLiked);
	}
}
