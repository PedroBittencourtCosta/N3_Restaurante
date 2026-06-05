package semana1;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Ponto de entrada do sistema de restaurante — Semana 1 (Implementação Ingênua).
 *
 * <p>Orquestra {@value TOTAL_GARCONS} garçons e {@value TOTAL_COZINHEIROS} cozinheiros
 * como threads concorrentes sobre uma {@link FilaDePedidos} <b>não sincronizada</b>.</p>
 *
 * <p>Ao final da execução, imprime um <b>relatório de diagnóstico</b> evidenciando:</p>
 * <ul>
 *   <li>Diferença entre pedidos gerados e processados (pedidos perdidos).</li>
 *   <li>Pedidos processados mais de uma vez (duplicatas — race condition).</li>
 *   <li>Desequilíbrio entre cozinheiros (starvation).</li>
 * </ul>
 */
public class Restaurante {

    // Configuração da simulação
    private static final int TOTAL_GARCONS       = 2;
    private static final int TOTAL_COZINHEIROS   = 3;
    private static final int PEDIDOS_POR_GARCOM  = 10;
    private static final int CAPACIDADE_FILA     = 15;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=".repeat(60));
        System.out.println("  RESTAURANTE CONCORRENTE — SEMANA 1 (Implementação Ingênua)");
        System.out.println("  Aguarde a execução para ver os problemas de concorrência...");
        System.out.println("=".repeat(60));
        System.out.println();

        FilaDePedidos fila = new FilaDePedidos(CAPACIDADE_FILA);

        // Criação dos cozinheiros (consumidores)
        List<Cozinheiro> cozinheiros = new ArrayList<>();
        List<Thread> threadsCozinheiros = new ArrayList<>();

        for (int i = 1; i <= TOTAL_COZINHEIROS; i++) {
            Cozinheiro c = new Cozinheiro("Cozinheiro-" + i, fila);
            cozinheiros.add(c);
            threadsCozinheiros.add(new Thread(c, "thread-cozinheiro-" + i));
        }

        // Criação dos garçons (produtores)
        List<Thread> threadsGarcons = new ArrayList<>();
        for (int i = 1; i <= TOTAL_GARCONS; i++) {
            Garcom g = new Garcom("Garcom-" + i, fila, PEDIDOS_POR_GARCOM);
            threadsGarcons.add(new Thread(g, "thread-garcom-" + i));
        }

        System.out.printf("Iniciando %d cozinheiros e %d garçons...%n%n",
            TOTAL_COZINHEIROS, TOTAL_GARCONS);

        // Inicia cozinheiros primeiro (ficam em busy-wait aguardando pedidos)
        for (Thread t : threadsCozinheiros) {
            t.start();
        }

        // Pequena pausa para garantir que cozinheiros já estão rodando
        Thread.sleep(50);

        // Inicia garçons (produtores de pedidos)
        for (Thread t : threadsGarcons) {
            t.start();
        }

        // Aguarda garçons terminarem de gerar pedidos
        for (Thread t : threadsGarcons) {
            t.join();
        }

        System.out.println();
        System.out.println("[MAIN] Todos os garçons encerraram. Aguardando cozinheiros...");

        // Aguarda cozinheiros processarem (timeout: 10 segundos)
        for (Thread t : threadsCozinheiros) {
            t.join(10_000);
        }

        // Imprime relatório de diagnóstico
        imprimirDiagnostico(cozinheiros);
    }

    /**
     * Imprime o relatório de diagnóstico evidenciando os problemas de concorrência.
     *
     * @param cozinheiros lista de cozinheiros para análise de starvation e duplicatas
     */
    private static void imprimirDiagnostico(List<Cozinheiro> cozinheiros) {
        System.out.println();
        System.out.println("=".repeat(60));
        System.out.println("          RELATÓRIO DE DIAGNÓSTICO — SEMANA 1");
        System.out.println("=".repeat(60));

        int totalGerado    = TOTAL_GARCONS * PEDIDOS_POR_GARCOM;
        int totalProcessado = 0;
        List<Integer> todosIds = new ArrayList<>();

        System.out.println();
        System.out.println("--- Pedidos por Cozinheiro (Starvation?) ---");
        for (Cozinheiro c : cozinheiros) {
            System.out.printf("  %-15s processou: %d pedidos%n",
                c.getNome(), c.getPedidosProcessados());
            totalProcessado += c.getPedidosProcessados();
            todosIds.addAll(c.getIdsPedidosProcessados());
        }

        System.out.println();
        System.out.println("--- Contagem Geral ---");
        System.out.printf("  Pedidos GERADOS pelos garçons  : %d%n", totalGerado);
        // NOTA: totalEnfileirado pode divergir de totalGerado por race condition no ++
        System.out.printf("  totalEnfileirado (sem sync)    : %d  (sem sync - pode divergir!)%n", Garcom.totalEnfileirado);
        System.out.printf("  Pedidos PROCESSADOS            : %d%n", totalProcessado);
        System.out.printf("  Pedidos PERDIDOS               : %d%n",
            Math.max(0, totalGerado - totalProcessado));

        // Detecção de duplicatas (pedidos processados mais de uma vez)
        Set<Integer> vistos = new HashSet<>();
        Set<Integer> duplicatas = new HashSet<>();
        for (int id : todosIds) {
            if (!vistos.add(id)) {
                duplicatas.add(id);
            }
        }

        System.out.println();
        System.out.println("--- Detecção de Duplicatas (Race Condition) ---");
        if (duplicatas.isEmpty()) {
            System.out.println("  Nenhuma duplicata detectada nesta execução.");
            System.out.println("  (Race conditions são não-determinísticas — execute novamente!)");
        } else {
            System.out.printf("  IDs PROCESSADOS MAIS DE UMA VEZ: %s%n", duplicatas);
        }

        // Análise de starvation
        System.out.println();
        System.out.println("--- Análise de Starvation ---");
        int max = cozinheiros.stream().mapToInt(Cozinheiro::getPedidosProcessados).max().orElse(0);
        int min = cozinheiros.stream().mapToInt(Cozinheiro::getPedidosProcessados).min().orElse(0);
        System.out.printf("  Cozinheiro mais ativo : %d pedidos%n", max);
        System.out.printf("  Cozinheiro menos ativo: %d pedidos%n", min);
        if (max > 0 && min < max / 2) {
            System.out.println("  ⚠ STARVATION POTENCIAL: distribuição muito desigual!");
        }

        System.out.println();
        System.out.println("--- Problemas Identificados ---");
        System.out.println("  1. RACE CONDITION: FilaDePedidos usa ArrayList sem synchronized.");
        System.out.println("     → Dois cozinheiros podem retirar o mesmo pedido.");
        System.out.println("     → enfileirar() e retirar() não são atômicos.");
        System.out.println("  2. LOST UPDATE: Garcom.totalEnfileirado incrementado sem sync.");
        System.out.println("     → Contador pode refletir valor menor que o real.");
        System.out.println("  3. BUSY-WAIT: Cozinheiros fazem polling ativo (sem wait/notify).");
        System.out.println("     → Desperdício de CPU e aumento da contenção.");
        System.out.println("  4. STARVATION POTENCIAL: sem fairness na disputa pela fila.");
        System.out.println("     → Distribuição de pedidos entre cozinheiros pode ser desigual.");
        System.out.println();
        System.out.println("=".repeat(60));
        System.out.println("  Semana 2: todas essas falhas serão corrigidas com");
        System.out.println("  synchronized/wait/notify, ReentrantLock e BlockingQueue.");
        System.out.println("=".repeat(60));
    }
}
