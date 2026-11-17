package com.tainguyen.master_node.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tainguyen.master_node.entity.FileChunk;

import jakarta.transaction.Transactional;

@Repository
public interface FileChunkRepository extends JpaRepository<FileChunk, Long> {

	// Tìm toàn bộ các mảnh (chunk) thuộc về một file cụ thể để báo cho React biết cần tải tiếp từ đâu
    List<FileChunk> findByFileEntityId(String fileId);
    
    // Lệnh dọn dẹp: Quét sạch toàn bộ chunk của một file sau khi Storage Node báo đã nối file thành công
    @Transactional
    void deleteByFileEntityId(String fileId);
}
