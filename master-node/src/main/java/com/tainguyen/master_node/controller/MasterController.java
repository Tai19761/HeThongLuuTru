package com.tainguyen.master_node.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tainguyen.master_node.dto.FileResponseDTO;
import com.tainguyen.master_node.dto.InitUploadRequest;
import com.tainguyen.master_node.dto.InitUploadResponse;
import com.tainguyen.master_node.entity.FileChunk;
import com.tainguyen.master_node.entity.FileEntity;
import com.tainguyen.master_node.entity.StorageNode;
import com.tainguyen.master_node.repository.FileChunkRepository;
import com.tainguyen.master_node.repository.FileRepository;
import com.tainguyen.master_node.repository.StorageNodeRepository;
import com.tainguyen.master_node.service.MasterFileService;

@RestController
@RequestMapping("/api/master")
@CrossOrigin("*") // Cho phép Frontend (React) ở cổng khác gọi API mà không bị chặn CORS
public class MasterController {
	@Autowired
	private MasterFileService fileService;

	@Autowired
	private FileRepository fileRepo;

	@Autowired
	private FileChunkRepository chunkRepo;
	
	@Autowired
	private StorageNodeRepository storageRepo;

	@PostMapping("/init-upload")
	public ResponseEntity<?> initUpload(@RequestBody InitUploadRequest request) {
		try {
			InitUploadResponse response = fileService.initUpload(request);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@GetMapping("/confirm-chunk")
	public ResponseEntity<?> confirmChunk(@RequestParam("fileId") String fileID,
			@RequestParam("chunkIndex") Integer chunkIndex, @RequestParam("storageNodeId") Long stogareNodeId) {
		try {
			FileEntity file = fileRepo.findById(fileID).orElseThrow(() -> new RuntimeException("File không tồn tại"));
			FileChunk chunk = new FileChunk();
			chunk.setFileEntity(file);
			chunk.setChunkIndex(chunkIndex);
			chunk.setStorageNodeId(stogareNodeId);
			chunk.setStatus("SAVED");

			chunkRepo.save(chunk);
			return ResponseEntity.ok("Master đã ghi nhận mảnh số " + chunkIndex);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}

	}

	@PostMapping("/complete-upload")
	public ResponseEntity<?> complete(@RequestParam("fileId") String fileId) {
		try {
			// 1. Tìm hồ sơ file trong cơ sở dữ liệu dựa vào fileId
			FileEntity uploadedFile = fileRepo.findById(fileId)
					.orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ file này trong hệ thống!"));
			
			uploadedFile.setStatus("COMPLETED");
			
			fileRepo.save(uploadedFile);
			
			return ResponseEntity.ok("Cập nhật trạng thái thành công. File đã sàng để tải về!");
		} catch (Exception e) {
			// TODO: handle exception
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
	
	// Lấy danh sách toàn bộ các File (hoặc lấy theo folderId nếu cần)
		@GetMapping("/files")
		public ResponseEntity<?> getAllFiles(@RequestParam(value = "folderId", required = false) String folderId){
			try {
	            List<FileEntity> entities;
	            
	            // XỬ LÝ LỌC: Kiểm tra xem người dùng muốn lấy file ở đâu
	            if (folderId != null && !folderId.trim().isEmpty()) {
	                // Nếu có truyền ID thư mục -> Lấy file trong thư mục đó
	                entities = fileRepo.findByFolderId(folderId);
	            } else {
	                // Nếu KHÔNG truyền ID -> Lấy file ở thư mục gốc (folder = null)
	                entities = fileRepo.findByFolderIsNull();
	            }
	            
	            List<FileResponseDTO> dtos = entities.stream().map(file -> {
	                FileResponseDTO dto = new FileResponseDTO();
	                dto.setId(file.getId());
	                dto.setOriginalName(file.getOriginalName());
	                dto.setFileSize(file.getSize());
	                dto.setStatus(file.getStatus());
	                return dto;
	            }).toList();
	            
	            return ResponseEntity.ok(dtos);
	            
	        } catch (Exception e) {
	            return ResponseEntity.badRequest().body("Lỗi lấy danh sách file: "+e.getMessage());
	        }
		}
	
		// Lấy danh sách các storage để hiển thị dung lượng
		@GetMapping("/nodes")
		public ResponseEntity<?> getStorageNodes() {
	        try {
	            List<StorageNode> nodes = storageRepo.findAll();
	            
	            // Duyệt qua từng kho để gán lại dung lượng thực tế trước khi gửi cho React
	            for (StorageNode node : nodes) {
	                // Tính tổng dung lượng các file thực tế đang nằm trong kho này
	                Long actualSize = fileRepo.calculateActualCapacityByNode(node.getId());
	                
	                // Gán tạm con số chính xác này vào thuộc tính để chuyển thành JSON
	                // (Thao tác này chỉ thay đổi dữ liệu trả về mạng, không lưu đè xuống DB)
	                node.setUsedCapacity(actualSize);
	            }
	            
	            return ResponseEntity.ok(nodes);
	        } catch (Exception e) {
	            return ResponseEntity.badRequest().body("Lỗi lấy danh sách kho: "+e.getMessage());
	        }
	    }
	
	@DeleteMapping("/files/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable("fileId") String fileId) {
        try {
            fileService.deleteFile(fileId);
            return ResponseEntity.ok().body("{\"message\": \"Xóa file thành công\"}"); // Trả về JSON cho React dễ đọc
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
	
	@GetMapping("/files/{fileId}/download-url")
    public ResponseEntity<?> getDownloadUrl(@PathVariable("fileId") String fileId) {
        try {
            String url = fileService.getDownloadUrl(fileId);
            return ResponseEntity.ok("{\"url\": \"" + url + "\"}"); 
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
