package com.tainguyen.master_node.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tainguyen.master_node.dto.CreateFolderRequest;
import com.tainguyen.master_node.dto.FolderResponseDTO;
import com.tainguyen.master_node.entity.FolderEntity;
import com.tainguyen.master_node.repository.FolderRepository;

@RestController
@RequestMapping("/api/master/folders")
@CrossOrigin("*")
public class FolderController {

	@Autowired
	private FolderRepository folderRepo;

	@PostMapping("/create")
	public ResponseEntity<?> createFolder(@RequestBody CreateFolderRequest request) {
		try {
			FolderEntity newFolder = new FolderEntity();
			newFolder.setId(UUID.randomUUID().toString());
			newFolder.setFolderName(request.getFolderName());

			// Xử lý gắn thư mục cha nếu có truyền parentId
			if (request.getParentId() != null && !request.getParentId().isEmpty()) {
				// Dòng 2 & 3: Cầm cái ID đó chui vào Database (folderRepo) để tìm.
				// Dùng hàm .findById(). Hàm .orElseThrow() có nghĩa là:
				// Nếu TÌM THẤY -> Nhét dữ liệu vào biến parentFolder.
				// Nếu KHÔNG TÌM THẤY (người dùng truyền ID bậy bạ) -> Ném ra lỗi ngay lập tức,
				// chặn đứng chương trình.
				FolderEntity parentFolder = folderRepo.findById(request.getParentId())
						.orElseThrow(() -> new RuntimeException("Không tìm thấy thư mục cha!"));

				// Dòng 4: Lấy cái thư mục mới đang chuẩn bị lưu, gắn nó vào làm "con" của cái
				// parentFolder vừa tìm được ở trên.
				newFolder.setParentFolder(parentFolder);
			}

			folderRepo.save(newFolder);
			return ResponseEntity.ok("Tạo thư mục thành công: " + newFolder.getId());

		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@GetMapping("/list")
	public ResponseEntity<?> getAllFolders() {
		try {
			List<FolderEntity> entities = folderRepo.findAll();

			List<FolderResponseDTO> dtos = entities.stream().map(folder -> {
				FolderResponseDTO dto = new FolderResponseDTO();
				dto.setId(folder.getId());
				dto.setFolderName(folder.getFolderName());
				if (folder.getParentFolder() != null) {
					dto.setParentId(folder.getParentFolder().getId());
				}
				return dto;
			}).toList();

			return ResponseEntity.ok(dtos);

		} catch (Exception e) {
			// TODO: handle exception
			return ResponseEntity.badRequest().body("Lỗi lấy danh sách thư mục: " + e.getMessage());
		}
	}
	
	@Autowired
    private com.tainguyen.master_node.service.MasterFileService fileService;

    // BỔ SUNG: API Xóa thư mục
    @DeleteMapping("/{folderId}")
    public ResponseEntity<?> deleteFolder(@PathVariable("folderId") String folderId) {
        try {
            fileService.deleteFolder(folderId);
            return ResponseEntity.ok().body("{\"message\": \"Xóa thư mục thành công\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
