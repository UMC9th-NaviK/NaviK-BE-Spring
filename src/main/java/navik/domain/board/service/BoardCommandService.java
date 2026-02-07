package navik.domain.board.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.board.dto.BoardCreateDTO;
import navik.domain.board.dto.BoardUpdateDTO;
import navik.domain.board.entity.Board;
import navik.domain.board.repository.BoardRepository;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.exception.code.GeneralErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;

@Service
@RequiredArgsConstructor
public class BoardCommandService {
	private final UserRepository userRepository;
	private final BoardRepository boardRepository;

	/**
	 * 게시글 생성
	 * @param userId
	 * @param request
	 * @return
	 */
	@Transactional
	public Long createBoard(Long userId, BoardCreateDTO request) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.USER_NOT_FOUND));
		Board board = Board.builder()
			.user(user)
			.articleTitle(request.getArticleTitle())
			.articleContent(request.getArticleContent())
			.articleViews(0)
			.articleLikes(0)
			.build();

		return boardRepository.save(board).getId();
	}

	/**
	 * 게시글 수정
	 * @param boardId
	 * @param userId
	 * @param request
	 * @return
	 */
	@Transactional
	public Long updateBoard(Long boardId, Long userId, BoardUpdateDTO request) {
		Board board = boardRepository.findById(boardId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.BOARD_NOT_FOUND));

		if (!board.getUser().getId().equals(userId)) {
			throw new GeneralException(GeneralErrorCode.AUTH_BOARD_NOT_WRITER);
		}

		board.updateBoard(request.getArticleTitle(), request.getArticleContent());
		return boardRepository.save(board).getId();
	}

	/**
	 * 게시글 삭제
	 * @param boardId
	 * @param userId
	 */
	@Transactional
	public void deleteBoard(Long boardId, Long userId) {
		Board board = boardRepository.findById(boardId) // 게시글 찾을 수 없음
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.BOARD_NOT_FOUND));

		if (!board.getUser().getId().equals(userId)) { // 게시글 작성자가 아님
			throw new GeneralException(GeneralErrorCode.AUTH_BOARD_NOT_WRITER);
		}
		boardRepository.delete(board);
	}
}
