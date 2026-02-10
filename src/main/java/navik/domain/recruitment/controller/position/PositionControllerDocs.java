package navik.domain.recruitment.controller.position;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.domain.recruitment.dto.position.PositionRequestDTO;
import navik.domain.recruitment.dto.position.PositionResponseDTO;
import navik.domain.recruitment.exception.code.RecruitmentErrorCode;
import navik.global.apiPayload.ApiResponse;
import navik.global.auth.annotation.AuthUser;
import navik.global.dto.CursorResponseDTO;
import navik.global.swagger.ApiErrorCodes;

@Tag(name = "Position", description = "채용 공고 중 포지션 관련 API")
public interface PositionControllerDocs {

	@Operation(summary = "사용자 추천 포지션 전체 검색", description = """
		**API 역할**
		- 마이페이지 -> 검색 필터를 적용하여 사용자에게 적합한 포지션을 전체 검색합니다.
		- Body로 주고받기 위해 의도적으로 `Post` 매핑하였습니다! 참고해주세요!
		
		**검색 필터 별 입력 가능한 상수 문자열 설명**
		- `특정 필터에 대해 상관없이 모두 보려면, 해당 필터의 List는 안보내시면 됩니다!`
		- JobType
			- PM("프로덕트 매니저")
			- DESIGNER("프로덕트 디자이너")
			- FRONTEND("프론트엔드 개발자")
		    - BACKEND("백엔드 개발자")
		- ExperienceType
			- ENTRY("신입")
		   	- EXPERIENCED("경력")
		- EmploymentType
			- FULL_TIME("정규직")
			- CONTRACT("계약직")
			- INTERN("인턴")
			- FREELANCER("프리랜서")
		- CompanySize
			- LARGE("대기업")
		  	- MID_LARGE("중견기업")
		  	- SMALL("중소기업")
		  	- PUBLIC("공기업")
		  	- FOREIGN("외국계기업")
		- EducationLevel
			- HIGH_SCHOOL("고등학교 졸업")
		   	- ASSOCIATE("전문대 졸업")
		   	- BACHELOR("4년제 대학 졸업")
		   	- MASTER("석사 졸업")
		   	- DOCTOR("박사 졸업")
		- AreaType
			- SEOUL("서울")
		   	- BUSAN("부산")
		   	- DAEGU("대구")
		   	- INCHEON("인천")
		   	- GWANGJU("광주")
		   	- DAEJEON("대전")
		   	- ULSAN("울산")
		   	- SEJONG("세종")
		   	- GYEONGGI("경기")
		   	- GANGWON("강원")
		   	- CHUNGBUK("충북")
		   	- CHUNGNAM("충남")
		   	- JEONBUK("전북")
		   	- JEONNAM("전남")
		   	- GYEONGBUK("경북")
		   	- GYEONGNAM("경남")
		   	- JEJU("제주")
		   	- OVERSEAS("해외")
		- IndustryType
			- SERVICE("서비스업")
			- FINANCE_BANKING("금융·은행업")
			- IT_TELECOMMUNICATION("IT·정보통신업")
			- SALES_DISTRIBUTION("판매·유통업")
			- MANUFACTURING_CHEMICAL("제조·생산·화학업")
			- EDUCATION("교육업")
			- CONSTRUCTION("건설업")
			- MEDICAL_PHARMACEUTICAL("의료·제약업")
			- MEDIA_ADVERTISING("미디어·광고업")
			- CULTURE_ART_DESIGN("문화·예술·디자인업")
			- PUBLIC_ORGANIZATION("공공기관·협회")
		- withEnded
			- true: 이미 끝난 공고도 검색에 포함합니다.
			- false: 이미 끝난 공고는 검색에 포함하지 않습니다.
		
		**커서 기반 페이징**
		- 첫 요청: cursor 없이 호출 → 첫 페이지 반환
		- 다음 요청: 응답의 `nextCursor` 값을 cursor 파라미터에 전달
		- size 기본값: 10
		- `hasNext`가 false면 마지막 페이지입니다.
		""")
	@ApiErrorCodes(
		enumClass = RecruitmentErrorCode.class,
		includes = {
			"AREA_TYPE_NOT_FOUND",
			"COMPANY_SIZE_NOT_FOUND",
			"EMPLOYMENT_TYPE_NOT_FOUND",
			"EXPERIENCE_TYPE_NOT_FOUND",
			"INDUSTRY_TYPE_NOT_FOUND",
			"JOB_TYPE_NOT_FOUND",
			"MAJOR_TYPE_NOT_FOUND"
		}
	)
	ApiResponse<CursorResponseDTO<PositionResponseDTO.RecommendedPosition>> getPositions(
		@AuthUser Long userId,
		@RequestBody PositionRequestDTO.SearchCondition searchCondition,
		@Parameter(description = "마지막으로 조회한 커서 Base64 인코딩 값 (nextCursor)") String cursor,
		@Parameter(description = "한번에 가져올 데이터 수") Integer size
	);

	@Operation(summary = "필터링된 포지션 전체 개수 조회", description = """
		**API 역할**
		- 마이페이지 -> 검색 필터를 적용하였을 때, Total 등록된 개수를 반환합니다.
		- `사용자 추천 포지션 전체 검색 API`에서 사용한 검색 필터를 그대로 여기 ResponseBody로 넣어주시면 됩니다!
		- 상수 문자열 및 에러 코드는 위 API와 동일합니다!
		- Body로 주고받기 위해 의도적으로 `Post` 매핑하였습니다! 참고해주세요!
		"""
	)
	@ApiErrorCodes(
		enumClass = RecruitmentErrorCode.class,
		includes = {
			"AREA_TYPE_NOT_FOUND",
			"COMPANY_SIZE_NOT_FOUND",
			"EMPLOYMENT_TYPE_NOT_FOUND",
			"EXPERIENCE_TYPE_NOT_FOUND",
			"INDUSTRY_TYPE_NOT_FOUND",
			"JOB_TYPE_NOT_FOUND",
			"MAJOR_TYPE_NOT_FOUND"
		}
	)
	ApiResponse<Long> getPositionCount(@RequestBody PositionRequestDTO.SearchCondition searchCondition);
}
