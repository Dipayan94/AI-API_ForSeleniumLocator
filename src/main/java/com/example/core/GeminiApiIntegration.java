package com.example.core;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.text.StringEscapeUtils;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GeminiApiIntegration {

    // IMPORTANT: Use an up-to-date and supported model ID here.
    private static final String GEMINI_API_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";
    private static final String LOCATORS_DIR = System.getProperty("user.dir")+ "/example/src/test/resources/locators/"; // Subdirectory for locators

    /**
     * Generates a unique and sanitized filename based on page context or a timestamp.
     * @param pageContext The context of the page (title, URL, etc.)
     * @param usePageContextForName If true, tries to use a sanitized page title/URL; otherwise, uses a timestamp.
     * @return A sanitized filename (e.g., "google_home_page_locators.json" or "locators_20250624_153045.json")
     */
    private static String generateUniqueFileName(HtmlCapture.PageContext pageContext, boolean usePageContextForName) {
        String baseName;
        if (usePageContextForName && pageContext.getPageTitle() != null && !pageContext.getPageTitle().trim().isEmpty()) {
            baseName = FilenameUtils.getName(pageContext.getPageTitle());
            // Replace invalid characters with underscores and limit length for clean filename
            baseName = baseName.replaceAll("[^a-zA-Z0-9_.-]", "_");
            if (baseName.length() > 50) { // Limit length to avoid excessively long filenames
                baseName = baseName.substring(0, 50);
            }
            if (baseName.trim().isEmpty()) { // Fallback if sanitization results in empty string
                baseName = "page_locators";
            }
            baseName = baseName.toLowerCase().replace(" ", "_"); // Make it lowercase and snake_case
        } else {
            // Fallback to timestamp if page context name is not desired or invalid
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            baseName = "locators_" + sdf.format(new Date());
        }
        return baseName + ".json";
    }

    public static String generateAndSaveXPathsWithAI(HtmlCapture.PageContext pageContext, String apiKey, boolean usePageContextForName) throws IOException {
        Files.createDirectories(Paths.get(LOCATORS_DIR)); // Ensure the locators directory exists

        String fileName = generateUniqueFileName(pageContext, usePageContextForName);
        String filePath = LOCATORS_DIR + fileName;

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(GEMINI_API_ENDPOINT + apiKey);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        // Escape HTML content to ensure it's valid within a JSON string
        String escapedHtmlContent = StringEscapeUtils.escapeJson(pageContext.getHtmlContent());

        // Crafting a clear and specific prompt for Gemini
        String prompt = "Given the following HTML content, identify common interactive elements " +
                        "(buttons, input fields, links, text areas, checkboxes, radio buttons, dropdowns) " +
                        "and generate a concise, unique, and robust XPath locator for each. " +
                        "For each element, provide a descriptive, camelCase name as the key " +
                        "and its corresponding XPath locator as the value. " +
                        "Prioritize using unique IDs, names, or clear semantic attributes. " +
                        "If a robust XPath isn't possible, indicate 'NOT_FOUND'. " +
                        "Present the output strictly as a JSON object, like this example: " +
                        "{\"loginButton\": \"//button[@id='login']\", \"usernameInput\": \"//input[@name='username']\", \"submitForm\": \"//form[@id='myForm']/button[text()='Submit']\"}\n\n" +
                        "HTML:\n" + escapedHtmlContent; // Use the escaped HTML here

        // Construct the JSON request body as required by the Gemini API
        JsonObject requestBody = new JsonObject();
        JsonObject contents = new JsonObject();
        contents.addProperty("role", "user");
        JsonObject parts = new JsonObject();
        parts.addProperty("text", prompt);
        contents.add("parts", new Gson().toJsonTree(new JsonObject[]{parts}));
        requestBody.add("contents", new Gson().toJsonTree(new JsonObject[]{contents}));

        StringEntity entity = new StringEntity(requestBody.toString());
        httpPost.setEntity(entity);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                String responseString = EntityUtils.toString(responseEntity);
                JsonObject geminiResponse = new Gson().fromJson(responseString, JsonObject.class);

                // Check for API errors first (e.g., 400, 403, 404, 500)
                if (geminiResponse.has("error")) {
                    System.err.println("Gemini API Error: " + geminiResponse.getAsJsonObject("error").toString());
                    throw new IOException("Gemini API returned an error: " + geminiResponse.getAsJsonObject("error").get("message").getAsString());
                }

                if (geminiResponse.has("candidates") && geminiResponse.getAsJsonArray("candidates").size() > 0) {
                    JsonObject candidate = geminiResponse.getAsJsonArray("candidates").get(0).getAsJsonObject();
                    if (candidate.has("content") && candidate.getAsJsonObject("content").has("parts")) {
                        String generatedText = candidate.getAsJsonObject("content").getAsJsonArray("parts").get(0).getAsJsonObject().get("text").getAsString();
                        
                        // Extracting the JSON part from the AI's response
                        int startIndex = generatedText.indexOf("{");
                        int endIndex = generatedText.lastIndexOf("}");
                        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                            String jsonLocators = generatedText.substring(startIndex, endIndex + 1);
                            
                            // Save the extracted JSON to a file
                            try (FileWriter file = new FileWriter(filePath)) {
                                Gson gson = new GsonBuilder().setPrettyPrinting().create(); // Pretty print for readability
                                JsonObject jsonObject = gson.fromJson(jsonLocators, JsonObject.class);
                                file.write(gson.toJson(jsonObject));
                                System.out.println("Generated locators successfully saved to: " + filePath);
                                return filePath; // Return the path of the saved file
                            } catch (IOException e) {
                                System.err.println("Error saving locators to file " + filePath + ": " + e.getMessage());
                                throw e; // Re-throw to indicate failure
                            }
                        }
                    }
                }
                System.err.println("Unexpected Gemini response format or no usable locators found: " + responseString);
                // Optionally, save an empty or error JSON file if no locators are found
                try (FileWriter file = new FileWriter(filePath)) {
                    file.write("{}"); // Save an empty JSON object
                    System.out.println("No locators generated. An empty JSON file was created at: " + filePath);
                } catch (IOException e) {
                    System.err.println("Error creating empty locators file: " + e.getMessage());
                }
                throw new IOException("Failed to extract valid JSON locators from Gemini response.");

            } else {
                System.err.println("Gemini API response entity was null or empty.");
                throw new IOException("Empty response from Gemini API.");
            }
        } finally {
            httpClient.close();
        }
    }
}