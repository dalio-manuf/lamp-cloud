# 微服务熔断降级与超时配置手册

> [!NOTE]
> **架构背景说明**：
> Netflix Hystrix 是微服务容错降级领域的经典开源实现。在现代微服务生态（Spring Cloud Alibaba / Spring Boot 3.x）中，容错限流已逐步演进为 **Alibaba Sentinel** 或 **Resilience4j**。
> 本手册汇总提炼了微服务调用链中**超时联动计算、线程隔离、熔断器滑动窗口、线程池容量评估**的核心原理与参数设计，对理解微服务稳定性设计具有普适参考价值。

---

## 1. 超时时间联动与核心计算公式

在 Spring Cloud 体系中，客户端调用通常经过 Ribbon/LoadBalancer 与 熔断组件（Hystrix/Sentinel）双重控制。配置超时的关键在于**确保熔断超时大于底层网络与重试的总耗时**。

### 1.1 关键依赖关系

- 若启用熔断超时（`hystrix.command.default.execution.timeout.enabled=true`）：
  - 最终生效时间取决于 **Ribbon 超时** 与 **Hystrix 超时** 中较小的一个。
- 若关闭熔断超时（`hystrix.command.default.execution.timeout.enabled=false`）：
  - 熔断器不主动进行超时中断，完全由 Ribbon 的 `ReadTimeout` 与网络连接超时控制。

### 1.2 理论计算公式

为确保重试机制能够正常运作，熔断器的超时时间必须大于 Ribbon 的最大重试总耗时，否则请求尚未完成重试便会被熔断器提前掐断：

$$\text{RibbonTotalTimeout} = (\text{ReadTimeout} + \text{ConnectTimeout}) \times (\text{MaxAutoRetries} + 1) \times (\text{MaxAutoRetriesNextServer} + 1)$$

$$\text{HystrixTimeout} > \text{RibbonTotalTimeout}$$

**计算示例**：
- 设 `ReadTimeout = 1000ms`，`ConnectTimeout = 1000ms`
- 设同一实例重试 `MaxAutoRetries = 0`，切换实例重试 `MaxAutoRetriesNextServer = 1`
- 则：$\text{RibbonTotalTimeout} = (1000 + 1000) \times (0 + 1) \times (1 + 1) = 4000\text{ms}$
- 因此，Hystrix 超时时间 `timeoutInMilliseconds` 必须设置在 **4000ms 以上**（建议 4500ms~5000ms）。

---

## 2. 执行与隔离策略 (Execution Properties)

| 配置项 | 默认值 | 作用说明 |
| :--- | :--- | :--- |
| `hystrix.command.default.execution.isolation.strategy` | `THREAD` | 隔离策略，可选 `THREAD`（线程池隔离）或 `SEMAPHORE`（信号量隔离） |
| `hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds` | `1000` | 命令执行超时时间（毫秒） |
| `hystrix.command.default.execution.timeout.enabled` | `true` | 是否启用超时判定 |
| `hystrix.command.default.execution.isolation.thread.interruptOnTimeout` | `true` | 发生超时时是否主动中断执行线程 |
| `hystrix.command.default.execution.isolation.semaphore.maxConcurrentRequests` | `10` | 信号量隔离下的最大并发请求数，超过直接拒绝 |

> **隔离策略选型建议**：
> - **线程池隔离（THREAD）**：默认且推荐方案。支持超时中断与异步调用，资源开销稍大，适用于绝大多数外部接口与网络调用。
> - **信号量隔离（SEMAPHORE）**：纯计数器模式，无额外线程开销，不支持超时主动中断，适用于极高并发、执行速度极快（毫秒级）的本地内存读取或缓存操作。

---

## 3. 服务降级策略 (Fallback Properties)

降级逻辑用于在主流程失败、超时、被拒绝或熔断器开启时，向调用方返回友好的默认响应。

| 配置项 | 默认值 | 作用说明 |
| :--- | :--- | :--- |
| `hystrix.command.default.fallback.enabled` | `true` | 当执行失败或拒绝时，是否尝试调用降级逻辑（`getFallback()`） |
| `hystrix.command.default.fallback.isolation.semaphore.maxConcurrentRequests` | `10` | 降级逻辑的最大并发请求数，超限后降级本身也会被短路抛出异常 |

---

## 4. 熔断器核心参数 (Circuit Breaker)

熔断器根据过去一段时间内的调用成功率动态决定是否放行请求。

