package com.tainguyen.storage_node_1.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/storage")
@CrossOrigin("*")
public class StorageController {
	// ĐƯờng dẫn lấy từ application.properties
	@Value("${storage.base-dir}")
	private String baseDir;

	// Địa chỉ Master Node (local: http://localhost:8000, Docker: http://master-node:8000)
	@Value("${master.url:http://localhost:8000}")
	private String masterUrl;

	private final RestTemplate restTemplate = new RestTemplate();

	@PostMapping("/upload-chunk")
	public ResponseEntity<?> uploadChunk(@RequestParam("fileId") String fileId,
			@RequestParam("chunkIndex") Integer chunkIndex, @RequestParam("storageNodeId") Long storageNodeId,
			@RequestParam("chunk") MultipartFile chunk) {
		try {
			// 1. Tạo thư mục con mang tên fileId (VD: D:/Kho_1/mã-uuid-của-file)
			Path folderPath = Paths.get(baseDir, fileId);
			if (!Files.exists(folderPath)) {
				Files.createDirectories(folderPath);
			}

			// 2. Lưu mảnh file vật lý vào thư mục đó với tên là số Index (VD: 0, 1, 2)
			Path chunkPath = folderPath.resolve(String.valueOf(chunkIndex));
			chunk.transferTo(chunkPath.toFile());

			// 3. Gọi điện báo cho Master Node biết mảnh này đã được lưu an toàn
			String masterConfirmUrl = masterUrl + "/api/master/confirm-chunk?fileId=" + fileId + "&chunkIndex="
					+ chunkIndex + "&storageNodeId=" + storageNodeId;

			ResponseEntity<String> response = restTemplate.getForEntity(masterConfirmUrl, String.class);

			if (response.getStatusCode().is2xxSuccessful()) {
				return ResponseEntity.ok("Kho 1 lưu thành công mảnh " + chunkIndex);
			} else {
				return ResponseEntity.badRequest().body("Lỗi báo cáo Master Node");
			}

		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}

	}

	@PostMapping("/merge-chunks")
	public ResponseEntity<?> mergeChunk(@RequestParam("fileId") String fileId,
			@RequestParam("originalName") String originalName, @RequestParam("totalChunks") Integer totalChunks // Tổng
																												// tất
																												// cả
																												// các
																												// mảnh
	) {
		try {
			// D:/Kho_1/idFile
			Path folderPath = Paths.get(baseDir, fileId);
			// D:/Kho_1/idFile/tenFile
			Path mergedFilePath = folderPath.resolve(originalName);

			// Xóa file cũ (Xủ lý file cũ lúc gặp sự cố)
			Files.deleteIfExists(mergedFilePath);
			// Tạo file mới
			Files.createFile(mergedFilePath);

			// 1. VÒNG LẶP GỘP (Chỉ đọc và ghép, tuyệt đối không xóa)
			for (int i = 0; i < totalChunks; i++) {
				// D:/Kho_1/idFile/0
				Path chunkPath = folderPath.resolve(String.valueOf(i));
				byte[] chunkBytes = Files.readAllBytes(chunkPath);
				Files.write(mergedFilePath, chunkBytes, StandardOpenOption.APPEND);
			}

			// 2. VÒNG LẶP DỌN RÁC
			// Chỉ chạy đến đây khi việc ghép file ở trên đã thành công 100%
			for (int i = 0; i < totalChunks; i++) {
				Path chunkPath = folderPath.resolve(String.valueOf(i));
				Files.deleteIfExists(chunkPath);
			}

			return ResponseEntity.ok("Kho đã hợp nhất thành công file " + originalName);

		} catch (Exception e) {
			// TODO: handle exception
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@GetMapping("/download")
	public ResponseEntity<Resource> download(@RequestParam("fileId") String fileId,
			@RequestParam("originalName") String originalName) {
		try {
			// Thành D:/Kho_1/idFile/tenFile
			Path filePath = Paths.get(baseDir, fileId).resolve(originalName);

			if (!Files.exists(filePath)) {
				return ResponseEntity.notFound().build();
			}

			// Chuyển đổi file vật lý thành một tài nguyên (Resource) để truyền qua mạng
			Resource resource = new UrlResource(filePath.toUri());

			// 3. Đóng gói và gửi về kèm theo tiêu đề (Header) ép trình duyệt phải tải xuống
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
					.header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
							org.springframework.http.ContentDisposition.attachment().filename(originalName, java.nio.charset.StandardCharsets.UTF_8).build().toString())
					.body(resource);

		} catch (Exception e) {
			// TODO: handle exception
			return ResponseEntity.badRequest().build();
		}

	}
	

	@DeleteMapping("/delete-file/{fileId}")
	public ResponseEntity<?> deletePhysicalFile(@PathVariable("fileId") String fileId) {
        try {
            // Tìm đến thư mục chứa các mảnh của file (VD: D:/Kho_1/mã-uuid)
            Path folderPath = Paths.get(baseDir, fileId);
            
            // Xóa toàn bộ thư mục và các mảnh bên trong
            if (Files.exists(folderPath)) {
                FileSystemUtils.deleteRecursively(folderPath);
            }
            return ResponseEntity.ok("Đã xóa sạch file vật lý tại kho lưu trữ.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi xóa file vật lý: " + e.getMessage());
        }
    }
	

}














































/*
 * transferTo(File dest): Đây là hàm của đối tượng MultipartFile. Nhiệm vụ của
 * nó là bốc dữ liệu của mảnh file (đang nằm trên RAM hoặc thư mục tạm của
 * Spring Boot) và ghi đè/lưu vật lý xuống ổ đĩa cứng tại đúng cái đích mà bạn
 * chỉ định.
 * 
 * toFile(): Đây là hàm của đối tượng Path. Hàm transferTo ở trên đời cũ nên bắt
 * buộc phải nhận vào một đối tượng kiểu java.io.File. Trong khi đó, chunkPath
 * của bạn lại là kiểu Path (đời mới). Hàm toFile() đóng vai trò chuyển đổi từ
 * Path sang File để nhét vừa vào hàm transferTo.
 * 
 * getStatusCode(): Lấy mã trạng thái HTTP (HTTP Status Code) mà Master Node trả
 * về sau cuộc gọi. Ví dụ: trả về 200 (thành công), 400 (lỗi dữ liệu), 500 (sập
 * server).
 * 
 * is2xxSuccessful(): Kiểm tra xem cái mã trạng thái lấy được ở trên có thuộc
 * nhóm đầu 2 (từ 200 đến 299) hay không. Nếu có, tức là Master Node đã nhận và
 * xử lý thành công, không bị lỗi.
 */