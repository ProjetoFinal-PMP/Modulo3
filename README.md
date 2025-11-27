# Módulo 3 – Lambda Kafka (Juan · Frettec)

Este repositório implementa o **Módulo 3** do projeto do aluno Juan: um serviço Spring Boot que simula uma função **“lambda-like”** integrada ao **Apache Kafka**.

A aplicação:

- Recebe mensagens via **HTTP**.
- Publica essas mensagens em um **tópico Kafka** (`lambda-topic`).
- Consome as mensagens do tópico e as processa através de um **handler de domínio**.
- É empacotada em **Docker** e possui **pipeline de CI/CD** para build e push de imagem no Docker Hub.

---

## 1. Visão Geral da Arquitetura

Componentes principais:

- **Aplicação Spring Boot** (`lambda_kafka`)
  - `api` – exposição HTTP.
  - `application` – DTOs, serviços de producer/consumer e handler.
  - `domain` – modelo de domínio e contrato de processamento.
  - `infrastructure` – configuração de Kafka.
- **Ambiente Kafka via Docker Compose**
  - `zookeeper` – coordenação do cluster Kafka.
  - `kafka` – broker principal.
  - `kafdrop` – interface web para inspecionar tópicos e mensagens.
  - `lambda_kafka` – serviço deste módulo.

Fluxo de alto nível:

1. O cliente envia um `POST /api/v1/messages` com o conteúdo da mensagem.
2. A API converte o payload em um `Message` de domínio e envia para o tópico Kafka via `KafkaProducerService`.
3. O `KafkaConsumerService` ouve o tópico `lambda-topic` e, ao receber a mensagem, delega o processamento ao `MessageHandler`.
4. A implementação `DefaultMessageHandler` processa a mensagem e registra logs, simulando um comportamento “lambda-like”.

---

## 2. Estrutura do Projeto

Estrutura em alto nível:

```text
modulo3-juan
├── .gitattributes
├── .gitignore
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── mvnw / mvnw.cmd
├── README.md
├── .github/
│   └── workflows/
│       └── publish.yml
└── src/
    ├── main/
    │   ├── java/br/com/frettec/modulo3/lambda_kafka
    │   │   ├── LambdaKafkaApplication.java
    │   │   ├── api/
    │   │   │   └── MessageController.java
    │   │   ├── application/
    │   │   │   ├── dto/MessageDTO.java
    │   │   │   └── service/
    │   │   │       ├── DefaultMessageHandler.java
    │   │   │       ├── KafkaConsumerService.java
    │   │   │       └── KafkaProducerService.java
    │   │   ├── domain/
    │   │   │   ├── Message.java
    │   │   │   └── MessageHandler.java
    │   │   └── infrastructure/
    │   │       └── config/
    │   │           └── KafkaConfig.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/br/com/frettec/modulo3/lambda_kafka/
            └── LambdaKafkaApplicationTests.java
````

---

## 3. Camadas da Aplicação

### 3.1 Domínio (`domain`)

Pacote: `br.com.frettec.modulo3.lambda_kafka.domain`

* `Message`

  * Modelo de domínio que representa a mensagem.
  * Campo:

    * `content` – conteúdo textual.
  * Utiliza Lombok (`@Data`, `@AllArgsConstructor`, `@NoArgsConstructor`) para gerar getters, setters e construtores.

* `MessageHandler`

  * Interface que define o contrato de processamento:

    ```java
    void handle(Message message);
    ```
  * Permite evoluir o comportamento da “função lambda” sem acoplar o restante da aplicação.

---

### 3.2 Camada de Aplicação (`application`)

Pacote: `br.com.frettec.modulo3.lambda_kafka.application`

#### DTO

* `MessageDTO`

  * DTO usado na API HTTP.
  * Campo:

    * `content` – mensagem enviada pelo cliente.

#### Serviços (`application.service`)

* `KafkaProducerService`

  * Responsável por enviar mensagens ao Kafka.
  * Usa `KafkaTemplate<String, String>`.
  * Tópico padrão:

    ```java
    private static final String TOPIC = "lambda-topic";
    ```

* `KafkaConsumerService`

  * Serviço anotado com `@KafkaListener`.
  * Escuta:

    * Tópico: `lambda-topic`
    * Grupo: `lambda-group-juan`
  * Ao receber um `payload`:

    * Loga o conteúdo.
    * Converte para `Message`.
    * Chama o `MessageHandler`.

* `DefaultMessageHandler`

  * Implementação padrão de `MessageHandler`.
  * Processa a mensagem e registra no log:

    ```text
    Processando mensagem de forma lambda-like: <conteúdo>
    ```

---

### 3.3 API HTTP (`api`)

Pacote: `br.com.frettec.modulo3.lambda_kafka.api`

* `MessageController`

  * Controlador REST.
  * Base path: `/api/v1/messages`.
  * Endpoint principal:

    * `POST /api/v1/messages`

      * Corpo esperado:

        ```json
        {
          "content": "Mensagem de teste"
        }
        ```

      * Comportamento:

        * Loga o conteúdo recebido.
        * Cria um `Message` de domínio.
        * Envia a mensagem para o Kafka via `KafkaProducerService`.
        * Retorna `202 ACCEPTED` com o próprio `MessageDTO` no corpo.

---

### 3.4 Infraestrutura (`infrastructure`)

Pacote: `br.com.frettec.modulo3.lambda_kafka.infrastructure.config`

* `KafkaConfig`

  * Classe de configuração Kafka com `@Configuration`.

  * Exemplo de bean:

    ```java
    @Bean
    public Map<String, Object> adminConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        return props;
    }
    ```

  * Aponta para o host `kafka` definido no `docker-compose.yml`.

---

## 4. Configurações da Aplicação

Arquivo: `src/main/resources/application.properties`

```properties
server.port=8080

