package navik.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 페이지 기반 페이지네이션 응답을 위한 DTO 클래스입니다.
 *
 * @param <T> 데이터 리스트의 타입
 */
@Getter
@Builder
@AllArgsConstructor
public class PageResponseDto<T> {

    /**
     * 데이터 리스트
     */
    private final List<T> content;

    /**
     * 현재 페이지 번호 (0부터 시작)
     */
    private final int pageNumber;

    /**
     * 페이지 크기
     */
    private final int pageSize;

    /**
     * 전체 페이지 수
     */
    private final int totalPages;

    /**
     * 전체 요소 개수
     */
    private final long totalElements;

    /**
     * 마지막 페이지 여부
     */
    private final boolean last;

    /**
     * 커서정보 필드 추가
     */
    private final String nextCursor;

    public PageResponseDto(Page<T> page) {
        this.content = page.getContent();
        this.pageNumber = page.getNumber(); // 0-based로 수정
        this.pageSize = page.getSize();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.last = page.isLast();
        this.nextCursor = null;
    }

    /**
     * Page 객체를 PageResponseDto로 변환하는 정적 팩토리 매서드
     */

    public static <T> PageResponseDto<T> of(Page<T> page) {
        return new PageResponseDto<>(page);
    }

    /**
     * 커서 기반 페이징 결과를 일반화하여 PageResponseDto로 변환
     */
    public static <T> PageResponseDto<T> of(List<T> content, boolean hasNext, String nextCursor) {
        return PageResponseDto.<T>builder()
                .content(content)
                .last(!hasNext)
                .nextCursor(nextCursor)
                .pageNumber(0)
                .pageSize(content.size())
                .totalElements(content.size())
                .totalPages(0)
                .build();
    }
}
