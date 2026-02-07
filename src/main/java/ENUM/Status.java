package ENUM;

public class Status {
    // Enum cho Nhân viên
    public enum Employee {
        ACTIVE, INACTIVE, ON_LEAVE
    }

    // Enum cho Tài khoản
    public enum Account {
        ACTIVE, LOCKED
    }

    // Enum cho Sản phẩm
    public enum Product {
        ACTIVE, INACTIVE
    }

    // Enum cho Thể loại
    public enum Category {
        ACTIVE, INACTIVE
    }

    // Enum cho Nhà cung cấp
    public enum Supplier {
        ACTIVE, INACTIVE
    }

    // Enum cho Khách hàng
    public enum Customer {
        ACTIVE, INACTIVE
    }

    // Enum cho Hóa đơn
    public enum Invoice {
        COMPLETED, CANCELED
    }

    // Enum cho Phiếu nhập
    public enum Import {
        COMPLETED, CANCELED
    }

    // Enum cho Loại nghỉ phép
    public enum LeaveType {
        ANNUAL_LEAVE("Nghỉ phép"),
        SICK_LEAVE("Nghỉ bệnh"),
        MATERNITY_LEAVE("Nghỉ thai sản"),
        PERSONAL_LEAVE("Nghỉ việc riêng");

        private final String displayName;

        LeaveType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Enum cho Trạng thái Đơn nghỉ phép
    public enum LeaveRequest {
        PENDING, // Chờ duyệt
        APPROVED, // Đã duyệt
        REJECTED, // Từ chối (bởi quản lý)
        CANCELED // Nhân viên tự hủy
    }

    // Enum cho Mức độ của báo cáo
    public enum ReportLevel {
        HIGH("Cao"),
        MEDIUM("Trung bình"),
        LOW("Thấp");

        private final String displayName;

        ReportLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Enum cho Danh mục báo cáo
    public enum ReportCategory {
        SYSTEM("Hệ thống"),
        FINANCE("Tài chính"),
        TECHNICAL("Kỹ thuật"),
        INVENTORY("Kho hàng"),
        SOFTWARE("Phần mềm"),
        SECURITY("An ninh"),
        HR("Nhân sự"),
        MAINTENANCE("Bảo trì"),
        SALES("Bán hàng");

        private final String displayName;

        ReportCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Enum cho mức độ phạt
    public enum FineLevel {
        LEVEL_1("Vi phạm nhẹ", 50000.0), // Phạt 50k
        LEVEL_2("Vi phạm vừa", 200000.0), // Phạt 200k
        LEVEL_3("Vi phạm nặng", 500000.0), // Phạt 500k
        CUSTOM("Tùy chỉnh", 0.0); // Số tiền nhập tay

        private final String description;
        private final double defaultAmount;

        FineLevel(String description, double defaultAmount) {
            this.description = description;
            this.defaultAmount = defaultAmount;
        }

        public String getDescription() {
            return description;
        }

        public double getDefaultAmount() {
            return defaultAmount;
        }
    }

    // Enum cho Phòng ban
    public enum Department {
        ACTIVE, INACTIVE
    }
}
