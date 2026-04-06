package com.bookweb.service;

import com.bookweb.model.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookService {

    @Autowired
    private GraphQLService graphQLService;

    private final Gson gson = new Gson();

    public List<BookDTO> getAllBooks(int page, int limit, String searchTerm, String sortBy, String order) throws Exception {
        return getAllBooks(page, limit, searchTerm, sortBy, order, null);
    }

    public List<BookDTO> getAllBooks(int page, int limit, String searchTerm, String sortBy, String order, String status) throws Exception {
        return getAllBooks(page, limit, searchTerm, sortBy, order, status, null, null, null, null);
    }

    public List<BookDTO> getAllBooks(int page, int limit, String searchTerm, String sortBy, String order, String status,
                                    String categoryId, String authorId, Double minPrice, Double maxPrice) throws Exception {
        String query = """
            query GetBooks($page: Int, $limit: Int, $searchTerm: String, $sortBy: String, $order: String, $status: BookStatus, $categoryId: ID, $authorId: ID, $minPrice: Float, $maxPrice: Float) {
              books(page: $page, limit: $limit, searchTerm: $searchTerm, sortBy: $sortBy, order: $order, status: $status, categoryId: $categoryId, authorId: $authorId, minPrice: $minPrice, maxPrice: $maxPrice) {
                books {
                  id
                  title
                  slug
                  description
                  price
                  discount
                  finalPrice
                  coverImage
                  rating
                  stock
                  status
                  category {
                    id
                    name
                  }
                  author {
                    id
                    name
                  }
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
        if (searchTerm != null && !searchTerm.isEmpty()) variables.addProperty("searchTerm", searchTerm);
        variables.addProperty("sortBy", sortBy);
        variables.addProperty("order", order);
        if (status != null && !status.isEmpty()) variables.addProperty("status", status);
        if (categoryId != null && !categoryId.isEmpty()) variables.addProperty("categoryId", categoryId);
        if (authorId != null && !authorId.isEmpty()) variables.addProperty("authorId", authorId);
        if (minPrice != null) variables.addProperty("minPrice", minPrice);
        if (maxPrice != null) variables.addProperty("maxPrice", maxPrice);

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
        
        JsonArray books = response.getAsJsonObject("data").getAsJsonObject("books").getAsJsonArray("books");

        List<BookDTO> bookList = new ArrayList<>();
        books.forEach(book -> {
            bookList.add(parseBookJson(book.getAsJsonObject()));
        });

        return bookList;
    }

    private BookDTO parseBookJson(JsonObject json) {
        BookDTO dto = gson.fromJson(json, BookDTO.class);
        // Map nested category object to flat fields
        if (json.has("category") && !json.get("category").isJsonNull()) {
            JsonObject cat = json.getAsJsonObject("category");
            if (cat.has("id") && !cat.get("id").isJsonNull()) dto.setCategoryId(cat.get("id").getAsString());
            if (cat.has("name") && !cat.get("name").isJsonNull()) dto.setCategoryName(cat.get("name").getAsString());
        }
        // Map nested author object to flat fields
        if (json.has("author") && !json.get("author").isJsonNull()) {
            JsonObject auth = json.getAsJsonObject("author");
            if (auth.has("id") && !auth.get("id").isJsonNull()) dto.setAuthorId(auth.get("id").getAsString());
            if (auth.has("name") && !auth.get("name").isJsonNull()) dto.setAuthorName(auth.get("name").getAsString());
        }
        return dto;
    }

    public BookDTO getBookBySlug(String slug) throws Exception {
        String query = """
            query GetBook($slug: String) {
              book(slug: $slug) {
                id
                title
                slug
                description
                isbn
                price
                discount
                finalPrice
                coverImage
                publisher
                publicationDate
                pages
                stock
                rating
                views
                status
                category {
                  id
                  name
                }
                author {
                  id
                  name
                }
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("slug", slug);

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
        
        var bookElement = response.getAsJsonObject("data").get("book");
        
        if (bookElement == null || bookElement.isJsonNull()) {
            throw new Exception("Book not found");
        }
        
        JsonObject bookJson = bookElement.getAsJsonObject();

        return parseBookJson(bookJson);
    }

    public BookDTO getBookById(String id) throws Exception {
        String query = """
            query GetBook($id: ID) {
              book(id: $id) {
                id
                title
                slug
                description
                isbn
                price
                discount
                finalPrice
                coverImage
                publisher
                publicationDate
                pages
                stock
                rating
                views
                status
                category {
                  id
                  name
                }
                author {
                  id
                  name
                }
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("id", id);

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

        var bookElement = response.getAsJsonObject("data").get("book");

        if (bookElement == null || bookElement.isJsonNull()) {
            throw new Exception("Book not found");
        }

        JsonObject bookJson = bookElement.getAsJsonObject();

        return parseBookJson(bookJson);
    }

    public List<BookDTO> searchBooks(String searchTerm) throws Exception {
        String query = """
            query SearchBooks($searchTerm: String) {
              books(searchTerm: $searchTerm, limit: 20) {
                books {
                  id
                  title
                  slug
                  price
                  discount
                  finalPrice
                  coverImage
                  rating
                }
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("searchTerm", searchTerm);

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
        
        JsonArray books = response.getAsJsonObject("data").getAsJsonObject("books").getAsJsonArray("books");

        List<BookDTO> bookList = new ArrayList<>();
        books.forEach(book -> {
            bookList.add(parseBookJson(book.getAsJsonObject()));
        });

        return bookList;
    }

    // CRUD Operations
    public BookDTO createBook(
            String title, String description, Double price, Double discount,
            String isbn, String categoryId, String authorId, String coverImage,
            String publisher, String publicationDate, Integer pages, Integer stock, String token) throws Exception {

        // Generate slug from title if not provided
        String slug = generateSlug(title);

        String mutation = """
            mutation CreateBook(
              $title: String!, $slug: String, $description: String, $price: Float!, $discount: Float,
              $isbn: String, $categoryId: ID, $authorId: ID, $coverImage: String,
              $publisher: String, $publicationDate: String, $pages: Int, $stock: Int
            ) {
              createBook(
                title: $title, slug: $slug, description: $description, price: $price, discount: $discount,
                isbn: $isbn, categoryId: $categoryId, authorId: $authorId, coverImage: $coverImage,
                publisher: $publisher, publicationDate: $publicationDate, pages: $pages, stock: $stock
              ) {
                id
                title
                slug
                price
                discount
                finalPrice
                status
                createdAt
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("title", title);
        variables.addProperty("slug", slug);
        if (description != null) variables.addProperty("description", description);
        variables.addProperty("price", price);
        if (discount != null) variables.addProperty("discount", discount);
        if (isbn != null) variables.addProperty("isbn", isbn);
        if (categoryId != null && !categoryId.isEmpty()) variables.addProperty("categoryId", categoryId);
        if (authorId != null && !authorId.isEmpty()) variables.addProperty("authorId", authorId);
        if (coverImage != null) variables.addProperty("coverImage", coverImage);
        if (publisher != null) variables.addProperty("publisher", publisher);
        if (publicationDate != null) variables.addProperty("publicationDate", publicationDate);
        if (pages != null) variables.addProperty("pages", pages);
        if (stock != null) variables.addProperty("stock", stock);
        else variables.addProperty("stock", 0);

        JsonObject response = graphQLService.executeQuery(mutation, variables, token);

        if (response.has("errors") && !response.get("errors").isJsonNull()) {
            var errors = response.getAsJsonArray("errors");
            if (errors.size() > 0) {
                String errorMessage = errors.get(0).getAsJsonObject().get("message").getAsString();
                throw new Exception(errorMessage);
            }
        }

        if (response.has("data") && !response.get("data").isJsonNull()) {
            JsonObject bookData = response.getAsJsonObject("data").getAsJsonObject("createBook");
            return gson.fromJson(bookData, BookDTO.class);
        }

        throw new Exception("Failed to create book");
    }

    // Helper method to generate slug
    private String generateSlug(String title) {
        if (title == null || title.isEmpty()) {
            return "";
        }
        return title
            .toLowerCase()
            .trim()
            .replaceAll("[^\\w\\s-]", "") // Remove special characters
            .replaceAll("\\s+", "-") // Replace spaces with hyphens
            .replaceAll("-+", "-") // Replace multiple hyphens with single hyphen
            .replaceAll("^-+|-+$", ""); // Remove leading/trailing hyphens
    }

    public BookDTO updateBook(String id, String title, String description, Double price, Double discount,
                             String isbn, String categoryId, String authorId, String coverImage, String publisher,
                             String publicationDate, Integer pages, Integer stock, String status, String token) throws Exception {

        String mutation = """
            mutation UpdateBook(
              $id: ID!, $title: String, $description: String, $price: Float,
              $discount: Float, $isbn: String, $categoryId: ID, $authorId: ID,
              $coverImage: String, $publisher: String, $publicationDate: String, $pages: Int, $stock: Int, $status: BookStatus
            ) {
              updateBook(
                id: $id, title: $title, description: $description, price: $price,
                discount: $discount, isbn: $isbn, categoryId: $categoryId, authorId: $authorId,
                coverImage: $coverImage, publisher: $publisher, publicationDate: $publicationDate, pages: $pages, stock: $stock, status: $status
              ) {
                id
                title
                slug
                price
                discount
                finalPrice
                stock
                coverImage
                status
                updatedAt
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("id", id);
        if (title != null) variables.addProperty("title", title);
        if (description != null) variables.addProperty("description", description);
        if (price != null) variables.addProperty("price", price);
        if (discount != null) variables.addProperty("discount", discount);
        if (isbn != null) variables.addProperty("isbn", isbn);
        if (categoryId != null && !categoryId.isEmpty()) variables.addProperty("categoryId", categoryId);
        if (authorId != null && !authorId.isEmpty()) variables.addProperty("authorId", authorId);
        if (coverImage != null) variables.addProperty("coverImage", coverImage);
        if (publisher != null) variables.addProperty("publisher", publisher);
        if (publicationDate != null) variables.addProperty("publicationDate", publicationDate);
        if (pages != null) variables.addProperty("pages", pages);
        if (stock != null) variables.addProperty("stock", stock);
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
            JsonObject bookData = response.getAsJsonObject("data").getAsJsonObject("updateBook");
            return gson.fromJson(bookData, BookDTO.class);
        }

        throw new Exception("Failed to update book");
    }

    public String toggleBookStatus(String id, String token) throws Exception {
        String mutation = """
            mutation ToggleBookStatus($id: ID!) {
              toggleBookStatus(id: $id) {
                id
                title
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

        throw new Exception("Failed to toggle book status");
    }

    public String deleteBook(String id, String token) throws Exception {
        String mutation = """
            mutation DeleteBook($id: ID!) {
              deleteBook(id: $id)
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
            return response.getAsJsonObject("data").get("deleteBook").getAsString();
        }

        throw new Exception("Failed to delete book");
    }
}
