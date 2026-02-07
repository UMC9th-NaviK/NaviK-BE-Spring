package navik.global.dto;

import java.util.List;

import org.springframework.data.domain.Slice;

import lombok.Getter;

@Getter
public class SliceResponseDTO<T> {

	/**
	 * 데이터 리스트
	 */
	private final List<T> content;

	/**
	 * 요청한 페이지 크기
	 */
	private final int pageSize;

	/**
	 * 다음 페이지 존재 여부
	 */
	private final boolean hasNext;

	public SliceResponseDTO(Slice<T> slice) {
		this.content = slice.getContent();
		this.pageSize = slice.getSize();
		this.hasNext = slice.hasNext();
	}

	public static <T> SliceResponseDTO<T> of(Slice<T> slice) {
		return new SliceResponseDTO<>(slice);
	}
}
