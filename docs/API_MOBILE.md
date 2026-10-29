# API REST do aplicativo mobile

Integração com a disciplina de **Mobile Application Development**: o app React Native/Expo do Zelo usa estes endpoints.

- **Base:** `http://localhost:8080` (local) ou a URL do Render.
- **Autenticação:** JWT (HS256). Envie o cabeçalho `Authorization: Bearer <token>`.
- **Validade do token:** 7 dias. Um usuário desativado perde o acesso na hora.
- **Erros** sempre em JSON: `{"message": "...", "status": 400, "errors": {"campo": "mensagem"}}`.

| Status | Quando |
|---|---|
| 400 | Dados inválidos (com `errors` por campo) |
| 401 | Sem token, token inválido ou expirado, ou login incorreto |
| 403 | Perfil sem permissão (ex.: veterinário chamando `/api/pets`) |
| 404 | Recurso inexistente ou de outro usuário |
| 422 | Regra de negócio violada (ex.: cancelar triagem já encaminhada) |

## Autenticação

| Método | Rota | Corpo | Resposta |
|---|---|---|---|
| POST | `/api/auth/login` | `{"email", "senha"}` | `{"token", "usuario"}` |
| POST | `/api/auth/register` | `{"nome", "email", "senha", "confirmarSenha", "telefone?", "cidade?", "estado?"}` | `{"token", "usuario"}` (cria um TUTOR) |
| GET | `/api/auth/me` | — | dados do usuário logado |

## Recursos do tutor (perfil `TUTOR`)

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/pets` | Lista os pets do tutor |
| GET | `/api/pets/{id}` | Detalhe de um pet |
| POST | `/api/pets` | Cadastra (`nome`, `especie` CAO/GATO/OUTRO, `raca?`, `sexo?` M/F/I, `dataNascimento?`, `pesoKg?`, `castrado`) → 201 |
| PUT | `/api/pets/{id}` | Atualiza |
| DELETE | `/api/pets/{id}` | Exclui (somente sem histórico) → 204 |
| GET | `/api/triagens` | Histórico de triagens do tutor |
| POST | `/api/triagens` | Abre triagem (`petId`, `relato`, `clinicaId?`, `canal?`). Sem `clinicaId`, vai para a primeira clínica ativa → 201 |
| PATCH | `/api/triagens/{id}/cancelar` | Cancela uma triagem aberta |
| DELETE | `/api/triagens/{id}` | Remove uma triagem do tutor → 204 |
| GET | `/api/alertas` | Cuidados pendentes e enviados |
| POST | `/api/alertas` | Cria lembrete (`petId`, `tipo` VACINA/CHECKUP/RETENCAO/POS_CONSULTA, `titulo`, `descricao?`, `dataPrevista`) → 201 |
| PATCH | `/api/alertas/{id}/confirmar` | Confirma que o cuidado foi feito |
| PATCH | `/api/alertas/{id}/cancelar` | Cancela |
| DELETE | `/api/alertas/{id}` | Remove → 204 |

## Qualquer usuário autenticado

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/clinicas` | Clínicas que estão recebendo triagens |

## Exemplo rápido (PowerShell)

```powershell
$login = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post `
    -ContentType "application/json" -Body '{"email":"tutor@zelo.com.br","senha":"Zelo@123"}'
Invoke-RestMethod -Uri "http://localhost:8080/api/pets" -Headers @{ Authorization = "Bearer $($login.token)" }
```

No app, configure `EXPO_PUBLIC_API_BASE_URL` com a URL da API. Em celular físico, use o IP da máquina na rede local ou a URL do Render.
