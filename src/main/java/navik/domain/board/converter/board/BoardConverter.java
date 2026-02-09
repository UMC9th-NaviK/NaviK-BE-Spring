package navik.domain.board.converter.board;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import navik.domain.board.dto.board.BoardResponseDTO;
import navik.domain.board.entity.Board;

public class BoardConverter {

	public static BoardResponseDTO.BoardDTO toBoardDTO(
		Board board,
		Integer commentCount,
		Boolean isLiked
	) {
		return BoardResponseDTO.BoardDTO.builder()
			.boardId(board.getId())
			.userId(board.getUser().getId())
			.jobName(board.getUser().getJob().getName())
			.profileImageUrl(board.getUser().getProfileImageUrl())
			.level(board.getUser().getLevel())
			.nickname(board.getUser().getNickname())
			.articleTitle(board.getArticleTitle())
			.articleContent(board.getArticleContent())
			.isEntryLevel(board.getUser().getIsEntryLevel())
			.likeCount(board.getArticleLikes())
			.commentCount(commentCount)
			.viewCount(board.getArticleViews())
			.isLiked(isLiked)
			.createdAt(board.getCreatedAt())
			.build();
	}

	public static BoardResponseDTO.HotBoardListDTO toHotBoardListDTO(
		List<Board> boards,
		Map<Long, Integer> commentCountMap,
		String nextCursor,
		boolean hasNext
	) {
		List<BoardResponseDTO.BoardDTO> boardList = boards.stream()
			.map(board1 -> toBoardDTO(
				board1,
				commentCountMap.getOrDefault(board1.getId(), 0),
				false
			))
			.collect(Collectors.toList());

		return BoardResponseDTO.HotBoardListDTO.builder()
			.boardList(boardList)
			.nextCursor(nextCursor)
			.hasNext(hasNext)
			.build();
	}
}
