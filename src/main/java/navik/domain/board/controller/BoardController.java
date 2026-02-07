package navik.domain.board.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import navik.domain.board.dto.BoardCreateDTO;
import navik.domain.board.dto.BoardLikeDTO;
import navik.domain.board.dto.BoardResponseDTO;
import navik.domain.board.dto.BoardUpdateDTO;
import navik.domain.board.service.BoardCommandService;
import navik.domain.board.service.BoardLikeService;
import navik.domain.board.service.BoardQueryService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.exception.code.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;
import navik.global.dto.CursorResponseDTO;
import navik.global.swagger.SwaggerPageable;

@RestController
@RequestMapping("/v1/boards")
@RequiredArgsConstructor
public class BoardController implements BoardControllerDocs {
	private final BoardQueryService boardQueryService;
	private final BoardLikeService boardLikeService;
	private final BoardCommandService boardCommandService;

	/**
	 * 게시글 전체 조회
	 * @param lastId
	 * @param pageSize
	 * @return
	 */
	@Override
	@SwaggerPageable
	@GetMapping // 전체
	public ApiResponse<CursorResponseDTO<BoardResponseDTO.BoardDTO>> getBoards(
		@RequestParam(value = "cursor", required = false) Long lastId,
		@RequestParam(value = "size", defaultValue = "10") int pageSize
	) {
		CursorResponseDTO<BoardResponseDTO.BoardDTO> response = boardQueryService.getBoardList(lastId, pageSize);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, response);
	}

	/**
	 * 게시글 직무별 조회
	 * @param jobName
	 * @param lastId
	 * @param pageSize
	 * @return
	 */
	@Override
	@SwaggerPageable
	@GetMapping("/jobs") // 전체
	public ApiResponse<CursorResponseDTO<BoardResponseDTO.BoardDTO>> getBoardsByJob(
		@RequestParam(name = "jobName") String jobName,
		@RequestParam(value = "cursor", required = false) Long lastId,
		@RequestParam(value = "size", defaultValue = "10") int pageSize
	) {
		CursorResponseDTO<BoardResponseDTO.BoardDTO> response = boardQueryService.getBoardListByJob(jobName, lastId,
			pageSize);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, response);
	}

	/**
	 * HOT 게시글 조회
	 * @param cursor
	 * @param pageable
	 * @return
	 */
	@GetMapping("/hot")
	@SwaggerPageable
	public ApiResponse<CursorResponseDTO<BoardResponseDTO.BoardDTO>> getHotBoards(
		@RequestParam(value = "cursor", required = false) String cursor,
		@PageableDefault(size = 10) Pageable pageable
	) {
		BoardResponseDTO.HotBoardListDTO response = boardQueryService.getHotBoardList(cursor, pageable);
		CursorResponseDTO<BoardResponseDTO.BoardDTO> result = CursorResponseDTO.of(
			response.getBoardList(),
			response.getHasNext(),
			response.getNextCursor()
		);

		return ApiResponse.onSuccess(GeneralSuccessCode._OK, result);
	}

	/**
	 * 게시글 검색
	 * @param keyword
	 * @param lastId
	 * @param size
	 * @return
	 */
	@Override
	@SwaggerPageable
	@GetMapping("/search")
	public ApiResponse<CursorResponseDTO<BoardResponseDTO.BoardDTO>> searchBoards(
		@RequestParam String keyword,
		@RequestParam(value = "cursor", required = false) Long lastId,
		@RequestParam(value = "size", defaultValue = "10") int size
	) {
		CursorResponseDTO<BoardResponseDTO.BoardDTO> response = boardQueryService.searchBoard(keyword, lastId, size);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, response);
	}

	/**
	 * 게시글 상세 조회
	 * @param boardId
	 * @return
	 */
	@GetMapping("/{boardId}")
	public ApiResponse<BoardResponseDTO.BoardDTO> getBoardDetail(
		@PathVariable Long boardId
	) {
		BoardResponseDTO.BoardDTO boardDetail = boardQueryService.getBoardDetail(boardId);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, boardDetail);
	}

	/**
	 * 게시글 생성
	 * @param request
	 * @param userId
	 * @return
	 */
	@PostMapping
	public ApiResponse<Long> createBoard(
		@RequestBody @Valid BoardCreateDTO request,
		@AuthUser Long userId
	) {
		Long boardId = boardCommandService.createBoard(userId, request);
		return ApiResponse.onSuccess(GeneralSuccessCode._CREATED, boardId);
	}

	/**
	 * 게시글 수정
	 * @param boardId
	 * @param request
	 * @param userId
	 * @return
	 */
	@PutMapping("/{boardId}")
	public ApiResponse<Long> updateBoard(
		@PathVariable Long boardId,
		@RequestBody @Valid BoardUpdateDTO request,
		@AuthUser Long userId
	) {
		Long updatedBoardId = boardCommandService.updateBoard(boardId, userId, request);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, updatedBoardId);
	}

	/**
	 * 게시글 삭제
	 * @param boardId
	 * @param userId
	 * @return
	 */
	@DeleteMapping("/{boardId}")
	public ApiResponse<Object> deleteBoard(
		@PathVariable Long boardId,
		@AuthUser Long userId
	) {
		boardCommandService.deleteBoard(boardId, userId);
		return ApiResponse.onSuccess(GeneralSuccessCode._DELETED);
	}

	/**
	 * 게시글 좋아요 확인
	 * @param boardId
	 * @param userId
	 * @return
	 */
	@PostMapping("/{boardId}/like")
	public ApiResponse<BoardLikeDTO.Response> toggleLike(
		@PathVariable Long boardId,
		@AuthUser Long userId
	) {
		BoardLikeDTO.Parameter parameter = BoardLikeDTO.Parameter.builder()
			.boardId(boardId)
			.userId(userId)
			.build();

		BoardLikeDTO.Response response = boardLikeService.toggleBoardLike(parameter);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, response);
	}
}

