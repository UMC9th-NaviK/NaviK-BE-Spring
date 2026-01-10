package navik.global.ocr.service;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.Imaging;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import navik.global.ocr.dto.ImageInfoDto;

@Component
@RequiredArgsConstructor
public class ImageHelper {

	private final int TIMEOUT_MS = 3000;

	public ImageInfoDto getImageInfo(String imageUrl) {
		try {
			URL url = new URL(imageUrl);
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(TIMEOUT_MS);
			connection.setReadTimeout(TIMEOUT_MS);

			long fileSize = connection.getContentLengthLong();
			InputStream stream = connection.getInputStream();
			ImageInfo imageInfo = Imaging.getImageInfo(stream, "");

			return ImageInfoDto.builder()
				.fileSize(fileSize)
				.width(imageInfo.getWidth())
				.height(imageInfo.getHeight())
				.format(imageInfo.getFormatName())
				.build();
		} catch (Exception e) {
			return null;
		}
	}
}
