// Desenha os gráficos dos painéis a partir de window.ZELO_GRAFICOS (montado no servidor).
(() => {
    const CORES = {
        EMERGENCIA: '#b3261e', ALTA: '#e76f51', MEDIA: '#f4a261', BAIXA: '#6f8b83',
        ABERTA: '#f4a261', ENCAMINHADA: '#3d8bd9', ATENDIDA: '#1f6f5c', CANCELADA: '#a3a9a7',
        PENDENTE: '#f4a261', ENVIADO: '#3d8bd9', CONCLUIDO: '#1f6f5c', CANCELADO: '#a3a9a7',
        TODAS: '#1f6f5c', CRITICAS: '#e76f51'
    };
    const COR_PADRAO = '#8aa399';
    const cor = (chave) => CORES[chave] || COR_PADRAO;

    const BASE = {
        responsive: true,
        maintainAspectRatio: false,
        interaction: { mode: 'index', intersect: false },
        plugins: {
            legend: { position: 'bottom', labels: { usePointStyle: true, pointStyle: 'circle', boxWidth: 8, padding: 16 } },
            tooltip: {
                backgroundColor: '#0e3129', titleColor: '#ffffff', bodyColor: '#e4f1ec', padding: 12,
                cornerRadius: 10, boxPadding: 4, usePointStyle: true, titleFont: { weight: '600' }
            }
        }
    };
    const GRADE = { color: '#efebe3', drawTicks: false };
    const EIXO_INTEIRO = { beginAtZero: true, ticks: { precision: 0, padding: 8 }, grid: GRADE, border: { display: false } };
    const EIXO_CATEGORIAS = { grid: { display: false }, border: { display: false }, ticks: { padding: 6 } };

    const CONFIGURACOES = {
        doughnut: (dados) => ({
            type: 'doughnut',
            data: {
                labels: dados.rotulos,
                datasets: [{ data: dados.series[0].valores, backgroundColor: dados.chaves.map(cor),
                    borderColor: '#ffffff', borderWidth: 3, hoverOffset: 8, borderRadius: 6 }]
            },
            options: { ...BASE, interaction: { mode: 'nearest', intersect: true }, cutout: '66%' }
        }),
        'bar-agrupado': (dados) => ({
            type: 'bar',
            data: {
                labels: dados.rotulos,
                datasets: dados.series.map((serie) => ({
                    label: serie.nome, data: serie.valores, backgroundColor: cor(serie.chave),
                    borderRadius: 6, borderSkipped: false, maxBarThickness: 16, categoryPercentage: 0.7
                }))
            },
            options: { ...BASE, scales: { y: EIXO_INTEIRO, x: { ...EIXO_CATEGORIAS, ticks: { maxTicksLimit: 10, padding: 6 } } } }
        }),
        'bar-horizontal': (dados) => ({
            type: 'bar',
            data: {
                labels: dados.rotulos,
                datasets: [{ label: dados.series[0].nome, data: dados.series[0].valores,
                    backgroundColor: dados.chaves.map(cor), borderRadius: 8, borderSkipped: false, maxBarThickness: 30 }]
            },
            options: { ...BASE, indexAxis: 'y', plugins: { ...BASE.plugins, legend: { display: false } },
                scales: { x: EIXO_INTEIRO, y: EIXO_CATEGORIAS } }
        }),
        'bar-empilhado': (dados) => ({
            type: 'bar',
            data: {
                labels: dados.rotulos,
                datasets: dados.series.map((serie) => ({
                    label: serie.nome, data: serie.valores, backgroundColor: cor(serie.chave),
                    borderRadius: 6, borderSkipped: false, maxBarThickness: 56, borderColor: '#ffffff', borderWidth: 1
                }))
            },
            options: { ...BASE, scales: { x: { ...EIXO_CATEGORIAS, stacked: true }, y: { ...EIXO_INTEIRO, stacked: true } } }
        })
    };

    document.addEventListener('DOMContentLoaded', () => {
        if (typeof Chart === 'undefined') {
            document.querySelectorAll('canvas[data-grafico]').forEach((canvas) => {
                canvas.parentElement.innerHTML = '<p class="small text-muted mb-0">Não foi possível carregar os gráficos.</p>';
            });
            return;
        }
        Chart.defaults.font.family = 'Inter, system-ui, sans-serif';
        Chart.defaults.color = '#66736f';
        Chart.defaults.font.size = 12;
        document.querySelectorAll('canvas[data-grafico]').forEach((canvas) => {
            const dados = (window.ZELO_GRAFICOS || {})[canvas.dataset.grafico];
            const configurar = CONFIGURACOES[canvas.dataset.tipo];
            if (dados && configurar) {
                new Chart(canvas, configurar(dados));
            }
        });
    });
})();
