package navik.domain.board.service;

import lombok.RequiredArgsConstructor;
import navik.domain.board.converter.CommentConverter;
import navik.domain.board.dto.CommentCreateRequestDTO;
import navik.domain.board.entity.Board;
import navik.domain.board.entity.Comment;
import navik.domain.board.repository.BoardRepository;
import navik.domain.board.repository.CommentRepository;
import navik.global.apiPayload.code.status.GeneralErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    public Long addComment(Long boardId, Long userId, CommentCreateRequestDTO request) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.BOARD_NOT_FOUND));

        Comment parentComment = null;
        if (request.getParentId() != null) {
            parentComment = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.COMMENT_NOT_FOUND));
        }

        Comment newComment = CommentConverter.toComment(request, userId, board, parentComment);
        return commentRepository.save(newComment).getId();
    }

    public void deleteComment(Long boardId, Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getBoard().getId().equals(boardId)) {
            throw new GeneralExceptionHandler(GeneralErrorCode.COMMENT_NOT_FOUND);
        }

        if(!comment.getUser().getId().equals(userId)) {
            throw new GeneralExceptionHandler(GeneralErrorCode.AUTH_COMMENT_NOT_WRITER);
        }

        commentRepository.delete(comment);
    }
}
