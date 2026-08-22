# User Mail Sender MS

Projeto de estudo composto por dois microsserviços Spring Boot. O serviço de usuários mantém os cadastros e publica um evento no RabbitMQ. O serviço de e-mails consome esse evento, envia a mensagem pelo SMTP do Gmail e registra o resultado no PostgreSQL.

## Arquitetura

```text
Cliente HTTP
    |
    v
user (porta 8081) ---> PostgreSQL user (porta 5435)
    |
    v
RabbitMQ / email-queue
    |
    v
email (porta 8080) --> Gmail SMTP
    |
    v
PostgreSQL email (porta 5433)
```

O cadastro de um usuário é síncrono até a publicação do evento. O envio do e-mail acontece de forma assíncrona pelo consumidor da fila `email-queue`.

## Tecnologias

- Java 17
- Spring Boot 3.5
- Spring Web
- Spring Data JPA
- Spring Validation
- Spring AMQP
- Spring Mail e JavaMailSender
- PostgreSQL 16
- RabbitMQ
- Flyway
- Maven
- Docker Compose

## Estrutura do repositório

```text
user-mail-sender-ms/
|-- user/   microsserviço de usuários
|-- email/  microsserviço de envio de e-mails
`-- README.md
```

### Microsserviço user

Responsabilidades:

- criar, listar, atualizar e excluir usuários;
- rejeitar e-mails duplicados;
- publicar um evento na fila após o cadastro;
- controlar o schema do banco com Flyway.

### Microsserviço email

Responsabilidades:

- consumir eventos da fila `email-queue`;
- montar e enviar mensagens com `JavaMailSender`;
- registrar cada tentativa no banco;
- armazenar os estados `PENDING`, `SENT` e `FAILED`.

O estado `SENT` informa que o servidor SMTP aceitou a mensagem. Ele não confirma leitura nem entrega na caixa de entrada do destinatário.

## Pré-requisitos

- JDK 17 ou superior
- Docker Desktop
- uma conta Gmail com verificação em duas etapas
- uma senha de app do Google
- Git

Não é necessário instalar o Maven globalmente. O repositório possui Maven Wrapper no módulo `user`.

## Subindo a infraestrutura

Execute os comandos a partir da raiz do repositório.

### RabbitMQ

O projeto não possui um Compose para o RabbitMQ. Para criar um container local com o painel de gerenciamento:

```powershell
docker run -d --name ms-rabbitmq --hostname ms-rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

Se o container já existir e estiver parado:

```powershell
docker start ms-rabbitmq
```

Painel de gerenciamento:

```text
URL: http://localhost:15672
Usuário: guest
Senha: guest
```

### PostgreSQL do serviço user

```powershell
docker compose -f user/docker-compose.yml up -d
```

Configuração:

```text
Host: localhost
Porta: 5435
Database: ms-user-ms
Usuário: postgres-user
Senha: postgres-password
```

### PostgreSQL do serviço email

```powershell
docker compose -f email/docker-compose.yml up -d
```

Configuração:

```text
Host: localhost
Porta: 5433
Database: ms_email_ms
Usuário: postgres
Senha: postgres
```

Para verificar os containers:

```powershell
docker ps
```

## Variáveis de ambiente

### RabbitMQ

Os dois microsserviços precisam de `RABBITMQ_ADDRESSES`:

```powershell
$env:RABBITMQ_ADDRESSES="amqp://guest:guest@localhost:5672"
```

### Gmail

Somente o microsserviço `email` precisa das credenciais SMTP:

```powershell
$env:GMAIL_USERNAME="seuemail@gmail.com"
$env:GMAIL_APP_PASSWORD="sua-senha-de-app"
```

`GMAIL_USERNAME` é a conta que enviará as mensagens. `GMAIL_APP_PASSWORD` deve receber a senha de app de 16 caracteres gerada na Conta Google, sem espaços. Não utilize a senha normal da conta.

As variáveis definidas dessa forma existem apenas no terminal atual. Se a aplicação for iniciada pelo IntelliJ, configure os mesmos nomes em `Run > Edit Configurations > Environment variables`.

Credenciais reais não devem ser adicionadas ao `application.yml` nem commitadas no repositório.

## Executando as aplicações

Abra dois terminais na raiz do projeto.

### Serviço user

```powershell
$env:RABBITMQ_ADDRESSES="amqp://guest:guest@localhost:5672"
cd user
.\mvnw.cmd spring-boot:run
```

O serviço ficará disponível em `http://localhost:8081`.

### Serviço email

O wrapper presente no diretório `email` não possui os arquivos internos necessários. Use o wrapper do módulo `user`:

```powershell
$env:RABBITMQ_ADDRESSES="amqp://guest:guest@localhost:5672"
$env:GMAIL_USERNAME="seuemail@gmail.com"
$env:GMAIL_APP_PASSWORD="sua-senha-de-app"
cd email
..\user\mvnw.cmd -f pom.xml spring-boot:run
```

O serviço ficará disponível em `http://localhost:8080` e começará a consumir a fila `email-queue`.

## API de usuários

### Criar usuário

```http
POST /user
Content-Type: application/json
```

```json
{
  "name": "Maria Silva",
  "email": "maria.silva@example.com"
}
```

Exemplo com PowerShell:

```powershell
curl.exe -X POST http://localhost:8081/user `
  -H "Content-Type: application/json" `
  -d '{"name":"Maria Silva","email":"maria.silva@example.com"}'
```

