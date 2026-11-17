package com.tainguyen.master_node.dto;

public class InitUploadResponse {

	// IP/Port và ID của kho
    private String fileId;
    private String nodeIp;
    private Integer nodePort;
    private String status;
    private Long storageNodeId;

    public String getFileId() {
        return fileId;
    }
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
    
    public String getNodeIp() {
        return nodeIp;
    }
    public void setNodeIp(String nodeIp) {
        this.nodeIp = nodeIp;
    }
    
    public Integer getNodePort() {
        return nodePort;
    }
    public void setNodePort(Integer nodePort) {
        this.nodePort = nodePort;
    }
    
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    
    

    public Long getStorageNodeId() {
        return storageNodeId;
    }
    public void setStorageNodeId(Long storageNodeId) {
        this.storageNodeId = storageNodeId;
    }
}