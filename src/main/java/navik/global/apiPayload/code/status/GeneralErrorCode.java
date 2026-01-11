package navik.global.apiPayload.code.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 일반적인 에러 코드를 정의하는 열거형입니다.
 * {@link BaseCode}를 구현하여 HTTP 상태, 코드, 메시지를 제공합니다.
 */
@Getter
@AllArgsConstructor
public enum GeneralErrorCode implements BaseCode {

    // 1. Common (공통/기본)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_400_01", "입력값이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_405", "지원하지 않는 HTTP 메서드입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404", "해당 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 내부 오류가 발생했습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "COMMON_400_02", "입력값의 타입이 유효하지 않습니다."),

    // 3. User (회원)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404", "존재하지 않는 회원입니다."),
    USER_EMAIL_DUPLICATION(HttpStatus.CONFLICT, "USER_409_01", "이미 존재하는 이메일입니다."),
    USER_NICKNAME_DUPLICATION(HttpStatus.CONFLICT, "USER_409_02", "이미 존재하는 닉네임입니다."),
    USER_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "USER_401", "이메일 또는 비밀번호가 일치하지 않습니다."),

    // 4. File (파일 업로드 - S3 등)
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_500", "파일 업로드에 실패했습니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "FILE_400_01", "파일 크기가 제한을 초과했습니다."),
    FILE_EXTENSION_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "FILE_400_02", "지원하지 않는 파일 형식입니다."),

    // 5. External / DB (외부 서비스, 데이터베이스)
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "EXTERNAL_502", "외부 API 호출 중 오류가 발생했습니다."),
    DB_CONSTRAINT_VIOLATION(HttpStatus.CONFLICT, "DB_409", "데이터 무결성 제약 조건을 위반했습니다."),

    // 6. Board (게시글)
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD_404", "존재하지 않는 게시글입니다."),
    AUTH_BOARD_NOT_WRITER(HttpStatus.UNAUTHORIZED, "BOARD_401_01", "해당 게시글의 작성자가 아닙니다.");

    /**
     * HTTP 상태 코드
     */
    private final HttpStatus httpStatus;

    /**
     * 에러 코드
     */
    private final String code;

    /**
     * 에러 메시지
     */
    private final String message;
}