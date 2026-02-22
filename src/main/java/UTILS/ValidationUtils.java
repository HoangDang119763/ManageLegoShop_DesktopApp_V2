
package UTILS;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;
import java.time.LocalDateTime;

public class ValidationUtils {
    private static final ValidationUtils INSTANCE = new ValidationUtils();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_WITH_HOUR_FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/yyyy-HH:mm:ss");
    private static final Pattern VIETNAMESE_TEXT_PATTERN = Pattern
            .compile("^[\\p{L}\\d_\\-./,]+(\\s[\\p{L}\\d_\\-./,]+)*$");
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");
    private static final Pattern VIETNAMESE_PHONE_PATTERN = Pattern.compile("^0\\d{9}$");

    private ValidationUtils() {
    }

    public static ValidationUtils getInstance() {
        return INSTANCE;
    }

    public boolean validateStringLength(String value, int maxLength) {
        if (value == null || value.trim().isEmpty())
            return false;
        return value.length() <= maxLength;
    }

    // Cho bằng 0
    public boolean validateBigDecimal(BigDecimal value, int precision, int scale, boolean allowNegative) {
        if (value == null)
            return false;

        // 1. Check âm
        if (!allowNegative && value.compareTo(BigDecimal.ZERO) < 0)
            return false;

        try {
            // 2. ÉP QUY MÔ (Normalization)
            // Thay vì strip, ta ép nó về đúng scale (VD: 2)
            // Nếu người dùng nhập 10.000 -> nó sẽ thành 10.00
            BigDecimal normalized = value.setScale(scale, java.math.RoundingMode.HALF_UP);

            // 3. Kiểm tra tổng số chữ số (Precision)
            // Công thức: Tổng chữ số - chữ số sau dấu phẩy <= Precision - Scale
            // Ví dụ: DECIMAL(10,2) thì phần nguyên tối đa có 8 chữ số
            int currentPrecision = normalized.precision();
            int currentScale = normalized.scale();

            return (currentPrecision - currentScale) <= (precision - scale);

        } catch (ArithmeticException e) {
            // Trường hợp người dùng nhập quá nhiều số lẻ mà không cho phép làm tròn (nếu
            // cần chặt chẽ)
            return false;
        }
    }

    public boolean validateSalary(BigDecimal value, int precision, int scale, boolean allowNegative) {
        if (value == null)
            return false;
        if (!allowNegative && value.compareTo(BigDecimal.ZERO) <= 0)
            return false;
        value = value.stripTrailingZeros();
        return value.scale() <= scale && value.precision() - value.scale() <= precision - scale;
    }

    public boolean validateVietnameseText(String value, int maxLength) {
        if (value == null)
            return false;
        String cleaned = normalizeWhiteSpace(value);
        return validateStringLength(cleaned, maxLength) && VIETNAMESE_TEXT_PATTERN.matcher(cleaned).matches();
    }

    public boolean validateVietnameseText50(String value) {
        return validateVietnameseText(value, 50);
    }

    public boolean validateVietnameseText100(String value) {
        return validateVietnameseText(value, 100);
    }

    public boolean validateVietnameseText255(String value) {
        return validateVietnameseText(value, 255);
    }

    public boolean validateVietnameseText248(String value) {
        return validateVietnameseText(value, 248);
    }

    public boolean validateVietnameseText65k4(String value) {
        return validateVietnameseText(value, 65400);
    }

    public boolean validatePassword(String password, int minLength, int maxLength) {
        if (password == null)
            return false;
        return password.length() >= minLength
                && password.length() <= maxLength
                && !password.contains(" "); // Ch�+� cߦ�n kh+�ng ch�+�a khoߦ�ng trߦ�ng, c+�n lߦ�i cho ph+�p tߦ�t cߦ�.
    }

    public boolean validateUsername(String username, int minLength, int maxLength) {
        if (username == null)
            return false;
        return username.length() >= minLength
                && username.length() <= maxLength
                && ALPHANUMERIC_PATTERN.matcher(username).matches();
    }

