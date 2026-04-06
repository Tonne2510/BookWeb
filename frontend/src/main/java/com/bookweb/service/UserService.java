package com.bookweb.service;

import com.bookweb.model.UserDTO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private GraphQLService graphQLService;

    private final Gson gson = new Gson();

    public List<UserDTO> getAllUsers(int page, int limit, String token) throws Exception {
        String query = """
            query GetUsers($page: Int, $limit: Int) {
              users(page: $page, limit: $limit) {
                id
                email
                firstName
                lastName
                fullName
                role
                status
                phone
                address
                avatar
                createdAt
                updatedAt
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("page", page);
        variables.addProperty("limit", limit);

        JsonObject response = graphQLService.executeQuery(query, variables, token);
        
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
        
        JsonArray users = response.getAsJsonObject("data").getAsJsonArray("users");

        List<UserDTO> userList = new ArrayList<>();
        users.forEach(user -> {
            userList.add(gson.fromJson(user, UserDTO.class));
        });

        return userList;
    }

    public UserDTO getUserById(String id, String token) throws Exception {
        String query = """
            query GetUser($id: ID!) {
              user(id: $id) {
                id
                email
                firstName
                lastName
                fullName
                role
                status
                phone
                address
                avatar
                createdAt
                updatedAt
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("id", id);

        JsonObject response = graphQLService.executeQuery(query, variables, token);
        
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
        
        JsonObject user = response.getAsJsonObject("data").getAsJsonObject("user");
        return gson.fromJson(user, UserDTO.class);
    }

    public UserDTO toggleUserStatus(String userId, String token) throws Exception {
        String query = """
            mutation ToggleUserStatus($userId: ID!) {
              toggleUserStatus(userId: $userId) {
                id
                email
                firstName
                lastName
                role
                status
                createdAt
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("userId", userId);

        JsonObject response = graphQLService.executeQuery(query, variables, token);
        
        // Handle GraphQL errors
        if (response.has("errors") && !response.get("errors").isJsonNull()) {
            var errors = response.getAsJsonArray("errors");
            if (errors.size() > 0) {
                String errorMessage = errors.get(0).getAsJsonObject().get("message").getAsString();
                throw new Exception("GraphQL Error: " + errorMessage);
            }
        }
        
        if (!response.has("data") || response.get("data").isJsonNull()) {
            throw new Exception("Failed to toggle user status");
        }
        
        JsonObject user = response.getAsJsonObject("data").getAsJsonObject("toggleUserStatus");
        return gson.fromJson(user, UserDTO.class);
    }

    public UserDTO changeUserRole(String userId, String role, String token) throws Exception {
        String query = """
            mutation ChangeUserRole($userId: ID!, $role: UserRole!) {
              changeUserRole(userId: $userId, role: $role) {
                id
                email
                firstName
                lastName
                role
                status
                createdAt
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("userId", userId);
        variables.addProperty("role", role);

        JsonObject response = graphQLService.executeQuery(query, variables, token);
        
        // Handle GraphQL errors
        if (response.has("errors") && !response.get("errors").isJsonNull()) {
            var errors = response.getAsJsonArray("errors");
            if (errors.size() > 0) {
                String errorMessage = errors.get(0).getAsJsonObject().get("message").getAsString();
                throw new Exception("GraphQL Error: " + errorMessage);
            }
        }
        
        if (!response.has("data") || response.get("data").isJsonNull()) {
            throw new Exception("Failed to change user role");
        }
        
        JsonObject user = response.getAsJsonObject("data").getAsJsonObject("changeUserRole");
        return gson.fromJson(user, UserDTO.class);
    }

    public String deleteUser(String userId, String token) throws Exception {
        String query = """
            mutation DeleteUser($userId: ID!) {
              deleteUser(userId: $userId)
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("userId", userId);

        JsonObject response = graphQLService.executeQuery(query, variables, token);
        
        // Handle GraphQL errors
        if (response.has("errors") && !response.get("errors").isJsonNull()) {
            var errors = response.getAsJsonArray("errors");
            if (errors.size() > 0) {
                String errorMessage = errors.get(0).getAsJsonObject().get("message").getAsString();
                throw new Exception("GraphQL Error: " + errorMessage);
            }
        }
        
        if (!response.has("data") || response.get("data").isJsonNull()) {
            throw new Exception("Failed to delete user");
        }
        
        return response.getAsJsonObject("data").get("deleteUser").getAsString();
    }
}
