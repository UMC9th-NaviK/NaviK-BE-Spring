package navik.domain.board.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import navik.domain.board.dto.CommentCreateDTO;
import navik.domain.board.dto.CommentListDTO;
import navik.domain.board.dto.ReplyDTO;
import navik.global.apiPayload.ApiResponse;
import navik.global.auth.annotation.AuthUser;
import navik.global.dto.PageResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Comment", description = "댓글 관련 API")
public interface CommentControllerDocs {
    @Operation(summary = "댓글 목록 조회", description = "특정 게시글의 댓글 목록을 계층 구조(부모-자식)로 페이징하여 조회합니다.")
    @Parameters({
            @Parameter(name = "boardId", description = "댓글 목록을 조회할 게시글의 ID입니다.", example = "1"),
            @Parameter(name = "userId", description = "현재 로그인한 유저 ID입니다.", hidden = true),
            @Parameter(name = "pageable", description = "페이징 파라미터입니다. (page, size, sort)")
    })
    ApiResponse<PageResponseDto<CommentListDTO.Comment>> getComments(
            @PathVariable Long boardId,
            @AuthUser Long userId,
            Pageable pageable
    );

    @Operation(summary = "댓글 작성", description = "게시글에 댓글 또는 대댓글을 작성합니다.")
    @Parameters({
            @Parameter(name = "boardId", description = "댓글을 작성할 게시글의 ID입니다."),
            @Parameter(name = "userId", description = "게시글을 작성하는 유저 ID입니다", hidden = true)
    })
    ApiResponse<CommentCreateDTO.Response> addComment(
            @PathVariable Long boardId,
            @RequestBody @Valid CommentCreateDTO.Request request,
            @AuthUser Long userId
    );

    @Operation(summary = "대댓글 작성", description = "특정 댓글에 대한 답글(대댓글)을 작성합니다.")
    @Parameters({
            @Parameter(name = "boardId", description = "게시글의 ID입니다.", example = "1"),
            @Parameter(name = "commentId", description = "부모 댓글의 ID입니다.", example = "10"),
            @Parameter(name = "userId", description = "작성자 유저 ID", hidden = true)
    })
    ApiResponse<ReplyDTO.Response> addReply(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @RequestBody @Valid ReplyDTO.Request request,
            @AuthUser Long userId
    );

    @Operation(summary = "댓글 삭제", description = "본인이 작성한 댓글을 삭제합니다.")
    @Parameters({
            @Parameter(name = "boardId", description = "게시글의 ID입니다."),
            @Parameter(name = "commentId", description = "삭제할 댓글의 ID입니다."),
            @Parameter(name = "userId", description = "댓글을 삭제하는 유저 ID입니다.",hidden = true)
    })
    ApiResponse<Object> deleteComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @AuthUser Long userId
    );
}
