package navik.domain.board.service;

import lombok.RequiredArgsConstructor;
import navik.domain.board.converter.BoardLikeConverter;
import navik.domain.board.dto.BoardLikeDTO;
import navik.domain.board.entity.Board;
import navik.domain.board.entity.BoardLike;
import navik.domain.board.repository.BoardLikeRepository;
import navik.domain.board.repository.BoardRepository;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.code.status.GeneralErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardLikeService {
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final BoardLikeRepository boardLikeRepository;


    @Transactional
    public BoardLikeDTO.Response toggleBoardLike(BoardLikeDTO.Parameter parameter) {
        // 1. 작성자 조회
        User user = userRepository.findById(parameter.getUserId())
                .orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.USER_NOT_FOUND));
        // 2. 게시글 조회
        Board board = boardRepository.findById(parameter.getBoardId())
                .orElseThrow(() -> new GeneralExceptionHandler(GeneralErrorCode.BOARD_NOT_FOUND));

        // 3. 이미 좋아요 놀렀는지 확인
        Optional<BoardLike> boardLikeOpt = boardLikeRepository.findByBoardAndUser(board, user);

        boolean isLiked;
        if(boardLikeOpt.isPresent()) {
            // 이미 좋아요가 있다면 좋아요 취소
            boardLikeRepository.delete(boardLikeOpt.get());
            isLiked = false;
        }
        else {
            // 좋아요가 없다면 좋아요 추가
            BoardLike boardLike = BoardLikeConverter.toEntity(user, board);
            boardLikeRepository.save(boardLike);
            isLiked = true;
        }

        // 4. 좋아요 총합 조회
        long totalLikeCount = boardLikeRepository.countLikeByBoard(board);

        return BoardLikeConverter.toResponse(board.getId(), (int) totalLikeCount, isLiked);
    }
}
