package semana1;

/**
 * Thread produtora: representa um garçom que recebe pedidos das mesas
 * e os enfileira na {@link FilaDePedidos} compartilhada.
 *
 * <p><strong>PROBLEMAS INTENCIONAIS (Semana 1):</strong></p>
 * <ul>
 *   <li>Acessa {@link FilaDePedidos} sem qualquer sincronização externa.</li>
 *   <li>Incrementa {@code Restaurante.totalEnfileirado} sem {@code synchronized},
 *       causando perda de atualizações (lost update).</li>
 *   <li>Usa {@code Thread.sleep()} curto para simular alta contenção.</li>
 * </ul>
 */
public class Garcom implements Runnable {

    private static final String[] PRATOS = {
        "Frango Grelhado", "Massa ao Sugo", "Risoto de Camarão",
        "Filé com Fritas", "Sopa do Dia", "Salada Caesar"
    };

    private final String nome;
    private final FilaDePedidos fila;
    private final int quantidadePedidos;

    // PROBLEMA INTENCIONAL: campo compartilhado sem volatile nem synchronized.
    // Visível em Restaurante.totalEnfileirado — race condition no incremento.
    static int totalEnfileirado = 0;

    public Garcom(String nome, FilaDePedidos fila, int quantidadePedidos) {
        this.nome              = nome;
        this.fila              = fila;
        this.quantidadePedidos = quantidadePedidos;
    }

    @Override
    public void run() {
        System.out.printf("[%s] Iniciando turno — vai gerar %d pedidos.%n", nome, quantidadePedidos);

        for (int i = 0; i < quantidadePedidos; i++) {
            // PROBLEMA INTENCIONAL: id não é atômico — dois garçons podem
            // gerar o mesmo id concorrentemente (lost update clássico).
            int idPedido = ++totalEnfileirado;
            String prato = PRATOS[i % PRATOS.length];

            Pedido pedido = new Pedido(idPedido, prato, nome);

            boolean enfileirado = fila.enfileirar(pedido);

            if (enfileirado) {
                System.out.printf("[%s] Enfileirou %s (fila tamanho ≈ %d)%n",
                    nome, pedido, fila.tamanho());
            } else {
                System.out.printf("[%s] FILA CHEIA — pedido %s DESCARTADO!%n", nome, pedido);
            }

            try {
                // Sleep curto aumenta a janela de race condition
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.printf("[%s] Interrompido. Saindo.%n", nome);
                return;
            }
        }

        System.out.printf("[%s] Turno encerrado.%n", nome);
    }
}
