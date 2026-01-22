package navik.global.dto;

import java.util.List;

import org.springframework.data.domain.Slice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 커서 기반 페이지네이션 응답을 위한 DTO 클래스입니다.
 *
 * @param <T> 데이터 리스트의 타입
 */
@Getter
@Builder
@AllArgsConstructor
public class CursorResponseDto<T> {

	/**
	 * 데이터 리스트
	 */
	private final List<T> content;

	/**
	 * 현재 페이지의 데이터 개수
	 */
	private final int pageSize;

	/**
	 * 다음 페이지를 위한 커서 값 (응답의 마지막 아이템의 식별자)
	 */
	private final String nextCursor;

	/**
	 * 다음 페이지 존재 여부
	 */
	private final boolean hasNext;

	public CursorResponseDto(Slice<T> slice, String nextCursor) {
		this.content = slice.getContent();
		this.pageSize = slice.getNumberOfElements();
		this.nextCursor = nextCursor;
		this.hasNext = slice.hasNext();
	}

	/**
	 * 커서 기반 페이징 결과를 일반화하여 CursorResponseDto로 변환
	 */
	public static <T> CursorResponseDto<T> of(List<T> content, boolean hasNext, String nextCursor) {
		return CursorResponseDto.<T>builder()
			.content(content)
			.hasNext(hasNext)
			.nextCursor(nextCursor)
			.pageSize(content.size())
			.build();
	}

	public static <T> CursorResponseDto<T> of(Slice<T> slice, String nextCursor) {
		return new CursorResponseDto<>(slice, nextCursor);
	}
}