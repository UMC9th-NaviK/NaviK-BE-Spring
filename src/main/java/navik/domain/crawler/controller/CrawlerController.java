package navik.domain.crawler.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.crawler.service.CrawlerService;
import navik.global.ocr.service.NaverOcrService;

/**
 * TODO: 실 환경 메모리 테스트 후 서버 분리 및 스웨거 설명 추가 예정
 */
@Slf4j
@RestController
@RequestMapping("/v1/crawler")
@RequiredArgsConstructor
public class CrawlerController {

	private final CrawlerService crawlerService;
	private final NaverOcrService naverOcrService;

	/**
	 * TODO: 스케쥴러 동작 테스트용 api 삭제 예정
	 */
	@GetMapping("/trigger-schedule")
	public void directCrawl() {
		crawlerService.scheduledCrawl();
	}
}
