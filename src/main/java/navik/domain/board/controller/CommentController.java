package navik.domain.board.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import navik.domain.board.converter.CommentConverter;
import navik.domain.board.converter.CommentListConverter;
import navik.domain.board.converter.ReplyConverter;
import navik.domain.board.dto.CommentCountDTO;
import navik.domain.board.dto.CommentCreateDTO;
import navik.domain.board.dto.CommentListDTO;
import navik.domain.board.dto.ReplyDTO;
import navik.domain.board.service.comment.CommentCommandService;
import navik.domain.board.service.comment.CommentQueryService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.exception.code.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;
import navik.global.dto.CursorResponseDTO;

@RestController
@RequestMapping("/v1/boards")
@RequiredArgsConstructor
public class CommentController implements CommentControllerDocs {
	private final CommentCommandService commentCommandService;
	private final CommentQueryService commentQueryService;

	/**
	 * 댓글 목록 조회
	 * @param boardId
	 * @param userId
	 * @param pageable
	 * @return
	 */
	@GetMapping("/{boardId}/comments")
	public ApiResponse<CursorResponseDTO<CommentListDTO.ResponseComment>> getComments(
		@PathVariable Long boardId,
		@AuthUser Long userId,
		@PageableDefault(size = 10) Pageable pageable
	) {
		CommentListDTO.Parameter parameter = CommentListConverter.toParameter(userId, boardId, pageable);
		CursorResponseDTO<CommentListDTO.ResponseComment> response = commentQueryService.getCommentList(parameter);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, response);
	}

	/**
	 * 댓글 작성
	 * @param boardId
	 * @param request
	 * @param userId
	 * @return
	 */
	@PostMapping("/{boardId}/comments")
	public ApiResponse<CommentCreateDTO.Response> addComment(
		@PathVariable Long boardId,
		@RequestBody @Valid CommentCreateDTO.Request request,
		@AuthUser Long userId
	) {
		CommentCreateDTO.Parameter parameter = CommentConverter.toParameter(userId, boardId, request);
		CommentCreateDTO.Response response = commentCommandService.createComment(parameter);
		return ApiResponse.onSuccess(GeneralSuccessCode._CREATED, response);
	}

	/**
	 * 대댓글 작성
	 * @param boardId
	 * @param commentId
	 * @param request
	 * @param userId
	 * @return
	 */
	@PostMapping("/{boardId}/comments/{commentId}/reply")
	public ApiResponse<ReplyDTO.Response> addReply(
		@PathVariable Long boardId,
		@PathVariable Long commentId,
		@RequestBody @Valid ReplyDTO.Request request,
		@AuthUser Long userId
	) {
		ReplyDTO.Parameter parameter = ReplyConverter.toParameter(userId, boardId, commentId, request);

		ReplyDTO.Response response = commentCommandService.createReply(parameter);

		return ApiResponse.onSuccess(GeneralSuccessCode._CREATED, response);
	}

	/**
	 * 댓글 삭제
	 * @param boardId
	 * @param commentId
	 * @param userId
	 * @return
	 */
	@DeleteMapping("/{boardId}/comments/{commentId}")
	public ApiResponse<Object> deleteComment(
		@PathVariable Long boardId,
		@PathVariable Long commentId,
		@AuthUser Long userId
	) {
		commentCommandService.deleteComment(CommentConverter.toDeleteParameter(userId, boardId, commentId));
		return ApiResponse.onSuccess(GeneralSuccessCode._DELETED);
	}

	/**
	 * 전체 댓글 수 조회
	 * @param boardId
	 * @return
	 */
	@GetMapping("/{boardId}/comments/count")
	public ApiResponse<CommentCountDTO> getCount(@PathVariable Long boardId) {
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, commentQueryService.getCommentCount(boardId));
	}
}
