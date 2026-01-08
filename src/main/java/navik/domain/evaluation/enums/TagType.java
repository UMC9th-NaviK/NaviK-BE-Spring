package navik.domain.evaluation.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TagType {

    HIGH_TAGGING("강점태깅"),
    LOW_TAGGING("약점태깅");

    private final String label;
}
