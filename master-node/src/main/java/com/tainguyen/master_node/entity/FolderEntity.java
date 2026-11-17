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
@Table(name = "folders")
public class FolderEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id; // Mã UUID

    @Column(name = "folder_name", length = 255)
    private String folderName;

    // 1. Thư mục cha (để tạo cấu trúc cây thư mục)
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private FolderEntity parentFolder;

    // 2. BỔ SUNG: Xử lý xóa thư mục thì xóa luôn các THƯ MỤC CON bên trong
    @OneToMany(mappedBy = "parentFolder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FolderEntity> subFolders;

    // 3. BỔ SUNG: Xử lý xóa thư mục thì xóa luôn các FILE bên trong
    // 'mappedBy = "folder"' vì trong FileEntity bạn đặt tên biến là 'folder'
    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileEntity> files;

    // --- Getter và Setter ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public FolderEntity getParentFolder() {
        return parentFolder;
    }

    public void setParentFolder(FolderEntity parentFolder) {
        this.parentFolder = parentFolder;
    }

    public List<FolderEntity> getSubFolders() {
        return subFolders;
    }

    public void setSubFolders(List<FolderEntity> subFolders) {
        this.subFolders = subFolders;
    }

    public List<FileEntity> getFiles() {
        return files;
    }

    public void setFiles(List<FileEntity> files) {
        this.files = files;
    }
}