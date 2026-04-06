package com.bookweb.service;

import com.bookweb.model.AuthResponse;
import com.bookweb.model.UserDTO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private GraphQLService graphQLService;

    private final Gson gson = new Gson();

    public AuthResponse register(String email, String password, String firstName, String lastName, String otp) throws Exception {
        String query = """
            mutation Register($email: String!, $password: String!, $firstName: String!, $lastName: String!, $otp: String!) {
              register(email: $email, password: $password, firstName: $firstName, lastName: $lastName, otp: $otp) {
                token
                user {
                  id
                  email
                  firstName
                  lastName
                  role
                  status
                }
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("email", email);
        variables.addProperty("password", password);
        variables.addProperty("firstName", firstName);
        variables.addProperty("lastName", lastName);
        variables.addProperty("otp", otp);

        JsonObject response = graphQLService.executeQuery(query, variables);
        
        // Handle GraphQL errors
        if (response.has("errors") && !response.get("errors").isJsonNull()) {
            var errors = response.getAsJsonArray("errors");
            if (errors.size() > 0) {
                String errorMessage = errors.get(0).getAsJsonObject().get("message").getAsString();
                throw new Exception(errorMessage);
            }
        }
        
        if (response.has("data") && !response.get("data").isJsonNull()) {
            JsonObject authData = response.getAsJsonObject("data").getAsJsonObject("register");
            return gson.fromJson(authData, AuthResponse.class);
        }
        
        throw new Exception("Invalid register response from server");
    }

    public String sendVerificationOtp(String email) throws Exception {
        String query = """
            mutation SendVerificationOtp($email: String!) {
              sendVerificationOtp(email: $email)
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("email", email);

        JsonObject response = graphQLService.executeQuery(query, variables);
        
        if (response.has("errors") && !response.get("errors").isJsonNull()) {
            var errors = response.getAsJsonArray("errors");
            if (errors.size() > 0) {
                String errorMessage = errors.get(0).getAsJsonObject().get("message").getAsString();
                throw new Exception(errorMessage);
            }
        }
        
        if (response.has("data") && !response.get("data").isJsonNull()) {
            return response.getAsJsonObject("data").get("sendVerificationOtp").getAsString();
        }
        
        throw new Exception("Không thể gửi mã xác nhận");
    }

    public AuthResponse login(String email, String password) throws Exception {
        String query = """
            mutation Login($email: String!, $password: String!) {
              login(email: $email, password: $password) {
                token
                user {
                  id
                  email
                  firstName
                  lastName
                  role
                  status
                }
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("email", email);
        variables.addProperty("password", password);

        JsonObject response = graphQLService.executeQuery(query, variables);
        logger.debug("Login response: {}", response.toString());
        
        // Handle GraphQL errors
        if (response.has("errors") && !response.get("errors").isJsonNull()) {
            var errors = response.getAsJsonArray("errors");
            if (errors.size() > 0) {
                String errorMessage = errors.get(0).getAsJsonObject().get("message").getAsString();
                logger.error("Login error: {}", errorMessage);
                throw new Exception(errorMessage);
            }
        }
        
        if (response.has("data") && !response.get("data").isJsonNull()) {
            JsonObject authData = response.getAsJsonObject("data").getAsJsonObject("login");
            logger.info("Login successful for email: {}", email);
            return gson.fromJson(authData, AuthResponse.class);
        }
        
        logger.error("Invalid login response from server: {}", response.toString());
        throw new Exception("Invalid login response from server");
    }

    public String forgotPassword(String email) throws Exception {
        String query = """
            mutation ForgotPassword($email: String!) {
              forgotPassword(email: $email)
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("email", email);

        JsonObject response = graphQLService.executeQuery(query, variables);
        
        // Handle GraphQL errors
        if (response.has("errors") && !response.get("errors").isJsonNull()) {
            var errors = response.getAsJsonArray("errors");
            if (errors.size() > 0) {
                String errorMessage = errors.get(0).getAsJsonObject().get("message").getAsString();
                throw new Exception(errorMessage);
            }
        }
        
        if (response.has("data") && !response.get("data").isJsonNull()) {
            return response.getAsJsonObject("data").get("forgotPassword").getAsString();
        }
        
        throw new Exception("Invalid forgot password response from server");
    }

    public String resetPassword(String token, String newPassword) throws Exception {
        String query = """
            mutation ResetPassword($token: String!, $newPassword: String!) {
              resetPassword(token: $token, newPassword: $newPassword)
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("token", token);
        variables.addProperty("newPassword", newPassword);

        JsonObject response = graphQLService.executeQuery(query, variables);
        
        // Handle GraphQL errors
        if (response.has("errors") && !response.get("errors").isJsonNull()) {
            var errors = response.getAsJsonArray("errors");
            if (errors.size() > 0) {
                String errorMessage = errors.get(0).getAsJsonObject().get("message").getAsString();
                throw new Exception(errorMessage);
            }
        }
        
        if (response.has("data") && !response.get("data").isJsonNull()) {
            return response.getAsJsonObject("data").get("resetPassword").getAsString();
        }
        
        throw new Exception("Invalid reset password response from server");
    }

    public UserDTO getMe(String token) throws Exception {
        String query = """
            query Me {
              me {
                id
                email
                firstName
                lastName
                phone
                avatar
                address
                role
                status
              }
            }
        """;

        JsonObject response = graphQLService.executeQuery(query, token);
        JsonObject userData = response.getAsJsonObject("data").getAsJsonObject("me");

        return gson.fromJson(userData, UserDTO.class);
    }

    public UserDTO updateProfile(String firstName, String lastName, String phone, String address, String token) throws Exception {
        String mutation = """
            mutation UpdateProfile($firstName: String, $lastName: String, $phone: String, $address: String) {
              updateProfile(firstName: $firstName, lastName: $lastName, phone: $phone, address: $address) {
                id
                email
                firstName
                lastName
                phone
                address
                avatar
                role
                status
              }
            }
        """;

        JsonObject variables = new JsonObject();
        if (firstName != null) variables.addProperty("firstName", firstName);
        if (lastName != null) variables.addProperty("lastName", lastName);
        if (phone != null) variables.addProperty("phone", phone);
        if (address != null) variables.addProperty("address", address);

        JsonObject response = graphQLService.executeQuery(mutation, variables, token);

        if (response.has("errors") && !response.get("errors").isJsonNull()) {
            var errors = response.getAsJsonArray("errors");
            if (errors.size() > 0) {
                String errorMessage = errors.get(0).getAsJsonObject().get("message").getAsString();
                throw new Exception(errorMessage);
            }
        }

        if (response.has("data") && !response.get("data").isJsonNull()) {
            JsonObject userData = response.getAsJsonObject("data").getAsJsonObject("updateProfile");
            return gson.fromJson(userData, UserDTO.class);
        }

        throw new Exception("Cập nhật thông tin thất bại");
    }
}
