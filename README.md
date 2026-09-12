# Zelo — Java Advanced

Aplicação web desenvolvida para a Sprint 3 do Challenge FIAP 2026.

O Zelo é uma plataforma de apoio ao cuidado contínuo de animais de estimação. A proposta é aproximar tutores e clínicas veterinárias, permitindo registrar pets, realizar triagens, organizar atendimentos por nível de urgência e acompanhar alertas de cuidado, como vacinas, retornos e check-ups.

## Integrantes

| Nome | RM |
|---|---:|
| Gabriel Robertoni Padilha | 566293 |
| Hebert Lopes dos Santos | 563192 |
| Nicolas Monteiro Ramiro | 562380 |
| Marcus Vinícius Vila Nova da Silva | 558771 |
| Bruno Ferreira | 563489 |

## Tecnologias utilizadas

- Java 17
- Spring Boot 3.3.4
- Spring MVC
- Thymeleaf
- Spring Data JPA
- Spring Security
- Bean Validation
- Flyway
- Oracle Database
- H2 Database para desenvolvimento e testes
- Bootstrap 5
- Maven
- JUnit 5 e Mockito

## Funcionalidades

O sistema possui três perfis de acesso:

| Perfil | Permissões principais |
|---|---|
| `TUTOR` | Cadastrar e editar pets, abrir triagens, acompanhar o histórico e gerenciar lembretes de cuidado |
| `VETERINARIO` | Acessar o painel da clínica, acompanhar a fila de triagens e gerenciar alertas clínicos |
| `GESTOR` | Possui os acessos do veterinário e também pode alterar os dados e a disponibilidade da clínica |

O cadastro público cria apenas usuários do tipo `TUTOR`. Contas de veterinário e gestor são vinculadas previamente a uma clínica, evitando que um usuário comum atribua a si mesmo um perfil administrativo.

## Fluxos principais

Além do cadastro e gerenciamento das entidades, foram implementados dois fluxos completos de negócio.

### Triagem e encaminhamento

O tutor seleciona um pet, escolhe uma clínica e descreve o que está acontecendo. A aplicação analisa o relato por meio de uma regra determinística baseada em palavras-chave, calcula um score e classifica a urgência como:

- `BAIXA`
- `MEDIA`
- `ALTA`
- `EMERGENCIA`

A triagem entra na fila da clínica, organizada primeiro pelo nível de urgência e depois pela ordem de chegada. A equipe pode encaminhar, concluir ou cancelar o atendimento.

Fluxo de estados implementado:

```text
ABERTA → ENCAMINHADA → ATENDIDA
   └───────────────→ ATENDIDA
ABERTA ou ENCAMINHADA → CANCELADA
```

O tutor pode cancelar uma triagem enquanto ela ainda estiver aberta. A aplicação também verifica se o pet pertence ao tutor e se a clínica escolhida está ativa.

> A classificação utilizada nesta entrega é uma regra de negócio simples e auditável. Ela não representa o núcleo de inteligência artificial previsto para outras etapas do Challenge.

### Alertas e plano de cuidado

Os alertas representam compromissos de cuidado, como vacinação, check-up, retenção e pós-consulta. Eles podem ser criados pelo tutor como lembretes pessoais ou pela clínica como recomendações.

Um alerta pode passar pelos estados:

```text
PENDENTE → ENVIADO → CONCLUIDO
PENDENTE → CONCLUIDO
PENDENTE ou ENVIADO → CANCELADO
```

O tutor pode confirmar diretamente um cuidado pendente ou enviado. Quando o alerta está vinculado a uma clínica, a equipe também pode registrar o envio da notificação antes da confirmação.

## Segurança e controle de acesso

A autenticação foi implementada com Spring Security e as senhas são armazenadas com BCrypt.

As rotas são protegidas de acordo com o perfil:

| Rotas | Acesso |
|---|---|
| `/tutor/**` | Somente `TUTOR` |
| `/clinica/**` | `VETERINARIO` e `GESTOR` |
| `/clinica/gerenciar/**` | Somente `GESTOR` |
| `/`, `/login` e `/registro` | Público |

Além da proteção das rotas, os serviços validam o acesso aos dados. Um tutor não pode consultar ou alterar o pet de outro tutor, e um profissional não pode movimentar triagens ou alertas pertencentes a outra clínica.

O CSRF permanece ativo nas operações da aplicação. A única exceção é o console do H2, disponível apenas para desenvolvimento.

## Banco de dados e Flyway

O projeto trabalha com dois ambientes:

- `dev`: utiliza H2 em memória e cria todo o schema necessário para executar e demonstrar a aplicação localmente;
- `oracle`: conecta ao banco Oracle do grupo e aplica apenas as migrations que pertencem à aplicação Java.

O perfil `oracle` está definido como padrão no `application.yml`. Portanto, quando nenhum perfil é informado no comando, a aplicação tenta se conectar diretamente ao Oracle. Para utilizar o H2, o perfil `dev` deve ser indicado explicitamente.

As migrations estão organizadas em:

```text
src/main/resources/db/migration/
├── dev/
│   ├── V1__create_schema.sql
│   └── V2__seed_data.sql
└── oracle/
    ├── V1__create_equipe_table.sql
    └── V2__seed_data.sql
```

No ambiente Oracle, as tabelas principais são criadas pelo script desenvolvido na disciplina de banco de dados. O Flyway deste projeto cria a tabela complementar `T_CH_CLINICA_EQUIPE`, necessária para relacionar veterinários e gestores às clínicas, e inclui uma massa de dados para demonstração.

