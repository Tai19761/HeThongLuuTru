package com.tainguyen.master_node.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.tainguyen.master_node.dto.InitUploadRequest;
import com.tainguyen.master_node.dto.InitUploadResponse;
import com.tainguyen.master_node.entity.FileEntity;
import com.tainguyen.master_node.entity.FolderEntity;
import com.tainguyen.master_node.entity.StorageNode;
import com.tainguyen.master_node.repository.FileRepository;
import com.tainguyen.master_node.repository.FolderRepository;
import com.tainguyen.master_node.repository.StorageNodeRepository;

@Service
public class MasterFileService {

	@Autowired
	private FileRepository fileRepo;

	@Autowired
	private StorageNodeRepository nodeRepo;

	@Autowired
	private FolderRepository folderRepo;

	@Autowired
	private Environment env;

	public InitUploadResponse initUpload(InitUploadRequest request) {
		InitUploadResponse response = new InitUploadResponse();

		// 1. Kiểm tra trùng lặp
		FileEntity existingFile = fileRepo.findByFileHash(request.getFileHash());
		if (existingFile != null) {
			if ("COMPLETED".equals(existingFile.getStatus())) {
				response.setFileId(existingFile.getId());
				// Trả về trạng thái "EXISTS" để React thông báo file trùng, khỏi tải nữa
				response.setStatus("EXISTS");
				return response;
			}
			// File cũ bị tải dở (lỗi giữa chừng) -> xóa bản ghi hỏng để tải lại từ đầu
			fileRepo.delete(existingFile);
			fileRepo.flush();
		}

		// 2. Tìm kho trống nhất (LOAD BALANCING) bằng cách tính dung lượng thực tế
		List<StorageNode> activeNodes = nodeRepo.findAll().stream().filter(n -> "ACTIVE".equals(n.getStatus()))
				.toList();

		if (activeNodes.isEmpty()) {
			throw new RuntimeException("Hệ thống lỗi: Không có kho lưu trữ nào đang hoạt động!");
		}

		// Dùng Stream API để tìm Kho có TỔNG fileSize nhỏ nhất
		StorageNode bestNode = activeNodes.stream().min((node1, node2) -> {
			Long size1 = fileRepo.calculateActualCapacityByNode(node1.getId());
			Long size2 = fileRepo.calculateActualCapacityByNode(node2.getId());
			return size1.compareTo(size2);
		}).orElseThrow(() -> new RuntimeException("Không tìm thấy kho phù hợp!"));

		// 3. Tạo hồ sơ trong database cho file đang lưu
		FileEntity newFile = new FileEntity();
		newFile.setId(UUID.randomUUID().toString()); // Tạo mã ID cho File
		newFile.setOriginalName(request.getOriginalName());
		newFile.setSize(request.getFileSize());
		newFile.setFileHash(request.getFileHash());
		newFile.setStatus("UPLOADING");
		newFile.setStorageNodeId(bestNode.getId());

		if (request.getFolderId() != null && !request.getFolderId().isEmpty()) {
			FolderEntity parentFolder = folderRepo.findById(request.getFolderId())
					.orElseThrow(() -> new RuntimeException("Không tìm thấy thư mục!"));
			newFile.setFolder(parentFolder);
		}
		fileRepo.save(newFile);

		// 4. Trả kết quả (Cấu hình IP và Port của Storage Node) cho React
		response.setFileId(newFile.getId());
		response.setNodeIp(bestNode.getIpAddress());
		response.setNodePort(bestNode.getPort());
		response.setStatus(newFile.getStatus()); // UPLOADING
		response.setStorageNodeId(bestNode.getId()); // Gửi ID của kho cho React
		return response;
	}

	public void deleteFile(String fileId) {
		// 1. Tìm file trong DB
		FileEntity file = fileRepo.findById(fileId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy file để xóa!"));

		// 2. Lấy thông tin kho đang giữ file này
		StorageNode node = nodeRepo.findById(file.getStorageNodeId())
				.orElseThrow(() -> new RuntimeException("Không tìm thấy Node lưu trữ!"));

		// 3. Gọi điện (API) xuống Storage Node yêu cầu xóa file vật lý
		// Trong Docker, "localhost" của Master là chính container Master, nên phải dùng tên service
		// (VD: biến môi trường STORAGE_HOST_8081=storage-node-1). Chạy local thì dùng IP trong DB.
		String internalHost = env.getProperty("STORAGE_HOST_" + node.getPort(), node.getIpAddress());
		String storageDeleteUrl = "http://" + internalHost + ":" + node.getPort() + "/api/storage/delete-file/"
				+ fileId;
		try {
			// Dùng RestTemplate để gửi lệnh DELETE
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.delete(storageDeleteUrl);
		} catch (Exception e) {
			System.out.println("Cảnh báo: Không thể xóa file vật lý ở Storage Node. Có thể thư mục không tồn tại.");
		}


		// 4.  Xóa thông tin file trong Database
		fileRepo.delete(file);
	}

	public String getDownloadUrl(String fileId) {
		FileEntity file = fileRepo.findById(fileId).orElseThrow(() -> new RuntimeException("Không tìm thấy file!"));

		StorageNode node = nodeRepo.findById(file.getStorageNodeId())
				.orElseThrow(() -> new RuntimeException("Không tìm thấy Node lưu trữ!"));

		// BỌT ĐÂY LÀ ĐIỂM QUAN TRỌNG NHẤT: Khớp 100% với API ở Storage Node
		// Phải mã hóa tên file: tên có dấu cách, "&", "#", tiếng Việt... sẽ làm hỏng URL
		// (VD: "A & B.docx" -> trình duyệt hiểu nhầm "&" là bắt đầu tham số mới -> lỗi 404)
		String encodedName = URLEncoder.encode(file.getOriginalName(), StandardCharsets.UTF_8);
		String downloadUrl = "http://" + node.getIpAddress() + ":" + node.getPort() + "/api/storage/download"
				+ "?fileId=" + file.getId() + "&originalName=" + encodedName;

		return downloadUrl;
	}

	// 1. Hàm đệ quy quét và xóa file vật lý
	private void deletePhysicalFilesInFolder(FolderEntity folder) {
		// Quét tất cả file trong thư mục này và xóa vật lý
		for (FileEntity file : folder.getFiles()) {
			deleteFile(file.getId());
		}

		// Đệ quy: Tiếp tục chui vào các thư mục con để xóa tiếp
		for (FolderEntity subFolder : folder.getSubFolders()) {
			deletePhysicalFilesInFolder(subFolder);
		}
	}

	// 2. Hàm chính để gọi từ Controller
	public void deleteFolder(String folderId) {
		FolderEntity folder = folderRepo.findById(folderId)
				.orElseThrow(() -> new RuntimeException("Thư mục không tồn tại"));

		// Bước 1: Quét dọn sạch sẽ file vật lý ở các kho Storage Nodes
		deletePhysicalFilesInFolder(folder);

		// Bước 2: Xóa thư mục trong Database (Hibernate Cascade sẽ tự động xóa sạch dữ
		// liệu con)
		folderRepo.delete(folder);
	}

}