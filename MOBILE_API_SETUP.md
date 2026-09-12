# Zelo Java — integração com o aplicativo mobile

O projeto Java original utiliza páginas Thymeleaf, formulários HTML e autenticação por sessão. Para o aplicativo React Native/Expo, esta versão adiciona uma API REST JSON sem remover as páginas web existentes.

## O que foi adicionado

A camada mobile inclui login com token Bearer, cadastro de tutor, consulta da sessão atual, pets, triagens, alertas e clínicas. Os endpoints principais estão documentados no README do aplicativo mobile.

Os arquivos REST ficam em `src/main/java/br/com/fiap/zelo/web/api/`. O filtro e o serviço de token ficam em `src/main/java/br/com/fiap/zelo/security/`. O tratamento de erros REST fica em `src/main/java/br/com/fiap/zelo/web/advice/ApiExceptionHandler.java`.

## Executar no Windows

Na pasta raiz deste projeto Java:

```powershell
mvnw.cmd spring-boot:run
```

Se o Maven estiver instalado globalmente:

```powershell
mvn spring-boot:run
```

A API deve ficar disponível em `http://localhost:8080`.

## Testar o cadastro sem o aplicativo

No PowerShell:

```powershell
$body = @{
    nome = "Usuario Teste"
    email = "teste-unico@exemplo.com"
    senha = "123456"
    confirmarSenha = "123456"
    telefone = ""
    cidade = ""
    estado = ""
} | ConvertTo-Json

Invoke-RestMethod `
    -Uri "http://localhost:8080/api/auth/register" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

Uma resposta JSON com `token` confirma que a integração está funcionando. Se o e-mail já existir, use outro endereço.

## Integração com o app

No arquivo `.env` do projeto `zelo-mobile`, use:

```env
EXPO_PUBLIC_API_BASE_URL=http://localhost:8080
```

Depois reinicie o Expo:

```powershell
npm start -- --clear
```

A API não precisa ser publicada. O computador precisa estar executando simultaneamente o Spring Boot e o aplicativo/preview. Para um celular físico, troque `localhost` pelo IP local do computador.

## Observação de segurança

O segredo do token deve ser fornecido por variável de ambiente em ambiente real. O valor usado para desenvolvimento não deve ser reutilizado em produção.
