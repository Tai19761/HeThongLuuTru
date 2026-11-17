import { UploadCloud, Loader2 } from "lucide-react";
import { useFileManager } from "./hooks/useFileManager";

// Import các components đã tách
import Sidebar from "./components/Sidebar";
import Header from "./components/Header";
import FolderList from "./components/FolderList";
import FileList from "./components/FileList";

function App() {
  // Lấy toàn bộ não bộ (logic) từ Hook
  const {
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
  } = useFileManager();

  return (
    <div className="min-h-screen flex bg-harbor-bg text-slate-800 font-sans">
      {/* INPUT ẨN */}
      <input
        type="file"
        className="hidden"
        ref={fileInputRef}
        onChange={handleFileUpload}
        multiple
      />
      <input
        type="file"
        className="hidden"
        ref={folderInputRef}
        onChange={handleFileUpload}
        {...{ webkitdirectory: "true" }}
      />

      {/* CỘT SIDEBAR TRÁI */}
      <Sidebar nodes={nodes} formatBytes={formatBytes} />

      {/* KHU VỰC NỘI DUNG CHÍNH */}
      <div className="flex-1 flex flex-col h-screen overflow-hidden">
        {/* HEADER */}
        <Header
          currentFolder={currentFolder}
          folderPath={folderPath}
          handleNavigate={handleNavigate}
          handleCreateFolder={handleCreateFolder}
          triggerFileUpload={() => fileInputRef.current?.click()}
          triggerFolderUpload={() => folderInputRef.current?.click()}
        />

        {/* NỘI DUNG */}
        <div className="flex-1 overflow-auto p-8 bg-slate-50/50">
          {/* VÙNG DROPZONE */}
          <div
            onClick={() => fileInputRef.current?.click()}
            onDragOver={(e) => e.preventDefault()}
            onDrop={handleDrop}
            className="cursor-pointer border-2 border-dashed border-blue-300 bg-white rounded-2xl p-10 mb-8 text-center flex flex-col items-center justify-center transition-all hover:bg-blue-50 hover:border-blue-400 hover:shadow-md"
          >
            <div className="bg-blue-50 p-4 rounded-full mb-4">
              <UploadCloud className="w-10 h-10 text-harbor-blue" />
            </div>
            <h3 className="text-xl font-bold text-slate-800">
              Bấm hoặc kéo thả File vào đây
            </h3>
            <p className="text-base text-slate-500 mt-2">
              File sẽ được cắt thành các mảnh 5MB và phân tán an toàn xuống
              Storage Node
            </p>
          </div>

          {isLoading ? (
            <div className="flex flex-col items-center justify-center py-12 text-slate-500">
              <Loader2 className="w-10 h-10 animate-spin mb-4 text-harbor-blue" />
              <span className="text-lg">Đang tải dữ liệu...</span>
            </div>
          ) : (
            <div className="w-full flex flex-col space-y-6">
              {/* DANH SÁCH THƯ MỤC */}
              <FolderList
                isRoot={currentFolder === null}
                currentFolderName={currentFolder?.folderName} // Thêm dòng này
                handleNavigateUp={() => handleNavigate(folderPath.length - 2)}
                folders={displayFolders}
                handleOpenFolder={handleOpenFolder}
                handleDeleteFolder={handleDeleteFolder}
              />

              {/* DANH SÁCH TỆP TIN */}
              <FileList
                files={files}
                uploadProgress={uploadProgress}
                formatBytes={formatBytes}
                handleDownloadFile={handleDownloadFile}
                handleDeleteFile={handleDeleteFile}
              />
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default App;
