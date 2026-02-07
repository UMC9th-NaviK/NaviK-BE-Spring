package navik.domain.board.service.comment;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.board.converter.comment.CommentListConverter;
import navik.domain.board.dto.comment.CommentCountDTO;
import navik.domain.board.dto.comment.CommentListDTO;
import navik.domain.board.entity.Comment;
import navik.domain.board.repository.board.BoardRepository;
import navik.domain.board.repository.comment.CommentRepository;
import navik.domain.users.repository.UserRepository;
import navik.domain.users.service.UserQueryService;
import navik.global.dto.CursorResponseDTO;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService {
	private final BoardRepository boardRepository;
	private final CommentRepository commentRepository;
	private final UserRepository userRepository;
	private final UserQueryService userQueryService;

	/**
	 * 전체 댓글 수 조회
	 * @param boardId
	 * @return
	 */
	public CommentCountDTO getCommentCount(Long boardId) {
		Long count = commentRepository.countActiveComments(boardId);
		return new CommentCountDTO(count != null ? count : 0L);
	}

	/**
	 * 게시글의 댓글 목록 조회
	 * @param parameter
	 * @return
	 */
	public CursorResponseDTO<CommentListDTO.ResponseComment> getCommentList(CommentListDTO.Parameter parameter) {
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

}
