# OTC-001 押金管理/申请成为承兑商（DDD重构）

本改造将原三层架构迁移为DDD + 事件驱动，提供最小可运行闭环：

- Domain Aggregates：`ApplicationAggregate`, `UserPermissionAggregate`（路径：`dsv/dbs/aggregate`）
- Repository IF：`ApplicationsRepository`, `UserPermissionsRepository`（路径：`dsv/dbs/repository`）
- MySQL Impl：`dat1/mysql/*Impl`，实体在`dat1/entity`，JPA在`dat1/mysql`
- DC（纯计算）：`dsv/dc/DepositValidator`
- DA0（读写DB）：`dsv/da_0/ApplicationReader`, `dsv/da_0/PermissionWriter`
- FSM（Saga）：`dsv/fsm/AcceptorApplicationSaga`
- Adapter：`dsv/adapter/ApiGatewayController`, `EventBusPublisher`
- CDC（Outbox）：`T_OUTBOX_EVT` + `dat2/kafka/OutboxForwarder`（演示用），生产建议使用 Debezium
- 统一消息契约：`common/{cmd,qry,doc,evt}`
- 错误码：`common/error/ErrorCodes.kt`

## 迁移步骤
1. 保留原路由（`POST /otc/acceptor/apply`），内部改为调用FSM。
2. 标注旧Service/Repository为`@Deprecated`，引导使用新的FSM与Repository接口。
3. 数据库执行Flyway迁移（V1~V3）。
4. 配置Kafka与MySQL，启动应用。

## 回滚策略
- 若新FSM出现问题，可临时切回旧Service但仍保留新Repository接口，确保数据模型兼容。
- Outbox转发器为演示用组件，可单独停用，改由Debezium读取。

## 数据兼容
- 表结构新增不破坏原数据；`T_MERCHANT_APPLICATION`保留已有列名。
- Outbox为新增表，不影响主流程。

## 启动与验证
- Gradle：`./gradlew bootRun`
- Flyway会自动迁移（`resources/db/migrations`）。
- 健康检查：`GET /actuator/health`
- 最小演示脚本：
```
curl -X POST http://localhost:8080/otc/acceptor/apply \
  -H 'Content-Type: application/json' \
  -d '{"userId":"u-1","depositAmount":"1500","depositCurrency":"USDT"}'
```
- 期望：HTTP 202 + `applicationId`，日志输出FSM阶段；Kafka `otc.applications`收到`ApplicationApproved`。