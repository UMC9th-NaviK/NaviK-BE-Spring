package navik.domain.study.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StudyRole {

    STUDYLEADER("스터디장"),
    STUDYMEMBER("스터디원");

    private final String label;
}
