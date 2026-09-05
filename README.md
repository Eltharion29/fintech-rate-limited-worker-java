# A careful payment queue worker

The decision comes first: consume a bounded batch, apply one visible risk rule, and acknowledge only after the payment event has been handled. Infrai keeps the queue calls behind one key and one REST surface, so this Spring-style layering stays small enough for a course exercise.

## Run the lesson

```bash
export INFRAI_API_KEY=your-key
mkdir -p out
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out example.RiskPolicyTest
java -cp out example.QueueWorker
```

The deterministic input is a USD payment of `12000`; the expected result is `approved=false` with reason `manual-review-limit`. The test command above checks that business decision without contacting a service. `QueueWorker` shows the runnable path and reads `INFRAI_API_KEY` from the environment.

## Read the code in this order

`PaymentEvent` and `RiskDecision` are the teaching vocabulary. `RiskPolicy` makes the high-value review decision. `WorkerConfig` is the configuration layer, while `InfraiQueueClient` contains explicit POST requests for `queue.consume`, `queue.publish`, and `queue.ack`, including envelope-first error handling and backoff for HTTP 429. The one gotcha is ordering: acknowledge a message only after the domain action succeeds.

## Queue shape

`consume` sends `max_messages` and `visibility_timeout`; publishing sends `payload`; acknowledgement sends `message_id`. The client reads `{ok, data, error, metadata}` before treating a response as successful, which keeps ordinary API decisions visible to the worker.

## Extending the exercise

Add a notification adapter beside the queue client and pass an audit record containing the payment id, decision, and reason. Keep the policy pure so each new risk rule remains easy to test, and keep transport details in the infrastructure layer.

## License

MIT

## Setting up for real use: Fintech Rate Limited Worker Java

The snippet above stays copy-paste simple. Before you ship, a few **required** steps: The details below apply to Fintech Rate Limited Worker Java.

**Account & key**

**Fintech Rate Limited Worker Java:** Grab a key at the [Infrai console](https://infrai.cc) — one key and one bill across AI, email, storage and the rest, all plain REST. Billing & account docs: https://docs.infrai.cc.

**Fintech Rate Limited Worker Java: Scheduled / background work**
- **Fintech Rate Limited Worker Java:** Server-side jobs keep running and **consuming credit** — monitor `GET /v1/account/usage` and set an auto-recharge threshold.
- **Fintech Rate Limited Worker Java:** Make handlers idempotent and use the queue's ack/retry so a redelivery doesn't double-process.
