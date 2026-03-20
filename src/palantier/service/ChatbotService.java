package palantier.service;

import palantier.model.Task;
import palantier.model.User;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * ChatbotService — Handles AI chatbot logic.
 * Integrates with an LLM via REST API (e.g., OpenAI).
 */
public class ChatbotService {

    // Default to Groq compatible endpoint for chat completions
    private static final String API_ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL_NAME = "llama-3.1-8b-instant";

    public ChatbotService() {
    }

    /**
     * Gets a response from the AI chatbot based on the user's prompt and current
     * context.
     *
     * @param prompt      The user's chat message
     * @param apiKey      The API key (entered by the user)
     * @param currentUser The logged-in user
     * @param taskManager The TaskManager to pull task data from
     * @return The AI's response text
     */
    public String getChatbotResponse(String prompt, String apiKey, User currentUser, TaskManager taskManager) {

        if (apiKey == null || apiKey.trim().isEmpty()) {
            String env = System.getenv("GROQ_API_KEY");
            apiKey = (env != null && !env.isEmpty()) ? env : "";
        }

        if (apiKey.equals("YOUR_API_KEY_HERE") || apiKey == null || apiKey.trim().isEmpty()) {
            return generateLocalFallbackResponse(prompt, currentUser, taskManager);
        }

        try {
            String systemPrompt = buildSystemPrompt(currentUser, taskManager);

            // Build the JSON payload manually to avoid extra dependencies
            String jsonPayload = String.format(
                    "{\"model\": \"%s\", \"messages\": [{\"role\": \"system\", \"content\": \"%s\"}, {\"role\": \"user\", \"content\": \"%s\"}]}",
                    MODEL_NAME, escapeJson(systemPrompt), escapeJson(prompt));

            URL url = new URL(API_ENDPOINT);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey.trim());
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }
                return extractContentFromJson(response.toString());
            } else {
                StringBuilder errorResponse = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    String errorLine;
                    while ((errorLine = br.readLine()) != null) {
                        errorResponse.append(errorLine.trim());
                    }
                }
                return "API Error (" + responseCode + "): " + errorResponse.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to communicate with AI: " + e.getMessage();
        }
    }

    /**
     * Builds the system context with tasks.
     */
    private String buildSystemPrompt(User currentUser, TaskManager taskManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a helpful task management assistant for the user '").append(currentUser.getFullName())
                .append("'.\n");
        sb.append("Here is the current task data:\n");

        ArrayList<Task> assignedTasks = taskManager.getFilteredTasks(null, null, currentUser.getEmail());
        sb.append("Tasks assigned to me:\n");
        if (assignedTasks.isEmpty()) {
            sb.append("- None\n");
        } else {
            for (Task t : assignedTasks) {
                sb.append("- [").append(t.getStatus()).append("] ").append(t.getTitle())
                        .append(" (Priority: ").append(t.getPriority()).append(", Due: ")
                        .append(t.getFormattedDueDate()).append(")\n");
            }
        }

        ArrayList<Task> allTasks = taskManager.getAllTasks();
        sb.append("\nAll Pending Tasks (not assigned to me):\n");
        boolean hasOtherPending = false;
        for (Task t : allTasks) {
            if (!Task.STATUS_DONE.equals(t.getStatus())
                    && (t.getAssigneeEmail() == null || !t.getAssigneeEmail().equals(currentUser.getEmail()))) {
                sb.append("- [").append(t.getStatus()).append("] ").append(t.getTitle())
                        .append(" (Priority: ").append(t.getPriority()).append(", Due: ")
                        .append(t.getFormattedDueDate())
                        .append(", Assignee: ").append(t.isAssigned() ? t.getAssigneeName() : "Unassigned")
                        .append(")\n");
                hasOtherPending = true;
            }
        }
        if (!hasOtherPending) {
            sb.append("- None\n");
        }

        sb.append(
                "\nPlease answer the user's questions based on this data. Be concise, friendly, and helpful. If asked what to do first, recommend based on Priority (High first) and Due Dates (earliest first).");
        return sb.toString();
    }

    /**
     * Very simple fallback if no API key is provided.
     */
    private String generateLocalFallbackResponse(String prompt, User currentUser, TaskManager taskManager) {
        String p = prompt.toLowerCase();

        if (p.contains("assigned to me") || p.contains("my task")) {
            ArrayList<Task> tasks = taskManager.getFilteredTasks(null, null, currentUser.getEmail());
            if (tasks.isEmpty())
                return "You have no tasks assigned to you right now. (Configure an API Key for full AI capabilities!)";
            StringBuilder sb = new StringBuilder("Here are your assigned tasks:\n");
            for (Task t : tasks) {
                sb.append("- ").append(t.getTitle()).append("\n");
            }
            sb.append("\n(Configure an API Key for full AI capabilities!)");
            return sb.toString();
        }

        if (p.contains("due") || p.contains("deadline")) {
            int overdue = taskManager.getOverdueCount();
            return "You have " + overdue
                    + " overdue tasks across the project. To get a detailed breakdown of due dates, please configure your API Key so the AI can analyze your tasks.";
        }

        if (p.contains("first") || p.contains("priority")) {
            return "To get AI-driven recommendations on what to prioritize based on deadlines and high priority, please configure your API Key. Otherwise, just sort the dashboard by 'High' priority!";
        }

        return "I am running in local fallback mode because no API Key was provided. Please enter your API Key at the bottom to enable AI features! Try asking about your assigned tasks.";
    }

    /**
     * Very basic manual JSON string escape to avoid external dependencies.
     */
    private String escapeJson(String text) {
        if (text == null)
            return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Basic manual JSON content extraction for the OpenAI format response.
     * Looks for "content":"..." and extracts the value.
     */
    private String extractContentFromJson(String json) {
        String searchStr = "\"content\": \"";
        int start = json.indexOf(searchStr);
        if (start == -1) {
            // Alternative without space
            searchStr = "\"content\":\"";
            start = json.indexOf(searchStr);
            if (start == -1)
                return "Could not parse AI response.";
        }

        start += searchStr.length();

        // Find the unescaped closing quote
        int end = -1;
        for (int i = start; i < json.length(); i++) {
            if (json.charAt(i) == '\"' && json.charAt(i - 1) != '\\') {
                end = i;
                break;
            }
        }

        if (end == -1)
            return "Could not parse AI response.";

        String extracted = json.substring(start, end);
        return unescapeJson(extracted);
    }

    private String unescapeJson(String text) {
        return text.replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\t", "\t")
                .replace("\\r", "\r")
                .replace("\\b", "\b")
                .replace("\\f", "\f");
    }
}
