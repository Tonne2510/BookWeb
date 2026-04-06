package com.bookweb.service;

import com.bookweb.model.VoucherDTO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VoucherService {

    @Autowired
    private GraphQLService graphQLService;

    private final Gson gson = new Gson();

    public Map<String, Object> validateVoucher(String code, Double subtotal, String token) throws Exception {
        String query = """
            query ValidateVoucher($code: String!, $subtotal: Float!) {
              validateVoucher(code: $code, subtotal: $subtotal) {
                valid
                message
                discountAmount
                finalAmount
                voucher {
                  id
                  code
                  userId
                  name
                  description
                  type
                  distributionType
                  value
                  minOrderValue
                  maxDiscount
                  isActive
                  endDate
                  giftSource
                  giftConditionType
                  minGiftAmount
                  maxGiftAmount
                  minGiftReviewCount
                  maxGiftReviewCount
                  giftedBySystem
                }
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("code", code);
        variables.addProperty("subtotal", subtotal);

        JsonObject response = graphQLService.executeQuery(query, variables, token);
        throwIfGraphQLErrors(response);

        JsonObject data = response.getAsJsonObject("data").getAsJsonObject("validateVoucher");
        Map<String, Object> result = new HashMap<>();
        result.put("valid", data.get("valid").getAsBoolean());
        result.put("message", data.get("message").getAsString());
        result.put("discountAmount", data.get("discountAmount").getAsDouble());
        result.put("finalAmount", data.get("finalAmount").getAsDouble());
        if (data.has("voucher") && !data.get("voucher").isJsonNull()) {
            result.put("voucher", gson.fromJson(data.get("voucher"), VoucherDTO.class));
        }
        return result;
    }

    public List<VoucherDTO> getAllVouchers(int page, int limit, String token) throws Exception {
        String query = """
            query GetVouchers($page: Int, $limit: Int) {
              vouchers(page: $page, limit: $limit) {
                id
                code
                userId
                name
                description
                type
                distributionType
                value
                minOrderValue
                maxDiscount
                totalUsageLimit
                usedCount
                giftedCount
                perUserLimit
                startDate
                endDate
                isActive
                giftSource
                giftConditionType
                minGiftAmount
                maxGiftAmount
                minGiftReviewCount
                maxGiftReviewCount
                giftedBySystem
                sourceTemplateId
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("page", page);
        variables.addProperty("limit", limit);

        JsonObject response = graphQLService.executeQuery(query, variables, token);
        throwIfGraphQLErrors(response);

        JsonArray vouchers = response.getAsJsonObject("data").getAsJsonArray("vouchers");
        List<VoucherDTO> result = new ArrayList<>();
        vouchers.forEach(v -> result.add(gson.fromJson(v, VoucherDTO.class)));
        return result;
    }

    public List<VoucherDTO> getMyGiftVouchers(Double subtotal, String token) throws Exception {
        String query = """
            query MyGiftVouchers($subtotal: Float) {
              myGiftVouchers(subtotal: $subtotal) {
                id
                code
                userId
                name
                description
                type
                distributionType
                value
                minOrderValue
                maxDiscount
                totalUsageLimit
                usedCount
                giftedCount
                perUserLimit
                startDate
                endDate
                isActive
                giftSource
                giftConditionType
                minGiftAmount
                maxGiftAmount
                minGiftReviewCount
                maxGiftReviewCount
                giftedBySystem
                sourceTemplateId
              }
            }
        """;

        JsonObject variables = new JsonObject();
        if (subtotal != null) variables.addProperty("subtotal", subtotal);

        JsonObject response = graphQLService.executeQuery(query, variables, token);
        throwIfGraphQLErrors(response);

        JsonArray vouchers = response.getAsJsonObject("data").getAsJsonArray("myGiftVouchers");
        List<VoucherDTO> result = new ArrayList<>();
        vouchers.forEach(v -> result.add(gson.fromJson(v, VoucherDTO.class)));
        return result;
    }

    public List<VoucherDTO> getMyVouchers(Double subtotal, String token) throws Exception {
        String query = """
            query MyVouchers($subtotal: Float) {
              myVouchers(subtotal: $subtotal) {
                id
                code
                userId
                name
                description
                type
                distributionType
                value
                minOrderValue
                maxDiscount
                totalUsageLimit
                usedCount
                giftedCount
                perUserLimit
                startDate
                endDate
                isActive
                giftSource
                giftConditionType
                minGiftAmount
                maxGiftAmount
                minGiftReviewCount
                maxGiftReviewCount
                giftedBySystem
                sourceTemplateId
              }
            }
        """;

        JsonObject variables = new JsonObject();
        if (subtotal != null) variables.addProperty("subtotal", subtotal);

        JsonObject response = graphQLService.executeQuery(query, variables, token);
        throwIfGraphQLErrors(response);

        JsonArray vouchers = response.getAsJsonObject("data").getAsJsonArray("myVouchers");
        List<VoucherDTO> result = new ArrayList<>();
        vouchers.forEach(v -> result.add(gson.fromJson(v, VoucherDTO.class)));
        return result;
    }

    public List<VoucherDTO> getPublicCodeVouchers(Double subtotal, String token) throws Exception {
        String query = """
            query PublicCodeVouchers($subtotal: Float) {
              publicCodeVouchers(subtotal: $subtotal) {
                id
                code
                name
                description
                type
                distributionType
                value
                minOrderValue
                maxDiscount
                totalUsageLimit
                usedCount
                giftedCount
                perUserLimit
                startDate
                endDate
                isActive
              }
            }
        """;

        JsonObject variables = new JsonObject();
        if (subtotal != null) variables.addProperty("subtotal", subtotal);

        JsonObject response = graphQLService.executeQuery(query, variables, token);
        throwIfGraphQLErrors(response);

        JsonArray vouchers = response.getAsJsonObject("data").getAsJsonArray("publicCodeVouchers");
        List<VoucherDTO> result = new ArrayList<>();
        vouchers.forEach(v -> result.add(gson.fromJson(v, VoucherDTO.class)));
        return result;
    }

    public void createVoucher(Map<String, Object> payload, String token) throws Exception {
        String mutation = """
            mutation CreateVoucher(
              $code: String!,
              $name: String!,
              $description: String,
              $type: VoucherType!,
              $distributionType: VoucherDistributionType,
              $value: Float!,
              $minOrderValue: Float,
              $maxDiscount: Float,
              $totalUsageLimit: Int,
              $perUserLimit: Int,
              $giftConditionType: GiftConditionType,
              $minGiftAmount: Float,
              $maxGiftAmount: Float,
              $minGiftReviewCount: Int,
              $maxGiftReviewCount: Int,
              $startDate: String!,
              $endDate: String!,
              $isActive: Boolean
            ) {
              createVoucher(
                code: $code,
                name: $name,
                description: $description,
                type: $type,
                distributionType: $distributionType,
                value: $value,
                minOrderValue: $minOrderValue,
                maxDiscount: $maxDiscount,
                totalUsageLimit: $totalUsageLimit,
                perUserLimit: $perUserLimit,
                giftConditionType: $giftConditionType,
                minGiftAmount: $minGiftAmount,
                maxGiftAmount: $maxGiftAmount,
                minGiftReviewCount: $minGiftReviewCount,
                maxGiftReviewCount: $maxGiftReviewCount,
                startDate: $startDate,
                endDate: $endDate,
                isActive: $isActive
              ) {
                id
              }
            }
        """;

        JsonObject variables = new JsonObject();
        variables.addProperty("code", (String) payload.get("code"));
        variables.addProperty("name", (String) payload.get("name"));
        if (payload.get("description") != null) variables.addProperty("description", (String) payload.get("description"));
        variables.addProperty("type", (String) payload.get("type"));
        if (payload.get("distributionType") != null) variables.addProperty("distributionType", (String) payload.get("distributionType"));
        variables.addProperty("value", (Double) payload.get("value"));
        if (payload.get("minOrderValue") != null) variables.addProperty("minOrderValue", (Double) payload.get("minOrderValue"));
        if (payload.get("maxDiscount") != null) variables.addProperty("maxDiscount", (Double) payload.get("maxDiscount"));
        if (payload.get("totalUsageLimit") != null) variables.addProperty("totalUsageLimit", (Integer) payload.get("totalUsageLimit"));
        if (payload.get("perUserLimit") != null) variables.addProperty("perUserLimit", (Integer) payload.get("perUserLimit"));
        if (payload.get("giftConditionType") != null) variables.addProperty("giftConditionType", (String) payload.get("giftConditionType"));
        if (payload.get("minGiftAmount") != null) variables.addProperty("minGiftAmount", (Double) payload.get("minGiftAmount"));
        if (payload.get("maxGiftAmount") != null) variables.addProperty("maxGiftAmount", (Double) payload.get("maxGiftAmount"));
        if (payload.get("minGiftReviewCount") != null) variables.addProperty("minGiftReviewCount", (Integer) payload.get("minGiftReviewCount"));
        if (payload.get("maxGiftReviewCount") != null) variables.addProperty("maxGiftReviewCount", (Integer) payload.get("maxGiftReviewCount"));
        variables.addProperty("startDate", (String) payload.get("startDate"));
        variables.addProperty("endDate", (String) payload.get("endDate"));
        variables.addProperty("isActive", true);

        JsonObject response = graphQLService.executeQuery(mutation, variables, token);
        throwIfGraphQLErrors(response);
    }

    public void toggleVoucherStatus(String id, String token) throws Exception {
        String mutation = """
            mutation ToggleVoucher($id: ID!) {
              toggleVoucherStatus(id: $id) {
                id
              }
            }
        """;
        JsonObject variables = new JsonObject();
        variables.addProperty("id", id);
        JsonObject response = graphQLService.executeQuery(mutation, variables, token);
        throwIfGraphQLErrors(response);
    }

    public void deleteVoucher(String id, String token) throws Exception {
        String mutation = """
            mutation DeleteVoucher($id: ID!) {
              deleteVoucher(id: $id)
            }
        """;
        JsonObject variables = new JsonObject();
        variables.addProperty("id", id);
        JsonObject response = graphQLService.executeQuery(mutation, variables, token);
        throwIfGraphQLErrors(response);
    }

    private void throwIfGraphQLErrors(JsonObject response) throws Exception {
        if (response.has("errors") && !response.get("errors").isJsonNull()) {
            var errors = response.getAsJsonArray("errors");
            if (errors.size() > 0) {
                throw new Exception(errors.get(0).getAsJsonObject().get("message").getAsString());
            }
        }
    }
}