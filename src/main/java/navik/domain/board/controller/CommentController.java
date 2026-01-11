package navik.domain.board.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import navik.domain.board.dto.CommentCreateRequestDTO;
import navik.domain.board.service.CommentService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.code.status.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/boards")
@RequiredArgsConstructor

public class CommentController implements CommentControllerDocs {
    private final CommentService commentService;

    /**
     * 댓글 생성
     * @param boardId
     * @param request
     * @param userId
     * @return
     */

    @PostMapping("/{boardId}/comments")
    public ApiResponse<Long> addComment(
            @PathVariable Long boardId,
            @RequestBody @Valid CommentCreateRequestDTO request,
            @AuthUser Long userId
    ) {
        Long commentId = commentService.addComment(boardId, userId, request);
        return ApiResponse.onSuccess(GeneralSuccessCode._OK, commentId);
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
        commentService.deleteComment(boardId, commentId, userId);
        return ApiResponse.onSuccess(GeneralSuccessCode._DELETED);
    }
}
