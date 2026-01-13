package navik.domain.users.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import navik.global.apiPayload.code.status.BaseCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum JobErrorCode implements BaseCode {

    JOB_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "JOB_404",
            "존재하지 않는 직무입니다."
    );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}