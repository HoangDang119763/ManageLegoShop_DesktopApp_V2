package ENUM;

public enum ServiceAccessCode {
    ROLE_PERMISSION_SERVICE(1001),
    INVOICE_DETAILINVOICE_SERVICE(1002),
    IMPORT_DETAILIMPORT_SERVICE(1003),
    LOGIN_SERVICE(1004),
    DISCOUNT_DETAILDISCOUNT_SERVICE(1005);

    private final int code;

    ServiceAccessCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
