package com.ctu.lab5.exporter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentExporterTest {
    private DocumentExporterService exporter;

    @BeforeEach
    void setUp() {
        exporter = new DocumentExporterService();
    }

    @Test
    @DisplayName("Chưa set Strategy nén -> ném IllegalStateException")
    void exportWithoutStrategy_throwsIllegalStateException() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> exporter.exportDocument("report.pdf", "PDF"));
        assertEquals("Chưa thiết lập thuật toán nén!", ex.getMessage());
    }

    @Test
    @DisplayName("CSV + TAR hoạt động qua service gốc")
    void exportCsvWithTar_returnsTarWrappedContent() {
        exporter.setCompressionStrategy(new TarCompressionStrategy());
        byte[] result = exporter.exportDocument("data.csv", "CSV");
        assertEquals("TAR[Content from CSV file: data.csv]",
                new String(result, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Định dạng không hỗ trợ -> IllegalArgumentException")
    void factory_unsupportedType_throws() {
        exporter.setCompressionStrategy(new ZipCompressionStrategy());
        assertThrows(IllegalArgumentException.class,
                () -> exporter.exportDocument("img.png", "PNG"));
    }

    static class RecordingObserver implements ExportObserver {
        final List<String> files = new ArrayList<>();
        final List<Integer> sizes = new ArrayList<>();

        @Override
        public void onExportSuccess(String fileName, int compressedSize) {
            files.add(fileName);
            sizes.add(compressedSize);
        }
    }

    @Test
    @DisplayName("Chưa chọn strategy thì không thông báo thành công")
    void exportWithoutStrategy_doesNotNotifyObservers() {
        RecordingObserver spy = new RecordingObserver();
        exporter.attachObserver(spy);
        assertThrows(IllegalStateException.class,
                () -> exporter.exportDocument("report.pdf", "PDF"));
        assertTrue(spy.files.isEmpty());
        assertTrue(spy.sizes.isEmpty());
    }

    @Test
    @DisplayName("Observer nhận đúng 1 sự kiện với kích thước chính xác")
    void export_notifiesObserverOnce() {
        RecordingObserver spy = new RecordingObserver();
        exporter.attachObserver(spy);
        exporter.setCompressionStrategy(new ZipCompressionStrategy());
        byte[] result = exporter.exportDocument("BaoCao.pdf", "PDF");
        assertEquals(List.of("BaoCao.pdf"), spy.files);
        assertEquals(List.of(result.length), spy.sizes);
    }

    @Test
    @DisplayName("Đổi ZIP sang RAR trên cùng service và đọc DOCX chữ thường")
    void export_switchStrategyAtRuntime() {
        exporter.setCompressionStrategy(new ZipCompressionStrategy());
        assertEquals("ZIP[Content from DOCX file: plan.docx]",
                new String(exporter.exportDocument("plan.docx", "docx"), StandardCharsets.UTF_8));
        exporter.setCompressionStrategy(new RarCompressionStrategy());
        assertEquals("RAR[Content from DOCX file: plan.docx]",
                new String(exporter.exportDocument("plan.docx", "DOCX"), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Factory từ chối loại file null")
    void factory_nullType_throws() {
        assertThrows(IllegalArgumentException.class, () -> DocumentReaderFactory.getReader(null));
    }

    @Test
    @DisplayName("Observer được gọi theo thứ tự đăng ký, không gọi khi đọc thất bại")
    void export_preservesObserverOrderAndDoesNotNotifyOnUnsupportedType() {
        List<String> events = new ArrayList<>();
        exporter.attachObserver((file, size) -> events.add("audit"));
        exporter.attachObserver((file, size) -> events.add("notification"));
        exporter.setCompressionStrategy(new ZipCompressionStrategy());
        assertThrows(IllegalArgumentException.class,
                () -> exporter.exportDocument("image.png", "PNG"));
        assertTrue(events.isEmpty());
        exporter.exportDocument("report.pdf", "PDF");
        assertEquals(List.of("audit", "notification"), events);
    }
}
