package navik.domain.recruitment.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import navik.domain.recruitment.exception.code.RecruitmentErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Getter
@RequiredArgsConstructor
public enum IndustryType {

	SERVICE("서비스업"),
	FINANCE_BANKING("금융·은행업"),
	IT_TELECOMMUNICATION("IT·정보통신업"),
	SALES_DISTRIBUTION("판매·유통업"),
	MANUFACTURING_CHEMICAL("제조·생산·화학업"),
	EDUCATION("교육업"),
	CONSTRUCTION("건설업"),
	MEDICAL_PHARMACEUTICAL("의료·제약업"),
	MEDIA_ADVERTISING("미디어·광고업"),
	CULTURE_ART_DESIGN("문화·예술·디자인업"),
	PUBLIC_ORGANIZATION("공공기관·협회");

	private final String label;

	@JsonCreator
	public static IndustryType deserialize(String industryType) {
		return Arrays.stream(values())
			.filter(type -> type.name().equalsIgnoreCase(industryType))
			.findAny()
			.orElseThrow(() -> new GeneralExceptionHandler(RecruitmentErrorCode.INDUSTRY_TYPE_NOT_FOUND));
	}
}
