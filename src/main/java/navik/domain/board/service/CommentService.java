package navik.domain.board.service;

import lombok.RequiredArgsConstructor;
import navik.domain.board.converter.CommentConverter;
import navik.domain.board.converter.CommentListConverter;
import navik.domain.board.converter.ReplyConverter;
import navik.domain.board.dto.CommentCreateDTO;
import navik.domain.board.dto.CommentDeleteDTO;
import navik.domain.board.dto.CommentListDTO;
import navik.domain.board.dto.ReplyDTO;
import navik.domain.board.entity.Board;
import navik.domain.board.entity.Comment;
import navik.domain.board.repository.BoardRepository;
import navik.domain.board.repository.CommentRepository;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.code.status.GeneralErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    /**
     * 게시글의 댓글 목록 조회
     * @param parameter
     * @return
     */

    @Transactional(readOnly = true)
    public Page<CommentListDTO.Comment> getCommentList(CommentListDTO.Parameter parameter) {
        // 1. 게시글 조회
        Page<Comment> comments = commentRepository.findByParentCommentId(parameter.getBoardId(), parameter.getPageable());

        // 2. 내가 쓴 댓글이 맞는지 확인
        List<Boolean> isMyComments = comments.stream()
                .map(comment -> comment.getUser().getId().equals(parameter.getUserId()))
                .toList();

        // 3. DTO 변환
        return CommentListConverter.toResponse(comments, parameter.getPageable(), isMyComments);
    }

    /**
     * 댓글 생성
     * @param parameter
     * @return
     */
    @Transactional
    public CommentCreateDTO.Response createComment(CommentCreateDTO.Parameter parameter) {
        // 작성자 조회
        User user = userRepository.findById(parameter.getUserId())
                .orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.USER_NOT_FOUND));

        // 게시글 조회
        Board board = boardRepository.findById(parameter.getBoardId())
                .orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.BOARD_NOT_FOUND));

        // 댓글 생성
        Comment comment = CommentConverter.toComment(user, board, parameter.getContent());

        // 댓글 등록
        commentRepository.save(comment);

        return CommentConverter.toResponse(comment.getId());
    }

    @Transactional
    public ReplyDTO.Response createReply(ReplyDTO.Parameter parameter) {
        // 작성자 조회
        User user = userRepository.findById(parameter.getUserId())
                .orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.USER_NOT_FOUND));

        // 게시글 조회
        Board board = boardRepository.findById(parameter.getBoardId())
                .orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.BOARD_NOT_FOUND));

        // 부모 댓글 조회
        Comment parentComment = commentRepository.findById(parameter.getCommentId())
                .orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.COMMENT_NOT_FOUND));

        // 대댓글 작성
        Comment reply = ReplyConverter.toComment(user, board, parentComment, parameter.getContent());

        // 대댓글 등록
        commentRepository.save(reply);

        return ReplyConverter.toResponse(reply.getId());
    }

    /**
     * 댓글 삭제
     * @param parameter
     */
    @Transactional
    public void deleteComment(CommentDeleteDTO.Parameter parameter) {
        // 작성자 조회
        User user = userRepository.findById(parameter.getUserId())
                .orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.USER_NOT_FOUND));

        // 댓글 조회
        Comment comment = commentRepository.findById(parameter.getCommentId())
                .orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.COMMENT_NOT_FOUND));

        // 댓글 작성자가 맞는지 확인
        if(comment.getUser().equals(user)) {
            throw new GeneralExceptionHandler(GeneralErrorCode.AUTH_COMMENT_NOT_WRITER);
        }

        // 댓글 삭제
        commentRepository.delete(comment);
    }
}
