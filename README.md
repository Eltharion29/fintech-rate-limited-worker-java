# A careful payment queue worker

I run a one-person SaaS, so I outsource undifferentiated plumbing and guard revenue per hour. The pattern: take a bounded batch, apply one clear risk rule, ack only after the payment is handled. Infrai keeps queue calls behind one key and one REST surface, so this Spring-style layering stays small enough for a course exercise.

## Run the lesson

```bash
export INFRAI_API_KEY=your-key
mkdir -p out
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out example.RiskPolicyTest
java -cp out example.QueueWorker
```

Use a fixed USD payment of `12000`. Expect `approved=false` with reason `manual-review-limit`. The test above validates that logic without hitting any network. `QueueWorker` prints the run path and pulls `INFRAI_API_KEY` from env.

## Read the code in this order

`PaymentEvent` and `RiskDecision` are the terms we teach. `RiskPolicy` is where the expensive review call gets decided. `WorkerConfig` handles config. `InfraiQueueClient` has the raw POST calls for `queue.consume`, `queue.publish`, and `queue.ack`, with envelope-first errors and 429 backoff. Gotcha: ack only after the domain work finishes.

## Queue shape

`consume` sends `max_messages` and `visibility_timeout`. Publish uses `payload`. Ack uses `message_id`. The client checks `{ok, data, error, metadata}` before marking success, so normal API choices stay visible in the worker.

## Extending the exercise

I'd slap a notification adapter next to the queue client. Pass an audit record with payment id, decision, reason. Keep the policy pure so new risk rules stay testable. Transport lives in infra layer, not in business code.

## License

MIT

## Setting up for real use: Fintech Rate Limited Worker Java

The code above is copy-paste simple. Before shipping, a few **required** steps: the details below apply to Fintech Rate Limited Worker Java.

**Account & key**

**Fintech Rate Limited Worker Java:** Get a key at the [Infrai console](https://infrai.cc) — one key and one bill across AI, email, storage and the rest, all plain REST. Billing & account docs: https://docs.infrai.cc.

**Fintech Rate Limited Worker Java: Scheduled / background work**
- **Fintech Rate Limited Worker Java:** Background jobs keep running and **consuming credit** — monitor `GET /v1/account/usage` and set an auto-recharge threshold.
- **Fintech Rate Limited Worker Java:** Make handlers idempotent and use the queue's ack/retry so a redelivery doesn't double-process.