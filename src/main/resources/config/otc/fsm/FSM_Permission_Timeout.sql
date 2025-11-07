-- FSM_Permission_Timeout.sql: Permission grant timeout path

-- Step: ApplyReq (optional context)
INSERT INTO T_MERCHANT_APPLICATION (
  id, user_id, status, deposit_amount, deposit_currency, created_at, updated_at
) VALUES (
  :applicationId, :userId, 'Requested', :depositAmount, :depositCurrency, NOW(), NOW()
);

-- Step: ValidatingDeposit
UPDATE T_MERCHANT_APPLICATION
SET status = 'ValidatingDeposit', updated_at = NOW()
WHERE id = :applicationId;

-- Step: GrantingPermission
UPDATE T_MERCHANT_APPLICATION
SET status = 'GrantingPermission', updated_at = NOW()
WHERE id = :applicationId;

-- Step: PermissionTimeout
UPDATE T_MERCHANT_APPLICATION
SET status = 'Failed', updated_at = NOW()
WHERE id = :applicationId;

-- Step: PublishTimeout -> Outbox
INSERT INTO T_OUTBOX_EVT (
  id, aggregate_type, aggregate_id, event_type, payload_json, created_at
) VALUES (
  :outboxId, 'ApplicationAggregate', :applicationId, 'PermissionTimeoutEvt', :payloadJson, NOW()
);