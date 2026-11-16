package com.ctu.lab5.exporter;

/** Concrete Product mới, mô phỏng đọc nội dung CSV. */
class CsvReader implements DocumentReader {
    @Override
    public String readContent(String filePath) {
        return "Content from CSV file: " + filePath;
    }
}
