package navik.domain.recruitment.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import navik.domain.recruitment.exception.code.RecruitmentErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;

@Getter
@RequiredArgsConstructor
public enum AreaType {

	SEOUL("서울"),
	BUSAN("부산"),
	DAEGU("대구"),
	INCHEON("인천"),
	GWANGJU("광주"),
	DAEJEON("대전"),
	ULSAN("울산"),
	SEJONG("세종"),
	GYEONGGI("경기"),
	GANGWON("강원"),
	CHUNGBUK("충북"),
	CHUNGNAM("충남"),
	JEONBUK("전북"),
	JEONNAM("전남"),
	GYEONGBUK("경북"),
	GYEONGNAM("경남"),
	JEJU("제주"),
	OVERSEAS("해외");

	private final String label;

	@JsonCreator
	public static AreaType deserialize(String areaType) {
		return Arrays.stream(values())
			.filter(type -> type.name().equalsIgnoreCase(areaType))
			.findAny()
			.orElseThrow(() -> new GeneralException(RecruitmentErrorCode.AREA_TYPE_NOT_FOUND));
	}
}