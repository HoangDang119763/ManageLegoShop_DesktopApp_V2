package SERVICE;

import BUS.InvoiceBUS;
import DTO.BUSResult;
import ENUM.BUSOperationResult;
import DTO.DetailInvoicePDFDTO;
import DTO.InvoicePDFDTO;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.File;
import java.io.FileOutputStream;
import java.awt.Desktop;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PrintService {
    private static final PrintService INSTANCE = new PrintService();
    private static final String FONT_PATH = "src/main/resources/fonts/arial-unicode-ms.ttf";
    private static final BaseColor PRIMARY_COLOR = new BaseColor(31, 125, 132); // Màu #1f7d84

    private PrintService() {
    }

    public static PrintService getInstance() {
        return INSTANCE;
    }

    public BUSResult printInvoiceForm(int invoiceId) {
        try {
            InvoiceBUS invBus = InvoiceBUS.getInstance();
            InvoicePDFDTO invoice = invBus.getInvoiceForPDF(invoiceId);

            if (invoice == null) {
                return new BUSResult(BUSOperationResult.FAIL, "Không tìm thấy hóa đơn hoặc chưa hoàn thành!");
            }

            String folderName = "pdf";
            File folder = new File(folderName);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"));
            String filePath = folderName + File.separator + "invoice_" + "ID" + invoice.getId() + "_" + timestamp
                    + ".pdf";

            createInvoicePDF(invoice, filePath);

            return new BUSResult(BUSOperationResult.SUCCESS, "Xuất PDF thành công!");
        } catch (Exception e) {
            System.err.println("Lỗi khi in hóa đơn: " + e.getMessage());
            e.printStackTrace();
            return new BUSResult(BUSOperationResult.FAIL, "Lỗi khi in hóa đơn: " + e.getMessage());
        }
    }

    private void createInvoicePDF(InvoicePDFDTO invoice, String filePath) {
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // 1. Khởi tạo Font
            BaseFont baseFont = BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font titleFont = new Font(baseFont, 26, Font.BOLD, PRIMARY_COLOR);
            Font headerFont = new Font(baseFont, 12, Font.BOLD, BaseColor.WHITE);
            Font boldFont = new Font(baseFont, 12, Font.BOLD);
            Font normalFont = new Font(baseFont, 12, Font.NORMAL);
            Font labelFont = new Font(baseFont, 12, Font.BOLD, new BaseColor(51, 51, 51));

            // ========== HEADER SECTION ==========
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[] { 70, 30 });

            PdfPCell titleCell = new PdfPCell(new Phrase("HÓA ĐƠN BÁN HÀNG", titleFont));
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            titleCell.setPaddingBottom(10f);
            headerTable.addCell(titleCell);

            PdfPCell invoiceIdCell = new PdfPCell(new Phrase("Mã số: #" + invoice.getId(), boldFont));
            invoiceIdCell.setBorder(Rectangle.NO_BORDER);
            invoiceIdCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            invoiceIdCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            invoiceIdCell.setPaddingBottom(10f);
            headerTable.addCell(invoiceIdCell);
            document.add(headerTable);

            // Đường kẻ ngang màu chủ đạo
            PdfPTable line = new PdfPTable(1);
            line.setWidthPercentage(100);
            PdfPCell lineCell = new PdfPCell();
            lineCell.setBorder(Rectangle.BOTTOM);
            lineCell.setBorderColorBottom(PRIMARY_COLOR);
            lineCell.setBorderWidthBottom(2.5f);
            lineCell.setPaddingTop(30f);
            line.addCell(lineCell);
            document.add(line);

            document.add(new Paragraph("\n"));

            // ========== INFO SECTION ==========
            PdfPTable infoTable = new PdfPTable(4);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[] { 18, 32, 20, 30 });

            addInfoCell(infoTable, "Khách hàng:", labelFont);
            addInfoCell(infoTable, invoice.getCustomerName(), normalFont);
            addInfoCell(infoTable, "Người lập:", labelFont);
            addInfoCell(infoTable, invoice.getEmployeeName(), normalFont);

            addInfoCell(infoTable, "Ngày tạo:", labelFont);
            addInfoCell(infoTable, invoice.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                    normalFont);

            String discountStr = (invoice.getDiscountCode() != null)
                    ? invoice.getDiscountCode() + " (-" + formatCurrency(invoice.getDiscountAmount()) + ")"
                    : "Không có";
            addInfoCell(infoTable, "Khuyến mãi:", labelFont);
            addInfoCell(infoTable, discountStr, normalFont);

            document.add(infoTable);
            document.add(new Paragraph("\n"));

            // ========== DATA TABLE SECTION ==========
            PdfPTable dataTable = new PdfPTable(5);
            dataTable.setWidthPercentage(100);
            // STT: 8%, Tên: 42%, SL: 10%, Đơn giá: 20%, Thành tiền: 20%
            dataTable.setWidths(new float[] { 8, 42, 10, 20, 20 });

            String[] headers = { "STT", "Sản phẩm", "SL", "Đơn giá", "Thành tiền" };
            for (String h : headers) {
                PdfPCell hCell = new PdfPCell(new Phrase(h, headerFont));
                hCell.setBackgroundColor(PRIMARY_COLOR);
                hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                hCell.setPadding(8f);
                hCell.setBorder(Rectangle.NO_BORDER);
                dataTable.addCell(hCell);
            }

            int index = 1;
            for (DetailInvoicePDFDTO d : invoice.getDetails()) {
                addDataCell(dataTable, String.valueOf(index++), normalFont, Element.ALIGN_CENTER);
                addDataCell(dataTable, d.getProductName(), normalFont, Element.ALIGN_LEFT);
                addDataCell(dataTable, String.valueOf(d.getQuantity()), normalFont, Element.ALIGN_CENTER);
                addDataCell(dataTable, formatCurrency(d.getPrice()), normalFont, Element.ALIGN_RIGHT);
                addDataCell(dataTable, formatCurrency(d.getTotalPrice()), normalFont, Element.ALIGN_RIGHT);
            }
            document.add(dataTable);

            // ========== SUMMARY SECTION ==========
            document.add(new Paragraph("\n"));
            PdfPTable footerWrapper = new PdfPTable(2);
            footerWrapper.setWidthPercentage(100);
            footerWrapper.setWidths(new float[] { 60, 40 });

            PdfPCell noteCell = new PdfPCell(
                    new Phrase("Cảm ơn quý khách đã mua sắm!", new Font(baseFont, 11, Font.ITALIC, BaseColor.GRAY)));
            noteCell.setBorder(Rectangle.NO_BORDER);
            footerWrapper.addCell(noteCell);

            PdfPTable summaryTable = new PdfPTable(2);
            addSummaryRow(summaryTable, "Tổng cộng:", formatCurrency(invoice.getTotalPrice()), normalFont, false);
            addSummaryRow(summaryTable, "Giảm giá:", "-" + formatCurrency(invoice.getDiscountAmount()), normalFont,
                    false);

            BigDecimal finalAmt = invoice.getTotalPrice().subtract(invoice.getDiscountAmount()).max(BigDecimal.ZERO);
            addSummaryRow(summaryTable, "Thành tiền:", formatCurrency(finalAmt),
                    new Font(baseFont, 16, Font.BOLD, PRIMARY_COLOR), true);

            PdfPCell summaryCell = new PdfPCell(summaryTable);
            summaryCell.setBorder(Rectangle.NO_BORDER);
            footerWrapper.addCell(summaryCell);

            document.add(footerWrapper);

            document.close();

            // Mở file tự động
            File pdfFile = new File(filePath);
            if (pdfFile.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
            }

        } catch (Exception e) {
            System.err.println("Lỗi xử lý PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addInfoCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingBottom(8f);
        table.addCell(cell);
    }

    private void addDataCell(PdfPTable table, String text, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(align);
        cell.setPadding(8f);
        cell.setBorder(Rectangle.BOTTOM); // Chỉ kẻ dòng dưới cho hiện đại
        cell.setBorderColor(new BaseColor(230, 230, 230));
        table.addCell(cell);
    }

    private void addSummaryRow(PdfPTable table, String label, String value, Font valFont, boolean isTotal) {
        PdfPCell lCell = new PdfPCell(new Phrase(label, isTotal ? new Font(valFont.getBaseFont(), 12, Font.BOLD)
                : new Font(valFont.getBaseFont(), 12, Font.NORMAL)));
        lCell.setBorder(Rectangle.NO_BORDER);
        lCell.setPadding(5f);
        table.addCell(lCell);

        PdfPCell vCell = new PdfPCell(new Phrase(value, valFont));
        vCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        vCell.setBorder(Rectangle.NO_BORDER);
        vCell.setPadding(5f);
        table.addCell(vCell);
    }

    private String formatCurrency(BigDecimal amount) {
        return (amount == null) ? "0đ" : String.format("%,.0f", amount.doubleValue()) + "đ";
    }
}