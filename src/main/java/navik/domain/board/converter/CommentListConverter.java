package navik.domain.board.converter;

import navik.domain.board.dto.CommentListDTO;
import navik.domain.board.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

public class CommentListConverter {

    // controller에서 받은 파라미터를 DTO로 저장
    public static CommentListDTO.Parameter toParameter(Long userId, Long boardId, Pageable pageable) {
        return CommentListDTO.Parameter.builder()
                .userId(userId)
                .boardId(boardId)
                .pageable(pageable)
                .build();
    }

    // 계층 구조가 적용된 Page 응답으로 변환
    public static Page<CommentListDTO.ResponseComment> toResponse(Page<Comment> comments, Pageable pageable, List<Boolean> isMyComments) {

        List<CommentListDTO.ResponseComment> responseCommentDtoList = new ArrayList<>();
        List<Comment> commentList = comments.getContent();

        for (int i = 0; i < commentList.size(); i++) {
            // 1. 개별 엔티티를 DTO로 변환
            CommentListDTO.ResponseComment responseCommentDto = toComment(commentList.get(i), isMyComments.get(i));

            // 2. 계층형 로직: 자식 댓글인 경우 바로 직전에 추가된 부모의 리스트에 삽입
            if (responseCommentDto.getParentCommentId() != null && !responseCommentDtoList.isEmpty()) {
                CommentListDTO.ResponseComment parent = responseCommentDtoList.get(responseCommentDtoList.size() - 1);
                parent.getChildResponseComments().add(responseCommentDto);
            } else {
                // 부모 댓글인 경우 최상위 리스트에 추가
                responseCommentDtoList.add(responseCommentDto);
            }
        }
        return new PageImpl<>(responseCommentDtoList, pageable, comments.getTotalElements());
    }

    // 단일 엔티티를 DTO로 매핑
    public static CommentListDTO.ResponseComment toComment(Comment comment, Boolean isMyComment) {
        return CommentListDTO.ResponseComment.builder()
                .commentId(comment.getId())
                .userId(comment.getUser().getId())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .content(comment.getContent())
                .nickname(comment.getUser().getNickname())
                .isMyComment(isMyComment)
                .createdAt(comment.getCreatedAt())
                .childResponseComments(new ArrayList<>()) // 빈 리스트로 초기화
                .build();
    }
}
