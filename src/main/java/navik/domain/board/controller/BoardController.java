package navik.domain.board.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import navik.domain.board.dto.BoardCreateRequestDTO;
import navik.domain.board.dto.BoardResponseDTO;
import navik.domain.board.dto.BoardUpdateRequestDTO;
import navik.domain.board.service.BoardService;
import navik.domain.job.enums.JobType;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.code.status.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;
import navik.global.dto.PageResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/boards")
@RequiredArgsConstructor
public class BoardController implements BoardControllerDocs {
    private final BoardService boardService;
    /**
     * 게시글 전체조회
     * @param pageable
     * @return
     */
    @GetMapping("/") // 전체
    public ApiResponse<PageResponseDto<BoardResponseDTO>> getBoards(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<BoardResponseDTO> boardList = boardService.getBoardList(pageable);

        PageResponseDto<BoardResponseDTO> response = PageResponseDto.of(boardList);
        return ApiResponse.onSuccess(GeneralSuccessCode._OK, response);
    }

    /**
     * 게시글 직무별 조회
     * @param
     * @param
     * @param
     * @return
     */
    @GetMapping("/jobs") // 전체
    public ApiResponse<PageResponseDto<BoardResponseDTO>> getBoardsByJob(
            @RequestParam JobType jobType,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable
            ) {
                Page<BoardResponseDTO> boardList = boardService.getBoardListByJob(pageable, jobType);
                PageResponseDto<BoardResponseDTO> response = PageResponseDto.of(boardList);
                return ApiResponse.onSuccess(GeneralSuccessCode._OK, response);
    }

    /**
     * 게시글 상세 조회
     * @param boardId
     * @return
     */
    @GetMapping("/{boardId}")
    public ApiResponse<BoardResponseDTO> getBoardDetail(
            @PathVariable Long boardId
    ) {
        BoardResponseDTO boardDetail = boardService.getBoardDetail(boardId);
        return ApiResponse.onSuccess(GeneralSuccessCode._OK, boardDetail);
    }

    /**
     * 게시글 생성
     * @param request
     * @param userId
     * @return
     */

    @PostMapping("/")
    public ApiResponse<Long> createBoard(
            @RequestBody @Valid BoardCreateRequestDTO request,
            @AuthUser Long userId
    ) {
        Long boardId = boardService.createBoard(userId, request);
        return ApiResponse.onSuccess(GeneralSuccessCode._OK, boardId);
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
            @RequestBody @Valid BoardUpdateRequestDTO request,
            @AuthUser Long userId
    ) {
        Long updatedBoardId = boardService.updateBoard(boardId, userId, request);
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
        boardService.deleteBoard(boardId, userId);
        return ApiResponse.onSuccess(GeneralSuccessCode._DELETED);
    }
}

