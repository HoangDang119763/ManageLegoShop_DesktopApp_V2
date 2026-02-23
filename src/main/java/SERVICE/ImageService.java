package SERVICE;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ImageService {
    private static final ImageService INSTANCE = new ImageService();

    private ImageService() {
    };

    public static ImageService gI() {
        return INSTANCE;
    }

    /**
     * Phương thức chung xử lý resize và lưu ảnh (Square)
     * 
     * @param id              ID của object (product/employee)
     * @param imageUrl        Đường dẫn ảnh (từ FileChooser hoặc file:// URI)
     * @param targetDirectory Thư mục đích (ví dụ: "images/product/" hoặc
     *                        "images/avatar/")
     * @param targetSize      Kích thước ảnh sau resize (ví dụ: 400, 200)
     * @return Đường dẫn ảnh đã lưu
     * @throws IOException Nếu có lỗi đọc/ghi file
     */
    private String saveImageCommon(String id, String imageUrl, String targetDirectory, int targetSize)
            throws IOException {
        return saveImageCommon(id, imageUrl, targetDirectory, targetSize, targetSize);
    }

    /**
     * Phương thức chung xử lý resize và lưu ảnh (Rectangle/Custom dimensions)
     * 
     * @param id              ID của object (product/employee)
     * @param imageUrl        Đường dẫn ảnh (từ FileChooser hoặc file:// URI)
     * @param targetDirectory Thư mục đích (ví dụ: "images/product/" hoặc
     *                        "images/avatar/")
     * @param targetWidth     Chiều rộng ảnh sau resize
     * @param targetHeight    Chiều cao ảnh sau resize
     * @return Đường dẫn ảnh đã lưu
     * @throws IOException Nếu có lỗi đọc/ghi file
     */
    private String saveImageCommon(String id, String imageUrl, String targetDirectory, int targetWidth,
            int targetHeight)
            throws IOException {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return "";
        }

        // Xử lý URI file:// từ FileChooser
        if (imageUrl.startsWith("file:")) {
            try {
                File sourceFile = new File(new URI(imageUrl));
                imageUrl = sourceFile.getAbsolutePath();
            } catch (Exception e) {
                // Fallback: xóa tiền tố file:/
                imageUrl = imageUrl.replace("file:/", "");
                // Loại bỏ dấu / thừa trên Windows (file:/C:/path → C:/path)
                if (imageUrl.startsWith("/") && imageUrl.length() > 2 && imageUrl.charAt(2) == ':') {
                    imageUrl = imageUrl.substring(1);
                }
            }
        }

        // Kiểm tra sự tồn tại của tệp
        File sourceFile = new File(imageUrl);
        if (!sourceFile.exists()) {
            throw new IOException("Tệp nguồn không tồn tại: " + imageUrl);
        }

        String extension = imageUrl.substring(imageUrl.lastIndexOf(".") + 1).toLowerCase();
        if (!extension.matches("png|jpg|jpeg|gif|bmp")) {
            throw new IllegalArgumentException("Chỉ hỗ trợ các đị nhãng ảnh: PNG, JPG, JPEG, GIF, BMP.");
        }

        File targetDirFile = new File(targetDirectory);
        if (!targetDirFile.exists()) {
            targetDirFile.mkdirs();
        }

        String tempFileName = id + "_temp." + extension;
        File tempFile = new File(targetDirFile, tempFileName);

        // Tạo tệp tạm thời
        Files.copy(sourceFile.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Đọc ảnh từ file tạm thời và kiểm tra
        BufferedImage originalImage = ImageIO.read(tempFile);
        if (originalImage == null) {
            tempFile.delete();
            throw new IOException("Không thể đọc ảnh từ tệp: " + tempFile);
        }

        // Chọn loại BufferedImage phù hợp với format
        // Đối với PNG giữ ARGB (transparency), JPG/GIF/BMP dùng RGB
        int imageType = BufferedImage.TYPE_INT_RGB;
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, imageType);
        Graphics2D g2d = resizedImage.createGraphics();

        // Fill nền trắng để không bị transparent (hoặc đen cho JPG)
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, targetWidth, targetHeight);

        // Tối ưu chất lượng ảnh khi resize
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Tính toán tỷ lệ co giãn cho ảnh để giữ nguyên tỷ lệ
        double scale = Math.min((double) targetWidth / originalWidth, (double) targetHeight / originalHeight);
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);

        // Tính vị trí để canh giữa ảnh
        int x = (targetWidth - newWidth) / 2;
        int y = (targetHeight - newHeight) / 2;

        // Vẽ ảnh đã resize và canh giữa
        g2d.drawImage(originalImage, x, y, newWidth, newHeight, null);
        g2d.dispose();

        // Kiểm tra và xóa ảnh cũ
        new File(targetDirectory, id + ".png").delete();
        new File(targetDirectory, id + ".jpg").delete();

        // Xác định format name đúng cho ImageIO.write()
        String formatName;
        if (extension.equals("jpg") || extension.equals("jpeg")) {
            formatName = "jpg";
        } else if (extension.equals("png")) {
            formatName = "png";
        } else if (extension.equals("gif")) {
            formatName = "gif";
        } else if (extension.equals("bmp")) {
            formatName = "bmp";
        } else {
            formatName = extension;
        }

        // Lưu ảnh đã resize vào file chính
        String fileName = id + "." + extension;
        File targetFile = new File(targetDirFile, fileName);
        ImageIO.write(resizedImage, formatName, targetFile);

        // Xóa tệp tạm thời
        tempFile.delete();

        return targetDirectory + fileName;
    }

    /**
     * Lưu ảnh sản phẩm
     * Kích thước: 400x400px
     */
    public String saveProductImage(String productId, String imageUrl) throws IOException {
        return saveImageCommon(productId, imageUrl, "images/product/", 400);
    }

    /**
     * Lưu ảnh đại diện nhân viên
     * Kích thước: 275x183px (match default image)
     */
    public String saveEmployeeAvatar(String employeeId, String imageUrl) throws IOException {
        return saveImageCommon(employeeId, imageUrl, "images/avatar/", 300);
    }

}
