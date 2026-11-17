package com.tainguyen.master_node.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tainguyen.master_node.entity.FileEntity;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, String> {

	// Thuật toán Deduplication: Tìm xem trong DB đã có file nào mang mã băm SHA-256 này chưa
	FileEntity findByFileHash(String fileHash);
	
	// Tìm các file CÓ nằm trong một thư mục cụ thể
    List<FileEntity> findByFolderId(String folderId);
    
    // Tìm các file KHÔNG nằm trong thư mục nào (tức là ở Thư mục gốc)
    List<FileEntity> findByFolderIsNull();
    
    
 // Tính tổng fileSize của các file đã hoàn thành trên một Storage Node cụ thể
    @Query("SELECT COALESCE(SUM(f.size), 0) FROM FileEntity f WHERE f.storageNodeId = :nodeId AND f.status = 'COMPLETED'")
    Long calculateActualCapacityByNode(@Param("nodeId") Long nodeId);
}
