package BUS;

import DAL.StatusDAL;
import DTO.StatusDTO;
import ENUM.StatusType;

import java.util.ArrayList;
import java.util.Objects;

public class StatusBUS extends BaseBUS<StatusDTO, Integer> {
    private static final StatusBUS INSTANCE = new StatusBUS();

    private StatusBUS() {
    }

    public static StatusBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<StatusDTO> getAll() {
        return StatusDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(StatusDTO obj) {
        return obj.getId();
    }

    // Lấy các status theo type
    public ArrayList<StatusDTO> getAllByTypeLocal(StatusType type) {
        ArrayList<StatusDTO> result = new ArrayList<>();
        if (type == null)
            return result;

        for (StatusDTO status : arrLocal) {
            if (type.name().equalsIgnoreCase(status.getType())) {
                result.add(new StatusDTO(status));
            }
        }
        return result;
    }

    // Lấy id của status theo Type và Status Enum
    // Sử dụng: statusBUS.getByIdLocal(StatusType.EMPLOYEE, Status.Employee.ACTIVE)
    public StatusDTO getByTypeAndStatusNameLocal(StatusType type, Enum<?> statusEnum) {
        if (type == null || statusEnum == null)
            return null;

        for (StatusDTO status : arrLocal) {
            // Cách này an toàn hơn nếu status.getName() bị null
            if (type.name().equalsIgnoreCase(status.getType())
                    && statusEnum.name().equalsIgnoreCase(status.getName())) {
                return new StatusDTO(status);
            }
        }
        return null;
    }

    public ArrayList<StatusDTO> filterStatus(String searchBy, String keyword) {
        ArrayList<StatusDTO> filteredList = new ArrayList<>();

        if (keyword == null)
            keyword = "";
        if (searchBy == null)
            searchBy = "";

        keyword = keyword.trim().toLowerCase();

        for (StatusDTO status : arrLocal) {
            boolean matchesSearch = true;

            String id = String.valueOf(status.getId());
            String name = status.getName() != null ? status.getName().toLowerCase() : "";
            String type = status.getType() != null ? status.getType().toLowerCase() : "";

            if (!keyword.isEmpty()) {
                switch (searchBy) {
                    case "Mã trạng thái" -> matchesSearch = id.contains(keyword);
                    case "Tên trạng thái" -> matchesSearch = name.contains(keyword);
                    case "Loại" -> matchesSearch = type.contains(keyword);
                }
            }

            if (matchesSearch) {
                filteredList.add(status);
            }
        }

        return filteredList;
    }
}
