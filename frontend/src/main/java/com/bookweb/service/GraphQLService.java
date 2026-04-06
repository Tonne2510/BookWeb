package com.bookweb.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GraphQLService {

    @Value("${graphql.endpoint:http://localhost:4000/graphql}")
    private String graphqlEndpoint;

    private final Gson gson = new Gson();

    public JsonObject executeQuery(String query, JsonObject variables, String token) throws Exception {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("query", query);
        if (variables != null) {
            requestBody.add("variables", variables);
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(graphqlEndpoint);
            request.setHeader("Content-Type", "application/json");
            
            if (token != null) {
                request.setHeader("Authorization", "Bearer " + token);
            }

            request.setEntity(new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String responseBody = new String(response.getEntity().getContent().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                return gson.fromJson(responseBody, JsonObject.class);
            }
        }
    }

    public JsonObject executeQuery(String query) throws Exception {
        return executeQuery(query, null, null);
    }

    public JsonObject executeQuery(String query, String token) throws Exception {
        return executeQuery(query, null, token);
    }

    public JsonObject executeQuery(String query, JsonObject variables) throws Exception {
        return executeQuery(query, variables, null);
    }
}
