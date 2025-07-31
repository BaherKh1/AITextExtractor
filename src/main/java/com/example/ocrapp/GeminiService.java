package com.example.ocrapp;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

@Service
public class GeminiService {

    // Inject the API key from application.properties
    @Value("${gemini.api.key}")
    private String API_KEY;

    public String extractTextFromImage(MultipartFile image) {
        try {
            byte[] imageBytes = image.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String requestJson = """
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": "Extract all text from this image and provide only the text."
                    },
                    {
                      "inlineData": {
                        "mimeType": "%s",
                        "data": "%s"
                      }
                    }
                  ]
                }
              ]
            }
            """.formatted(image.getContentType(), base64Image);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            System.out.println("Gemini response: " + responseBody);

            JSONObject json = new JSONObject(responseBody);
            JSONArray candidates = json.optJSONArray("candidates");

            if (candidates == null || candidates.isEmpty()) {
                JSONObject promptFeedback = json.optJSONObject("promptFeedback");
                if (promptFeedback != null) {
                    return "⚠️ Gemini prompt feedback: " + promptFeedback.toString();
                }
                return "⚠️ Gemini returned no candidates. Full response: " + responseBody;
            }

            JSONObject contentObj = candidates.getJSONObject(0).optJSONObject("content");
            if (contentObj == null) {
                return "⚠️ No 'content' field in Gemini response.";
            }

            JSONArray parts = contentObj.optJSONArray("parts");
            if (parts == null || parts.isEmpty()) {
                return "⚠️ No 'parts' field in content.";
            }

            String extractedText = parts.getJSONObject(0).getString("text");

            return extractedText;

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Error processing image with Gemini: " + e.getMessage();
        }
    }
}