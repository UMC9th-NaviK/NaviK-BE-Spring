package navik.domain.board.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import navik.domain.board.dto.BoardCreateRequestDTO;
import navik.domain.board.dto.BoardResponseDTO;
import navik.domain.board.service.BoardService;
import navik.domain.users.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/boards")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @GetMapping("/board")
    public ResponseEntity<List<BoardResponseDTO>> getBoards() {
        return ResponseEntity.ok(boardService.getBoardList());
    }

    @PostMapping("/board/create")
    public ResponseEntity<Long> createBoard(
            @RequestBody BoardCreateRequestDTO request,
            @RequestAttribute User user
            ) {
        return ResponseEntity.ok(boardService.createBoard(user, request));
    }
}
