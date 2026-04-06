package com.bookweb.service;

import com.bookweb.model.AuthorDTO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthorService {

    @Autowired
    private GraphQLService graphQLService;

    private final Gson gson = new Gson();

    public List<AuthorDTO> getAllAuthors(int page, int limit) throws Exception {
        return getAllAuthors(page, limit, null);
    }

    public List<AuthorDTO> getAllAuthors(int page, int limit, String status) throws Exception {
        String query = """
            query GetAuthors($page: Int, $limit: Int, $status: BookStatus) {
              authors(page: $page, limit: $limit, status: $status) {
                authors {
                  id
                  name
                  slug
                  bio
                  imageUrl
                  nationality
                  dateOfBirth
                  status
                  createdAt
                }
                total
                page
                pages
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("page", page);
        variables.addProperty("limit", limit);
        if (status != null && !status.isEmpty()) variables.addProperty("status", status);

        JsonObject response = graphQLService.executeQuery(query, variables);
        
        // Handle GraphQL errors
        if (response.has("errors") && !response.get("errors").isJsonNull()) {
            var errors = response.getAsJsonArray("errors");
            if (errors.size() > 0) {
                String errorMessage = errors.get(0).getAsJsonObject().get("message").getAsString();
                throw new Exception("GraphQL Error: " + errorMessage);
            }
        }
        
        if (!response.has("data") || response.get("data").isJsonNull()) {
            throw new Exception("No data returned from GraphQL");
        }
        
        JsonArray authors = response.getAsJsonObject("data").getAsJsonObject("authors").getAsJsonArray("authors");

        List<AuthorDTO> authorList = new ArrayList<>();
        authors.forEach(author -> {
            authorList.add(gson.fromJson(author, AuthorDTO.class));
        });

        return authorList;
    }

    public AuthorDTO getAuthorById(String id) throws Exception {
        String query = """
            query GetAuthor($id: ID!) {
              author(id: $id) {
                id
                name
                slug
                bio
                imageUrl
                nationality
                dateOfBirth
                status
                createdAt
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("id", id);

        JsonObject response = graphQLService.executeQuery(query, variables);
        
        // Handle GraphQL errors
        if (response.has("errors") && !response.get("errors").isJsonNull()) {
            var errors = response.getAsJsonArray("errors");
            if (errors.size() > 0) {
                String errorMessage = errors.get(0).getAsJsonObject().get("message").getAsString();
                throw new Exception("GraphQL Error: " + errorMessage);
            }
        }
        
        if (!response.has("data") || response.get("data").isJsonNull()) {
            throw new Exception("No data returned from GraphQL");
        }
        JsonObject authorJson = response.getAsJsonObject("data").getAsJsonObject("author");

        return gson.fromJson(authorJson, AuthorDTO.class);
    }

    public AuthorDTO createAuthor(String name, String bio, String nationality, String avatar, String token) throws Exception {
        // Generate slug from name
        String slug = generateSlug(name);

        String mutation = """
            mutation CreateAuthor($name: String!, $slug: String, $bio: String, $imageUrl: String, $nationality: String) {
              createAuthor(name: $name, slug: $slug, bio: $bio, imageUrl: $imageUrl, nationality: $nationality) {
                id
                name
                slug
                bio
                imageUrl
                nationality
                status
                createdAt
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("name", name);
        variables.addProperty("slug", slug);
        if (bio != null) variables.addProperty("bio", bio);
        if (nationality != null) variables.addProperty("nationality", nationality);
        if (avatar != null) variables.addProperty("imageUrl", avatar);

        JsonObject response = graphQLService.executeQuery(mutation, variables, token);

        if (response.has("errors") && !response.get("errors").isJsonNull()) {
            var errors = response.getAsJsonArray("errors");
            if (errors.size() > 0) {
                String errorMessage = errors.get(0).getAsJsonObject().get("message").getAsString();
                throw new Exception(errorMessage);
            }
        }

        if (response.has("data") && !response.get("data").isJsonNull()) {
            JsonObject authorData = response.getAsJsonObject("data").getAsJsonObject("createAuthor");
            return gson.fromJson(authorData, AuthorDTO.class);
        }

        throw new Exception("Failed to create author");
    }

    // Helper method to generate slug
    private String generateSlug(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text
            .toLowerCase()
            .trim()
            .replaceAll("[^\\w\\s-]", "") // Remove special characters
            .replaceAll("\\s+", "-") // Replace spaces with hyphens
            .replaceAll("-+", "-") // Replace multiple hyphens with single hyphen
            .replaceAll("^-+|-+$", ""); // Remove leading/trailing hyphens
    }

    public AuthorDTO updateAuthor(String id, String name, String bio, String nationality, String avatar, String status, String token) throws Exception {
        String mutation = """
            mutation UpdateAuthor($id: ID!, $name: String, $bio: String, $imageUrl: String, $nationality: String, $status: BookStatus) {
              updateAuthor(id: $id, name: $name, bio: $bio, imageUrl: $imageUrl, nationality: $nationality, status: $status) {
                id
                name
                slug
                bio
                imageUrl
                nationality
                status
                updatedAt
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("id", id);
        if (name != null) variables.addProperty("name", name);
        if (bio != null) variables.addProperty("bio", bio);
        if (nationality != null) variables.addProperty("nationality", nationality);
        if (avatar != null) variables.addProperty("imageUrl", avatar);
        if (status != null) variables.addProperty("status", status);

        JsonObject response = graphQLService.executeQuery(mutation, variables, token);

        if (response.has("errors") && !response.get("errors").isJsonNull()) {
            var errors = response.getAsJsonArray("errors");
            if (errors.size() > 0) {
                String errorMessage = errors.get(0).getAsJsonObject().get("message").getAsString();
                throw new Exception(errorMessage);
            }
        }

        if (response.has("data") && !response.get("data").isJsonNull()) {
            JsonObject authorData = response.getAsJsonObject("data").getAsJsonObject("updateAuthor");
            return gson.fromJson(authorData, AuthorDTO.class);
        }

        throw new Exception("Failed to update author");
    }

    public String toggleAuthorStatus(String id, String token) throws Exception {
        String mutation = """
            mutation ToggleAuthorStatus($id: ID!) {
              toggleAuthorStatus(id: $id) {
                id
                name
                status
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("id", id);

        JsonObject response = graphQLService.executeQuery(mutation, variables, token);

        if (response.has("errors") && !response.get("errors").isJsonNull()) {
            var errors = response.getAsJsonArray("errors");
            if (errors.size() > 0) {
                String errorMessage = errors.get(0).getAsJsonObject().get("message").getAsString();
                throw new Exception(errorMessage);
            }
        }

        if (response.has("data") && !response.get("data").isJsonNull()) {
            return "Status toggled successfully";
        }

        throw new Exception("Failed to toggle author status");
    }

    public String deleteAuthor(String id, String token) throws Exception {
        String mutation = """
            mutation DeleteAuthor($id: ID!) {
              deleteAuthor(id: $id)
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("id", id);

        JsonObject response = graphQLService.executeQuery(mutation, variables, token);

        if (response.has("errors") && !response.get("errors").isJsonNull()) {
            var errors = response.getAsJsonArray("errors");
            if (errors.size() > 0) {
                String errorMessage = errors.get(0).getAsJsonObject().get("message").getAsString();
                throw new Exception(errorMessage);
            }
        }

        if (response.has("data") && !response.get("data").isJsonNull()) {
            return response.getAsJsonObject("data").get("deleteAuthor").getAsString();
        }

        throw new Exception("Failed to delete author");
    }
}
