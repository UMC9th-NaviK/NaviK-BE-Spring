package navik.global.s3.enums;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum S3PathType {

	PORTFOLIO_PDF("pdf/raw") {
		@Override
		public String generateKey(Long id, String extension) {
			// pdf/raw/{userId}/{date}_{uuid}.pdf
			String fileName = LocalDate.now() + "_" + UUID.randomUUID().toString().substring(0, 8);
			return getPrefix() + "/" + id + "/" + fileName + extension;
		}
	},

	BOARD_IMAGE("board/images") {
		@Override
		public String generateKey(Long id, String extension) {
			// board/images/{boardId}/{uuid}.png
			String fileName = UUID.randomUUID().toString().substring(0, 12);
			return getPrefix() + "/" + id + "/" + fileName + extension;
		}
	},

	USER_PROFILE("user/profile") {
		@Override
		public String generateKey(Long id, String extension) {
			// user/profile/{userId}/{uuid}.jpg
			String fileName = UUID.randomUUID().toString().substring(0, 8);
			return getPrefix() + "/" + id + "/" + fileName + extension;
		}
	};

	private final String prefix;

	public abstract String generateKey(Long id, String extension);
}
