package navik.global.init;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import navik.domain.evaluation.entity.EvaluationTag;
import navik.domain.evaluation.enums.Tag;
import navik.domain.evaluation.enums.TagType;
import navik.domain.evaluation.repository.EvaluationTagRepository;

@Component
@RequiredArgsConstructor
public class EvaluationTagInitializer implements CommandLineRunner {

	private final EvaluationTagRepository evaluationTagRepository;

	@Override
	public void run(String... args) throws Exception {
		if (evaluationTagRepository.count() > 0) {
			return;
		}

		ClassPathResource resource = new ClassPathResource("csv/evaluation_tags.csv");

		try (BufferedReader br = new BufferedReader(
			new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
			br.readLine(); // 첫 줄(헤더) 건너뛰기
			String line;
			List<EvaluationTag> tagList = new ArrayList<>();

			while ((line = br.readLine()) != null) {
				String[] data = line.split(",");
				EvaluationTag tag = EvaluationTag.builder()
					.tagType(TagType.valueOf(data[0]))
					.tag(Tag.valueOf(data[1]))
					.categoryName(data[2])
					.tagContent(data[3])
					.build();
				tagList.add(tag);
			}
			evaluationTagRepository.saveAll(tagList);
		}
	}
}
