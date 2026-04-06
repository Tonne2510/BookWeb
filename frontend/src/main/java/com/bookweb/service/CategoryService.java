package com.bookweb.service;

import com.bookweb.model.CategoryDTO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private GraphQLService graphQLService;

    private final Gson gson = new Gson();

    public List<CategoryDTO> getAllCategories(int page, int limit) throws Exception {
        return getAllCategories(page, limit, null);
    }

    public List<CategoryDTO> getAllCategories(int page, int limit, String status) throws Exception {
        String query = """
            query GetCategories($page: Int, $limit: Int, $status: BookStatus) {
              categories(page: $page, limit: $limit, status: $status) {
                categories {
                  id
                  name
                  slug
                  description
                  image
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
        
        JsonArray categories = response.getAsJsonObject("data").getAsJsonObject("categories").getAsJsonArray("categories");

        List<CategoryDTO> categoryList = new ArrayList<>();
        categories.forEach(category -> {
            categoryList.add(gson.fromJson(category, CategoryDTO.class));
        });

        return categoryList;
    }

    public CategoryDTO getCategoryById(String id) throws Exception {
        String query = """
            query GetCategory($id: ID!) {
              category(id: $id) {
                id
                name
                slug
                description
                image
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
        
        JsonObject categoryJson = response.getAsJsonObject("data").getAsJsonObject("category");

        return gson.fromJson(categoryJson, CategoryDTO.class);
    }

    public CategoryDTO createCategory(String name, String description, String icon, String token) throws Exception {
        // Generate slug from name
        String slug = generateSlug(name);

        String mutation = """
            mutation CreateCategory($name: String!, $slug: String, $description: String, $image: String) {
              createCategory(name: $name, slug: $slug, description: $description, image: $image) {
                id
                name
                slug
                description
                image
                status
                createdAt
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("name", name);
        variables.addProperty("slug", slug);
        if (description != null) variables.addProperty("description", description);
        if (icon != null) variables.addProperty("image", icon);

        JsonObject response = graphQLService.executeQuery(mutation, variables, token);

        if (response.has("errors") && !response.get("errors").isJsonNull()) {
            var errors = response.getAsJsonArray("errors");
            if (errors.size() > 0) {
                String errorMessage = errors.get(0).getAsJsonObject().get("message").getAsString();
                throw new Exception(errorMessage);
            }
        }

        if (response.has("data") && !response.get("data").isJsonNull()) {
            JsonObject categoryData = response.getAsJsonObject("data").getAsJsonObject("createCategory");
            return gson.fromJson(categoryData, CategoryDTO.class);
        }

        throw new Exception("Failed to create category");
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

    public CategoryDTO updateCategory(String id, String name, String description, String icon, String status, String token) throws Exception {
        String mutation = """
            mutation UpdateCategory($id: ID!, $name: String, $description: String, $image: String, $status: BookStatus) {
              updateCategory(id: $id, name: $name, description: $description, image: $image, status: $status) {
                id
                name
                slug
                description
                image
                status
                updatedAt
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("id", id);
        if (name != null) variables.addProperty("name", name);
        if (description != null) variables.addProperty("description", description);
        if (icon != null) variables.addProperty("image", icon);
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
            JsonObject categoryData = response.getAsJsonObject("data").getAsJsonObject("updateCategory");
            return gson.fromJson(categoryData, CategoryDTO.class);
        }

        throw new Exception("Failed to update category");
    }

    public String toggleCategoryStatus(String id, String token) throws Exception {
        String mutation = """
            mutation ToggleCategoryStatus($id: ID!) {
              toggleCategoryStatus(id: $id) {
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

        throw new Exception("Failed to toggle category status");
    }

    public String deleteCategory(String id, String token) throws Exception {
        String mutation = """
            mutation DeleteCategory($id: ID!) {
              deleteCategory(id: $id)
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
            return response.getAsJsonObject("data").get("deleteCategory").getAsString();
        }

        throw new Exception("Failed to delete category");
    }
}
