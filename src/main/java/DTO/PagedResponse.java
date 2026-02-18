package DTO;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PagedResponse<T> {
    private List<T> items; // Danh sách dữ liệu (VD: List<EmployeeHistoryDTO>)
    private int totalItems; // Tổng số bản ghi trong DB (để UI tính số trang)
    private int pageIndex; // Trang hiện tại
    private int pageSize; // Số dòng mỗi trang
}