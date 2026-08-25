package io.point3.p3api.local;

import io.point3.p3api.common.web.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/local-scenario")
@Profile("local-scenario")
@Validated
public class LocalScenarioCleanupController {

  private static final UUID MISSING_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public LocalScenarioCleanupController(DataSource dataSource) {
    this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
  }

  @PostMapping("/reset")
  public ApiResponse<ResetResponse> reset(@Valid @RequestBody ResetRequest request) {
    List<UUID> userIds = findUserIds(request.cognitoSubs());
    if (userIds.isEmpty()) {
      return ApiResponse.ok(new ResetResponse(0));
    }

    Map<String, Object> userParams = Map.of("userIds", userIds);
    List<UUID> storeIds = jdbcTemplate.queryForList(
        "select id from stores where owner_user_id in (:userIds)", userParams, UUID.class);

    Map<String, Object> params = Map.of("userIds", userIds, "storeIds", nonEmptyIds(storeIds));
    int affectedRows = deleteScenarioRows(params);
    affectedRows += jdbcTemplate.update("delete from users where id in (:userIds)", userParams);

    return ApiResponse.ok(new ResetResponse(affectedRows));
  }

  @PostMapping("/seller-onboarding/approve")
  public ApiResponse<ApproveResponse> approveSellerOnboarding(
      @Valid @RequestBody ApproveRequest request) {
    int affectedRows = jdbcTemplate.update("""
        update seller_onboardings
        set status = 'APPROVED', reviewed_at = now(), updated_at = now()
        where id = (
          select seller_onboardings.id
          from seller_onboardings
          join users on users.id = seller_onboardings.applicant_user_id
          where users.cognito_sub = :cognitoSub
          order by seller_onboardings.created_at desc
          limit 1
        )
        """, Map.of("cognitoSub", request.cognitoSub()));

    return ApiResponse.ok(new ApproveResponse(affectedRows));
  }

  private List<UUID> findUserIds(List<String> cognitoSubs) {
    return jdbcTemplate.queryForList(
        "select id from users where cognito_sub in (:cognitoSubs)",
        Map.of("cognitoSubs", cognitoSubs),
        UUID.class);
  }

  private List<UUID> nonEmptyIds(List<UUID> ids) {
    if (ids.isEmpty()) {
      return List.of(MISSING_ID);
    }
    return ids;
  }

  private int deleteScenarioRows(Map<String, Object> params) {
    int affectedRows = 0;
    affectedRows += jdbcTemplate.update("""
        delete from refunds
        where order_id in (
          select id from orders where store_id in (:storeIds) or buyer_user_id in (:userIds)
        )
        or payment_attempt_id in (
          select id from payment_attempts where payer_user_id in (:userIds)
        )
        or requested_by in (:userIds)
        """, params);
    affectedRows += jdbcTemplate.update(
        "delete from orders where store_id in (:storeIds) or buyer_user_id in (:userIds)", params);
    affectedRows += jdbcTemplate.update("""
        delete from payment_attempts
        where payer_user_id in (:userIds)
        or confirmation_id in (
          select id from order_confirmations
          where inquiry_id in (
            select id from inquiries where store_id in (:storeIds) or buyer_user_id in (:userIds)
          )
        )
        """, params);
    affectedRows += jdbcTemplate.update("""
        update order_confirmations
        set replaced_by_confirmation_id = null
        where inquiry_id in (
          select id from inquiries where store_id in (:storeIds) or buyer_user_id in (:userIds)
        )
        """, params);
    affectedRows += jdbcTemplate.update("""
        delete from order_confirmations
        where inquiry_id in (
          select id from inquiries where store_id in (:storeIds) or buyer_user_id in (:userIds)
        )
        or created_by in (:userIds)
        """, params);
    affectedRows += jdbcTemplate.update("""
        delete from chat_message_assets
        where message_id in (
          select id from chat_messages
          where inquiry_id in (
            select id from inquiries where store_id in (:storeIds) or buyer_user_id in (:userIds)
          )
        )
        or asset_id in (select id from assets where uploaded_by in (:userIds))
        """, params);
    affectedRows += jdbcTemplate.update("""
        delete from chat_messages
        where inquiry_id in (
          select id from inquiries where store_id in (:storeIds) or buyer_user_id in (:userIds)
        )
        or sender_user_id in (:userIds)
        """, params);
    affectedRows += jdbcTemplate.update("""
        delete from chat_timeline_items
        where inquiry_id in (
          select id from inquiries where store_id in (:storeIds) or buyer_user_id in (:userIds)
        )
        or sender_user_id in (:userIds)
        """, params);
    affectedRows += jdbcTemplate.update("""
        delete from order_form_submissions
        where inquiry_id in (
          select id from inquiries where store_id in (:storeIds) or buyer_user_id in (:userIds)
        )
        or submitted_by in (:userIds)
        """, params);
    affectedRows += jdbcTemplate.update(
        "delete from inquiries where store_id in (:storeIds) or buyer_user_id in (:userIds)",
        params);
    affectedRows += jdbcTemplate.update(
        "delete from order_form_templates where store_id in (:storeIds)", params);
    affectedRows += jdbcTemplate.update(
        "delete from store_representative_images where store_id in (:storeIds)", params);
    affectedRows += jdbcTemplate.update(
        "delete from store_gallery_items where store_id in (:storeIds)", params);
    affectedRows += jdbcTemplate.update("delete from stores where id in (:storeIds)", params);
    affectedRows += jdbcTemplate.update(
        "delete from asset_variants where asset_id in (select id from assets where uploaded_by in (:userIds))",
        params);
    affectedRows +=
        jdbcTemplate.update("delete from assets where uploaded_by in (:userIds)", params);
    affectedRows += jdbcTemplate.update(
        "delete from seller_onboardings where applicant_user_id in (:userIds) or reviewed_by in (:userIds)",
        params);
    affectedRows +=
        jdbcTemplate.update("delete from notifications where user_id in (:userIds)", params);
    return affectedRows;
  }

  public record ResetRequest(@NotEmpty List<String> cognitoSubs) {
    public ResetRequest {
      cognitoSubs = cognitoSubs == null ? null : List.copyOf(cognitoSubs);
    }

    @Override
    public List<String> cognitoSubs() {
      return cognitoSubs == null ? null : List.copyOf(cognitoSubs);
    }
  }

  public record ResetResponse(int affectedRows) {}

  public record ApproveRequest(@NotBlank String cognitoSub) {}

  public record ApproveResponse(int affectedRows) {}
}
