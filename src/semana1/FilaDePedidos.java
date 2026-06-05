package semana1;

import java.util.ArrayList;
import java.util.List;

/**
 * Fila de pedidos compartilhada entre garçons e cozinheiros.
 *
 * <p><strong>ATENÇÃO — IMPLEMENTAÇÃO INGÊNUA (Semana 1):</strong><br>
 * Esta classe usa {@link ArrayList} sem qualquer sincronização.
 * Múltiplas threads acessando {@code enfileirar()} e {@code retirar()}
 * simultaneamente causam:</p>
 * <ul>
 *   <li><b>Race condition</b>: dois cozinheiros retiram o mesmo pedido.</li>
 *   <li><b>Pedidos perdidos</b>: {@code size()} lido e {@code remove()} executados
 *       em momentos distintos por threads diferentes.</li>
 *   <li><b>ArrayIndexOutOfBoundsException</b>: possível em alta carga.</li>
 * </ul>
 * <p>Esses problemas são <em>intencionais</em> para diagnóstico.</p>
 */
public class FilaDePedidos {

    // PROBLEMA INTENCIONAL: ArrayList NÃO é thread-safe.
    // Deveria ser LinkedBlockingQueue para sincronização correta.
    private final List<Pedido> fila = new ArrayList<>();

    private final int capacidadeMaxima;

    public FilaDePedidos(int capacidadeMaxima) {
        this.capacidadeMaxima = capacidadeMaxima;
    }

    /**
     * Enfileira um pedido.
     * <p><b>NÃO É THREAD-SAFE:</b> sem sincronização — race condition intencional.</p>
     *
     * @param pedido pedido a ser adicionado
     * @return {@code true} se enfileirado, {@code false} se fila cheia
     */
    public boolean enfileirar(Pedido pedido) {
        // RACE CONDITION: verificação e inserção não são atômicas.
        // Entre o if e o add(), outra thread pode inserir e exceder a capacidade.
        if (fila.size() >= capacidadeMaxima) {
            return false;
        }
        fila.add(pedido); // ArrayList.add() não é thread-safe
        return true;
    }

    /**
     * Retira o primeiro pedido da fila.
     * <p><b>NÃO É THREAD-SAFE:</b> dois cozinheiros podem retirar o mesmo pedido
     * ou causar {@link IndexOutOfBoundsException}.</p>
     *
     * @return o pedido retirado, ou {@code null} se a fila estiver vazia
     */
    public Pedido retirar() {
        // RACE CONDITION: size() e remove(0) não são atômicos.
        if (fila.isEmpty()) {
            return null;
        }
        // Outra thread pode ter esvaziado a fila entre isEmpty() e remove(0).
        try {
            return fila.remove(0);
        } catch (IndexOutOfBoundsException e) {
            // Evidência visível da race condition no log
            System.out.println("[RACE CONDITION DETECTADA] remove(0) falhou — fila vazia concorrentemente!");
            return null;
        }
    }

    /**
     * Retorna o tamanho atual da fila (leitura não sincronizada — valor pode ser stale).
     *
     * @return tamanho aproximado da fila
     */
    public int tamanho() {
        return fila.size();
    }

    public boolean estaVazia() {
        return fila.isEmpty();
    }
}
