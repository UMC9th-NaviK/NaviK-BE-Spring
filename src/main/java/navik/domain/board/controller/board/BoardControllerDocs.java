package navik.domain.board.controller.board;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.domain.board.dto.board.BoardCreateDTO;
import navik.domain.board.dto.board.BoardResponseDTO;
import navik.domain.board.dto.board.BoardUpdateDTO;
import navik.domain.board.dto.boardLike.BoardLikeDTO;
import navik.global.apiPayload.ApiResponse;
import navik.global.auth.annotation.AuthUser;
import navik.global.dto.CursorResponseDTO;

@Tag(name = "Board", description = "게시판 관련 API")
public interface BoardControllerDocs {

	@Operation(
		summary = "전체 게시글 조회 API",
		description = "**[최신순 조회]** 커서 기반 페이징을 사용하여 전체 게시글을 조회합니다. 커서로는 마지막 게시글의 생성 시간을 사용합니다."
	)
	@Parameters({
		@Parameter(name = "cursor", description = "마지막으로 조회한 게시글의 생성 시간 (첫 조회 시에는 X)"),
		@Parameter(name = "size", description = "한 페이지에 가져올 게시글 개수 (기본 10개)", example = "10")
	})
	ApiResponse<CursorResponseDTO<BoardResponseDTO.BoardDTO>> getBoards(
		@RequestParam(value = "cursor", required = false) String cursor,
		@RequestParam(value = "size", defaultValue = "10") int pageSize
	);

	@Operation(
		summary = "직무별 게시글 조회 API",
		description = "특정 직무 게시글을 최신순으로 조회합니다. 커서로 생성 시간을 지원합니다."
	)
	@Parameters({
		@Parameter(name = "jobName", description = "조회할 직무의 이름, (예: 프로덕트 매니저, 프로덕트 디자이너, 프론트엔드 개발자, 백엔드 개발자)"),
		@Parameter(name = "cursor", description = "마지막으로 조회한 게시글의 생성 시간 (첫 조회 시에는 X)"),
		@Parameter(name = "size", description = "한 페이지에 가져올 게시글 개수 (기본 10개)", example = "10")
	})
	ApiResponse<CursorResponseDTO<BoardResponseDTO.BoardDTO>> getBoardsByJob(
		@RequestParam(name = "jobName") String jobName,
		@RequestParam(value = "cursor", required = false) String cursor,
		@RequestParam(value = "size", defaultValue = "10") int pageSize
	);

	@Operation(
		summary = "HOT 게시글 조회 API",
		description = "좋아요와 조회수 합산 점수가 높은 순으로 게시글을 조회합니다. '점수_시간' 형태의 복합 커서를 사용합니다."
	)
	@Parameters({
		@Parameter(name = "cursor", description = "마지막으로 조회한 게시글의 '점수_시간' (첫 조회 시에는 X)"),
		@Parameter(name = "size", description = "한 페이지에 가져올 게시글 개수", example = "10")
	})
	ApiResponse<CursorResponseDTO<BoardResponseDTO.BoardDTO>> getHotBoards(
		@RequestParam(value = "cursor", required = false) String cursor,
		@RequestParam(defaultValue = "10") int size
	);

	@Operation(
		summary = "게시글 검색 API",
		description = "탭(전체/직무/HOT) 범위 내에서 키워드를 검색합니다. 커서 형식은 탭 종류에 따라 달라집니다."
	)
	@Parameters({
		@Parameter(name = "keyword", description = "검색어", example = "Spring"),
		@Parameter(name = "type", description = "게시판 종류 (ALL, JOB, HOT)", example = "ALL"),
		@Parameter(name = "jobName", description = "직무 이름 (type이 JOB일 때 필수), (예: 프로덕트 매니저, 프로덕트 디자이너, 프론트엔드 개발자, 백엔드 개발자)"),
		@Parameter(name = "cursor", description = "마지막 게시글의 커서 (ALL/JOB은 '시간', HOT은 '점수_시간') (첫 조회 시에는 X)"),
		@Parameter(name = "size", description = "페이지 크기", example = "10")
	})
	ApiResponse<CursorResponseDTO<BoardResponseDTO.BoardDTO>> searchBoards(
		@RequestParam String keyword,
		@RequestParam(value = "type", defaultValue = "ALL") String type,
		@RequestParam(value = "jobName", required = false) String jobName,
		@RequestParam(value = "cursor", required = false) String cursor,
		@RequestParam(value = "size", defaultValue = "10") int size
	);

	// 상세 조회, 작성, 수정, 삭제, 좋아요 API는 기존 ID 기반이므로 유지합니다.
	@Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 정보를 조회합니다.")
	@Parameter(name = "boardId", description = "조회할 게시글 ID", example = "1")
	ApiResponse<BoardResponseDTO.BoardDTO> getBoardDetail(@PathVariable Long boardId, @AuthUser Long userId);

	@Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(
		value = {@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "CREATED")
		})
	ApiResponse<Long> createBoard(BoardCreateDTO request, @AuthUser Long userId);

	@Operation(summary = "게시글 수정", description = "게시글의 제목과 내용을 수정합니다.")
	@Parameters({
		@Parameter(name = "boardId", description = "수정할 게시글 ID", example = "1"),
	})
	ApiResponse<Long> updateBoard(@PathVariable Long boardId, BoardUpdateDTO request, @AuthUser Long userId);

	@Operation(summary = "게시글 삭제", description = "게시글을 삭제 합니다.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공")
	@Parameters({
		@Parameter(name = "boardId", description = "삭제할 게시글 ID", example = "1"),
	})
	ApiResponse<Object> deleteBoard(@PathVariable Long boardId, @AuthUser Long userId);

	@Operation(summary = "게시글 좋아요 확인", description = "게시글에 좋아요를 누르거나 취소합니다.")
	ApiResponse<BoardLikeDTO.Response> toggleLike(
		@PathVariable Long boardId,
		@AuthUser Long userId
	);
}