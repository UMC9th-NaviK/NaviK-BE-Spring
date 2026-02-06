package navik.domain.board.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import navik.domain.board.dto.CommentListDTO;
import navik.domain.board.entity.Comment;
import navik.global.dto.CursorResponseDto;

public class CommentListConverter {

	// controller에서 받은 파라미터를 DTO로 저장
	public static CommentListDTO.Parameter toParameter(Long userId, Long boardId, Pageable pageable) {
		return CommentListDTO.Parameter.builder()
			.userId(userId)
			.boardId(boardId)
			.pageable(pageable)
			.build();
	}

	// Slice로 변경 및 파라미터 최적화 진행
	public static CursorResponseDto<CommentListDTO.ResponseComment> toResponse(
		Slice<Comment> comments,
		String nextCursor,
		Long currentUserId
	) {

		// 부모 댓글 리스트
		List<CommentListDTO.ResponseComment> rootComments = new ArrayList<>();
		// Map을 사용하여 부모 댓글 빠르게 찾을 수 있도록 리팩토링
		Map<Long, CommentListDTO.ResponseComment> commentMap = new TreeMap<>();

		// 2. 모든 데이터를 먼저 DTO로 변환하여 Map에 저장
		comments.getContent().forEach(comment -> {
			CommentListDTO.ResponseComment dto = toComment(comment, currentUserId);
			commentMap.put(dto.getCommentId(), dto);
		});

		// 3. 다시 순회하며 계층 구조 형성
		comments.getContent().forEach(comment -> {
			CommentListDTO.ResponseComment dto = commentMap.get(comment.getId());

			if (comment.getParentComment() != null) {
				// 부모가 현재 페이지(Map)에 존재하는 경우에만 자식으로 삽입
				CommentListDTO.ResponseComment parentDto = commentMap.get(comment.getParentComment().getId());
				if (parentDto != null) {
					parentDto.getChildResponseComments().add(dto);
				} else {
					// 부모가 현재 페이지에 없으면 최상위 댓글
					rootComments.add(dto);
				}
			} else {
				// 부모 댓글(null)인 경우 최상위 리스트에 추가
				rootComments.add(dto);
			}
		});

		return CursorResponseDto.<CommentListDTO.ResponseComment>builder()
			.content(rootComments)
			.nextCursor(nextCursor)
			.pageSize(rootComments.size())
			.hasNext(comments.hasNext())
			.build();
	}

	// 단일 엔티티 변환시 currentUserId 직접 받아서 비교 진행한다
	public static CommentListDTO.ResponseComment toComment(Comment comment, Long currentUserId) {

		String displayContent = comment.isDeleted() ? "삭제되었습니다" : comment.getContent();

		return CommentListDTO.ResponseComment.builder()
			.boardId(comment.getBoard().getId())
			.commentId(comment.getId())
			.userId(comment.getUser().getId())
			.parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
			.profileImageUrl(comment.getUser().getProfileImageUrl())
			.level(comment.getUser().getLevel())
			.content(displayContent)
			.isEntryLevel(comment.getUser().getIsEntryLevel())
			.jobName(comment.getUser().getJob().getName())
			.nickname(comment.getUser().getNickname())
			.isMyComment(comment.getUser().getId().equals(currentUserId))
			.createdAt(comment.getCreatedAt())
			.childResponseComments(new ArrayList<>()) // 빈 리스트로 초기화
			.build();
	}
}
