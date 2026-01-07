package navik.domain.users.entity;


public enum UserStatus {
	/**
	 * 준회원 상태 : 소셜 로그인을 통해 가입했지만 닉네임 등의 정보를 입력하지 않은 상태
	 */
	PENDING,
	/**
	 * 정회원 상태 : 모든 필수 정보 입력 및 약관 동의가 완료된 상태
	 */
	ACTIVE
}
