package navik.domain.board.service;

import lombok.RequiredArgsConstructor;
import navik.domain.board.converter.BoardConverter;
import navik.domain.board.dto.BoardCreateRequestDTO;
import navik.domain.board.dto.BoardResponseDTO;
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

import java.util.List;

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
        return boardRepository.findByArticleDeletedFalse(pageable)
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
        return boardRepository.findByJobDeletedFalse(pageable, jobType)
                .map(board -> BoardConverter.toResponse(
                        board,
                        boardLikeRepository.countLikeByBoard(board),
                        commentRepository.countCommentByBoard(board)
                ));
    }

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

    public Long createBoard(User user, BoardCreateRequestDTO request) {
        Board board = Board.builder()
                .user(user)
                .articleTitle(request.getArticleTitle())
                .articleContent(request.getArticleContent())
                .articleViews(0)
                .articleDeleted(false)
                .build();

        return boardRepository.save(board).getId();
    }

    // 게시글 삭제



    // 게시글 수정
}
