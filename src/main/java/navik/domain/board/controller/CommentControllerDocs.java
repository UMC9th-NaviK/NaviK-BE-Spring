package navik.domain.board.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import navik.domain.board.dto.CommentCreateRequestDTO;
import navik.global.apiPayload.ApiResponse;
import navik.global.auth.annotation.AuthUser;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Comment", description = "댓글 관련 API")
public interface CommentControllerDocs {

    @Operation(summary = "댓글 작성", description = "게시글에 댓글 또는 대댓글을 작성합니다.")
    @Parameters({
            @Parameter(name = "boardId", description = "댓글을 작성할 게시글의 ID입니다."),
            @Parameter(name = "userId", description = "게시글을 작성하는 유저 ID입니다", hidden = true)
    })
    ApiResponse<Long> addComment(
            @PathVariable Long boardId,
            @RequestBody @Valid CommentCreateRequestDTO request,
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
