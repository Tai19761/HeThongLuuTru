package com.tainguyen.master_node.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "files")
public class FileEntity {

	@Id
	@Column(name = "id", length = 36)
	private String id; // Mã UUID (Mã chia sẻ)
	
	@Column(name = "original_name")
	private String originalName;
	
	@Column(name = "file_size")
	private Long size;
	
	@Column(name = "file_hash", length = 64)
	private String fileHash; // Chuỗi băm SHA-256 dùng để Khử trùng lặp (Deduplication)
	
	@Column(name = "status", length = 20)
	private String status; // Trạng thái: UPLOADING (Đang tải dở) hoặc COMPLETED (Đã tải xong

	@Column(name = "storage_node_id") 
	private Long storageNodeId;
	
	// File này nằm trong thư mục nào (Nếu = null là nằm ở ngoài cùng)
    @ManyToOne
    @JoinColumn(name = "folder_id")
    private FolderEntity folder;
    
 // mappedBy = "fileEntity" phải khớp y hệt tên biến bên class FileChunk
    @OneToMany(mappedBy = "fileEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileChunk> chunks;

    public List<FileChunk> getChunks() {
        return chunks;
    }

    public void setChunks(List<FileChunk> chunks) {
        this.chunks = chunks;
    }
    
    
	
	public FolderEntity getFolder() {
		return folder;
	}

	public void setFolder(FolderEntity folder) {
		this.folder = folder;
	}

	public Long getStorageNodeId() {
		return storageNodeId;
	}

	public void setStorageNodeId(Long storageNodeId) {
		this.storageNodeId = storageNodeId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOriginalName() {
		return originalName;
	}

	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}
	



	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public String getFileHash() {
		return fileHash;
	}

	public void setFileHash(String fileHash) {
		this.fileHash = fileHash;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status; //Trạng thái: UPLOADING (Đang tải dở) hoặc COMPLETED (Đã tải xong)
	}

	
	
	
	
}