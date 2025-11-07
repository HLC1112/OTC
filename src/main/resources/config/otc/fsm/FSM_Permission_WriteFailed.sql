-- FSM_Permission_WriteFailed.sql: Permission grant write failure path

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

-- Step: PermissionWriteFailed
UPDATE T_MERCHANT_APPLICATION
SET status = 'Failed', updated_at = NOW()
WHERE id = :applicationId;

-- Step: PublishWriteFailed -> Outbox
INSERT INTO T_OUTBOX_EVT (
  id, aggregate_type, aggregate_id, event_type, payload_json, created_at
) VALUES (
  :outboxId, 'ApplicationAggregate', :applicationId, 'PermissionGrantFailedEvt', :payloadJson, NOW()
);