spring.kafka.bootstrap-servers=kafka:9092
spring.kafka.consumer.group-id=lambda-group-juan
spring.kafka.consumer.auto-offset-reset=earliest

app.topic=lambda-topic
```

* A aplicação sobe na porta **8080** (exposta como **8083** no Docker Compose).
* Kafka é acessado via `kafka:9092` (nome do serviço no Compose).
* O grupo do consumidor é `lambda-group-juan`.
* O tópico padrão é `lambda-topic`.

---

## 5. Docker e Orquestração

### 5.1 `docker-compose.yml`

Serviços:

* `zookeeper`

  * Imagem: `confluentinc/cp-zookeeper:7.6.1`
  * Porta: `2181`.

* `kafka`

  * Imagem: `confluentinc/cp-kafka:7.6.1`
  * Depende de `zookeeper`.
  * Porta: `9092`.
  * Configurado com listeners para uso dentro do Docker.

* `kafdrop`

  * Imagem: `obsidiandynamics/kafdrop`
  * Variável `KAFKA_BROKERCONNECT=kafka:9092`.
  * Porta: `9000`.
  * Permite inspecionar tópicos e mensagens.

* `lambda_kafka`

  * Serviço deste módulo.
  * `build: .` (usa o `Dockerfile` do projeto).
  * Depende de `kafka`.
  * Porta mapeada: `8083:8080`.

### 5.2 Subindo o ambiente

Na raiz do projeto:

```bash
docker-compose up -d
```

Acessos principais:

* API:

  * `http://localhost:8083/api/v1/messages`
* Kafdrop:

  * `http://localhost:9000`

Para parar:

```bash
docker-compose down
```

---

## 6. Testando o Fluxo

### 6.1 Enviando uma mensagem

Exemplo com `curl`:

```bash
curl -X POST "http://localhost:8083/api/v1/messages" \
  -H "Content-Type: application/json" \
  -d "{\"content\":\"Mensagem de teste do Juan\"}"
```

Comportamento esperado:

1. Log no controller:

   * `[JUAN-LAMBDA] Recebido via HTTP: Mensagem de teste do Juan`
2. O `KafkaProducerService` envia a mensagem para o tópico `lambda-topic`.
3. O `KafkaConsumerService` consome a mensagem.
4. O `DefaultMessageHandler` registra:

   * `Processando mensagem de forma lambda-like: Mensagem de teste do Juan`

### 6.2 Visualizando no Kafdrop

1. Acesse `http://localhost:9000`.
2. Localize o tópico `lambda-topic`.
3. Veja as mensagens publicadas e consumidas.

---

## 7. Pipeline de CI/CD (GitHub Actions)

Arquivo: `.github/workflows/publish.yml`

Responsável por:

* Realizar checkout do código.

* Configurar JDK 21.

* Build do projeto com Maven:

  ```bash
  mvn -B package --file pom.xml
  ```

* Login no Docker Hub usando os secrets:

  * `DOCKER_USERNAME`
  * `DOCKER_PASSWORD`

* Build da imagem Docker:

  * `${{ secrets.DOCKER_USERNAME }}/lambda_kafka:latest`

* Push da imagem para o Docker Hub.

O workflow é disparado em `push` para o branch `main`.

