# CLAUDE.md — Contexto do Projeto

## Visão Geral

Sistema concorrente em camadas desenvolvido em **Java 17 puro** (sem frameworks).  
Domínio escolhido: **Restaurante** — fila de pedidos, cozinheiros e garçons como threads concorrentes.  
Disciplina: Desenvolvimento de Software para Concorrência — Engenharia de Software.

---

## Stack Tecnológica

| Camada       | Tecnologia                     |
|--------------|-------------------------------|
| Linguagem    | Java 17                        |
| Frameworks   | **Nenhum** — Java puro apenas  |
| Concorrência | `java.util.concurrent.*`, `synchronized`, `ReentrantLock`, `Semaphore`, `ExecutorService` |
| Build        | Compilação manual (`javac`) ou script simples |
| Testes       | Testes manuais + logs de saída |

---

## Princípios de Desenvolvimento

- **Simplicidade**: código direto ao ponto, sem abstrações desnecessárias.
- **Legibilidade**: nomes de variáveis, métodos e classes claros e expressivos.
- **Desempenho**: considerar throughput e latência sem sacrificar a clareza.
- **Manutenibilidade**: cada classe com responsabilidade única e bem definida.
- **Testabilidade**: lógica desacoplada de I/O; fácil de isolar e verificar.
- **Reusabilidade**: componentes genéricos onde couber (ex.: pool, fila compartilhada).
- **Menos código = menos dívida técnica**: evitar código morto, duplicação e over-engineering.

---

## Estrutura de Entregas

### Semana 1 — Implementação Ingênua e Diagnóstico (entrega: 08/06)
- Implementar o sistema funcional com **mínimo 4 threads concorrentes**.
- Introduzir **intencionalmente** race conditions ou inconsistências visíveis na saída.
- Identificar e nomear os problemas: race condition, deadlock potencial, starvation, etc.
- Entregar: código-fonte + relatório parcial (PDF, 2–4 páginas) com log evidenciando o comportamento incorreto.

### Semana 2 — Aplicação das Soluções de Concorrência (entrega: 15/06)
- Refatorar usando os mecanismos adequados: `synchronized/wait/notify`, `ReentrantLock`, `ReadWriteLock`, `Semaphore`, `ExecutorService/ThreadPool`.
- Incluir **teste de stress** com alto volume de threads simultâneas.
- Justificar cada mecanismo escolhido (por que esse e não outro?).
- Entregar: código refatorado + relatório comparativo (PDF, 3–5 páginas).

### Semana 3 — Vídeo e Relatório Final (entrega: 22/06)
- Vídeo de 5–10 minutos: sistema rodando ao vivo, decisões de design, limitações.
- Relatório final consolidando semanas 1 e 2, com diagrama de threads e recursos.
- Entregar: vídeo (link/arquivo) + relatório final (PDF, 6–10 páginas).

---

## Domínio: Restaurante

| Entidade        | Papel como Thread / Recurso Compartilhado            |
|-----------------|------------------------------------------------------|
| `Pedido`        | Unidade de trabalho produzida pelos garçons          |
| `Garcom`        | Thread produtora — recebe pedidos e enfileira        |
| `Cozinheiro`    | Thread consumidora — processa pedidos da fila        |
| `FilaDePedidos` | Buffer compartilhado (produtor-consumidor)           |
| `Mesa`          | Recurso com capacidade limitada                      |

**Problema central de concorrência:** múltiplas threads acessando a fila de pedidos simultaneamente sem coordenação adequada → corrida de dados, pedidos duplicados ou perdidos.

---

## Convenções de Código

```java
// Nomes em português para entidades do domínio
class Cozinheiro implements Runnable { ... }
class FilaDePedidos { ... }

// Javadoc obrigatório em classes públicas e métodos de sincronização
/**
 * Enfileira um pedido de forma thread-safe.
 * Bloqueia se a fila estiver cheia.
 */
public void enfileirar(Pedido pedido) throws InterruptedException { ... }
```

- Classes: `PascalCase`
- Métodos e variáveis: `camelCase`
- Constantes: `UPPER_SNAKE_CASE`
- Um arquivo por classe pública
- Sem imports curinga (`import java.util.*` → proibido)

---

## Restrições Técnicas

- **Java 17** — sem versões anteriores ou posteriores.
- **Sem frameworks** (Spring, Quarkus, etc. são proibidos).
- Usar **apenas** a API padrão do JDK (`java.util.concurrent`, `java.util.logging`, etc.).
- Código deve compilar com `javac` sem flags especiais além de `--release 17`.
- Logs de saída devem ser legíveis e evidenciar o comportamento concorrente (correto ou incorreto).

---

## Guia de Decisão para Mecanismos de Concorrência

| Problema                              | Mecanismo preferido                    |
|---------------------------------------|----------------------------------------|
| Seção crítica simples                 | `synchronized`                         |
| Espera condicional                    | `wait()` / `notify()` / `notifyAll()`  |
| Controle fino de lock                 | `ReentrantLock`                        |
| Leituras concorrentes + escrita rara  | `ReadWriteLock`                        |
| Limite de acesso a recurso            | `Semaphore`                            |
| Pool de threads gerenciado            | `ExecutorService` + `Executors`        |
| Fila produtor-consumidor              | `BlockingQueue` (`LinkedBlockingQueue`)|

---

## O que NÃO fazer

- ❌ Usar `Thread.sleep()` como substituto de sincronização real.
- ❌ Ignorar `InterruptedException` sem tratar (`catch` vazio).
- ❌ Criar threads sem `try/finally` para garantir liberação de recursos.
- ❌ Compartilhar estado mutável sem proteção.
- ❌ Usar coleções não thread-safe (`ArrayList`, `HashMap`) em contexto concorrente sem lock.
- ❌ God classes — uma classe fazendo tudo.
- ❌ Magic numbers sem constantes nomeadas.
