package com.email.writer.app;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class EmailGeneratorService {

    private final WebClient webClient;

    @Value("${gemini.api.url}")
    private String geminiApiURL;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public EmailGeneratorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String generateEmailReply( EmailRequest emailrequest){
        // build a prompt
        String prompt = buildPrompt(emailrequest);

        // craft a request
        Map<String , Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text",prompt)
                        })
                }
        );

        // do request and get response
        String response  = webClient.post()
                .uri(geminiApiURL)
                .header("X-goog-api-key", geminiApiKey)
                .header("Content-Type","application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        // Extract response Return response
        return extractResponseContent(response);
    }

    private String extractResponseContent(String response) {
        try{
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);
            return rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

        }catch (Exception e ){
            return "Error Processing request :" +e.getMessage();
        }
    }

    private String buildPrompt(EmailRequest emailrequest) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate professional email reply for the following email content. Don't generate subject line ......just wright mail dont give anything extra ");
        if(emailrequest.getTone() != null && !emailrequest.getTone().isEmpty()){
            prompt.append("Use a ").append(emailrequest.getTone()).append(" tone ");
        }
        prompt.append("\nOriginal email: \n").append(emailrequest.getEmailContent());
        return prompt.toString();
    }
}


































