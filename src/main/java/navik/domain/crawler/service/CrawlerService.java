package navik.domain.crawler.service;

import java.util.List;
import java.util.Optional;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.crawler.constant.CrawlerConstant;
import navik.domain.crawler.constant.JobKoreaConstant;
import navik.domain.crawler.dto.RecruitmentPost;
import navik.domain.crawler.enums.JobCode;
import navik.domain.crawler.factory.WebDriverFactory;
import navik.domain.recruitment.dto.RecruitmentResponseDTO;
import navik.global.ai.service.LLMService;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlerService {

	private final WebDriverFactory webDriverFactory;
	private final CrawlerSearchHelper crawlerSearchHelper;
	private final CrawlerDataExtractor crawlerDataExtractor;
	private final CrawlerValidator crawlerValidator;
	private final LLMService llmService;

	/**
	 * 스케쥴링에 의해 주기적으로 실행되는 메서드입니다.
	 */
	public void scheduledCrawl() {
		// 1. 크롬 드라이버 생성
		WebDriver driver = webDriverFactory.createChromeDriver();
		WebDriverWait wait = webDriverFactory.createDriverWait(driver);

		// 2. JobCode(직무)별 크롤링
		try {
			for (JobCode jobCode : JobCode.values()) {
				log.info("=== [{}] 직무 크롤링 시작 ===", jobCode.name());
				driver.get(JobKoreaConstant.RECRUITMENT_LIST_URL);
				search(wait, jobCode);    // 직무 기반 필터 적용 및 검색
				processPages(driver, wait, CrawlerConstant.CRAWL_PAGES_PER_JOB); // 페이지 수 만큼 파싱
			}
		} catch (Exception exception) {
			log.error("스케쥴링 작업 중 오류 발생\n{}", exception.getMessage());
		} finally {
			driver.quit(); // 리소스 해제
		}
	}

	/**
	 * 필터를 적용하여 직무 별 검색을 수행합니다.
	 *
	 * @param wait
	 * @param jobCode
	 */
	private void search(WebDriverWait wait, JobCode jobCode) {
		crawlerSearchHelper.applyJobFilter(wait, jobCode);    // 필터 적용
		crawlerSearchHelper.search(wait);    // 검색
		crawlerSearchHelper.applySort(wait); // 정렬
		crawlerSearchHelper.applyQuantity(wait);    // 한 페이지 당 보여질 개수 설정
	}

	/**
	 * 검색 이후 pages만큼 페이지를 처리합니다.
	 *
	 * @param driver
	 * @param wait
	 * @param pages
	 */
	private void processPages(WebDriver driver, WebDriverWait wait, int pages) {

		// 1. base window 기억
		String baseUrl = driver.getCurrentUrl();
		String originalWindow = driver.getWindowHandle();
		String newWindow = "";

		// 2. 해당 페이지로 이동
		for (int currentPage = 1; currentPage <= pages; currentPage++) {

			// currentPage로 이동
			String newUrl = baseUrl.replace("#anchorGICnt_\\d+", "#anchorGICnt_" + currentPage);
			driver.get(newUrl);

			// 공고 목록 대기
			wait.until(ExpectedConditions.presenceOfElementLocated(
				By.cssSelector("tr[data-index='0']")
			));

			// 전체 공고 개수 확인
			List<WebElement> posts = driver.findElements(By.cssSelector("tr[data-index] strong a"));
			log.info("{}페이지에서 총 {}개의 공고 발견", currentPage, posts.size());

			// 공고 처리
			for (WebElement post : posts) {

				// 클릭 및 대기
				post.click();
				wait.until(ExpectedConditions.numberOfWindowsToBe(2));

				// 해당 공고 창 추출
				Optional<String> postWindow = driver.getWindowHandles().stream()
					.filter(handle -> !handle.equals(originalWindow))
					.findFirst();

				if (postWindow.isEmpty()) {
					log.error("새 창을 찾지 못하였습니다.");
					continue;
				}

				// 창 전환 및 추출
				try {
					driver.switchTo().window(newWindow);
					wait.until(ExpectedConditions.not(
						ExpectedConditions.urlToBe("about:blank")
					));
					extractData(wait);
				} catch (Exception exception) {
					log.error("상세 페이지 처리 중 오류 발생\n{}", exception.getMessage());
				} finally {
					driver.close();
					driver.switchTo().window(originalWindow);
				}
			}
		}
	}

	private boolean extractData(WebDriverWait wait) {

		// 1. 채용 공고 상세 페이지 url 유효성 검사
		String link = crawlerDataExtractor.extractCurrentUrl(wait);
		if (!crawlerValidator.isValidDetailUrl(link)) {
			log.info("유효하지 않은 채용 공고 링크: {}", link);
			return false;
		}

		// 2. 제목 유효성 검사
		String title = crawlerDataExtractor.extractTitle(wait);
		if (crawlerValidator.isSkipTitle(title)) {
			log.info("유효하지 않은 채용 공고 제목: {}", title);
			return false;
		}

		// 3. 나머지 데이터 추출
		String postId = crawlerDataExtractor.extractPostId(wait);
		String companyName = crawlerDataExtractor.extractCompanyName(wait);
		String companyLogo = crawlerDataExtractor.extractCompanyLogo(wait);
		String companyInfo = crawlerDataExtractor.extractCompanyInfo(wait);
		String qualification = crawlerDataExtractor.extractQualification(wait);
		String timeInfo = crawlerDataExtractor.extractTimeInfo(wait);
		String outline = crawlerDataExtractor.extractOutline(wait);
		String recruitmentDetail = crawlerDataExtractor.extractRecruitmentDetail(wait);

		// 4. LLM 전달용 DTO 작성
		RecruitmentPost recruitmentPost = RecruitmentPost.builder()
			.link(link)
			.title(title)
			.postId(postId)
			.companyName(companyName)
			.companyLogo(companyLogo)
			.companyInfo(companyInfo)
			.qualification(qualification)
			.timeInfo(timeInfo)
			.outline(outline)
			.recruitmentDetail(recruitmentDetail)
			.build();

		// 5. LLM 호출
		String html = recruitmentPost.toHtmlString();
		RecruitmentResponseDTO.LLMResponse result = llmService.getCrawledDataDTO(html);

		System.out.println(result);

		return true;
	}
}
