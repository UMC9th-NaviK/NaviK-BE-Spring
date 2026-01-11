package navik.domain.board.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import navik.domain.board.dto.BoardCreateRequestDTO;
import navik.domain.board.dto.BoardResponseDTO;
import navik.domain.board.service.BoardService;
import navik.domain.job.enums.JobType;
import navik.domain.users.entity.User;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.code.status.GeneralSuccessCode;
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
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
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
            // Pagable로 리팩터링, @SwaggerPagable 이걸 사용하면 좋음. swagger ui에서 불필요한 페이저블의 필드가 사라지고 page, size 두 개만 노출됨
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
     * @param user
     * @return
     */
    @PostMapping("/")
    public ApiResponse<Long> createBoard(
            @RequestBody @Valid BoardCreateRequestDTO request,
            @RequestAttribute User user
    ) {
        Long boardId = boardService.createBoard(user, request);
        return ApiResponse.onSuccess(GeneralSuccessCode._OK, boardId);
    }
}
