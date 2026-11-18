package com.tainguyen.master_node.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.tainguyen.master_node.entity.StorageNode;
import com.tainguyen.master_node.repository.StorageNodeRepository;

/**
 * Tự động tạo danh sách Storage Node khi bảng storage_nodes đang trống
 * (ví dụ: lần đầu chạy bằng Docker với database mới).
 *
 * Địa chỉ lưu trong DB là địa chỉ mà TRÌNH DUYỆT gọi tới (mặc định localhost),
 * vì React upload/download trực tiếp tới Storage Node.
 */
@Component
public class StorageNodeSeeder implements CommandLineRunner {

	private final StorageNodeRepository nodeRepo;

	@Value("${storage.seed.host:localhost}")
	private String publicHost;

	@Value("${storage.seed.ports:8081,8082}")
	private Integer[] ports;

	// Khi chạy nhiều Master, chỉ cho 1 Master tự tạo node để tránh tạo trùng
	// (Docker: Master 2 đặt STORAGE_SEED_ENABLED=false)
	@Value("${storage.seed.enabled:true}")
	private boolean enabled;

	public StorageNodeSeeder(StorageNodeRepository nodeRepo) {
		this.nodeRepo = nodeRepo;
	}

	@Override
	public void run(String... args) {
		if (!enabled || nodeRepo.count() > 0) {
			return; // Không được phép tạo, hoặc đã có node -> không làm gì
		}
		for (Integer port : ports) {
			StorageNode node = new StorageNode();
			node.setIpAddress(publicHost);
			node.setPort(port);
			node.setStatus("ACTIVE");
			node.setUsedCapacity(0L);
			nodeRepo.save(node);
		}
		System.out.println(">>> Đã tự động tạo " + ports.length + " Storage Node (" + publicHost + ")");
	}
}
