package com.tainguyen.master_node.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tainguyen.master_node.entity.FolderEntity;

public interface FolderRepository extends JpaRepository<FolderEntity, String> {

}
