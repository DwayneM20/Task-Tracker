public class Task {
    private int id;
    private String description;
    private String status;
    private String priority;
    private String createdAt;
    private String updatedAt;

    // ANSI Color codes
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String RESET = "\u001B[0m";

    public Task(int id, String description, String status, String priority, String createdAt, String updatedAt) {
        this.id = id;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Constructor for backward compatibility (default priority to "medium")
    public Task(int id, String description, String status, String createdAt, String updatedAt) {
        this(id, description, status, "medium", createdAt, updatedAt);
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getPriority() {
        return priority;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getPriorityValue() {
        return switch (priority.toLowerCase()) {
            case "high" -> 3;
            case "medium" -> 2;
            case "low" -> 1;
            default -> 2; // default to medium
        };
    }

    public String getPriorityDisplay() {
        return switch (priority.toLowerCase()) {
            case "high" -> RED + "HIGH" + RESET;
            case "medium" -> YELLOW + "MED" + RESET;
            case "low" -> GREEN + "LOW" + RESET;
            default -> YELLOW + "MED" + RESET;
        };
    }

    @Override
    public String toString() {
        return String.format("Task{id=%d, description='%s', status='%s', priority='%s', createdAt='%s', updatedAt='%s'}",
                id, description, status, priority, createdAt, updatedAt);
    }
}