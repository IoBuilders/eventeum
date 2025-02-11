# Eventeum

An Ethereum event listener that bridges your smart contract events and backend microservices. Eventeum listens for
specified event emissions from the Ethereum network, and broadcasts these events into your middleware layer. This
provides a distinct separation of concerns and means that your microservices do not have to subscribe to events directly
to an Ethereum node.

[![Gitter](https://badges.gitter.im/eventeum/community.svg)](https://gitter.im/eventeum/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Eventeum CI](https://github.com/IoBuilders/eventeum/actions/workflows/main.yml/badge.svg)](https://github.com/IoBuilders/eventeum/actions/workflows/main.yml)

## Features

* **Configurable Event Filters**. Supports filtering by smart contract events content, transaction fields (e.g., from,
  to) and more.
* **Resilient Event Extraction**. Designed to handle high volumes of operations while remaining tolerant to issues such
  as node connection failures.
* **Configurable Broadcast Recipients**. Events can be broadcast to databases, queues, in-memory objects when embedded
  in another application, among other options. New recipients can be easily added.
* **Flexible Deployment**. Can be deployed as a standalone microservice with a REST API for event filter configuration
  or embedded within another backend component, exposing Java interfaces for seamless integration.
* **Multi-DLT Support**. Compatible with multiple DLT protocols, including Ethereum clients, Hedera mirror nodes, and
  more.

## Supported Broadcast Mechanisms

* Kafka
* HTTP Post
* RabbitMQ
* Pulsar

For **RabbitMQ**, you can configure the following extra values

* `rabbitmq.blockNotification`
* `rabbitmq.routingKey.contractEvents`
* `rabbitmq.routingKey.blockEvents`
* `rabbitmq.routingKey.transactionEvents`

## Documentation

- [Getting started](./docs/getting_started.md)
- [Configuration](./docs/configuration.md)
- [Usage](./docs/usage.md)
- [Metrics](./docs/metrics.md)
- [Known Caveats / Issues](./docs/issues.md)