    public boolean validateDiscountCode(String code, int minLength, int maxLength) {
        if (code == null)
            return false;
        return code.length() >= minLength
                && code.length() <= maxLength
                && ALPHANUMERIC_PATTERN.matcher(code).matches();
    }

    public boolean validateVietnamesePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty())
            return false;
        return VIETNAMESE_PHONE_PATTERN.matcher(phoneNumber).matches();
    }

    public boolean validateEmail(String email) {
        if (email == null || email.trim().isEmpty())
            return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.matches(emailRegex, email.trim());
    }

    public String formatCurrency(BigDecimal price) {
        if (price == null)
            return "0";
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.of("vi", "VN"));
        return currencyFormat.format(price);
    }

    public String formatPercent(BigDecimal value) {
        if (value == null) {
            return "0%";
        }

        // Sử dụng định dạng số của Việt Nam (để có dấu phẩy nếu có số lẻ)
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.of("vi", "VN"));

        // Thiết lập số lượng số lẻ tối đa (ví dụ: 2 số lẻ như 5,25%)
        numberFormat.setMaximumFractionDigits(2);

        return numberFormat.format(value) + "%";
    }

    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "";
    }

    public String formatDateTime(LocalDate date) {
        return date != null ? date.format(DATE_TIME_FORMATTER) : "";
    }

    public String formatDateTimeWithHour(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_WITH_HOUR_FORMATTER) : "";
    }

    public String normalizeWhiteSpace(String input) {
        if (input == null)
            return null;
        return input.trim().replaceAll("\\s+", " ");
    }

    public boolean validateDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth == null)
            return false;
        LocalDate today = LocalDate.now();
        return dateOfBirth.isBefore(today);
    }

    public boolean validateDateOfBirth(LocalDateTime dateOfBirth) {
        if (dateOfBirth == null)
            return false; // Ng+�y sinh kh+�ng -榦�+�c l+� null
        LocalDate today = LocalDate.now();
        return dateOfBirth.toLocalDate().isBefore(today);
    }

    // validate and return for quick get data or return -1 for get error
    public int canParseToInt(String input) {
        try {
            return Integer.parseInt(input);
        } catch (Exception ex) {
            return -1;
        }
    }

    public BigDecimal canParseToBigDecimal(String input) {
        try {
            if (input.contains("."))
                input = input.substring(0, input.indexOf("."));
            return BigDecimal.valueOf(Long.parseLong(input));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return BigDecimal.valueOf(-1);
        }
    }

    public LocalDateTime canParseToLocalDateTime(String value) {
        try {
            // Nếu chuỗi giống định dạng "yyyy-MM-dd HH:mm:ss"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(value, formatter);
        } catch (DateTimeException e) {
            System.err.println("Lỗi định dạng thời gian: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parse String thành LocalDate
     * Định dạng mong đợi: dd-MM-yyyy (ví dụ: 11-06-2004)
     * 
     * @param dateString chuỗi ngày tháng cần parse
     * @return LocalDate nếu parse thành công, null nếu có lỗi
     */
    public LocalDate parseToLocalDate(String dateString) {
        try {
            if (dateString == null || dateString.trim().isEmpty())
                return null;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            return LocalDate.parse(dateString.trim(), formatter);
        } catch (DateTimeException e) {
            System.err.println("Lỗi định dạng ngày tháng (dd-MM-yyyy): " + e.getMessage());
            return null;
        }
    }

    /**
     * Chuyển chuỗi rỗng hoặc chỉ toàn khoảng trắng thành null
     * Dùng để làm sạch dữ liệu trước khi lưu vào DB (null thay vì empty string)
     * 
     * @param value chuỗi cần kiểm tra
     * @return null nếu value rỗng/khoảng trắng, ngược lại trả về value
     */
    public String convertEmptyStringToNull(String value) {
        if (value != null && value.trim().isEmpty()) {
            return null;
        }
        return value;
    }
}