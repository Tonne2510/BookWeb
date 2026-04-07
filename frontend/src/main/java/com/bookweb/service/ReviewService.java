package com.bookweb.service;

import com.bookweb.model.ReviewDTO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private GraphQLService graphQLService;

    private final Gson gson = new Gson();

    public List<ReviewDTO> getBookReviews(String bookId, int page, int limit) throws Exception {
        String query = """
            query GetReviews($bookId: ID!, $page: Int, $limit: Int) {
              reviews(bookId: $bookId, page: $page, limit: $limit, status: approved) {
                total
                reviews {
                  id
                  rating
                  title
                  content
                  imageUrl
                  helpfulCount
                  createdAt
                  user {
                    firstName
                    lastName
                  }
                }
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("bookId", bookId);
        variables.addProperty("page", page);
        variables.addProperty("limit", limit);

        JsonObject response = graphQLService.executeQuery(query, variables);
        
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
        
        JsonArray reviews = response.getAsJsonObject("data").getAsJsonObject("reviews").getAsJsonArray("reviews");

        List<ReviewDTO> reviewList = new ArrayList<>();
        reviews.forEach(review -> {
            reviewList.add(gson.fromJson(review, ReviewDTO.class));
        });

        return reviewList;
    }

    public int getBookReviewCount(String bookId) throws Exception {
        String query = """
            query GetReviewCount($bookId: ID!) {
              reviews(bookId: $bookId, status: approved, page: 1, limit: 1) {
                total
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("bookId", bookId);

        JsonObject response = graphQLService.executeQuery(query, variables);
        
        if (response.has("data") && !response.get("data").isJsonNull()) {
            JsonObject reviewsData = response.getAsJsonObject("data").getAsJsonObject("reviews");
            if (reviewsData != null && reviewsData.has("total")) {
                return reviewsData.get("total").getAsInt();
            }
        }
        return 0;
    }

    public List<ReviewDTO> getAllReviews(int page, int limit, String status) throws Exception {
        String query = """
            query GetAllReviews($page: Int, $limit: Int, $status: ReviewStatus) {
              reviews(page: $page, limit: $limit, status: $status) {
                reviews {
                  id
                  rating
                  title
                  content
                  imageUrl
                  status
                  helpfulCount
                  user {
                    id
                    email
                    firstName
                    lastName
                  }
                  book {
                    id
                    title
                    slug
                    coverImage
                  }
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
        if (status != null) variables.addProperty("status", status);

        JsonObject response = graphQLService.executeQuery(query, variables);
        
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
        
        JsonArray reviews = response.getAsJsonObject("data").getAsJsonObject("reviews").getAsJsonArray("reviews");

        List<ReviewDTO> reviewList = new ArrayList<>();
        reviews.forEach(review -> {
            reviewList.add(gson.fromJson(review, ReviewDTO.class));
        });

        return reviewList;
    }

    public ReviewDTO createReview(String bookId, Integer rating, String title, String content, String token) throws Exception {
        String mutation = """
            mutation CreateReview($bookId: ID!, $rating: Int!, $title: String, $content: String!) {
              createReview(bookId: $bookId, rating: $rating, title: $title, content: $content) {
                id
                rating
                title
                content
                imageUrl
                status
                user {
                  firstName
                  lastName
                }
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("bookId", bookId);
        variables.addProperty("rating", rating);
        variables.addProperty("title", title);
        variables.addProperty("content", content);

        JsonObject response = graphQLService.executeQuery(mutation, variables, token);

        if (response.has("errors") && !response.get("errors").isJsonNull()) {
            var errors = response.getAsJsonArray("errors");
            if (errors.size() > 0) {
                String errorMessage = errors.get(0).getAsJsonObject().get("message").getAsString();
                throw new Exception(errorMessage);
            }
        }

        if (response.has("data") && !response.get("data").isJsonNull()) {
            JsonObject reviewJson = response.getAsJsonObject("data").getAsJsonObject("createReview");
            return gson.fromJson(reviewJson, ReviewDTO.class);
        }

        throw new Exception("Failed to create review");
    }

    public ReviewDTO updateReview(String id, Integer rating, String title, String content, String token) throws Exception {
        String mutation = """
            mutation UpdateReview($id: ID!, $rating: Int, $title: String, $content: String) {
              updateReview(id: $id, rating: $rating, title: $title, content: $content) {
                id
                rating
                title
                content
                status
                updatedAt
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("id", id);
        if (rating != null) variables.addProperty("rating", rating);
        if (title != null) variables.addProperty("title", title);
        if (content != null) variables.addProperty("content", content);

        JsonObject response = graphQLService.executeQuery(mutation, variables, token);

        if (response.has("errors") && !response.get("errors").isJsonNull()) {
            var errors = response.getAsJsonArray("errors");
            if (errors.size() > 0) {
                String errorMessage = errors.get(0).getAsJsonObject().get("message").getAsString();
                throw new Exception(errorMessage);
            }
        }

        if (response.has("data") && !response.get("data").isJsonNull()) {
            JsonObject reviewJson = response.getAsJsonObject("data").getAsJsonObject("updateReview");
            return gson.fromJson(reviewJson, ReviewDTO.class);
        }

        throw new Exception("Failed to update review");
    }

    public ReviewDTO approveReview(String id, String token) throws Exception {
        String mutation = """
            mutation ApproveReview($id: ID!) {
              approveReview(id: $id) {
                id
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
            JsonObject reviewJson = response.getAsJsonObject("data").getAsJsonObject("approveReview");
            return gson.fromJson(reviewJson, ReviewDTO.class);
        }

        throw new Exception("Failed to approve review");
    }

    public ReviewDTO rejectReview(String id, String token) throws Exception {
        String mutation = """
            mutation RejectReview($id: ID!) {
              rejectReview(id: $id) {
                id
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
            JsonObject reviewJson = response.getAsJsonObject("data").getAsJsonObject("rejectReview");
            return gson.fromJson(reviewJson, ReviewDTO.class);
        }

        throw new Exception("Failed to reject review");
    }

    public String deleteReview(String id, String token) throws Exception {
        String mutation = """
            mutation DeleteReview($id: ID!) {
              deleteReview(id: $id)
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
            return response.getAsJsonObject("data").get("deleteReview").getAsString();
        }

        throw new Exception("Failed to delete review");
    }
}
