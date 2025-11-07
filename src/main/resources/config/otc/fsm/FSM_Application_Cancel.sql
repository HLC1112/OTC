-- FSM_Application_Cancel.sql: Cancel application process

-- Step: CancelApplication
UPDATE T_MERCHANT_APPLICATION
SET status = 'Canceled', updated_at = NOW()
WHERE id = :applicationId;

-- Step: PublishCanceled -> Outbox
INSERT INTO T_OUTBOX_EVT (
  id, aggregate_type, aggregate_id, event_type, payload_json, created_at
) VALUES (
  :outboxId, 'ApplicationAggregate', :applicationId, 'ApplicationCanceledDoc', :payloadJson, NOW()
);