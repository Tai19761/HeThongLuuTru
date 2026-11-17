// Nhiệm vụ của file này là toàn bộ Logic (State, gọi API, thuật toán Upload, tính toán Hash...)
import { useState, useEffect, useRef } from "react";
import CryptoJS from "crypto-js";
// Lưu ý: Đường dẫn import api.ts phải lùi ra 1 cấp (../api) vì file này nằm trong thư mục hooks
import { api } from "../api";
import type { FolderDTO, FileDTO, StorageNodeDTO } from "../api";

export function useFileManager() {
  const [folders, setFolders] = useState<FolderDTO[]>([]);
  const [files, setFiles] = useState<FileDTO[]>([]);
  const [nodes, setNodes] = useState<StorageNodeDTO[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const [currentFolder, setCurrentFolder] = useState<FolderDTO | null>(null);
  const [folderPath, setFolderPath] = useState<FolderDTO[]>([]);

  const [uploadProgress, setUploadProgress] = useState<{
    [key: string]: number;
  }>({});

  const fileInputRef = useRef<HTMLInputElement>(null);
  const folderInputRef = useRef<HTMLInputElement>(null);

  const fetchData = async (targetFolderId: string | null = null) => {
    try {
      setIsLoading(true);
      const [foldersRes, filesRes, nodesRes] = await Promise.all([
        api.getFolders(),
        api.getFiles(targetFolderId != null ? targetFolderId : undefined),
        api.getNodes(),
      ]);
      setFolders(foldersRes.data);
      setFiles(filesRes.data);
      setNodes(nodesRes.data);
    } catch (error) {
      console.error("Lỗi lấy dữ liệu:", error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData(null);
  }, []);

  const handleCreateFolder = async () => {
    const folderName = window.prompt("Nhập tên thư mục mới:");
    if (!folderName || folderName.trim() === "") return;

    try {
      await api.createFolder(
        folderName,
        currentFolder ? currentFolder.id : null,
      );
      fetchData(currentFolder ? currentFolder.id : null);
    } catch (error) {
      console.error("Lỗi tạo thư mục:", error);
      alert("Tạo thư mục thất bại!");
    }
  };

  const handleDeleteFolder = async (
    e: React.MouseEvent,
    folderId: string,
    folderName: string,
  ) => {
    e.stopPropagation();
    const isConfirm = window.confirm(
      `CẢNH BÁO: Bạn có chắc muốn xóa thư mục "${folderName}"?\nToàn bộ thư mục con và tệp tin bên trong cũng sẽ bị xóa vĩnh viễn!`,
    );
    if (!isConfirm) return;

    try {
      await api.deleteFolder(folderId);
      fetchData(currentFolder ? currentFolder.id : null);
    } catch (error) {
      console.error("Lỗi xóa thư mục:", error);
      alert("Xóa thư mục thất bại!");
    }
  };

  const formatBytes = (bytes: any) => {
    if (
      bytes === null ||
      bytes === undefined ||
      bytes === "" ||
      isNaN(Number(bytes))
    )
      return "0 Bytes";
    const numBytes = Number(bytes);
    if (numBytes <= 0) return "0 Bytes";
    const k = 1024;
    const sizes = ["Bytes", "KB", "MB", "GB", "TB"];
    const i = Math.floor(Math.log(numBytes) / Math.log(k));
    if (i >= sizes.length) return "Quá lớn";
    return parseFloat((numBytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
  };

  const calculateHash = (file: File): Promise<string> => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = (e) => {
        try {
          const arrayBuffer = e.target?.result as ArrayBuffer;
          const wordArray = CryptoJS.lib.WordArray.create(arrayBuffer as any);
          const hash = CryptoJS.SHA256(wordArray).toString(CryptoJS.enc.Hex);
          resolve(hash);
        } catch (err) {
          reject(err);
        }
      };
      reader.onerror = (err) => reject(err);
      reader.readAsArrayBuffer(file);
    });
  };

  const handleFileUpload = async (
    event: React.ChangeEvent<HTMLInputElement>,
  ) => {
    const selectedFiles = event.target.files;
    if (!selectedFiles || selectedFiles.length === 0) return;

    const filesToUpload = Array.from(selectedFiles);
    event.target.value = "";

    // CACHE DÙNG CHUNG cho toàn bộ đợt upload này (lưu key: 'parentId/folderName' -> value: 'folderId')
    const folderCache = new Map<string, string>();
    // Dùng cái này để chặn gọi API trùng lặp trong lúc đợi API tạo thư mục trả về
    const pendingFolderPromises = new Map<string, Promise<string>>();

    // Gom tên các file bị trùng / bị lỗi để thông báo 1 lần sau khi tải xong
    const duplicateFiles: string[] = [];
    const failedFiles: string[] = [];

    for (const file of filesToUpload) {
      const fileName = file.name;
      const relativePath = file.webkitRelativePath;

      let targetFolderId = currentFolder ? currentFolder.id : null;

      // XỬ LÝ TẠO CÂY THƯ MỤC NGẦM KHI UPLOAD FOLDER
      if (relativePath && relativePath.includes("/")) {
        const parts = relativePath.split("/");
        parts.pop();
        // Bỏ đi tên file ở cuối

        let currentParentId = currentFolder ? currentFolder.id : null;

        for (const folderName of parts) {
          const pathKey = `${currentParentId || "root"}/${folderName}`;

          if (folderCache.has(pathKey)) {
            currentParentId = folderCache.get(pathKey)!;
          } else {
            // Nếu có promise đang chạy cho thư mục này, đợi nó xong
            if (pendingFolderPromises.has(pathKey)) {
              try {
                currentParentId = await pendingFolderPromises.get(pathKey)!;
              } catch (e) {
                console.error("Lỗi đợi tạo thư mục:", e);
              }
            } else {
              // Chưa có ai tạo, mình là người đầu tiên tạo
              const createPromise = new Promise<string>(
                async (resolve, reject) => {
                  try {
                    const res = await api.createFolder(
                      folderName,
                      currentParentId,
                    );
                    // Tùy theo BE trả về ID kiểu gì. Tạm giả định "Thêm thư mục thành công: ID"
                    const newFolderId = res.data.split(": ")[1].trim();
                    folderCache.set(pathKey, newFolderId);
                    resolve(newFolderId);
                  } catch (err: any) {
                    console.error(
                      "Lỗi API tạo thư mục:",
                      err.response?.data || err.message,
                    );
                    // Nếu lỡ lỗi (có thể do đã tồn tại ở DB trước đó), cố gắng tải lại cây thư mục để lấy ID
                    // (Ở đây tạm thời reject để bạn dễ debug nếu vẫn lỗi)
                    reject(err);
                  }
                },
              );

              pendingFolderPromises.set(pathKey, createPromise);

              try {
                currentParentId = await createPromise;
              } catch (e) {
                console.error("Thất bại việc tạo thư mục nhánh:", pathKey);
              }
            }
          }
        }
        targetFolderId = currentParentId;
      }

      setUploadProgress((prev) => ({ ...prev, [fileName]: 0 }));

      // ... (Phần code dưới băm file và upload giữ nguyên không thay đổi) ...
      try {
        console.log(`Đang băm file ${fileName}...`);
        const fileHash = await calculateHash(file);
        const initRes = await api.initUpload({
          originalName: fileName,
          fileSize: file.size,
          fileHash: fileHash,
          folderId: targetFolderId,
        });

        const { fileId, nodeIp, nodePort, status, storageNodeId } =
          initRes.data;

        if (status === "EXISTS" || status === "COMPLETED") {
          duplicateFiles.push(fileName);
          setUploadProgress((prev) => {
            const newProgress = { ...prev };
            delete newProgress[fileName];
            return newProgress;
          });
          continue;
        }

        const CHUNK_SIZE = 5 * 1024 * 1024;
        const totalChunks = Math.ceil(file.size / CHUNK_SIZE);

        for (let i = 0; i < totalChunks; i++) {
          const start = i * CHUNK_SIZE;
          const end = Math.min(start + CHUNK_SIZE, file.size);
          const chunk = file.slice(start, end);

          await api.uploadChunk(
            nodeIp,
            nodePort,
            fileId,
            i,
            storageNodeId,
            chunk,
          );
          const percent = Math.round(((i + 1) / totalChunks) * 100);
          setUploadProgress((prev) => ({ ...prev, [fileName]: percent }));
        }

        await api.mergeChunks(nodeIp, nodePort, fileId, fileName, totalChunks);
        await api.completeUpload(fileId);
      } catch (error) {
        console.error("Lỗi tải file:", fileName, error);
        failedFiles.push(fileName);
      } finally {
        setUploadProgress((prev) => {
          const newProgress = { ...prev };
          delete newProgress[fileName];
          return newProgress;
        });
      }
    }
    await fetchData(currentFolder ? currentFolder.id : null);

    // Thông báo kết quả cho người dùng
    const messages: string[] = [];
    if (duplicateFiles.length > 0) {
      messages.push(
        `Các file sau đã tồn tại trong hệ thống (nội dung giống hệt), không tải lại:\n- ${duplicateFiles.join("\n- ")}`,
      );
    }
    if (failedFiles.length > 0) {
      messages.push(
        `Các file sau tải lên thất bại:\n- ${failedFiles.join("\n- ")}`,
      );
    }
    if (messages.length > 0) {
      // alert() chặn trình duyệt vẽ lại giao diện -> dòng "đang tải 0%" bị đứng hình.
      // Đợi React vẽ xong (xóa dòng tạm) rồi mới hiện thông báo.
      await new Promise((resolve) =>
        requestAnimationFrame(() => setTimeout(resolve, 0)),
      );
      alert(messages.join("\n\n"));
    }
  };

  const handleOpenFolder = (folder: FolderDTO) => {
    setCurrentFolder(folder);
    setFolderPath([...folderPath, folder]);
    fetchData(folder.id);
  };

  const handleNavigate = (index: number) => {
    if (index === -1) {
      setCurrentFolder(null);
      setFolderPath([]);
      fetchData(null);
    } else {
      const newPath = folderPath.slice(0, index + 1);
      const targetFolder = newPath[newPath.length - 1];
      setCurrentFolder(targetFolder);
      setFolderPath(newPath);
      fetchData(targetFolder.id);
    }
  };

  const handleDrop = async (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    const droppedFile = e.dataTransfer.files[0];
    if (!droppedFile) return;

    const syntheticEvent = {
      target: { files: [droppedFile], value: "" },
    } as unknown as React.ChangeEvent<HTMLInputElement>;

    handleFileUpload(syntheticEvent);
  };

  const handleDownloadFile = async (fileId: string) => {
    try {
      const res = await api.getDownloadUrl(fileId);
      const downloadUrl = res.data.url;
      const link = document.createElement("a");
      link.href = downloadUrl;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } catch (error) {
      console.error("Lỗi khi tải file:", error);
      alert("Lấy đường dẫn tải file thất bại!");
    }
  };

  const handleDeleteFile = async (fileId: string, fileName: string) => {
    const isConfirm = window.confirm(
      `Bạn có chắc chắn muốn xóa file "${fileName}" không?`,
    );
    if (!isConfirm) return;
    try {
      await api.deleteFile(fileId);
      fetchData(currentFolder ? currentFolder.id : null);
    } catch (error) {
      console.error("Lỗi khi xóa file:", error);
      alert("Không thể xóa file. Vui lòng thử lại!");
    }
  };

  // Tính toán dữ liệu hiển thị (Derived state)
  const currentFolderId = currentFolder ? currentFolder.id : null;
  const displayFolders = folders.filter(
    (f) => (f.parentId || null) === currentFolderId,
  );

  // Gói toàn bộ State và Function vào một Object để Component lấy ra dùng
  return {
    files,
    nodes,
    isLoading,
    currentFolder,
    folderPath,
    uploadProgress,
    fileInputRef,
    folderInputRef,
    displayFolders,
    formatBytes,
    handleCreateFolder,
    handleDeleteFolder,
    handleFileUpload,
    handleOpenFolder,
    handleNavigate,
    handleDrop,
    handleDownloadFile,
    handleDeleteFile,
  };
}
