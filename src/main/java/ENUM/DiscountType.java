package ENUM;

public enum DiscountType {
    PERCENTAGE(0, "Phần trăm"),
    FIXED_AMOUNT(1, "Giảm cứng");

    private final int code;
    private final String displayName;

    DiscountType(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public int getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static DiscountType fromCode(int code) {
        for (DiscountType type : DiscountType.values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid discount type code: " + code);
    }

    public static DiscountType fromDisplayName(String displayName) {
        for (DiscountType type : DiscountType.values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid discount type display name: " + displayName);
    }
}
