package navik.domain.board.service;

import lombok.RequiredArgsConstructor;
import navik.domain.board.converter.BoardConverter;
import navik.domain.board.dto.BoardCreateRequestDTO;
import navik.domain.board.dto.BoardResponseDTO;
import navik.domain.board.entity.Board;
import navik.domain.board.repository.BoardLikeRepository;
import navik.domain.board.repository.BoardRepository;
import navik.domain.board.repository.CommentRepository;
import navik.domain.users.entity.User;
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

    public List<BoardResponseDTO> getBoardList() {
        return boardRepository.findByArticleDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .map(board -> BoardConverter.toResponse(
                        board,
                        boardLikeRepository.countLikeByBoard(board),
                        commentRepository.countCommentByBoard(board)
                ))
                .toList();
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
}