Resposta de sucesso: `201 Created`.

### Listar usuários

```http
GET /user/list
```

```powershell
curl.exe http://localhost:8081/user/list
```

Resposta de sucesso: `200 OK`.

### Atualizar usuário

```http
PUT /user/{id}
Content-Type: application/json
```

```json
{
  "name": "Maria Souza",
  "email": "maria.souza@example.com"
}
```

```powershell
curl.exe -X PUT http://localhost:8081/user/UUID_DO_USUARIO `
  -H "Content-Type: application/json" `
  -d '{"name":"Maria Souza","email":"maria.souza@example.com"}'
```

Resposta de sucesso: `200 OK`.

### Excluir usuário

```http
DELETE /user/delete/{id}
```

```powershell
curl.exe -X DELETE http://localhost:8081/user/delete/UUID_DO_USUARIO
```

Resposta de sucesso: `204 No Content`.

## Respostas de erro

| Status | Situação |
| --- | --- |
| `400 Bad Request` | nome vazio ou e-mail inválido |
| `404 Not Found` | usuário não encontrado durante a atualização ou exclusão |
| `409 Conflict` | e-mail já cadastrado |

Exemplo de conflito:

```json
{
  "type": "about:blank",
  "title": "E-mail já cadastrado",
  "status": 409,
  "detail": "O e-mail 'maria.silva@example.com' já está cadastrado.",
  "instance": "/user"
}
```

## Versionamento do banco de usuários

O Flyway executa as migrations de `user/src/main/resources/db/migration` na inicialização do serviço:

| Versão | Descrição |
| --- | --- |
| V1 | cria a tabela `tb_users` |
| V2 | limpa a tabela e insere três usuários de exemplo |
| V3 | adiciona a restrição de unicidade ao e-mail |

A V2 usa `TRUNCATE TABLE` e remove os usuários existentes quando é aplicada pela primeira vez. Depois de registrada em `flyway_schema_history`, ela não é executada novamente.

O Hibernate do serviço `user` está com `ddl-auto: validate`; alterações futuras no schema devem ser feitas em novas migrations, sem editar migrations que já tenham sido aplicadas.

O serviço `email` ainda utiliza `ddl-auto: update` para manter a tabela `tb_email`.

## Consultando os bancos pelo terminal

### Usuários

```powershell
docker compose -f user/docker-compose.yml exec postgres `
  psql -U postgres-user -d ms-user-ms -c "SELECT * FROM tb_users;"
```

### E-mails

```powershell
docker compose -f email/docker-compose.yml exec postgres `
  psql -U postgres -d ms_email_ms -c "SELECT * FROM tb_email;"
```

Para verificar as migrations aplicadas:

```powershell
docker compose -f user/docker-compose.yml exec postgres `
  psql -U postgres-user -d ms-user-ms -c "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
```

## DBeaver

Crie uma conexão PostgreSQL para cada banco usando as configurações da seção de infraestrutura. Depois de conectar, as tabelas ficam em:

```text
Schemas > public > Tables
```

No banco de usuários, procure `tb_users` e `flyway_schema_history`. No banco de e-mails, procure `tb_email`.

## Build

### Serviço user

```powershell
cd user
.\mvnw.cmd clean package -DskipTests
```

### Serviço email

```powershell
cd email
..\user\mvnw.cmd -f pom.xml clean package -DskipTests
```

Os artefatos são gerados em `user/target` e `email/target`.

## Testes

Cada módulo possui um teste básico de carregamento do contexto Spring. Para executá-los, os bancos precisam estar ativos e as variáveis de ambiente devem estar configuradas.

```powershell
cd user
.\mvnw.cmd test
```

```powershell
cd email
..\user\mvnw.cmd -f pom.xml test
```

## Problemas comuns

### Could not resolve placeholder

Erros com `RABBITMQ_ADDRESSES`, `GMAIL_USERNAME` ou `GMAIL_APP_PASSWORD` indicam que a variável não foi definida no mesmo processo que iniciou a aplicação. Configure-a no terminal atual ou na configuração de execução do IntelliJ e reinicie a aplicação.

### Connection refused no PostgreSQL

Confirme se os containers estão ativos e se as portas correspondem ao serviço:

```powershell
docker ps
```

- `user`: porta `5435`;
- `email`: porta `5433`.

### Falha de autenticação no Gmail

Confirme se a verificação em duas etapas está habilitada, se foi utilizada uma senha de app e se os 16 caracteres foram informados sem espaços.

### Mensagens antigas aparecem ao iniciar o serviço email

A fila `email-queue` é durável. Mensagens publicadas enquanto o consumidor estava desligado permanecem no RabbitMQ e são processadas quando o serviço volta a executar.

### E-mail salvo como FAILED

Consulte o log do serviço `email` para ver a exceção retornada pelo SMTP. Atualmente, falhas são registradas no banco, mas não possuem retentativa automática nem fila de mensagens mortas.

## Encerrando o ambiente

Para parar os bancos sem apagar os volumes:

```powershell
docker compose -f user/docker-compose.yml stop
docker compose -f email/docker-compose.yml stop
docker stop ms-rabbitmq
```

Para iniciar novamente:

```powershell
docker compose -f user/docker-compose.yml start
docker compose -f email/docker-compose.yml start
docker start ms-rabbitmq
```
