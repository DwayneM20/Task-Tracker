import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TaskTracker {
    private static final String TASKS_FILE = "tasks.json";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        try {
            String command = args[0].toLowerCase();

            switch (command) {
                case "add":
                    handleAdd(args);
                    break;
                case "update":
                    handleUpdate(args);
                    break;
                case "delete":
                    handleDelete(args);
                    break;
                case "mark-in-progress":
                    handleMarkInProgress(args);
                    break;
                case "mark-done":
                    handleMarkDone(args);
                    break;
                case "list":
                    handleList(args);
                    break;
                default:
                    System.err.println("Unknown command: " + command);
                    printUsage();
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void handleAdd(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: java TaskTracker add \"Task description\"");
            return;
        }

        String description = args[1];
        if (description.trim().isEmpty()) {
            System.err.println("Task description cannot be empty");
            return;
        }

        List<Task> tasks = loadTasks();
        int newId = getNextId(tasks);

        Task newTask = new Task(newId, description, "todo", getCurrentTimestamp(), getCurrentTimestamp());
        tasks.add(newTask);

        saveTasks(tasks);
        System.out.println("Task added successfully (ID: " + newId + ")");
    }

    private static void handleUpdate(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("Usage: java TaskTracker update <id> \"New description\"");
            return;
        }

        try {
            int id = Integer.parseInt(args[1]);
            String newDescription = args[2];

            if (newDescription.trim().isEmpty()) {
                System.err.println("Task description cannot be empty");
                return;
            }

            List<Task> tasks = loadTasks();
            boolean found = false;

            for (Task task : tasks) {
                if (task.getId() == id) {
                    task.setDescription(newDescription);
                    task.setUpdatedAt(getCurrentTimestamp());
                    found = true;
                    break;
                }
            }

            if (!found) {
                System.err.println("Task with ID " + id + " not found");
                return;
            }

            saveTasks(tasks);
            System.out.println("Task updated successfully");

        } catch (NumberFormatException e) {
            System.err.println("Invalid task ID. Please provide a valid number.");
        }
    }

    private static void handleDelete(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: java TaskTracker delete <id>");
            return;
        }

        try {
            int id = Integer.parseInt(args[1]);
            List<Task> tasks = loadTasks();
            boolean removed = tasks.removeIf(task -> task.getId() == id);

            if (!removed) {
                System.err.println("Task with ID " + id + " not found");
                return;
            }

            saveTasks(tasks);
            System.out.println("Task deleted successfully");

        } catch (NumberFormatException e) {
            System.err.println("Invalid task ID. Please provide a valid number.");
        }
    }

    private static void handleMarkInProgress(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: java TaskTracker mark-in-progress <id>");
            return;
        }

        markTaskStatus(args[1], "in-progress");
    }

    private static void handleMarkDone(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: java TaskTracker mark-done <id>");
            return;
        }

        markTaskStatus(args[1], "done");
    }

    private static void markTaskStatus(String idStr, String status) throws IOException {
        try {
            int id = Integer.parseInt(idStr);
            List<Task> tasks = loadTasks();
            boolean found = false;

            for (Task task : tasks) {
                if (task.getId() == id) {
                    task.setStatus(status);
                    task.setUpdatedAt(getCurrentTimestamp());
                    found = true;
                    break;
                }
            }

            if (!found) {
                System.err.println("Task with ID " + id + " not found");
                return;
            }

            saveTasks(tasks);
            System.out.println("Task marked as " + status);

        } catch (NumberFormatException e) {
            System.err.println("Invalid task ID. Please provide a valid number.");
        }
    }

    private static void handleList(String[] args) throws IOException {
        String filter = "all";
        if (args.length > 1) {
            filter = args[1].toLowerCase();
        }

        List<Task> tasks = loadTasks();
        List<Task> filteredTasks = new ArrayList<>();

        switch (filter) {
            case "done":
                filteredTasks = tasks.stream()
                        .filter(task -> "done".equals(task.getStatus()))
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                break;
            case "todo":
                filteredTasks = tasks.stream()
                        .filter(task -> "todo".equals(task.getStatus()))
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                break;
            case "in-progress":
                filteredTasks = tasks.stream()
                        .filter(task -> "in-progress".equals(task.getStatus()))
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                break;
            case "all":
            default:
                filteredTasks = tasks;
                break;
        }

        if (filteredTasks.isEmpty()) {
            System.out.println("No tasks found" + (filter.equals("all") ? "" : " with status: " + filter));
            return;
        }

        System.out.println("Tasks" + (filter.equals("all") ? "" : " (" + filter + ")") + ":");
        System.out.println("ID\tStatus\t\tDescription\t\tCreated\t\t\tUpdated");
        System.out.println("─".repeat(100));

        for (Task task : filteredTasks) {
            System.out.printf("%d\t%-12s\t%-20s\t%s\t%s%n",
                    task.getId(),
                    task.getStatus(),
                    truncateString(task.getDescription(), 20),
                    task.getCreatedAt(),
                    task.getUpdatedAt()
            );
        }
    }

    private static String truncateString(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    private static List<Task> loadTasks() throws IOException {
        Path filePath = Paths.get(TASKS_FILE);

        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try {
            String content = Files.readString(filePath);
            if (content.trim().isEmpty()) {
                return new ArrayList<>();
            }

            return parseTasksFromJson(content);
        } catch (IOException e) {
            throw new IOException("Error reading tasks file: " + e.getMessage());
        }
    }

    private static void saveTasks(List<Task> tasks) throws IOException {
        String json = tasksToJson(tasks);

        try {
            Files.writeString(Paths.get(TASKS_FILE), json);
        } catch (IOException e) {
            throw new IOException("Error saving tasks: " + e.getMessage());
        }
    }

    private static List<Task> parseTasksFromJson(String json) {
        List<Task> tasks = new ArrayList<>();

        // Remove outer brackets and whitespace
        json = json.trim();
        if (json.startsWith("[")) {
            json = json.substring(1);
        }
        if (json.endsWith("]")) {
            json = json.substring(0, json.length() - 1);
        }

        if (json.trim().isEmpty()) {
            return tasks;
        }

        // Split by task objects (looking for },{ pattern)
        String[] taskStrings = json.split("(?<=\\}),\\s*(?=\\{)");

        for (String taskString : taskStrings) {
            taskString = taskString.trim();
            if (taskString.isEmpty()) continue;

            // Ensure the task string is properly wrapped with braces
            if (!taskString.startsWith("{")) {
                taskString = "{" + taskString;
            }
            if (!taskString.endsWith("}")) {
                taskString = taskString + "}";
            }

            try {
                Task task = parseTaskFromJson(taskString);
                if (task != null) {
                    tasks.add(task);
                }
            } catch (Exception e) {
                System.err.println("Warning: Could not parse task: " + taskString);
            }
        }

        return tasks;
    }

    private static Task parseTaskFromJson(String json) {
        try {
            // Simple JSON parsing without external libraries
            Map<String, String> values = new HashMap<>();

            // Remove braces
            json = json.trim();
            if (json.startsWith("{")) json = json.substring(1);
            if (json.endsWith("}")) json = json.substring(0, json.length() - 1);

            // Split by commas, but be careful with commas inside quoted strings
            List<String> pairs = splitJsonPairs(json);

            for (String pair : pairs) {
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().replace("\"", "");
                    String value = keyValue[1].trim().replace("\"", "");
                    values.put(key, value);
                }
            }

            int id = Integer.parseInt(values.get("id"));
            String description = values.get("description");
            String status = values.get("status");
            String createdAt = values.get("createdAt");
            String updatedAt = values.get("updatedAt");

            return new Task(id, description, status, createdAt, updatedAt);

        } catch (Exception e) {
            return null;
        }
    }

    private static List<String> splitJsonPairs(String json) {
        List<String> pairs = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        int braceCount = 0;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            } else if (c == '{' && !inQuotes) {
                braceCount++;
            } else if (c == '}' && !inQuotes) {
                braceCount--;
            } else if (c == ',' && !inQuotes && braceCount == 0) {
                pairs.add(current.toString().trim());
                current = new StringBuilder();
                continue;
            }

            current.append(c);
        }

        if (current.length() > 0) {
            pairs.add(current.toString().trim());
        }

        return pairs;
    }

    private static String tasksToJson(List<Task> tasks) {
        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            json.append("  {\n");
            json.append("    \"id\": ").append(task.getId()).append(",\n");
            json.append("    \"description\": \"").append(escapeJson(task.getDescription())).append("\",\n");
            json.append("    \"status\": \"").append(task.getStatus()).append("\",\n");
            json.append("    \"createdAt\": \"").append(task.getCreatedAt()).append("\",\n");
            json.append("    \"updatedAt\": \"").append(task.getUpdatedAt()).append("\"\n");
            json.append("  }");

            if (i < tasks.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("]");
        return json.toString();
    }

    private static String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static int getNextId(List<Task> tasks) {
        return tasks.stream()
                .mapToInt(Task::getId)
                .max()
                .orElse(0) + 1;
    }

    private static String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    private static void printUsage() {
        System.out.println("Task Tracker CLI");
        System.out.println("Usage:");
        System.out.println("  java TaskTracker add \"Task description\"");
        System.out.println("  java TaskTracker update <id> \"New description\"");
        System.out.println("  java TaskTracker delete <id>");
        System.out.println("  java TaskTracker mark-in-progress <id>");
        System.out.println("  java TaskTracker mark-done <id>");
        System.out.println("  java TaskTracker list [all|todo|in-progress|done]");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java TaskTracker add \"Buy groceries\"");
        System.out.println("  java TaskTracker list");
        System.out.println("  java TaskTracker list done");
        System.out.println("  java TaskTracker mark-in-progress 1");
        System.out.println("  java TaskTracker mark-done 1");
        System.out.println("  java TaskTracker update 1 \"Buy groceries and cook dinner\"");
        System.out.println("  java TaskTracker delete 1");
    }
}