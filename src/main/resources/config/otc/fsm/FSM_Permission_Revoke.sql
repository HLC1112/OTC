-- FSM_Permission_Revoke.sql: Revoke permission process

-- Step: RevokePermission
UPDATE T_MERCHANT_APPLICATION
SET status = 'Revoked', updated_at = NOW()
WHERE id = :applicationId;

-- Step: PublishRevoked -> Outbox
INSERT INTO T_OUTBOX_EVT (
  id, aggregate_type, aggregate_id, event_type, payload_json, created_at
) VALUES (
  :outboxId, 'ApplicationAggregate', :applicationId, 'PermissionRevokedDoc', :payloadJson, NOW()
);