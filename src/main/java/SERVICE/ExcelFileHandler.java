package SERVICE;

import UTILS.NotificationUtils;
import org.apache.poi.ss.usermodel.Workbook;

import java.awt.*;
import java.io.*;
import java.nio.channels.FileChannel;

/**
 * Xử lý tất cả file operations: lock, save, open
 * Mục đích: Loại bỏ code lặp lại trong Excel exports
 */
public class ExcelFileHandler {

    /**
     * Kiểm tra xem file có bị lock (đang mở) không
     */
    public boolean isFileLocked(File file) {
        if (!file.exists()) {
            return false;
        }

        try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
                FileChannel channel = raf.getChannel()) {
            try {
                channel.lock();
                return false; // File không bị lock
            } catch (IOException e) {
                return true; // File bị lock
            }
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Lưu workbook vào file và mở nó
     */
    public void saveAndOpenFile(String fileName, Workbook workbook, boolean showSuccess) {
        File file = new File(fileName);

        // Kiểm tra nếu file đang mở
        if (file.exists() && isFileLocked(file)) {
            System.err.println("File đang được mở. Vui lòng đóng file trước khi xuất.");
            NotificationUtils.showErrorAlert(
                    "File đang được mở. Vui lòng đóng file trước khi xuất.",
                    "Thông báo");
            try {
                workbook.close();
            } catch (IOException ignored) {
            }
            return;
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        } catch (IOException e) {
            NotificationUtils.showErrorAlert("Không thể lưu file: " + e.getMessage(), "Lỗi");
        } finally {
            try {
                workbook.close();
            } catch (IOException ignored) {
            }
        }

        // Mở file sau khi lưu
        if (file.exists()) {
            try {
                if (showSuccess) {
                    NotificationUtils.showInfoAlert("Xuất file Excel thành công!", "Thông báo");
                }
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                System.err.println("Không thể mở file: " + e.getMessage());
            }
        }
    }

    /**
     * Overload: mặc định hiển thị success message
     */
    public void saveAndOpenFile(String fileName, Workbook workbook) {
        saveAndOpenFile(fileName, workbook, true);
    }
}
