package BUS;

import DAL.StatusDAL;
import DTO.StatusDTO;
import ENUM.StatusType;
import java.util.ArrayList;

public class StatusBUS extends BaseBUS<StatusDTO, Integer> {
    private static final StatusBUS INSTANCE = new StatusBUS();

    // Sử dụng bộ nhớ đệm vì Status là dữ liệu tham chiếu cố định
    private static final ArrayList<StatusDTO> STATUS_CACHE = new ArrayList<>();

    private StatusBUS() {
    }

    public static StatusBUS getInstance() {
        return INSTANCE;
    }

    public void loadCache() {
        if (!STATUS_CACHE.isEmpty()) STATUS_CACHE.clear();
        STATUS_CACHE.addAll(StatusDAL.getInstance().getAll());
    }

    @Override
    public ArrayList<StatusDTO> getAll() {
        // Trả về bản sao để tránh việc lớp khác can thiệp trực tiếp vào Cache
        return new ArrayList<>(STATUS_CACHE);
    }

    /**
     * getById bây giờ sẽ tìm trong RAM cực nhanh
     */
    @Override
    public StatusDTO getById(Integer id) {
        if (id == null)
            return null;
        return STATUS_CACHE.stream()
                .filter(s -> s.getId() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    protected Integer getKey(StatusDTO obj) {
        return obj.getId();
    }

    // --- CÁC HÀM TRUY VẤN TỬ CACHE (CHẠY TRÊN RAM) ---

    public ArrayList<StatusDTO> getAllByType(StatusType type) {
        ArrayList<StatusDTO> result = new ArrayList<>();
        if (type == null)
            return result;

        for (StatusDTO status : STATUS_CACHE) {
            if (type.name().equalsIgnoreCase(status.getType())) {
                result.add(new StatusDTO(status));
            }
        }
        return result;
    }

    public StatusDTO getByTypeAndStatusName(StatusType type, Enum<?> statusEnum) {
        if (type == null || statusEnum == null)
            return null;

        for (StatusDTO status : STATUS_CACHE) {
            if (type.name().equalsIgnoreCase(status.getType())
                    && statusEnum.name().equalsIgnoreCase(status.getName())) {
                return new StatusDTO(status);
            }
        }
        return null;
    }

    public boolean isValidStatusIdForType(StatusType type, int statusId) {
        if (type == null || statusId <= 0)
            return false;

        for (StatusDTO status : STATUS_CACHE) {
            if (type.name().equalsIgnoreCase(status.getType()) && status.getId() == statusId) {
                return true;
            }
        }
        return false;
    }

    // --- SEARCH TRÊN RAM (VÌ DỮ LIỆU STATUS RẤT ÍT) ---

    public ArrayList<StatusDTO> filterStatus(String searchBy, String keyword) {
        ArrayList<StatusDTO> filteredList = new ArrayList<>();
        String finalKey = (keyword == null) ? "" : keyword.trim().toLowerCase();

        // [FIXED] Dùng STATUS_CACHE thay vì arrLocal đã bị xóa ở BaseBUS
        for (StatusDTO status : STATUS_CACHE) {
            boolean matchesSearch = true;

            if (!finalKey.isEmpty()) {
                String id = String.valueOf(status.getId());
                String name = status.getName() != null ? status.getName().toLowerCase() : "";
                String type = status.getType() != null ? status.getType().toLowerCase() : "";

                matchesSearch = switch (searchBy != null ? searchBy : "") {
                    case "Mã trạng thái" -> id.contains(finalKey);
                    case "Tên trạng thái" -> name.contains(finalKey);
                    case "Loại" -> type.contains(finalKey);
                    default -> true;
                };
            }

            if (matchesSearch)
                filteredList.add(new StatusDTO(status));
        }
        return filteredList;
    }
}