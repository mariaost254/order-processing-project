
# Distributed Order Processing System

This project consists of three Spring Boot microservices:  
| Service | Responsibilities |  
| ------ | ------ |  
| order-service | Accepts orders via REST, publishes events to Kafka, and stores orders in Redis. |  
| inventory-service | Consumes order events, validates inventory by category, and emits result events. |  
| notification-service | Consumes inventory results and logs approval or rejection (with missing items). |  


## Technologies Used
- Java 21
- Spring Boot
- Kafka
- Redis
- Docker + DOcker Compose
- Maven

## Architecture
Arcitercure also includes the used kafka topics  
https://drive.google.com/file/d/13mciujckZNiRwcpr5OR-oQkhTLeXyOf1/view?usp=sharing

#### Retry & DLQ
Kafka consumers use @RetryableTopic (3 attempts + DLT fallback) Kafka producers use Spring Retry (@Retryable + @Recover) DLT topic: order.created-dlt is handled by inventory-service

#### Redis Usage
Key format: order:{orderId} Value type: Serialized OrderRequestDTO Example key: order:4ef24abc-44d2-471f-8bc9-13f3dfb8c4a1

## Installation
Shell script is included  
This script will:
- Build the common shared module
- Package all services
- Launch Kafka, Redis, and services via Docker Compose

```sh
chmod +x start-all.sh
./start-all.sh
```

## API Usage
Endpoint: POST api/orders URL: http://localhost:8080/api/orders  
Content-Type: application/json  
A full set of scenarios is included in the provided Postman collection.

## Bonus
- Fully Dockerized (Kafka + Redis + Microservices)
- Postman collection included
- Common module extracted for reuse

## AI contribution
- docker-compose
- dockerfiles
- mock DB
- assistance with config files