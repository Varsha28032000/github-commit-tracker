package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.example.demo.entity.Author;
import com.example.demo.entity.CommitEntity;
import com.example.demo.repository.AuthorRepository;
import com.example.demo.repository.CommitRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webhook")
public class GitHubWebhookController {

    private final AuthorRepository authorRepo;
    private final CommitRepository commitRepo;

    @Value("${slack.webhook.url}")
    private String webhookUrl;

    @PostMapping("/github")
    public ResponseEntity<String> handleGitHubWebhook(
            @RequestHeader(value = "X-GitHub-Event", required = false) String event,
            @RequestBody Map<String, Object> payload) {

        System.out.println("=== WEBHOOK RECEIVED ===");
        System.out.println("Event type: " + event);
        System.out.println("Payload: " + payload);

       
        if (event == null) {
            event = "push";
        }

        if (!"push".equals(event)) {
            // Ignore non-push events (like ping)
            return ResponseEntity.ok("Ignored non-push event: " + event);
        }

        // Extract pusher safely
        Map<String, Object> pusher = (Map<String, Object>) payload.get("pusher");
        if (pusher == null) {
            pusher = (Map<String, Object>) payload.get("sender"); // fallback
        }
        String authorName = pusher != null ? (String) pusher.get("name") : "Unknown";

        // Save Author
        Author author = new Author();
        author.setName(authorName);
        author = authorRepo.save(author);

        // Extract commits safely
        List<Map<String, Object>> commits = (List<Map<String, Object>>) payload.get("commits");
        if (commits == null) commits = new ArrayList<>();

        StringBuilder message = new StringBuilder();
        message.append(authorName).append(" pushed:\n");

        for (Map<String, Object> commitData : commits) {
            String commitMsg = (String) commitData.get("message");

            CommitEntity commit = new CommitEntity();
            commit.setMessage(commitMsg);
            commit.setAuthor(author);
            commit.setAuthorName(authorName);

            commitRepo.save(commit);

            message.append("- ").append(commitMsg).append("\n");
        }

        // Send Slack notification only if commits exist
        if (!commits.isEmpty()) {
            sendToSlack(message.toString());
        }

        return ResponseEntity.ok("Webhook processed successfully!");
    }

    // 🔥 SLACK METHOD
    private void sendToSlack(String message) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String payload = "{ \"text\": \"" + message + "\" }";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(payload, headers);

            restTemplate.postForObject(webhookUrl, request, String.class);
            System.out.println("✅ Slack notification sent");
        } catch (Exception e) {
            System.err.println("⚠️ Slack send failed: " + e.getMessage());
        }
    }
}