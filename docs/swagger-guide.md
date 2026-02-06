# Swagger API 명세 가이드라인

## 1. 파일 구조

- 각 컨트롤러별 `*ControllerDocs.java` 인터페이스를 생성
- 컨트롤러는 해당 Docs 인터페이스를 `implements`
- 어노테이션은 Docs 인터페이스에만 작성, 컨트롤러에는 중복 작성하지 않음

## 2. 응답 포맷

모든 API 응답은 `ApiResponse.Body<T>` 구조를 따름:

```
isSuccess  : Boolean
code       : String
message    : String
result     : T (응답 데이터)
timestamp  : yyyy-MM-dd'T'HH:mm:ss
```

## 3. 에러 코드 네이밍 규칙

```
{도메인}_{HTTP상태코드}_{순번}
```

| 도메인 | 예시 |
|---|---|
| AUTH | `AUTH_401_01`, `AUTH_403_02` |
| COMMON | `COMMON_400_01`, `COMMON_404` |
| USER | `USER_404`, `USER_409_01` |
| BOARD | `BOARD_404`, `BOARD_401_01` |
| FILE | `FILE_400_01`, `FILE_500` |

- 동일 HTTP 상태에 에러가 1개뿐이면 순번 생략 가능 (`USER_404`)
- 동일 HTTP 상태에 에러가 2개 이상이면 순번 필수 (`AUTH_401_01`, `AUTH_401_02`)

## 4. 어노테이션 작성 순서

```
@Operation(summary, description)
@SecurityRequirement(name = "bearerAuth")   // 인증 필요 시
@ApiResponses({
    @ApiResponse(200, 성공)
    @ApiResponse(400, 입력값 오류)          // 해당 시
    @ApiResponse(401, 인증 실패)            // 인증 필요 API
    @ApiResponse(403, 권한/온보딩)          // 해당 시
    @ApiResponse(404, 리소스 없음)          // 해당 시
    @ApiResponse(409, 충돌)                // 해당 시
})
```

## 5. 에러 응답 포함 기준

### 5.1 공통 (인증 필요 API는 항상 포함)

| 상태 | 코드 | 포함 조건 |
|---|---|---|
| 401 | `AUTH_401_01` | `@SecurityRequirement` 있는 모든 API |
| 403 | `AUTH_403_02` | ACTIVE 전용 API (온보딩 허용 경로 제외) |

### 5.2 서비스별 (해당 서비스 코드 확인 후 포함)

| 상태 | 포함 조건 | 확인 방법 |
|---|---|---|
| 400 | `@Valid` 검증이 있는 요청 | RequestDTO에 `@NotBlank`, `@NotNull` 등 |
| 404 | 엔티티 조회 실패 가능성 | 서비스에서 `orElseThrow(NOT_FOUND)` 사용 |
| 409 | unique 제약조건 위반 가능성 | 엔티티에 `@Column(unique=true)` 확인 |

### 5.3 온보딩 허용 경로 (403 AUTH_403_02 미포함)

- `/v1/users/me/basic-info`
- `/v1/users/check-nickname`
- `/v1/jobs/**`
- `/v1/terms/**`
- `/v1/departments/**`

## 6. ExampleObject 작성 규칙

### 성공 응답
- `name`: "{API명} 성공 예시"
- result 내 필드는 ResponseDTO record 필드와 일치해야 함
- 여러 케이스가 있으면 `examples = { @ExampleObject(...), @ExampleObject(...) }` 배열로 작성

### 에러 응답
- `name`: 에러 상황 설명 (예: "존재하지 않는 사용자")
- `summary`: 발생 조건 설명
- `result`은 항상 `null`
- 401은 단일 예시 `AUTH_401_01`로 통일

## 7. Parameter 작성 규칙

| 경우 | 처리 |
|---|---|
| `@AuthUser Long userId` | `@Parameter(hidden = true)` |
| `@PathVariable` | `@Parameter(description, example, required = true)` |
| `@RequestParam` | `@Parameter(description, example, required)` |
| `@RequestBody` | 별도 Parameter 불필요 (DTO 스키마 자동 생성) |

## 8. 작업 순서 체크리스트

1. 대상 컨트롤러의 모든 엔드포인트 목록 정리
2. 각 엔드포인트의 서비스 코드 확인 → 발생 가능한 예외 파악
3. ResponseDTO record 필드 확인 → 성공 예시 JSON 작성
4. 에러 응답 결정 (5번 섹션 기준)
5. `*ControllerDocs.java` 인터페이스에 어노테이션 작성
6. 컨트롤러가 Docs 인터페이스를 implements 하는지 확인
