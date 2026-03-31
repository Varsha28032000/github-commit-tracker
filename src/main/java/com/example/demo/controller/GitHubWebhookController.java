package com.example.demo.controller;

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
	public String handleGitHubWebhook(@RequestBody Map<String, Object> payload) {

		// 🔹 1. Extract author
		Map<String, Object> pusher = (Map<String, Object>) payload.get("pusher");
		String authorName = (String) pusher.get("name");

		Author author = new Author();
		author.setName(authorName);
		author = authorRepo.save(author);

		// 🔹 2. Extract commits
		List<Map<String, Object>> commits = (List<Map<String, Object>>) payload.get("commits");

		StringBuilder message = new StringBuilder();
		message.append(authorName).append(" pushed:\n");

		for (Map<String, Object> commitData : commits) {
			String commitMsg = (String) commitData.get("message");

			CommitEntity commit = new CommitEntity();
			commit.setMessage(commitMsg);
			commit.setAuthor(author);
			commit.setAuthorName(authorName); // 👈 ADD THIS

			commitRepo.save(commit);

			message.append("- ").append(commitMsg).append("\n");
		}
		// 🔹 3. Send to Slack
		sendToSlack(message.toString());

		return "Webhook received!";
	}

	// 🔥 SLACK METHOD
	private void sendToSlack(String message) {

		String webhookUrl = "https://hooks.slack.com/services/XXXXX";

		RestTemplate restTemplate = new RestTemplate();

		String payload = "{ \"text\": \"" + message + "\" }";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> request = new HttpEntity<>(payload, headers);

		restTemplate.postForObject(webhookUrl, request, String.class);
	}
}