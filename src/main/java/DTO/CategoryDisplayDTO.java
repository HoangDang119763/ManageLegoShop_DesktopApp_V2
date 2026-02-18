package DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDisplayDTO {
    private Integer id;
    private String name;
    private Integer statusId;
    private String statusDescription; // Từ JOIN với status table
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
