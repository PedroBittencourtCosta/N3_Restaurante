package semana1;

/**
 * Entidade imutável representando um pedido feito em uma mesa do restaurante.
 * Imutabilidade garante que o objeto seja seguro para leitura concorrente,
 * mas a FILA onde ele é armazenado não é protegida — ponto de race condition.
 */
public final class Pedido {

    private final int id;
    private final String descricao;
    private final String nomeGarcom;
    private final long timestampMs;

    public Pedido(int id, String descricao, String nomeGarcom) {
        this.id          = id;
        this.descricao   = descricao;
        this.nomeGarcom  = nomeGarcom;
        this.timestampMs = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getNomeGarcom() {
        return nomeGarcom;
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    @Override
    public String toString() {
        return String.format("Pedido{id=%d, garcom='%s', descricao='%s'}", id, nomeGarcom, descricao);
    }
}
