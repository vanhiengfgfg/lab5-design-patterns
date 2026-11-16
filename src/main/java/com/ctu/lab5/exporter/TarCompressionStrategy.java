package com.ctu.lab5.exporter;

import java.nio.charset.StandardCharsets;

/** Concrete Strategy mới, mô phỏng nén theo chuẩn TAR. */
class TarCompressionStrategy implements CompressionStrategy {
    @Override
    public byte[] compress(String rawData) {
        System.out.println("Nén dữ liệu theo chuẩn TAR...");
        return ("TAR[" + rawData + "]").getBytes(StandardCharsets.UTF_8);
    }
}
