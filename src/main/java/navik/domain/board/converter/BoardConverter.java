package navik.domain.board.converter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import navik.domain.board.dto.BoardResponseDTO;
import navik.domain.board.entity.Board;

public class BoardConverter {

	public static BoardResponseDTO.BoardDTO toBoardDTO(
		Board board,
		Integer likeCount,
		Integer commentCount
	) {
		return BoardResponseDTO.BoardDTO.builder()
			.boardId(board.getId())
			.userId(board.getUser().getId())
			.jobType(board.getUser().getJob().getName()) // 경력도 추가해야함
			.nickname(board.getUser().getNickname())
			.articleTitle(board.getArticleTitle())
			.articleContent(board.getArticleContent())
			.likeCount(likeCount)
			.commentCount(commentCount)
			.viewCount(board.getArticleViews())
			.createdAt(board.getCreatedAt())
			.build();
	}

	public static BoardResponseDTO.HotBoardListDTO toHotBoardListDTO(
		List<Board> boards,
		Map<Long, Integer> likeCountMap,
		Map<Long, Integer> commentCountMap,
		String nextCursor,
		boolean hasNext
	) {
		List<BoardResponseDTO.BoardDTO> boardList = boards.stream()
			.map(board1 -> toBoardDTO(
				board1,
				likeCountMap.getOrDefault(board1.getId(), 0),
				commentCountMap.getOrDefault(board1.getId(), 0)
			))
			.collect(Collectors.toList());

		return BoardResponseDTO.HotBoardListDTO.builder()
			.boardList(boardList)
			.nextCursor(nextCursor)
			.hasNext(hasNext)
			.build();
	}
}
