package navik.domain.crawler.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.crawler.service.CrawlerService;

/**
 * TODO: 실 환경에서 직접 트리거 해보고 스웨거 문서화, 메모리 테스트해볼 생각입니다.
 *
 */
@Slf4j
@RestController
@RequestMapping("/v1/crawler")
@RequiredArgsConstructor
public class CrawlerController {

	private final CrawlerService crawlerService;

	/**
	 * TODO: 스케쥴러 동작 테스트용 api 삭제 예정
	 */
	@GetMapping("/trigger-schedule")
	public void directCrawl() {
		crawlerService.scheduledCrawl();
	}

	/**
	 * TODO: 개발자 따로, PM, 디자이너 따로
	 */
}