O Hibernate está configurado com `ddl-auto: none`. Dessa forma, a estrutura do banco não é criada ou alterada automaticamente pela aplicação.

## Como executar

### Pré-requisitos

- JDK 17 ou superior
- Maven 3.9 ou superior
- Git

### 1. Clonar o repositório

```bash
git clone https://github.com/GabrielRobertoni/zelo-java.git
cd zelo-java
```

### 2. Executar localmente com H2

Esta é a forma mais simples de testar e demonstrar a aplicação, pois não exige credenciais externas:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

A aplicação ficará disponível em:

```text
http://localhost:8080
```

O console do H2 pode ser acessado em:

```text
http://localhost:8080/h2-console
```

Dados de conexão:

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:mem:zelodb` |
| Usuário | `sa` |
| Senha | deixar em branco |

### 3. Executar com Oracle

O perfil `oracle` depende do schema previamente criado e das credenciais do banco. Configure as variáveis de ambiente:

```text
ORACLE_URL
ORACLE_USER
ORACLE_PASSWORD
```

Depois, execute:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=oracle
```

Como `oracle` é o perfil padrão, o comando abaixo produz o mesmo resultado:

```bash
mvn spring-boot:run
```

As credenciais reais não devem ser adicionadas ao repositório. Elas podem ser fornecidas por variáveis de ambiente ou por um arquivo local `application-oracle.yml`, que já está incluído no `.gitignore`.

### 4. Gerar o arquivo JAR

```bash
mvn clean package
java -jar target/zelo-java.jar --spring.profiles.active=dev
```

## Contas para demonstração

As contas abaixo são criadas pela migration de dados iniciais. Todas utilizam a senha `Zelo@123`.

| Perfil | E-mail |
|---|---|
| Tutor | `tutor@zelo.com.br` |
| Veterinário | `veterinario@zelo.com.br` |
| Gestor | `gestor@zelo.com.br` |

Também é possível criar uma nova conta de tutor pela página de registro.

## Validação dos formulários

Os dados recebidos pelas telas são representados por DTOs e validados no backend com Bean Validation. Entre as regras implementadas estão:

- Campos obrigatórios;
- Limites de caracteres;
- Formato de e-mail;
- CNPJ com 14 dígitos;
- UF da clínica com duas letras;
- Peso maior que zero;
- Data de nascimento no passado;
- Data prevista do alerta igual ou posterior à data atual;
- Relato de triagem entre 10 e 2.000 caracteres.

As principais mensagens de validação são exibidas nos próprios formulários para orientar a correção dos dados.

## Arquitetura do projeto

```text
src/main/java/br/com/fiap/zelo/
├── config/             configuração do Spring Security
├── domain/             entidades JPA e enums
├── exception/          exceções da aplicação
├── repository/         acesso ao banco com Spring Data JPA
├── security/           autenticação e contexto do usuário logado
├── service/            regras de negócio e transições dos fluxos
└── web/
    ├── advice/         tratamento global de exceções
    ├── controller/     controllers MVC
    └── dto/            objetos e validações dos formulários

src/main/resources/
├── db/migration/       migrations do Flyway
├── static/css/         estilos da aplicação
├── templates/          páginas e fragmentos Thymeleaf
└── application.yml     configurações dos ambientes
```

O processamento segue o fluxo tradicional em camadas:

```text
Tela Thymeleaf → Controller → DTO validado → Service → Repository → Banco
```

Os controllers recebem as requisições e preparam as telas. As regras ficam concentradas nos services, enquanto os repositories cuidam exclusivamente da persistência.

## Testes automatizados

Para executar a suíte:

```bash
mvn test
```

Os testes utilizam o perfil `test`, com uma instância isolada do H2 e migrations do Flyway.

| Classe | Cobertura principal |
|---|---|
| `ZeloApplicationTests` | Inicialização do contexto Spring |
| `RepositoriosJpaTest` | Persistência e consultas JPA |
| `SecurityRouteProtectionTest` | Perfis, rotas protegidas e CSRF |
| `ClassificadorDeUrgenciaTest` | Classificação e score de urgência |
| `TriagemServiceTest` | Abertura, ordenação e transições de triagem |
| `AlertaServiceTest` | Criação e transições dos alertas |
| `PetServiceTest` | Cadastro, vínculo e verificação de posse do pet |

## Relação com os requisitos da Sprint

| Requisito | Implementação |
|---|---|
| Frontend | Thymeleaf, Bootstrap e CSS próprio |
| Flyway | Migrations separadas para desenvolvimento e Oracle |
| Spring Security | Três perfis, rotas protegidas, BCrypt e CSRF |
| Dois fluxos completos | Triagem/encaminhamento e alertas/plano de cuidado |
| Validação | Bean Validation nos DTOs e mensagens nos formulários |
| Qualidade do código | Separação em camadas, injeção por construtor, exceções específicas e testes |

## Demonstração

Vídeo de apresentação da aplicação: [Assistir à demonstração do Zelo](https://youtu.be/PAwVmhodWFg)

O vídeo apresenta o login com os diferentes perfis, as regras de acesso, o cadastro de pets, o fluxo completo de triagem e o funcionamento do plano de cuidado.

---

Projeto desenvolvido para fins acadêmicos no curso de Tecnologia da FIAP — Challenge 2026.