```text
       [关闭 (Closed)]  --- 错误率超过阈值 --->  [打开 (Open)]
             ^                                      |
             |                                 休眠窗口过后
        连续成功放行                                 |
             |                                      v
             +------- 测试请求成功 / 失败 ------- [半开 (Half-Open)]
```

| 配置项 | 默认值 | 作用说明 |
| :--- | :--- | :--- |
| `hystrix.command.default.circuitBreaker.enabled` | `true` | 是否开启熔断器 |
| `hystrix.command.default.circuitBreaker.requestVolumeThreshold` | `20` | 滑动窗口期内触发熔断判定的最小请求数（未达该数量即使全失败也不熔断） |
| `hystrix.command.default.circuitBreaker.sleepWindowInMilliseconds` | `5000` | 熔断打开后休眠时间（毫秒），该时间内所有请求直接降级，之后进入半开状态试探 |
| `hystrix.command.default.circuitBreaker.errorThresholdPercentage` | `50` | 触发熔断的错误百分比阈值（当错误率 $\ge$ 50% 且达到最小请求数时开启熔断） |
| `hystrix.command.default.circuitBreaker.forceOpen` | `false` | 强制打开熔断器（用于故障应急手动切断） |
| `hystrix.command.default.circuitBreaker.forceClosed` | `false` | 强制关闭熔断器（忽略错误率，始终尝试调用） |

---

## 5. 线程池配置与容量评估 (ThreadPool Properties)

### 5.1 容量评估公式

线程池设计的核心原则是**保持尽可能小以释放容器压力，防止慢请求占满系统资源导致雪崩**。容量计算公式：

$$\text{CorePoolSize} = \text{Peak QPS} \times \text{P99 Latency (秒)} + \text{Buffer}$$

**评估案例**：
- 单机峰值每秒处理 1000 个请求（QPS = 1000）
- 99% 的请求在 0.060 秒（60ms）内完成
- 预留一定的缓冲量（如 20%）：
  $$\text{CorePoolSize} = 1000 \times 0.060 + 12 = 72$$

### 5.2 常用线程池参数

| 配置项 | 默认值 | 作用说明 |
| :--- | :--- | :--- |
| `hystrix.threadpool.default.coreSize` | `10` | 核心线程数 |
| `hystrix.threadpool.default.maximumSize` | `10` | 最大线程数（仅在配置了非 -1 队列或特定模式生效） |
| `hystrix.threadpool.default.maxQueueSize` | `-1` | 排队队列大小，`-1` 时使用 `SynchronousQueue`，大于 0 时使用 `LinkedBlockingQueue` |
| `hystrix.threadpool.default.queueSizeRejectionThreshold` | `5` | 即使 `maxQueueSize` 未满，达到此阈值也会拒绝请求（支持动态调整） |
| `hystrix.threadpool.default.keepAliveTimeMinutes` | `1` | 空闲线程存活时间（分钟） |

---

## 6. 指标统计参数 (Metrics)

| 配置项 | 默认值 | 作用说明 |
| :--- | :--- | :--- |
| `hystrix.command.default.metrics.rollingStats.timeInMilliseconds` | `10000` | 滑动统计窗口时间（毫秒） |
| `hystrix.command.default.metrics.rollingStats.numBuckets` | `10` | 滑动窗口切分的桶（Bucket）数量（需满足窗口时间整除桶数） |
| `hystrix.command.default.metrics.rollingPercentile.enabled` | `true` | 是否启用执行百分位统计（P50、P90、P99 等） |
| `hystrix.command.default.metrics.healthSnapshot.intervalInMilliseconds` | `500` | 重新计算健康快照（错误率与吞吐）的时间间隔（毫秒） |

---

## 7. 典型配置参考 (YAML)

```yaml
hystrix:
  command:
    default:
      execution:
        isolation:
          strategy: THREAD
          thread:
            timeoutInMilliseconds: 6000 # 熔断超时，需大于 Ribbon 超时重试总时间
            interruptOnTimeout: true
      circuitBreaker:
        enabled: true
        requestVolumeThreshold: 20
        sleepWindowInMilliseconds: 5000
        errorThresholdPercentage: 50
      fallback:
        enabled: true
        isolation:
          semaphore:
            maxConcurrentRequests: 50
  threadpool:
    default:
      coreSize: 20
      maxQueueSize: 200
      queueSizeRejectionThreshold: 100
```
