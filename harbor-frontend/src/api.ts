import axios from "axios";

//1. Cấu hình đường dẫn gốc tới Master Node
const apiClient = axios.create({
  // Gọi qua API Gateway (Nginx, cổng 8080) thay vì gọi thẳng Master Node (8000)
  baseURL: "http://localhost:8080/api/master",
  headers: {
    "Content-Type": "application/json",
  },
});

// 2. Định nghĩa các khuôn mẫu dữ liệu (Khớp 100% với DTO bên Spring Boot)
export interface FolderDTO {
  id: string;
  folderName: string;
  parentId?: string;
}

export interface FileDTO {
  id: string;
  originalName: string;
  fileSize: number;
  status: string;
}

export interface StorageNodeDTO {
  id: number;
  ipAddress: string;
  port: number;
  status: string;
  usedCapacity: number;
}

// 3. Khai báo các hàm gọi API
export const api = {
  // Gọi GET /api/master/folders/list
  getFolders: () => apiClient.get<FolderDTO[]>("/folders/list"),

  // Gọi GET /api/master/files (có thể truyền thêm folderId nếu đang ở trong thư mục con)
  getFiles: (folderId?: string) =>
    apiClient.get<FileDTO[]>("/files", { params: { folderId } }),

  // Gọi GET /api/master/nodes
  getNodes: () => apiClient.get<StorageNodeDTO[]>("/nodes"),

  // 4 API  phục vụ Upload:
  // 1. Cập nhật kiểu trả về của initUpload có thêm storageNodeId
  initUpload: (data: {
    originalName: string;
    fileSize: number;
    fileHash: string;
    folderId: string | null;
  }) =>
    apiClient.post<{
      fileId: string;
      nodeIp: string;
      nodePort: number;
      status: string;
      storageNodeId: number;
    }>("/init-upload", data),

  // 2. Cập nhật uploadChunk để nhận và gửi storageNodeId
  uploadChunk: (
    nodeIp: string,
    nodePort: number,
    fileId: string,
    chunkIndex: number,
    storageNodeId: number, // Thêm tham số này
    chunk: Blob,
  ) => {
    const formData = new FormData();
    formData.append("fileId", fileId);
    formData.append("chunkIndex", chunkIndex.toString());
    formData.append("storageNodeId", storageNodeId.toString()); // Ép kiểu chuỗi để gửi qua FormData
    formData.append("chunk", chunk);

    return axios.post(
      `http://${nodeIp}:${nodePort}/api/storage/upload-chunk`,
      formData,
      { headers: { "Content-Type": "multipart/form-data" } },
    );
  },

  mergeChunks: (
    nodeIp: string,
    nodePort: number,
    fileId: string,
    originalName: string,
    totalChunks: number,
  ) => {
    // Gửi dữ liệu dưới dạng URL Parameters thay vì FormData
    return axios.post(
      `http://${nodeIp}:${nodePort}/api/storage/merge-chunks`,
      null, // Không có body
      {
        params: {
          fileId: fileId,
          originalName: originalName,
          totalChunks: totalChunks,
        },
      },
    );
  },

  completeUpload: (fileId: string) =>
    apiClient.post("/complete-upload", null, { params: { fileId } }),

  // API Xóa file
  deleteFile: (fileId: string) => apiClient.delete(`/files/${fileId}`),

  // API xin Master Node đường link tải về
  getDownloadUrl: (fileId: string) =>
    apiClient.get<{ url: string }>(`/files/${fileId}/download-url`),

  //  Gọi API tạo thư mục
  createFolder: (folderName: string, parentId: string | null = null) =>
    apiClient.post("/folders/create", { folderName, parentId }),

  // BỔ SUNG: Gọi API xóa thư mục
  deleteFolder: (folderId: string) => apiClient.delete(`/folders/${folderId}`),
};
