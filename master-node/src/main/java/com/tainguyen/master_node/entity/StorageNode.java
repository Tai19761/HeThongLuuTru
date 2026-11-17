package com.tainguyen.master_node.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "storage_nodes")
public class StorageNode {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "port")
    private Integer port;

    @Column(name = "status", length = 20)
    private String status; // ACTIVE hoặc DOWN

    // Dung lượng đã sử dụng
    @Column(name = "used_capacity")
    private Long usedCapacity;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getUsedCapacity() {
		return usedCapacity;
	}

	public void setUsedCapacity(Long usedCapacity) {
		this.usedCapacity = usedCapacity;
	}
    
    

}
