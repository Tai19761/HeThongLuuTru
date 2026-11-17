import { UploadCloud, Folder, HardDrive } from "lucide-react";
import type { StorageNodeDTO } from "../api";

interface SidebarProps {
  nodes: StorageNodeDTO[];
  formatBytes: (bytes: any) => string;
}

export default function Sidebar({ nodes, formatBytes }: SidebarProps) {
  return (
    <div className="w-64 bg-white border-r border-slate-200 flex flex-col">
      <div className="h-16 flex items-center px-6 border-b border-slate-200">
        <UploadCloud className="text-harbor-blue w-8 h-8 mr-2" />
        <span className="text-xl font-bold text-slate-900">Harbor</span>
      </div>

      <div className="p-4 flex-1">
        <div className="bg-blue-50 text-harbor-blue px-4 py-2 rounded-lg font-medium flex items-center cursor-pointer">
          <Folder className="w-5 h-5 mr-3" />
          Tất cả tệp
        </div>
      </div>

      {/* STORAGE NODES */}
      <div className="p-4 border-t border-slate-200 bg-slate-50">
        <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-3">
          Storage Nodes
        </h3>
        {nodes.length === 0 ? (
          <p className="text-xs text-slate-500 italic">
            Chưa có node nào hoạt động
          </p>
        ) : (
          nodes.map((node) => (
            <div key={node.id} className="mb-3">
              <div className="flex justify-between text-sm mb-1">
                <span className="flex items-center text-slate-700">
                  <HardDrive
                    className={`w-4 h-4 mr-2 ${node.status === "ACTIVE" ? "text-green-500" : "text-red-500"}`}
                  />
                  Kho {node.id}
                </span>
                <span className="text-slate-500">
                  {formatBytes(node.usedCapacity)}
                </span>
              </div>
              <div className="w-full bg-slate-200 rounded-full h-1.5">
                <div
                  className={`h-1.5 rounded-full ${node.status === "ACTIVE" ? "bg-green-500" : "bg-red-500"}`}
                  style={{ width: "50%" }} // Có thể tính % thực tế sau nếu muốn
                ></div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
