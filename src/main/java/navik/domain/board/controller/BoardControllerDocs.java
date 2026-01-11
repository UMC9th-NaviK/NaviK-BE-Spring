package navik.domain.board.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.domain.board.dto.BoardCreateRequestDTO;
import navik.domain.board.dto.BoardResponseDTO;
import navik.domain.board.dto.BoardUpdateRequestDTO;
import navik.domain.job.enums.JobType;
import navik.global.apiPayload.ApiResponse;
import navik.global.dto.PageResponseDto;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;

@Tag(name = "Board", description = "게시판 관련 API")
public interface BoardControllerDocs {

    @Operation(summary = "게시글 전체 목록 조회", description = "게시글을 페이징하여 최신순으로 조회합니다.")
    ApiResponse<PageResponseDto<BoardResponseDTO>> getBoards(
            @ParameterObject Pageable pageable // page, size, sort 노출
    );

    @Operation(summary = "게시글 직무별 조회", description = "특정 직무에 해당하는 게시글을 페이징하여 최신순으로 조회합니다.")
    @Parameter(name = "jobType", description = "조회할 직무 타입")
    ApiResponse<PageResponseDto<BoardResponseDTO>> getBoardsByJob(
            JobType jobType,
            @ParameterObject Pageable pageable
    );

    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 정보를 조회합니다.")
    @Parameter(name = "boardId", description = "조회할 게시글 ID", example = "1")
    ApiResponse<BoardResponseDTO> getBoardDetail(Long boardId);

    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다.")
    @Parameter(name = "userId", description = "작성자 ID (경로변수)", example = "1")
    ApiResponse<Long> createBoard(BoardCreateRequestDTO request, Long userId);

    @Operation(summary = "게시글 수정", description = "게시글의 제목과 내용을 수정합니다.")
    @Parameters({
            @Parameter(name = "boardId", description = "수정할 게시글 ID", example = "1"),
            @Parameter(name = "userId", description = "작성자 ID (경로변수)", example = "1")
    })
    ApiResponse<Long> updateBoard(Long boardId, BoardUpdateRequestDTO request, Long userId);

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제 합니다.")
    @Parameters({
            @Parameter(name = "boardId", description = "삭제할 게시글 ID", example = "1"),
            @Parameter(name = "userId", description = "작성자 ID (경로변수)", example = "1")
    })
    ApiResponse<Object> deleteBoard(Long boardId, Long userId);
}
