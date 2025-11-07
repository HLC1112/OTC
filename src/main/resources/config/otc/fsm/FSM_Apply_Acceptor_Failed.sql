-- FSM_Apply_Acceptor_Failed.sql: Validation failed path for Apply Acceptor

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

-- Step: ValidateDepositFailed
-- Simulate a validation failure; replace with real validation and branching
UPDATE T_MERCHANT_APPLICATION
SET status = 'Failed', updated_at = NOW()
WHERE id = :applicationId;

-- Step: ApplicationFailed -> Outbox
INSERT INTO T_OUTBOX_EVT (
  id, aggregate_type, aggregate_id, event_type, payload_json, created_at
) VALUES (
  :outboxId, 'ApplicationAggregate', :applicationId, 'ApplicationFailedDoc', :payloadJson, NOW()
);