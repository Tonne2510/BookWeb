package com.bookweb.service;

import com.bookweb.model.OrderDTO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    @Autowired
    private GraphQLService graphQLService;

    private final Gson gson = new Gson();

    public List<OrderDTO> getAllOrders(int page, int limit, String status, String token) throws Exception {
        String query = """
            query GetOrders($page: Int, $limit: Int, $status: OrderStatus) {
              orders(page: $page, limit: $limit, status: $status) {
                orders {
                  id
                  orderNumber
                  customerName
                  totalPrice
                  status
                  shippingAddress
                  user {
                    id
                    email
                    firstName
                    lastName
                  }
                  items {
                    id
                    quantity
                    price
                    book {
                      id
                      title
                      slug
                    }
                  }
                  createdAt
                  updatedAt
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

        JsonObject response = graphQLService.executeQuery(query, variables, token);
        
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
        
        JsonArray orders = response.getAsJsonObject("data").getAsJsonObject("orders").getAsJsonArray("orders");

        List<OrderDTO> orderList = new ArrayList<>();
        orders.forEach(order -> {
            orderList.add(gson.fromJson(order, OrderDTO.class));
        });

        return orderList;
    }

    public OrderDTO getOrderById(String id, String token) throws Exception {
        String query = """
            query GetOrder($id: ID!) {
              order(id: $id) {
                id
                orderNumber
                customerName
                customerEmail
                customerPhone
                totalPrice
                totalDiscount
                shippingAddress
                shippingCost
                paymentMethod
                status
                notes
                user {
                  id
                  email
                  firstName
                  lastName
                  phone
                  address
                }
                items {
                  id
                  quantity
                  price
                  discount
                  book {
                    id
                    title
                    slug
                    coverImage
                  }
                }
                createdAt
                updatedAt
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("id", id);

        JsonObject response = graphQLService.executeQuery(query, variables, token);
        
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
        
        JsonObject orderJson = response.getAsJsonObject("data").getAsJsonObject("order");

        return gson.fromJson(orderJson, OrderDTO.class);
    }

    public OrderDTO updateOrderStatus(String id, String status, String token) throws Exception {
        String mutation = """
            mutation UpdateOrderStatus($id: ID!, $status: OrderStatus!) {
              updateOrderStatus(id: $id, status: $status) {
                id
                orderNumber
                totalPrice
                status
                updatedAt
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("id", id);
        variables.addProperty("status", status);

        JsonObject response = graphQLService.executeQuery(mutation, variables, token);

        if (response.has("errors") && !response.get("errors").isJsonNull()) {
            var errors = response.getAsJsonArray("errors");
            if (errors.size() > 0) {
                String errorMessage = errors.get(0).getAsJsonObject().get("message").getAsString();
                throw new Exception(errorMessage);
            }
        }

        if (response.has("data") && !response.get("data").isJsonNull()) {
            JsonObject orderData = response.getAsJsonObject("data").getAsJsonObject("updateOrderStatus");
            return gson.fromJson(orderData, OrderDTO.class);
        }

        throw new Exception("Failed to update order");
    }

    public String cancelOrder(String id, String token) throws Exception {
        String mutation = """
            mutation CancelOrder($id: ID!) {
              cancelOrder(id: $id) {
                id
                orderNumber
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
            return "Order cancelled successfully";
        }

        throw new Exception("Failed to cancel order");
    }

    public List<OrderDTO> getUserOrders(String userId, int page, int limit, String token) throws Exception {
        String query = """
            query GetUserOrders($userId: ID!, $page: Int, $limit: Int) {
              orders(userId: $userId, page: $page, limit: $limit) {
                orders {
                  id
                  orderNumber
                  totalPrice
                  status
                  user {
                    id
                    firstName
                    lastName
                  }
                  items {
                    quantity
                    book {
                      title
                    }
                  }
                  createdAt
                }
                total
                pages
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("userId", userId);
        variables.addProperty("page", page);
        variables.addProperty("limit", limit);

        JsonObject response = graphQLService.executeQuery(query, variables, token);
        
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
        
        JsonArray orders = response.getAsJsonObject("data").getAsJsonObject("orders").getAsJsonArray("orders");

        List<OrderDTO> orderList = new ArrayList<>();
        orders.forEach(order -> {
            orderList.add(gson.fromJson(order, OrderDTO.class));
        });

        return orderList;
    }

    // Wrapper methods for admin use
    public List<OrderDTO> getAllOrders(String status, int page, int limit, String token) throws Exception {
        return getAllOrders(page, limit, status, token);
    }

    public List<OrderDTO> getUserOrders(String userId, String token) throws Exception {
        return getUserOrders(userId, 1, 50, token);
    }

    public OrderDTO getOrderDetail(String id, String token) throws Exception {
        return getOrderById(id, token);
    }

    /**
     * Create a new order from cart checkout
     */
    public String createOrder(Map<String, Object> orderData, String token) throws Exception {
        List<?> items = (List<?>) orderData.get("items");
        
        // Build items array for GraphQL
        StringBuilder itemsGQL = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) itemsGQL.append(",");
            Map<String, Object> item = (Map<String, Object>) items.get(i);
            itemsGQL.append(String.format(
                "{bookId:\"%s\",quantity:%d}",
                item.get("bookId"),
                ((Number) item.get("quantity")).intValue()
            ));
        }
        itemsGQL.append("]");

        String mutation = """
            mutation CreateOrder($items: [OrderItemInput!]!, $shippingAddress: String!, $paymentMethod: PaymentMethod!, $customerName: String, $customerEmail: String, $customerPhone: String) {
              createOrder(items: $items, shippingAddress: $shippingAddress, paymentMethod: $paymentMethod, customerName: $customerName, customerEmail: $customerEmail, customerPhone: $customerPhone) {
                id
                orderNumber
                totalPrice
                status
              }
            }
        """;

        JsonObject variables = new JsonObject();
        
        // Parse items array using JsonParser
        variables.add("items", JsonParser.parseString(itemsGQL.toString()).getAsJsonArray());
        variables.addProperty("shippingAddress", (String) orderData.get("shippingAddress"));
        variables.addProperty("paymentMethod", (String) orderData.get("paymentMethod"));
        
        // Add optional customer info
        if (orderData.containsKey("fullName")) {
            variables.addProperty("customerName", (String) orderData.get("fullName"));
        }
        if (orderData.containsKey("email")) {
            variables.addProperty("customerEmail", (String) orderData.get("email"));
        }
        if (orderData.containsKey("phone")) {
            variables.addProperty("customerPhone", (String) orderData.get("phone"));
        }

        try {
            JsonObject response = graphQLService.executeQuery(mutation, variables, token);
            
            if (response.has("errors") && !response.get("errors").isJsonNull()) {
                var errors = response.getAsJsonArray("errors");
                if (errors.size() > 0) {
                    String errorMessage = errors.get(0).getAsJsonObject().get("message").getAsString();
                    throw new Exception(errorMessage);
                }
            }

            if (response.has("data") && !response.get("data").isJsonNull()) {
                JsonObject createOrderData = response.getAsJsonObject("data").getAsJsonObject("createOrder");
                if (createOrderData != null && createOrderData.has("id")) {
                    return createOrderData.get("id").getAsString();
                }
            }

            throw new Exception("Failed to create order - no data returned");
        } catch (Exception e) {
            throw new Exception("Order creation error: " + e.getMessage());
        }
    }
}
