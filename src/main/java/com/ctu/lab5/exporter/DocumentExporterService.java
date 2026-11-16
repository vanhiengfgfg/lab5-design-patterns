package com.ctu.lab5.exporter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

// Factory: cung cấp bộ đọc theo định dạng tài liệu.
interface DocumentReader {
    String readContent(String filePath);
}

class PDFReader implements DocumentReader {
    @Override
    public String readContent(String filePath) {
        return "Content from PDF file: " + filePath;
    }
}

class DocxReader implements DocumentReader {
    @Override
    public String readContent(String filePath) {
        return "Content from DOCX file: " + filePath;
    }
}

class DocumentReaderFactory {
    public static DocumentReader getReader(String fileType) {
        if (fileType == null) throw new IllegalArgumentException("File type null");
        return switch (fileType.toUpperCase(java.util.Locale.ROOT)) {
            case "PDF" -> new PDFReader();
            case "DOCX" -> new DocxReader();
            case "CSV" -> new CsvReader();
            default -> throw new IllegalArgumentException("Unsupported file type: " + fileType);
        };
    }
}

// Strategy: thay đổi cách nén khi chương trình đang chạy.
interface CompressionStrategy {
    byte[] compress(String rawData);
}

class ZipCompressionStrategy implements CompressionStrategy {
    @Override
    public byte[] compress(String rawData) {
        System.out.println("Nén dữ liệu theo chuẩn ZIP...");
        return ("ZIP[" + rawData + "]").getBytes(StandardCharsets.UTF_8);
    }
}

class RarCompressionStrategy implements CompressionStrategy {
    @Override
    public byte[] compress(String rawData) {
        System.out.println("Nén dữ liệu theo chuẩn RAR...");
        return ("RAR[" + rawData + "]").getBytes(StandardCharsets.UTF_8);
    }
}

// Observer: nhận thông báo khi xuất tài liệu thành công.
interface ExportObserver {
    void onExportSuccess(String fileName, int compressedSize);
}

class AuditLogObserver implements ExportObserver {
    @Override
    public void onExportSuccess(String fileName, int compressedSize) {
        System.out.println("[Audit Log] Đã ghi nhật ký xuất file: " + fileName
                + " (Kích thước: " + compressedSize + " bytes)");
    }
}

class ConsoleNotificationObserver implements ExportObserver {
    @Override
    public void onExportSuccess(String fileName, int compressedSize) {
        System.out.println("[Thông báo] Xuất báo cáo thành công file " + fileName);
    }
}

public class DocumentExporterService {
    private CompressionStrategy compressionStrategy;
    private final List<ExportObserver> observers = new ArrayList<>();

    public void setCompressionStrategy(CompressionStrategy strategy) {
        this.compressionStrategy = strategy;
    }

    public void attachObserver(ExportObserver observer) {
        observers.add(observer);
    }

    public byte[] exportDocument(String filePath, String fileType) {
        if (compressionStrategy == null) {
            throw new IllegalStateException("Chưa thiết lập thuật toán nén!");
        }
        // 1. Dùng Factory Method đọc nội dung
        DocumentReader reader = DocumentReaderFactory.getReader(fileType);
        String rawContent = reader.readContent(filePath);
        // 2. Dùng Strategy Pattern nén dữ liệu
        byte[] compressedData = compressionStrategy.compress(rawContent);
        // 3. Dùng Observer Pattern phát sự kiện
        for (ExportObserver obs : observers) {
            obs.onExportSuccess(filePath, compressedData.length);
        }
        return compressedData;
    }

    public static void main(String[] args) {
        DocumentExporterService exporter = new DocumentExporterService();
        exporter.attachObserver(new AuditLogObserver());
        exporter.attachObserver(new ConsoleNotificationObserver());
        exporter.setCompressionStrategy(new ZipCompressionStrategy());
        System.out.println("Đang tiến hành xuất file PDF...");
        exporter.exportDocument("BaoCaoTaiChinh2026.pdf", "PDF");

        exporter.setCompressionStrategy(new TarCompressionStrategy());
        System.out.println("Đang tiến hành xuất file CSV...");
        exporter.exportDocument("DanhSachSinhVien.csv", "CSV");
    }
}
