package navik.domain.evaluation.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Tag {

    COLLABORATION("협업&커뮤니케이션"),
    RESPONSIBILITY("책임감&실행력"),
    PROBLEMSOLVING("문제해결&주도성"),
    LEARNATTITUDE("학습태도&성장성"),
    CONTRIBUTION("기여도&전문성"),
    LEADERSHIP("리더쉽&조직화");

    private final String label;
}
