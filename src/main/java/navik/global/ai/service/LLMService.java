package navik.global.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import navik.global.ai.dto.LLMResponseDTO;

@Service
@RequiredArgsConstructor
public class LLMService {

	private final ChatClient.Builder chatClientBuilder;

	public LLMResponseDTO.Recruitment getRecruitment(String text) {

		ChatClient chatClient = chatClientBuilder.build();

		SystemMessage systemMessage = SystemMessage.builder()
			.text("""
				 [역할 부여]
				 당신은 채용 공고를 분석하여 직무 별로 요구되는 핵심 역량(KPI)을 추출하는 전문가입니다.
				 직무 핵심 역량(KPI)이란 '담당 업무', '요구 사항', '우대 사항' 등에서 '능력', '역량', '경험', '이해도'와 같은 맥락을 의미합니다.
				 [옳은 예시]: 'RESTful API 개발 및 설계 능력', '비동기 메시지 큐 사용 경험', 'MSA 아키텍처에 대한 이해도'
				 [잘못된 예시]: '계획성', '성실성', '협동성', '꼼꼼함', '윤리의식'
				
				 [규칙]
				 - '프로덕트 매니저', '프로덕트 디자이너', '프론트엔드 개발자', '백엔드 개발자'로 분류 가능한 직무만 추출한다.
				 - 채용 공고 OCR 내용은 구조화 되어있지 않다. 따라서 최대한 구조화하여 직무 별 KPI를 이해한다.
				 - 모든 KPI는 서술형 문장으로 작성한다.
				 - 추상적이거나 감성적인 표현은 사용하지 않는다.
				 - 기술적이거나 측정 가능한 내용을 우선한다.
				 - 공고 마감일은 현재 시간을 고려하여 계산한다. 상시 채용이라면 null로 출력한다.
				 - 시작일과 마감일에 타임존은 포함하지 않는다.
				 - 아래 규칙을 참고하여 유효한 JSON을 반환한다. 정보 없는 경우 null로 출력한다.
				
				 [Enum 규칙]
				 - companySize: 대기업, 중견기업, 중소기업, 공기업, 외국계기업
				 - industryType: 서비스업, 금융·은행업, IT·정보통신업, 판매·유통업, 제조·생산·화학업, 교육업, 건설업, 의료·제약업, 미디어·광고업, 문화·예술·디자인업, 공공기관·협회
				 - jobType: 프로덕트 매니저, 프로덕트 디자이너, 프론트엔드 개발자, 백엔드 개발자
				 - employmentType: 정규직, 계약직, 인턴, 프리랜서
				 - experienceType: 신입, 경력
				 - educationType: 고등학교 졸업, 전문대 졸업, 4년제 대학 졸업, 석사 졸업, 박사 졸업
				 - areaType: 서울, 부산, 대구, 인천, 광주, 대전, 울산, 세종, 경기, 강원, 충북, 충남, 전북, 전남, 경북, 경남, 제주, 해외
				""")
			.build();

		UserMessage userMessage = UserMessage.builder()
			.text(text)
			.build();

		return chatClient.prompt()
			.messages(systemMessage, userMessage)
			.options(ChatOptions.builder()
				.temperature(0.0)
				.build()
			)
			.call()
			.entity(LLMResponseDTO.Recruitment.class);
	}
}
