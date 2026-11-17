package com.tainguyen.master_node.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tainguyen.master_node.entity.StorageNode;

@Repository
public interface StorageNodeRepository extends JpaRepository<StorageNode, Long> {
	
	// Thuật toán Load Balancing: Tìm các kho đang ACTIVE và sắp xếp theo dung lượng từ thấp đến cao
    // Kho đầu tiên trong danh sách (Index 0) chính là kho đang trống nhất!
    List<StorageNode> findByStatusOrderByUsedCapacityAsc(String status);

}
