package DTO;

import java.time.LocalDateTime;

public class FileDTO {
    private int id;
    private String filePath;
    private String fileName;
    private LocalDateTime createdAt;

    public FileDTO() {
    }

    public FileDTO(int id, String filePath, String fileName, LocalDateTime createdAt) {
        this.id = id;
        this.filePath = filePath;
        this.fileName = fileName;
        this.createdAt = createdAt;
    }

    public FileDTO(FileDTO other) {
        this.id = other.id;
        this.filePath = other.filePath;
        this.fileName = other.fileName;
        this.createdAt = other.createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "FileDTO{" +
                "id=" + id +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
