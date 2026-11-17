import { useState } from "react";
import {
  FileText,
  Download,
  Trash2,
  Edit2,
  MoreVertical,
  CheckCircle2,
  Circle,
} from "lucide-react";
import type { FileDTO } from "../api";

interface FileListProps {
  files: FileDTO[];
  uploadProgress: { [key: string]: number };
  formatBytes: (bytes: any) => string;
  handleDownloadFile: (fileId: string) => void;
  handleDeleteFile: (fileId: string, fileName: string) => void;
}

export default function FileList({
  files,
  uploadProgress,
  formatBytes,
  handleDownloadFile,
  handleDeleteFile,
}: FileListProps) {
  // State để lưu danh sách các file đang được chọn
  const [selectedFiles, setSelectedFiles] = useState<Set<string>>(new Set());

  const toggleFileSelection = (fileId: string) => {
    const newSelected = new Set(selectedFiles);
    if (newSelected.has(fileId)) {
      newSelected.delete(fileId);
    } else {
      newSelected.add(fileId);
    }
    setSelectedFiles(newSelected);
  };

  const isUploading = Object.keys(uploadProgress).length > 0;

  if (files.length === 0 && !isUploading) {
    return (
      <div className="text-center py-10 text-slate-500 italic bg-white border border-slate-200 rounded-xl text-lg">
        Không có tập tin nào trong thư mục này
      </div>
    );
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-semibold text-slate-800">Tệp tin</h2>

        {/* THANH CÔNG CỤ (Chỉ hiện khi có file được chọn) */}
        {selectedFiles.size > 0 && (
          <div className="flex items-center space-x-2 bg-blue-50 px-4 py-2 rounded-lg border border-blue-100">
            <span className="text-sm font-medium text-blue-700 mr-2">
              Đã chọn {selectedFiles.size}
            </span>
            <button
              onClick={(e) => e.stopPropagation()} // Chặn sự kiện để không bị ẩn thanh công cụ
              className="p-2 text-slate-600 hover:text-blue-600 hover:bg-blue-100 rounded-md transition-colors"
              title="Đổi tên"
            >
              <Edit2 className="w-4 h-4" />
            </button>
            <button
              onClick={(e) => {
                e.stopPropagation(); // Cực kỳ quan trọng: chặn sự kiện click dòng file
                // Tạm thời tải file đầu tiên trong danh sách chọn
                const firstSelectedFileId = Array.from(selectedFiles)[0];
                handleDownloadFile(firstSelectedFileId);
              }}
              className="p-2 text-slate-600 hover:text-blue-600 hover:bg-blue-100 rounded-md transition-colors"
              title="Tải về"
            >
              <Download className="w-4 h-4" />
            </button>
            <button
              onClick={(e) => {
                e.stopPropagation(); // Cực kỳ quan trọng: chặn sự kiện click dòng file
                const confirmMessage =
                  selectedFiles.size === 1
                    ? "Bạn có chắc chắn muốn xóa file này không?"
                    : `Bạn có chắc chắn muốn xóa ${selectedFiles.size} file này không?`;

                if (window.confirm(confirmMessage)) {
                  selectedFiles.forEach((fileId) => {
                    const fileObj = files.find((f) => f.id === fileId);
                    if (fileObj) {
                      handleDeleteFile(fileId, fileObj.originalName);
                    }
                  });
                  // Xóa xong thì bỏ chọn
                  setSelectedFiles(new Set());
                }
              }}
              className="p-2 text-slate-600 hover:text-red-600 hover:bg-red-50 rounded-md transition-colors"
              title="Xóa"
            >
              <Trash2 className="w-4 h-4" />
            </button>
            <button
              onClick={(e) => e.stopPropagation()}
              className="p-2 text-slate-600 hover:text-slate-800 hover:bg-slate-200 rounded-md transition-colors"
            >
              <MoreVertical className="w-4 h-4" />
            </button>
          </div>
        )}
      </div>

      <div className="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
        <table className="w-full text-left border-collapse">
          <thead>
            {/* TIÊU ĐỀ BẢNG */}
            <tr className="border-b border-slate-200 bg-slate-50 text-slate-600 text-lg">
              <th className="py-4 px-6 w-12 text-center"></th>
              <th className="py-4 px-6 font-bold">Tên file</th>
              <th className="py-4 px-6 font-bold w-40">Trạng thái</th>
              <th className="py-4 px-6 font-bold w-48">Kích thước</th>
            </tr>
          </thead>
          <tbody>
            {/* DANH SÁCH FILE ĐANG TẢI LÊN */}
            {Object.entries(uploadProgress).map(([fileName, percent]) => (
              <tr
                key={`uploading-${fileName}`}
                className="border-b border-slate-100 bg-blue-50/30"
              >
                <td className="py-4 px-6 text-center">
                  <div className="w-5 h-5 rounded-full border-2 border-slate-200 mx-auto"></div>
                </td>
                <td className="py-4 px-6 flex items-center">
                  <FileText className="w-7 h-7 text-harbor-blue mr-4 shrink-0" />
                  <span className="font-semibold text-slate-800 text-lg">
                    {fileName}
                  </span>
                </td>
                <td className="py-4 px-6" colSpan={2}>
                  <div className="flex items-center">
                    <div className="w-full bg-slate-200 rounded-full h-2.5 mr-3">
                      <div
                        className="bg-harbor-blue h-2.5 rounded-full transition-all duration-300"
                        style={{ width: `${percent}%` }}
                      ></div>
                    </div>
                    <span className="text-sm font-bold text-harbor-blue min-w-[4ch]">
                      {percent}%
                    </span>
                  </div>
                </td>
              </tr>
            ))}

            {/* DANH SÁCH FILE ĐÃ HOÀN THÀNH */}
            {files.map((file) => {
              const isSelected = selectedFiles.has(file.id);
              return (
                <tr
                  key={file.id}
                  onClick={() => toggleFileSelection(file.id)}
                  className={`border-b border-slate-100 transition-colors group cursor-pointer ${
                    isSelected ? "bg-blue-50/50" : "hover:bg-slate-50"
                  }`}
                >
                  <td className="py-4 px-6 text-center">
                    {/* Checkbox tròn */}
                    <div
                      className={`${
                        isSelected
                          ? "opacity-100"
                          : "opacity-0 group-hover:opacity-100"
                      } transition-opacity flex justify-center items-center h-full`}
                    >
                      {isSelected ? (
                        <CheckCircle2 className="w-6 h-6 text-harbor-blue" />
                      ) : (
                        <Circle className="w-6 h-6 text-slate-400 hover:text-harbor-blue" />
                      )}
                    </div>
                  </td>
                  <td className="py-4 px-6 flex items-center">
                    <FileText
                      className={`w-7 h-7 mr-4 shrink-0 ${
                        isSelected ? "text-harbor-blue" : "text-slate-400"
                      }`}
                    />
                    <span
                      className="font-medium text-slate-800 text-lg truncate max-w-xl"
                      title={file.originalName}
                    >
                      {file.originalName}
                    </span>
                  </td>
                  <td className="py-4 px-6">
                    <span
                      className={`px-3 py-1.5 rounded-full text-sm font-bold ${
                        file.status === "COMPLETED"
                          ? "bg-green-100 text-green-700"
                          : "bg-yellow-100 text-yellow-700"
                      }`}
                    >
                      {file.status === "COMPLETED" ? "Hoàn tất" : "Đang xử lý"}
                    </span>
                  </td>
                  <td className="py-4 px-6 text-slate-700 font-medium text-lg">
                    {formatBytes(file.fileSize)}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
