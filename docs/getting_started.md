# Getting Started

This guide will help you set up **Eventeum** on your local machine for development and testing.

## Prerequisites

Before running Eventeum, ensure you have the following installed:

- **Java 21**
- **Maven**
- **Docker** (optional, for containerized deployment)

## Building the Project

1. Clone the repository and navigate to the project directory:

    ```sh
    git clone https://github.com/your-repo/eventeum.git
    cd eventeum
    ```

2. Compile, test, and package the project using Maven:

    ```sh
    mvn clean package
    ```

## Running Eventeum

### Option 1: Running as a JAR File

If you already have a running instance of **MongoDB, Kafka, Zookeeper, and an Ethereum node**, you can start Eventeum as
a JAR file:

```sh
cd server
export SPRING_DATA_MONGODB_HOST=<mongodb-host:port>
export ETHEREUM_NODE_URL=http://<node-host:port>
export ZOOKEEPER_ADDRESS=<zookeeper-host:port>
export KAFKA_ADDRESSES=<kafka-host:port>
export RABBIT_ADDRESSES=<rabbit-host:port>

java -jar target/eventeum-server.jar
```

### Option 2: Running with Docker

To run Eventeum using Docker:

```sh
cd server
docker build -t eventeum:latest .
docker run -p 8060:8060 \
  -e SPRING_DATA_MONGODB_HOST=<mongodb-host:port> \
  -e ETHEREUM_NODE_URL=http://<node-host:port> \
  -e ZOOKEEPER_ADDRESS=<zookeeper-host:port> \
  -e KAFKA_ADDRESSES=<kafka-host:port> \
  -e RABBIT_ADDRESSES=<rabbit-host:port> \
  eventeum
```

### Option 3: Running with Docker-Compose

For a quick test environment with a local Parity Ethereum dev node, you can use **docker-compose**:

```sh
cd server
docker-compose -f docker-compose.yml build
docker-compose -f docker-compose.yml up
```

## Next Steps

- [Configuration](configuration.md)
- [Usage](usage.md)
- [Metrics](metrics.md)
- [Known Caveats / Issues](issues.md)
