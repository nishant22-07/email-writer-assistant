package com.email.writer.app;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Objects;

@Service
public class EmailGeneratorService {

    private final WebClient webClient;

    @Value("${gemini.api.url}")
    private String geminiApiURL;

    @Value("${gemini.api.api}")
    private String geminiApiKey;

    public EmailGeneratorService(WebClient webClient) {
        this.webClient = webClient;
    }

    public String generateEmailReply( Emailrequest emailrequest){
        // build a prompt
        String prompt = buildPrompt(emailrequest);

        // craft a request
        Map<String , Object> requestBody = Map.of(
                "Contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("texts",prompt)
                        })
                }
        );

        // do request and get response
        String response  = webClient.post()
                .uri(geminiApiURL + geminiApiKey )
                .header("Content-Type","Application/json")
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

    private String buildPrompt(Emailrequest emailrequest) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate professional email reply for the following email content. Don't generate subject line ");
        if(emailrequest.getTone() != null && !emailrequest.getTone().isEmpty()){
            prompt.append("Use a ").append(emailrequest.getTone()).append(" tone ");
        }
        prompt.append("\nOriginal email: \n").append(emailrequest.getEmailContent());
        return prompt.toString();
    }
}


































