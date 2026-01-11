package navik.domain.board.service;

import lombok.RequiredArgsConstructor;
import navik.domain.board.converter.BoardConverter;
import navik.domain.board.dto.BoardCreateRequestDTO;
import navik.domain.board.dto.BoardResponseDTO;
import navik.domain.board.dto.BoardUpdateRequestDTO;
import navik.domain.board.entity.Board;
import navik.domain.board.repository.BoardLikeRepository;
import navik.domain.board.repository.BoardRepository;
import navik.domain.board.repository.CommentRepository;
import navik.domain.job.enums.JobType;
import navik.domain.users.entity.User;
import navik.global.apiPayload.code.status.GeneralErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final CommentRepository commentRepository;

    /**
     * 전체 조회
     *
     * @param pageable
     * @return
     */
    public Page<BoardResponseDTO> getBoardList(Pageable pageable) {
        return boardRepository.findAll(pageable)
                .map(board -> BoardConverter.toResponse(
                        board,
                        boardLikeRepository.countLikeByBoard(board),
                        commentRepository.countCommentByBoard(board)
                ));
    }

    /**
     * 직무별 게시글 조회
     * @param pageable
     * @param jobType
     * @return
     */
    public Page<BoardResponseDTO> getBoardListByJob(Pageable pageable, JobType jobType) {
        return boardRepository.findByUserJobType(jobType, pageable)
                .map(board -> BoardConverter.toResponse(
                        board,
                        boardLikeRepository.countLikeByBoard(board),
                        commentRepository.countCommentByBoard(board)
                ));
    }

    /**
     * 상세 게시글 조회
     * @param boardId
     * @return
     */

    public BoardResponseDTO getBoardDetail(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.BOARD_NOT_FOUND));

        board.incrementArticleViews(); // 조회수 증가, 동시성 이슈로 redis로 변경예정
        boardRepository.save(board); // 변경된 조회수 저장

        return BoardConverter.toResponse(
                board,
                boardLikeRepository.countLikeByBoard(board),
                commentRepository.countCommentByBoard(board)
        );
    }

    /**
     *
     * @param userId
     * @param request
     * @return
     */
    public Long createBoard(Long userId, BoardCreateRequestDTO request) {

        Board board = Board.builder()
                .id(userId)
                .articleTitle(request.getArticleTitle())
                .articleContent(request.getArticleContent())
                .articleViews(0)
                .build();

        return boardRepository.save(board).getId();
    }

    /**
     * 게시글 수정
     * @param boardId
     * @param userId
     * @param request
     * @return
     */
    public Long updateBoard(Long boardId, Long userId, BoardUpdateRequestDTO request) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.BOARD_NOT_FOUND));

        if (!board.getUser().getId().equals(userId)) {
            throw new GeneralExceptionHandler(GeneralErrorCode.AUTH_BOARD_NOT_WRITER);
        }

        board.updateBoard(request.getArticleTitle(), request.getArticleContent());
        return boardRepository.save(board).getId();
    }

    /**
     * 게시글 삭제
     * @param boardId
     * @param userId
     */
    public void deleteBoard(Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId) // 게시글 찾을 수 없음
                .orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.BOARD_NOT_FOUND));

        if (!board.getUser().getId().equals(userId)) { // 게시글 작성자가 아님
            throw new GeneralExceptionHandler(GeneralErrorCode.AUTH_BOARD_NOT_WRITER);
        }
        boardRepository.delete(board);
    }
}
