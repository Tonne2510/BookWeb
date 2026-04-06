package com.bookweb.service;

import com.bookweb.model.FavoriteDTO;
import com.bookweb.model.BookDTO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FavoriteService {

    @Autowired
    private GraphQLService graphQLService;

    private final Gson gson = new Gson();

    public List<FavoriteDTO> getMyFavorites(String token) throws Exception {
        String query = """
            query MyFavorites {
              myFavorites(page: 1, limit: 100) {
                favorites {
                  id
                  createdAt
                  book {
                    id
                    title
                    slug
                    coverImage
                    price
                    discount
                    finalPrice
                    rating
                    stock
                    status
                    author { name }
                    category { name }
                  }
                }
                total
              }
            }
        """;

        JsonObject response = graphQLService.executeQuery(query, token);

        if (response.has("errors") && !response.get("errors").isJsonNull()) {
            var errors = response.getAsJsonArray("errors");
            if (errors.size() > 0) {
                String msg = errors.get(0).getAsJsonObject().get("message").getAsString();
                throw new Exception(msg);
            }
        }

        if (!response.has("data") || response.get("data").isJsonNull()) {
            return new ArrayList<>();
        }

        JsonObject myFavObj = response.getAsJsonObject("data").getAsJsonObject("myFavorites");
        if (myFavObj == null) return new ArrayList<>();

        JsonArray favArray = myFavObj.getAsJsonArray("favorites");
        List<FavoriteDTO> list = new ArrayList<>();

        if (favArray != null) {
            favArray.forEach(el -> {
                JsonObject favJson = el.getAsJsonObject();
                FavoriteDTO dto = new FavoriteDTO();
                dto.setId(favJson.has("id") ? favJson.get("id").getAsString() : null);
                dto.setCreatedAt(favJson.has("createdAt") ? favJson.get("createdAt").getAsString() : null);

                if (favJson.has("book") && !favJson.get("book").isJsonNull()) {
                    JsonObject bookJson = favJson.getAsJsonObject("book");
                    BookDTO book = new BookDTO();
                    book.setId(getStr(bookJson, "id"));
                    book.setTitle(getStr(bookJson, "title"));
                    book.setSlug(getStr(bookJson, "slug"));
                    book.setCoverImage(getStr(bookJson, "coverImage"));
                    book.setStatus(getStr(bookJson, "status"));
                    if (bookJson.has("price") && !bookJson.get("price").isJsonNull())
                        book.setPrice(bookJson.get("price").getAsDouble());
                    if (bookJson.has("discount") && !bookJson.get("discount").isJsonNull())
                        book.setDiscount(bookJson.get("discount").getAsDouble());
                    if (bookJson.has("finalPrice") && !bookJson.get("finalPrice").isJsonNull())
                        book.setFinalPrice(bookJson.get("finalPrice").getAsDouble());
                    if (bookJson.has("rating") && !bookJson.get("rating").isJsonNull())
                        book.setRating(bookJson.get("rating").getAsDouble());
                    if (bookJson.has("stock") && !bookJson.get("stock").isJsonNull())
                        book.setStock(bookJson.get("stock").getAsInt());
                    if (bookJson.has("author") && !bookJson.get("author").isJsonNull())
                        book.setAuthorName(bookJson.getAsJsonObject("author").get("name").getAsString());
                    if (bookJson.has("category") && !bookJson.get("category").isJsonNull())
                        book.setCategoryName(bookJson.getAsJsonObject("category").get("name").getAsString());
                    dto.setBook(book);
                }
                list.add(dto);
            });
        }

        return list;
    }

    public boolean isFavorite(String bookId, String token) {
        if (token == null) return false;
        String query = """
            query IsFavorite($bookId: ID!) {
              isFavorite(bookId: $bookId)
            }
        """;
        JsonObject variables = new JsonObject();
        variables.addProperty("bookId", bookId);
        try {
            JsonObject response = graphQLService.executeQuery(query, variables, token);
            if (response.has("data") && !response.get("data").isJsonNull()) {
                return response.getAsJsonObject("data").get("isFavorite").getAsBoolean();
            }
        } catch (Exception ignored) {}
        return false;
    }

    public void addToFavorites(String bookId, String token) throws Exception {
        String mutation = """
            mutation AddToFavorites($bookId: ID!) {
              addToFavorites(bookId: $bookId) {
                id
              }
            }
        """;
        JsonObject variables = new JsonObject();
        variables.addProperty("bookId", bookId);
        JsonObject response = graphQLService.executeQuery(mutation, variables, token);
        if (response.has("errors") && !response.get("errors").isJsonNull()) {
            var errors = response.getAsJsonArray("errors");
            if (errors.size() > 0) {
                throw new Exception(errors.get(0).getAsJsonObject().get("message").getAsString());
            }
        }
    }

    public void removeFromFavorites(String bookId, String token) throws Exception {
        String mutation = """
            mutation RemoveFromFavorites($bookId: ID!) {
              removeFromFavorites(bookId: $bookId)
            }
        """;
        JsonObject variables = new JsonObject();
        variables.addProperty("bookId", bookId);
        JsonObject response = graphQLService.executeQuery(mutation, variables, token);
        if (response.has("errors") && !response.get("errors").isJsonNull()) {
            var errors = response.getAsJsonArray("errors");
            if (errors.size() > 0) {
                throw new Exception(errors.get(0).getAsJsonObject().get("message").getAsString());
            }
        }
    }

    private String getStr(JsonObject obj, String key) {
        return (obj.has(key) && !obj.get(key).isJsonNull()) ? obj.get(key).getAsString() : null;
    }
}
