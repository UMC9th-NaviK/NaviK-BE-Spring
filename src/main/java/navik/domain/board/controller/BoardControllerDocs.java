package navik.domain.board.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.domain.board.dto.BoardCreateDTO;
import navik.domain.board.dto.BoardLikeDTO;
import navik.domain.board.dto.BoardResponseDTO;
import navik.domain.board.dto.BoardUpdateDTO;
import navik.global.apiPayload.ApiResponse;
import navik.global.auth.annotation.AuthUser;
import navik.global.dto.CursorResponseDto;

@Tag(name = "Board", description = "게시판 관련 API")
public interface BoardControllerDocs {

	@Operation(
		summary = "전체 게시글 조회 API",
		description = "최신순으로 전체 게시글을 조회합니다. 커서 기반 페이징(ID)을 지원합니다."
	)
	@Parameters({
		@Parameter(name = "cursor", description = "마지막으로 조회한 게시글의 ID (첫 조회 시 비움)", example = "100"),
		@Parameter(name = "size", description = "한 페이지에 가져올 게시글 개수 (기본 10개)", example = "10")
	})
	ApiResponse<CursorResponseDto<BoardResponseDTO.BoardDTO>> getBoards(
		@RequestParam(value = "cursor", required = false) Long lastId,
		@RequestParam(value = "size", defaultValue = "10") int pageSize
	);

	@Operation(
		summary = "직무별 게시글 조회 API",
		description = "특정 직무에 해당하는 게시글을 최신순으로 조회합니다. 커서 기반 페이징(ID)을 지원합니다."
	)
	@Parameters({
		@Parameter(name = "jobName", description = "조회할 직무의 이름", example = "백엔드"),
		@Parameter(name = "cursor", description = "마지막으로 조회한 게시글의 ID (첫 조회 시 비움)", example = "90"),
		@Parameter(name = "size", description = "한 페이지에 가져올 게시글 개수 (기본 10개)", example = "10")
	})
	ApiResponse<CursorResponseDto<BoardResponseDTO.BoardDTO>> getBoardsByJob(
		@RequestParam(name = "jobName") String jobName,
		@RequestParam(value = "cursor", required = false) Long lastId,
		@RequestParam(value = "size", defaultValue = "10") int pageSize
	);

	@Operation(
		summary = "HOT 게시글 조회 API",
		description = "좋아요와 조회수 합산 점수가 높은 순으로 게시글을 조회합니다. 커서 기반 페이징을 지원합니다."
	)
	@Parameters({
		@Parameter(name = "cursor", description = "마지막으로 조회한 게시글의 '점수_ID' (첫 조회 시에는 비워서 보냅니다)", example = "15_23"),
		@Parameter(name = "size", description = "한 페이지에 가져올 게시글 개수 (기본 10개)", example = "10")
	})
	ApiResponse<CursorResponseDto<BoardResponseDTO.BoardDTO>> getHotBoards(
		@RequestParam(value = "cursor", required = false) String cursor,
		@PageableDefault(size = 10) Pageable pageable
	);

	@Operation(summary = "게시글 검색 API", description = "제목 또는 내용에 키워드가 포함된 글을 검색합니다.")
	@Parameters({
		@Parameter(name = "keyword", description = "검색어", example = "Spring"),
		@Parameter(name = "cursor", description = "마지막 게시글 ID", example = "100"),
		@Parameter(name = "size", description = "페이지 크기", example = "10")
	})
	ApiResponse<CursorResponseDto<BoardResponseDTO.BoardDTO>> searchBoards(
		@RequestParam String keyword,
		@RequestParam(value = "cursor", required = false) Long lastId,
		@RequestParam(value = "size", defaultValue = "10") int size
	);

	@Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 정보를 조회합니다.")
	@Parameter(name = "boardId", description = "조회할 게시글 ID", example = "1")
	ApiResponse<BoardResponseDTO.BoardDTO> getBoardDetail(Long boardId);

	@Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(
		value = {@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "CREATED")
		})
	@Parameter(name = "userId", description = "작성자 ID (경로변수)", example = "1")
	ApiResponse<Long> createBoard(BoardCreateDTO request, Long userId);

	@Operation(summary = "게시글 수정", description = "게시글의 제목과 내용을 수정합니다.")
	@Parameters({
		@Parameter(name = "boardId", description = "수정할 게시글 ID", example = "1"),
		@Parameter(name = "userId", description = "작성자 ID (경로변수)", example = "1")
	})
	ApiResponse<Long> updateBoard(Long boardId, BoardUpdateDTO request, Long userId);

	@Operation(summary = "게시글 삭제", description = "게시글을 삭제 합니다.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(
		value = {@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "DELETED")
		})
	@Parameters({
		@Parameter(name = "boardId", description = "삭제할 게시글 ID", example = "1"),
		@Parameter(name = "userId", description = "작성자 ID (경로변수)", example = "1")
	})
	ApiResponse<Object> deleteBoard(Long boardId, Long userId);

	@Operation(summary = "게시글 좋아요 확인", description = "게시글에 좋아요를 누르거나 취소합니다.")
	@Parameters({
		@Parameter(name = "boardId", description = "게시글 ID", example = "1"),
		@Parameter(name = "userId", description = "유저 ID", hidden = true)
	})
	ApiResponse<BoardLikeDTO.Response> toggleLike(
		@PathVariable Long boardId,
		@AuthUser Long userId
	);
}
