# Metrics

## Prometheus

Eventeum includes a prometheus metrics export endpoint.

It includes standard jvm, tomcat metrics enabled by
spring-boot https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready-metrics-export-prometheus https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready-metrics-meter.

Added to the standard metrics, custom metrics have been added:

* eventeum_%Network%_syncing: 1 if node is syncing (latestBlock + syncingThresholds < currentBlock). 0 if not syncing
* eventeum_%Network%_latestBlock: latest block read by Eventeum
* eventeum_%Network%_currentBlock: Current node block
* eventeum_%Network%_status: Current node status. 0 = Subscribed, 1 = Connected, 2 = Down

All metrics include application="Eventeum",environment="local" tags.

The endpoint is: GET /monitoring/prometheus

## Next Steps

- [Known Caveats / Issues](issues.md)

## Previous Steps

- [Getting started](getting_started.md)
- [Configuration](configuration.md)
- [Usage](usage.md)
- [Metrics](metrics.md)
