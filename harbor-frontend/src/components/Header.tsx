import { FolderPlus, FileText, UploadCloud } from "lucide-react";
import type { FolderDTO } from "../api";

interface HeaderProps {
  currentFolder: FolderDTO | null;
  folderPath: FolderDTO[];
  handleNavigate: (index: number) => void;
  handleCreateFolder: () => void;
  triggerFileUpload: () => void;
  triggerFolderUpload: () => void;
}

export default function Header({
  currentFolder,
  folderPath,
  handleNavigate,
  handleCreateFolder,
  triggerFileUpload,
  triggerFolderUpload,
}: HeaderProps) {
  return (
    <div className="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-8 shrink-0">
      {/* BREADCRUMB (Đường dẫn) */}
      <div className="flex items-center text-xl font-medium text-slate-500">
        <span
          className={`cursor-pointer hover:text-harbor-blue ${!currentFolder ? "text-slate-900 font-bold" : ""}`}
          onClick={() => handleNavigate(-1)}
        >
          Thư mục gốc
        </span>
        {folderPath.map((folder, index) => (
          <span key={folder.id} className="flex items-center">
            <span className="mx-2 text-slate-400">/</span>
            <span
              className={`cursor-pointer hover:text-harbor-blue ${index === folderPath.length - 1 ? "text-slate-900 font-bold" : ""}`}
              onClick={() => handleNavigate(index)}
            >
              {folder.folderName}
            </span>
          </span>
        ))}
      </div>

      {/* CÁC NÚT HÀNH ĐỘNG */}
      <div className="flex space-x-3">
        <button
          onClick={handleCreateFolder}
          className="flex items-center px-4 py-2 border border-slate-300 rounded-md text-lg font-medium text-slate-700 hover:bg-slate-50 transition-colors shadow-sm"
        >
          <FolderPlus className="w-4 h-4 mr-2" /> Tạo thư mục
        </button>
        <button
          onClick={triggerFileUpload}
          className="flex items-center px-4 py-2 bg-harbor-blue text-white rounded-md text-lg font-medium hover:bg-blue-700 shadow-sm transition-colors"
        >
          <FileText className="w-4 h-4 mr-2" /> Tải file
        </button>
        <button
          onClick={triggerFolderUpload}
          className="flex items-center px-4 py-2 bg-slate-800 text-white rounded-md text-lg font-medium hover:bg-slate-900 shadow-sm transition-colors"
        >
          <UploadCloud className="w-4 h-4 mr-2" /> Tải thư mục
        </button>
      </div>
    </div>
  );
}
