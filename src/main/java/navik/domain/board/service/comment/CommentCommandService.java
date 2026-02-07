package navik.domain.board.service.comment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.board.converter.CommentConverter;
import navik.domain.board.converter.ReplyConverter;
import navik.domain.board.dto.comment.CommentCreateDTO;
import navik.domain.board.dto.comment.CommentDeleteDTO;
import navik.domain.board.dto.comment.ReplyDTO;
import navik.domain.board.entity.Board;
import navik.domain.board.entity.Comment;
import navik.domain.board.repository.board.BoardRepository;
import navik.domain.board.repository.comment.CommentRepository;
import navik.domain.users.entity.User;
import navik.domain.users.service.UserQueryService;
import navik.global.apiPayload.exception.code.GeneralErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentCommandService {
	private final BoardRepository boardRepository;
	private final CommentRepository commentRepository;
	private final UserQueryService userQueryService;

	/**
	 * 댓글 생성
	 * @param parameter
	 * @return
	 */
	public CommentCreateDTO.Response createComment(CommentCreateDTO.Parameter parameter) {
		// 작성자 조회
		User user = userQueryService.getUser(parameter.getUserId());

		// 게시글 조회
		Board board = boardRepository.findById(parameter.getBoardId())
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.BOARD_NOT_FOUND));

		// 댓글 생성
		Comment comment = CommentConverter.toComment(user, board, parameter.getContent());

		// 댓글 등록
		commentRepository.save(comment);

		return CommentConverter.toResponse(comment);
	}

	/**
	 * 대댓글 생성
	 * @param parameter
	 * @return
	 */
	public ReplyDTO.Response createReply(ReplyDTO.Parameter parameter) {
		// 작성자 조회
		User user = userQueryService.getUser(parameter.getUserId());

		// 게시글 조회
		Board board = boardRepository.findById(parameter.getBoardId())
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.BOARD_NOT_FOUND));

		// 부모 댓글 조회
		Comment parentComment = commentRepository.findById(parameter.getCommentId())
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.COMMENT_NOT_FOUND));

		// 대댓글 작성
		Comment reply = ReplyConverter.toComment(user, board, parentComment, parameter.getContent());

		// 대댓글 등록
		commentRepository.save(reply);

		return ReplyConverter.toResponse(reply);
	}

	/**
	 * 댓글 삭제
	 * @param parameter
	 */
	public void deleteComment(CommentDeleteDTO.Parameter parameter) {
		// 작성자 조회
		User user = userQueryService.getUser(parameter.getUserId());

		// 댓글 조회
		Comment comment = commentRepository.findById(parameter.getCommentId())
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.COMMENT_NOT_FOUND));

		// 댓글 작성자가 맞는지 확인
		if (!comment.getUser().equals(user)) {
			throw new GeneralException(GeneralErrorCode.AUTH_COMMENT_NOT_WRITER);
		}

		// 댓글 상태값 변경
		comment.changeToDeletedStatus();
	}
}
