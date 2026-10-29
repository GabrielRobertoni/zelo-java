// Comportamentos gerais da interface do Zelo (sem dependências além do Bootstrap).
document.addEventListener('DOMContentLoaded', () => {
    // Pede confirmação antes de ações destrutivas (formulários com data-confirmar).
    document.querySelectorAll('form[data-confirmar]').forEach((formulario) => {
        formulario.addEventListener('submit', (evento) => {
            if (!window.confirm(formulario.dataset.confirmar)) {
                evento.preventDefault();
            }
        });
    });

    // Evita envio duplicado: desabilita o botão após o primeiro clique válido.
    document.querySelectorAll('form').forEach((formulario) => {
        formulario.addEventListener('submit', (evento) => {
            if (evento.defaultPrevented) {
                return;
            }
            const botao = formulario.querySelector('button[type="submit"]');
            if (botao) {
                window.setTimeout(() => { botao.disabled = true; }, 0);
            }
        });
    });

    // Preenche o login com uma conta de demonstração.
    document.querySelectorAll('[data-conta-demo]').forEach((botao) => {
        botao.addEventListener('click', () => {
            document.getElementById('username').value = botao.dataset.contaDemo;
            const senha = document.getElementById('password');
            senha.value = 'Zelo@123';
            senha.focus();
        });
    });

    // Envia o formulário ao trocar a opção (filtro de período do painel).
    document.querySelectorAll('[data-enviar-ao-mudar]').forEach((campo) => {
        campo.addEventListener('change', () => campo.form.submit());
    });

    // Copia o código de convite da clínica.
    document.querySelectorAll('[data-copiar]').forEach((botao) => {
        botao.addEventListener('click', async () => {
            const texto = document.querySelector(botao.dataset.copiar).textContent.trim();
            try {
                await navigator.clipboard.writeText(texto);
                botao.innerHTML = '<i class="bi bi-check2"></i> Copiado';
            } catch (erro) {
                window.prompt('Copie o código:', texto);
            }
        });
    });
});
