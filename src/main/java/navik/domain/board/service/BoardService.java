package navik.domain.board.service;

import lombok.RequiredArgsConstructor;
import navik.domain.board.converter.BoardConverter;
import navik.domain.board.dto.BoardCreateDTO;
import navik.domain.board.dto.BoardDTO;
import navik.domain.board.dto.BoardUpdateDTO;
import navik.domain.board.entity.Board;
import navik.domain.board.repository.BoardLikeRepository;
import navik.domain.board.repository.BoardRepository;
import navik.domain.board.repository.CommentRepository;
import navik.global.apiPayload.code.status.GeneralErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
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
    @Transactional
    public Page<BoardDTO> getBoardList(Pageable pageable) {
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
    @Transactional
    public Page<BoardDTO> getBoardListByJob(Pageable pageable, String jobType) {
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
    @Transactional
    public BoardDTO getBoardDetail(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.BOARD_NOT_FOUND));

        board.incrementArticleViews(); // 조회수 증가
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
    @Transactional
    public Long createBoard(Long userId, BoardCreateDTO request) {

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
    @Transactional
    public Long updateBoard(Long boardId, Long userId, BoardUpdateDTO request) {
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
    @Transactional
    public void deleteBoard(Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId) // 게시글 찾을 수 없음
                .orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.BOARD_NOT_FOUND));

        if (!board.getUser().getId().equals(userId)) { // 게시글 작성자가 아님
            throw new GeneralExceptionHandler(GeneralErrorCode.AUTH_BOARD_NOT_WRITER);
        }
        boardRepository.delete(board);
    }
}
