package com.tainguyen.master_node.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "file_chunks")
public class FileChunk {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "file_id")
    private FileEntity fileEntity; // Móc khóa ngoại sang bảng files

    @Column(name = "chunk_index")
    private Integer chunkIndex; // Số thứ tự của mảnh (0, 1, 2...)

    @Column(name = "storage_node_id")
    private Long storageNodeId; // Mảnh này đang nằm ở kho nào (lưu ID của bảng storage_nodes)

    @Column(name = "status", length = 20)
    private String status; // Trạng thái: PENDING hoặc SAVED

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public FileEntity getFileEntity() {
		return fileEntity;
	}

	public void setFileEntity(FileEntity fileEntity) {
		this.fileEntity = fileEntity;
	}

	public Integer getChunkIndex() {
		return chunkIndex;
	}

	public void setChunkIndex(Integer chunkIndex) {
		this.chunkIndex = chunkIndex;
	}

	public Long getStorageNodeId() {
		return storageNodeId;
	}

	public void setStorageNodeId(Long storageNodeId) {
		this.storageNodeId = storageNodeId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
    
    
}
