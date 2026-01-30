package navik.domain.board.service;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.board.converter.CommentConverter;
import navik.domain.board.converter.CommentListConverter;
import navik.domain.board.converter.ReplyConverter;
import navik.domain.board.dto.CommentCountDTO;
import navik.domain.board.dto.CommentCreateDTO;
import navik.domain.board.dto.CommentDeleteDTO;
import navik.domain.board.dto.CommentListDTO;
import navik.domain.board.dto.ReplyDTO;
import navik.domain.board.entity.Board;
import navik.domain.board.entity.Comment;
import navik.domain.board.repository.BoardRepository;
import navik.domain.board.repository.CommentRepository;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;
import navik.domain.users.service.UserQueryService;
import navik.global.apiPayload.code.status.GeneralErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;
import navik.global.dto.CursorResponseDto;

@Service
@RequiredArgsConstructor
public class CommentService {
	private final BoardRepository boardRepository;
	private final CommentRepository commentRepository;
	private final UserRepository userRepository;
	private final UserQueryService userQueryService;

	/**
	 * 전체 댓글 수 조회
	 * @param boardId
	 * @return
	 */
	@Transactional(readOnly = true)
	public CommentCountDTO getCommentCount(Long boardId) {
		Long count = commentRepository.countActiveComments(boardId);
		return new CommentCountDTO(count != null ? count : 0L);
	}

	/**
	 * 게시글의 댓글 목록 조회
	 * @param parameter
	 * @return
	 */
	@Transactional(readOnly = true)
	public CursorResponseDto<CommentListDTO.ResponseComment> getCommentList(CommentListDTO.Parameter parameter) {
		// 1. 게시글 조회
		Slice<Comment> comments = commentRepository.findByBoardId(
			parameter.getBoardId(),
			parameter.getPageable());

		// 2. 다믐 커서 값 추출 (마지막 데이터 Id)
		String nextCusor = (comments.hasNext() && !comments.isEmpty()) ?
			comments.getContent().get(comments.getNumberOfElements() - 1).getId().toString() : null;

		// 3. DTO 변환
		return CommentListConverter.toResponse(comments, nextCusor, parameter.getUserId());
	}

	/**
	 * 댓글 생성
	 * @param parameter
	 * @return
	 */
	@Transactional
	public CommentCreateDTO.Response createComment(CommentCreateDTO.Parameter parameter) {
		// 작성자 조회
		User user = userQueryService.getUser(parameter.getUserId());

		// 게시글 조회
		Board board = boardRepository.findById(parameter.getBoardId())
			.orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.BOARD_NOT_FOUND));

		// 댓글 생성
		Comment comment = CommentConverter.toComment(user, board, parameter.getContent());

		// 댓글 등록
		commentRepository.save(comment);

		return CommentConverter.toResponse(comment.getId());
	}

	@Transactional
	public ReplyDTO.Response createReply(ReplyDTO.Parameter parameter) {
		// 작성자 조회
		User user = userQueryService.getUser(parameter.getUserId());

		// 게시글 조회
		Board board = boardRepository.findById(parameter.getBoardId())
			.orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.BOARD_NOT_FOUND));

		// 부모 댓글 조회
		Comment parentComment = commentRepository.findById(parameter.getCommentId())
			.orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.COMMENT_NOT_FOUND));

		// 대댓글 작성
		Comment reply = ReplyConverter.toComment(user, board, parentComment, parameter.getContent());

		// 대댓글 등록
		commentRepository.save(reply);

		return ReplyConverter.toResponse(reply.getId());
	}

	/**
	 * 댓글 삭제
	 * @param parameter
	 */
	@Transactional
	public void deleteComment(CommentDeleteDTO.Parameter parameter) {
		// 작성자 조회
		User user = userQueryService.getUser(parameter.getUserId());

		// 댓글 조회
		Comment comment = commentRepository.findById(parameter.getCommentId())
			.orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.COMMENT_NOT_FOUND));

		// 댓글 작성자가 맞는지 확인
		if (!comment.getUser().equals(user)) {
			throw new GeneralExceptionHandler(GeneralErrorCode.AUTH_COMMENT_NOT_WRITER);
		}

		// 댓글 상태값 변경
		comment.changeToDeletedStatus();
	}
}
