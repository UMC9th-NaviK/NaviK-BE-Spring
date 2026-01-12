package navik.global.dto;

import lombok.Getter;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 페이지 기반 페이지네이션 응답을 위한 DTO 클래스입니다.
 *
 * @param <T> 데이터 리스트의 타입
 */
@Getter
public class PageResponseDto<T> {

	/**
	 * 데이터 리스트
	 */
	private final List<T> content;

	/**
	 * 현재 페이지 번호 (1부터 시작)
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

	public PageResponseDto(Page<T> page) {
		this.content = page.getContent();
		this.pageNumber = page.getNumber() + 1; // 0-based to 1-based
		this.pageSize = page.getSize();
		this.totalPages = page.getTotalPages();
		this.totalElements = page.getTotalElements();
		this.last = page.isLast();
	}
}
