package semana1;

import java.util.ArrayList;
import java.util.List;

/**
 * Thread consumidora: representa um cozinheiro que retira pedidos da
 * {@link FilaDePedidos} e os processa.
 *
 * <p><strong>PROBLEMAS INTENCIONAIS (Semana 1):</strong></p>
 * <ul>
 *   <li><b>Busy-wait</b>: polling ativo na fila sem {@code wait/notify},
 *       desperdiçando CPU e aumentando contenção.</li>
 *   <li><b>Starvation potencial</b>: sem mecanismo de fairness, um cozinheiro
 *       pode dominar o processamento enquanto outros ficam ociosos.</li>
 *   <li><b>Pedidos duplicados</b>: se dois cozinheiros chamam {@code retirar()}
 *       simultaneamente na {@link FilaDePedidos} sem lock, podem processar
 *       o mesmo pedido.</li>
 * </ul>
 */
public class Cozinheiro implements Runnable {

    private static final int TEMPO_PREPARO_MS   = 50;
    private static final int TEMPO_ESPERA_MS    = 5;
    private static final int MAX_OCIOSIDADE_MS  = 3_000;

    private final String nome;
    private final FilaDePedidos fila;

    // Rastreamento local — usado no relatório de diagnóstico
    private final List<Integer> idsPedidosProcessados = new ArrayList<>();
    private int pedidosProcessados = 0;

    public Cozinheiro(String nome, FilaDePedidos fila) {
        this.nome = nome;
        this.fila = fila;
    }

    @Override
    public void run() {
        System.out.printf("[%s] Entrando na cozinha.%n", nome);
        long tempoInicioOciosidade = -1;

        while (true) {
            // PROBLEMA INTENCIONAL: busy-wait sem wait/notify.
            // Correto seria usar BlockingQueue.take() ou wait() condicional.
            Pedido pedido = fila.retirar();

            if (pedido == null) {
                // Fila vazia — verifica tempo de ociosidade para sair
                if (tempoInicioOciosidade < 0) {
                    tempoInicioOciosidade = System.currentTimeMillis();
                }

                long ocioso = System.currentTimeMillis() - tempoInicioOciosidade;
                if (ocioso >= MAX_OCIOSIDADE_MS) {
                    System.out.printf("[%s] Ocioso por %dms — encerrando turno.%n", nome, ocioso);
                    break;
                }

                try {
                    Thread.sleep(TEMPO_ESPERA_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                continue;
            }

            // Resetar contador de ociosidade ao receber pedido
            tempoInicioOciosidade = -1;

            // PROBLEMA INTENCIONAL: sem verificação de duplicata aqui.
            // Se dois cozinheiros pegaram o mesmo pedido, ambos processam.
            System.out.printf("[%s] PREPARANDO %s...%n", nome, pedido);

            try {
                Thread.sleep(TEMPO_PREPARO_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            pedidosProcessados++;
            idsPedidosProcessados.add(pedido.getId());
            System.out.printf("[%s] PRONTO: %s | Total processado: %d%n",
                nome, pedido, pedidosProcessados);
        }

        System.out.printf("[%s] Saindo da cozinha. Processou %d pedidos.%n", nome, pedidosProcessados);
    }

    public int getPedidosProcessados() {
        return pedidosProcessados;
    }

    public List<Integer> getIdsPedidosProcessados() {
        return idsPedidosProcessados;
    }

    public String getNome() {
        return nome;
    }
}
