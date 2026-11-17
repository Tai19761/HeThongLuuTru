import { Folder, Trash2, CornerUpLeft } from "lucide-react";
import type { FolderDTO } from "../api";

interface FolderListProps {
  folders: FolderDTO[];
  isRoot: boolean;
  currentFolderName?: string;
  handleNavigateUp: () => void;
  handleOpenFolder: (folder: FolderDTO) => void;
  handleDeleteFolder: (
    e: React.MouseEvent,
    folderId: string,
    folderName: string,
  ) => void;
}

export default function FolderList({
  folders,
  isRoot,
  currentFolderName,
  handleNavigateUp,
  handleOpenFolder,
  handleDeleteFolder,
}: FolderListProps) {
  return (
    <div className="mb-8">
      {/* --- KHU VỰC TIÊU ĐỀ & NÚT QUAY LẠI ĐƯỢC XẾP NGANG HÀNG --- */}
      <div className="flex items-center mb-4">
        {/* NÚT TRỞ VỀ (Chỉ hiện khi không ở thư mục gốc) */}
        {!isRoot && (
          <button
            onClick={handleNavigateUp}
            className="flex items-center justify-center p-2 mr-3 bg-white border border-slate-200 text-slate-600 rounded-lg hover:bg-slate-50 hover:text-harbor-blue transition-all shadow-sm"
            title="Quay lại"
          >
            <CornerUpLeft className="w-5 h-5" />
          </button>
        )}

        {/* TIÊU ĐỀ: Thư mục gốc hoặc Tên thư mục hiện tại */}
        <h2 className="text-xl font-semibold text-slate-800">
          {isRoot ? "Thư mục" : currentFolderName}
        </h2>
      </div>

      {/* --- KHU VỰC HIỂN THỊ DANH SÁCH THƯ MỤC CON --- */}
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
        {folders.map((folder) => (
          <div
            key={folder.id}
            onDoubleClick={() => handleOpenFolder(folder)}
            className="bg-white border border-slate-200 p-4 rounded-xl flex justify-between items-center cursor-pointer hover:shadow-md hover:border-blue-300 transition-all select-none group h-16"
          >
            <div className="flex items-center overflow-hidden">
              <Folder className="w-8 h-8 text-blue-400 mr-3 shrink-0" />
              <div className="truncate font-semibold text-slate-800 text-xl">
                {folder.folderName}
              </div>
            </div>
            <button
              onClick={(e) => {
                e.stopPropagation(); // Thêm cái này cho an toàn, tránh double-click nhầm
                handleDeleteFolder(e, folder.id, folder.folderName);
              }}
              className="opacity-0 group-hover:opacity-100 text-slate-400 hover:text-red-500 hover:bg-red-50 p-2 rounded transition-all"
              title="Xóa thư mục"
            >
              <Trash2 className="w-5 h-5" />
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
