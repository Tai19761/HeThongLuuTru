package com.tainguyen.master_node.dto;

public class CreateFolderRequest {

	private String folderName;
    private String parentId; // Nếu là null thì thư mục này nằm ngoài cùng
	public String getFolderName() {
		return folderName;
	}
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
    
}
