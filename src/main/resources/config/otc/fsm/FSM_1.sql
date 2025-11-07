-- FSM_1.sql: Declarative plan for FSM001
-- Variables: :applicationId, :userId, :depositAmount, :depositCurrency, :permissionId, :outboxId, :payloadJson
-- Error mapping: TIMEOUT -> E.OTC.5002, WRITE_FAILED -> E.OTC.5001

-- step: ApplyReq
INSERT INTO T_MERCHANT_APPLICATION (
  id, user_id, deposit_amount, deposit_currency, status, created_at, updated_at
) VALUES (
  :applicationId, :userId, :depositAmount, :depositCurrency, 'Requested', NOW(), NOW()
);

-- step: ValidatingDeposit
UPDATE T_MERCHANT_APPLICATION SET status = 'ValidatingDeposit', updated_at = NOW()
WHERE id = :applicationId;

-- step: ValidateDeposit
-- rules: SELECT min_deposit_amount, allowed_currencies FROM acceptor_rules WHERE rules_id = 'default';
-- check: IF :depositAmount < min_deposit_amount THEN RAISE 'E.OTC.5003';
-- check: IF UPPER(:depositCurrency) NOT IN (allowed_currencies) THEN RAISE 'E.OTC.5004';
SELECT :applicationId AS application_id, TRUE AS valid;

-- step: GrantingPermission
UPDATE T_MERCHANT_APPLICATION SET status = 'GrantingPermission', updated_at = NOW()
WHERE id = :applicationId;

-- step: GrantPermission
INSERT INTO T_USER_PERMISSION (id, user_id, permission, granted_at)
VALUES (:permissionId, :userId, 'OTC_ACCEPTOR', NOW());
UPDATE T_MERCHANT_APPLICATION SET status = 'Approved', updated_at = NOW()
WHERE id = :applicationId;
INSERT INTO T_OUTBOX_EVT (
  id, aggregate_type, aggregate_id, event_type, payload_json, created_at, sent_at
) VALUES (
  :outboxId, 'Application', :applicationId, 'ApplicationApprovedDoc', :payloadJson, NOW(), NULL
);

-- step: PermissionGranted
-- sink only (produced by GrantPermission)

-- step: ApplicationApproved
-- published via Outbox (handled in DA0 layer)