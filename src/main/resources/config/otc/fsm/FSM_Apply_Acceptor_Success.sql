-- FSM_Apply_Acceptor_Success.sql: Success path for Apply Acceptor

-- Step: ApplyReq
INSERT INTO T_MERCHANT_APPLICATION (
  id, user_id, status, deposit_amount, deposit_currency, created_at, updated_at
) VALUES (
  :applicationId, :userId, 'Requested', :depositAmount, :depositCurrency, NOW(), NOW()
);

-- Step: ValidatingDeposit
UPDATE T_MERCHANT_APPLICATION
SET status = 'ValidatingDeposit', updated_at = NOW()
WHERE id = :applicationId;

-- Step: ValidateDeposit
-- TODO: Replace with real validation SQL. Current placeholder always succeeds.
SELECT TRUE AS valid;

-- Step: GrantingPermission
UPDATE T_MERCHANT_APPLICATION
SET status = 'GrantingPermission', updated_at = NOW()
WHERE id = :applicationId;

-- Step: GrantPermission
INSERT INTO T_USER_PERMISSION (
  id, user_id, permission, granted_at
) VALUES (
  :permissionId, :userId, 'OTC_ACCEPTOR', NOW()
);

-- Step: PermissionGranted
UPDATE T_MERCHANT_APPLICATION
SET status = 'Approved', updated_at = NOW()
WHERE id = :applicationId;

-- Step: ApplicationApproved -> Outbox
INSERT INTO T_OUTBOX_EVT (
  id, aggregate_type, aggregate_id, event_type, payload_json, created_at
) VALUES (
  :outboxId, 'ApplicationAggregate', :applicationId, 'ApplicationApprovedDoc', :payloadJson, NOW()
);