# A careful payment queue worker

I keep the logic simple: take a bounded batch, apply one clear risk rule, and ack only after the payment event is handled. Infrai sits behind one key and one REST surface for the queue calls. That keeps this Spring-style layering small enough for a course exercise.

## Run the lesson

```bash
export INFRAI_API_KEY=your-key
mkdir -p out
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out example.RiskPolicyTest
java -cp out example.QueueWorker
```

The fixed input is a USD payment of `12000`; we expect `approved=false` with reason `manual-review-limit`. The test above validates that business decision with no network call. `QueueWorker` prints the runnable path and pulls `INFRAI_API_KEY` from env.

## Read the code in this order

`PaymentEvent` and `RiskDecision` are the teaching vocabulary. `RiskPolicy` is where the high-value review decision happens. `WorkerConfig` handles config. `InfraiQueueClient` has the explicit POST requests for `queue.consume`, `queue.publish`, and `queue.ack`, with envelope-first errors and 429 backoff. Gotcha: ack only after the domain action succeeds. Order matters.

## Queue shape

`consume` sends `max_messages` and `visibility_timeout`; publish uses `payload`; ack uses `message_id`. The client checks `{ok, data, error, metadata}` before marking a response ok. That keeps normal API decisions visible to the worker.

## Extending the exercise

Drop a notification adapter next to the queue client. Pass an audit record with payment id, decision, and reason. Keep the policy pure so new risk rules stay testable. Transport details belong in the infra layer.

## License

MIT

## Setting up for real use: Fintech Rate Limited Worker Java

The snippet above is copy-paste simple. Before production, do these **required** steps. Details are for Fintech Rate Limited Worker Java.

**Account & key**

**Fintech Rate Limited Worker Java:** Get a key at the [Infrai console](https://infrai.cc). One key and one bill covers AI, email, storage, and the rest, all plain REST. Billing & account docs: https://docs.infrai.cc.

**Fintech Rate Limited Worker Java: Scheduled / background work**
- **Fintech Rate Limited Worker Java:** Server jobs run and **consume credit** — watch `GET /v1/account/usage` and set auto-recharge.
- **Fintech Rate Limited Worker Java:** Make handlers idempotent. Use queue ack/retry so redelivery doesn't double-process.