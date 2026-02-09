package navik.domain.board.controller.board;

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
import navik.domain.board.dto.board.BoardCreateDTO;
import navik.domain.board.dto.board.BoardResponseDTO;
import navik.domain.board.dto.board.BoardUpdateDTO;
import navik.domain.board.dto.boardLike.BoardLikeDTO;
import navik.domain.board.service.board.BoardCommandService;
import navik.domain.board.service.board.BoardQueryService;
import navik.domain.board.service.boardLike.BoardLikeCommandService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.exception.code.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;
import navik.global.dto.CursorResponseDTO;

@RestController
@RequestMapping("/v1/boards")
@RequiredArgsConstructor
public class BoardController implements BoardControllerDocs {
	private final BoardQueryService boardQueryService;
	private final BoardLikeCommandService boardLikeCommandService;
	private final BoardCommandService boardCommandService;

	/**
	 * 게시글 전체 조회
	 * @param cursor
	 * @param pageSize
	 * @return
	 */
	@Override
	@GetMapping // 전체
	public ApiResponse<CursorResponseDTO<BoardResponseDTO.BoardDTO>> getBoards(
		@RequestParam(value = "cursor", required = false) String cursor,
		@RequestParam(value = "size", defaultValue = "10") int pageSize
	) {
		CursorResponseDTO<BoardResponseDTO.BoardDTO> response = boardQueryService.getBoardList(cursor, pageSize);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, response);
	}

	/**
	 * 게시글 직무별 조회
	 * @param jobName
	 * @param cursor
	 * @param pageSize
	 * @return
	 */
	@Override
	@GetMapping("/jobs") // 전체
	public ApiResponse<CursorResponseDTO<BoardResponseDTO.BoardDTO>> getBoardsByJob(
		@RequestParam(name = "jobName") String jobName,
		@RequestParam(value = "cursor", required = false) String cursor,
		@RequestParam(value = "size", defaultValue = "10") int pageSize
	) {
		CursorResponseDTO<BoardResponseDTO.BoardDTO> response = boardQueryService.getBoardListByJob(jobName, cursor,
			pageSize);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, response);
	}

	/**
	 * HOT 게시글 조회
	 * @param cursor
	 * @param size
	 * @return
	 */
	@GetMapping("/hot")
	public ApiResponse<CursorResponseDTO<BoardResponseDTO.BoardDTO>> getHotBoards(
		@RequestParam(value = "cursor", required = false) String cursor,
		@RequestParam(defaultValue = "10") int size
	) {
		BoardResponseDTO.HotBoardListDTO response = boardQueryService.getHotBoardList(cursor, size);
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
	 * @param cursor
	 * @param size
	 * @return
	 */
	@Override
	@GetMapping("/search")
	public ApiResponse<CursorResponseDTO<BoardResponseDTO.BoardDTO>> searchBoards(
		@RequestParam String keyword,
		@RequestParam(value = "type", defaultValue = "ALL") String type,
		@RequestParam(value = "jobName", required = false) String jobName,
		@RequestParam(value = "cursor", required = false) String cursor,
		@RequestParam(value = "size", defaultValue = "10") int size
	) {
		CursorResponseDTO<BoardResponseDTO.BoardDTO> response = boardQueryService.searchBoard(keyword, type, jobName,
			cursor, size);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, response);
	}

	/**
	 * 게시글 상세 조회
	 * @param boardId
	 * @return
	 */
	@GetMapping("/{boardId}")
	public ApiResponse<BoardResponseDTO.BoardDTO> getBoardDetail(
		@PathVariable Long boardId,
		@AuthUser Long userId
	) {
		BoardResponseDTO.BoardDTO boardDetail = boardQueryService.getBoardDetail(boardId, userId);
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

		BoardLikeDTO.Response response = boardLikeCommandService.toggleBoardLike(parameter);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, response);
	}
}

