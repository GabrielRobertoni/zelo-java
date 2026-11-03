# Deploy no Render (gratuito)

O deploy não precisa de banco externo. Ao iniciar, a aplicação tenta o Oracle do grupo: se o Render conseguir alcançá-lo, usa o Oracle; senão, usa o H2 embutido, populado pelo Flyway. Os logs do Render mostram qual foi escolhido na linha `Database:`.

Para o site no Render usar sempre o H2 (dados de demonstração zerados a cada reinício), adicione no Render a variável de ambiente `ZELO_ORACLE` com o valor `false`.

## Passo a passo

1. Suba o projeto para um repositório **público** no GitHub. O `README.md` principal tem as instruções.
2. Acesse <https://render.com>, clique em **Get Started** e entre com a sua conta do GitHub.
3. No painel do Render, clique em **New > Blueprint**.
4. Selecione o repositório do Zelo e confirme. O Render lê o arquivo `render.yaml` e cria:
   - um *Web Service* chamado `zelo-java`, construído pelo `Dockerfile`;
   - a variável `ZELO_JWT_SECRET`, gerada automaticamente;
   - o *health check* em `/actuator/health`.
5. Clique em **Apply** e aguarde o primeiro build (de 5 a 10 minutos: ele baixa as dependências e compila).
6. Quando o status ficar **Live**, copie a URL (ex.: `https://zelo-java.onrender.com`) e cole no topo do `README.md`, em **Aplicação online**.

### Alternativa sem Blueprint

**New > Web Service** → escolha o repositório → **Language: Docker** → **Instance type: Free** → em **Environment** adicione `ZELO_JWT_SECRET` com um texto aleatório de 40 ou mais caracteres → em **Advanced** defina **Health Check Path** como `/actuator/health` → **Create Web Service**.

## Conferindo o deploy

- `https://SUA-URL/actuator/health` deve responder `{"status":"UP"}`.
- Entre com `gestor@zelo.com.br` / `Zelo@123` e abra o painel da clínica.

## Comportamento do plano gratuito

- O serviço **dorme após 15 minutos sem acesso**. O próximo acesso leva alguns minutos para acordar, porque a aplicação reconecta ao Oracle. **Abra o link uns minutos antes de gravar o vídeo ou de apresentar.**
- Conectado ao Oracle, os dados ficam salvos entre reinícios: contas, pets e triagens criados nos testes continuam lá.
- Se o serviço estiver usando o H2 (Oracle indisponível ou `ZELO_ORACLE=false`), o banco em memória é recriado a cada reinício com a massa de demonstração.

## Atualizações

Cada `git push` na branch configurada dispara um novo deploy automático.
