import java.util.concurrent.atomic.AtomicInteger;

/*
 * EXCLUSÃO MÚTUA COM A INSTRUÇÃO EXCHANGE (XCHG / TSL)
 *
 * Problema: duas threads incrementam a mesma variável (contador) ao mesmo tempo.
 * A operação "contador++" NÃO é atômica; ela é feita em 3 passos:
 *     1) ler o valor da memória para um registrador
 *     2) somar 1 no registrador
 *     3) gravar o registrador de volta na memória
 * Se as duas threads intercalarem esses passos, uma sobrescreve o resultado da
 * outra e incrementos são perdidos -> CONDIÇÃO DE CORRIDA.
 *
 * Solução: proteger a REGIÃO CRÍTICA (o contador++) com um lock baseado na
 * instrução EXCHANGE, que troca o valor de uma variável e devolve o valor antigo
 * em uma única operação indivisível (atômica), garantida pelo hardware.
 *
 * O programa roda duas vezes: primeiro SEM exclusão mútua (resultado errado)
 * e depois COM exclusão mútua (resultado correto).
 */
public class ExclusaoMutuaExchange {

    // Número de incrementos que cada thread fará na região crítica.
    // Um número grande aumenta a chance de a condição de corrida aparecer.
    static final int VEZES = 1_000_000;

    // Variável compartilhada que representa o contador global.
    // As duas threads leem e escrevem nela -> é o recurso que precisa de proteção.
    static int contador = 0;

    // Lock de exclusão mútua usando atomicidade do Java.
    // 0 = livre, 1 = ocupado.
    // AtomicInteger é usado porque o Java não dá acesso direto à instrução XCHG;
    // o método getAndSet() é traduzido pela JVM para essa instrução do processador.
    static AtomicInteger lock = new AtomicInteger(0);

    // Controla se a exclusão mútua será usada neste teste.
    static boolean usarExclusao;

    // Troca atômica: retorna o valor antigo e grava o novo valor em uma única operação.
    // Em outras palavras, realiza um 'exchange' no lock de forma indivisível.
    // Por ser atômica, nenhuma outra thread consegue ler/escrever o lock "no meio" da troca.
    static int exchange(int novoValor) {
        return lock.getAndSet(novoValor);
    }

    // Entrada na região crítica.
    // A thread sempre tenta gravar 1 no lock e olha o valor ANTIGO:
    //   - se o antigo era 1: alguém já está dentro -> continua tentando (spin waiting);
    //   - se o antigo era 0: o lock estava livre -> agora ele vale 1 e a thread entra.
    // Como a troca é atômica, só UMA thread consegue ver o valor 0; as outras veem 1.
    static void entrarRegiaoCritica() {
        while (exchange(1) == 1) {
            // Espera ocupada: a thread repete até conseguir o lock.
            // Desvantagem: gasta CPU enquanto espera (busy waiting).
        }
    }

    // Saída da região crítica.
    // Libera o lock (volta para 0), permitindo que outra thread entre.
    static void sairRegiaoCritica() {
        exchange(0);
    }

    // Simula trabalho concorrente sobre uma variável compartilhada.
    // Sem exclusão mútua, duas threads podem intercalar as instruções e perder atualizações.
    static void trabalhar() {
        for (int i = 0; i < VEZES; i++) {
            // Se a opção estiver ativada, entra na seção crítica antes da operação.
            if (usarExclusao) entrarRegiaoCritica();

            // ----- REGIÃO CRÍTICA -----
            // Incremento do contador compartilhado (ler, somar, gravar).
            contador++;
            // --------------------------

            // Libera o lock ao sair da seção crítica.
            if (usarExclusao) sairRegiaoCritica();
        }
    }

    // Executa o teste com ou sem exclusão mútua.
    static void rodar(boolean comExclusao) throws InterruptedException {
        // Reinicia o estado para cada teste começar do zero.
        usarExclusao = comExclusao;
        contador = 0;
        lock.set(0);

        // Cria duas threads que executam o mesmo método trabalhar() ao mesmo tempo.
        Thread t1 = new Thread(ExclusaoMutuaExchange::trabalhar);
        Thread t2 = new Thread(ExclusaoMutuaExchange::trabalhar);

        // start(): inicia a execução das threads em paralelo.
        t1.start();
        t2.start();
        // join(): a thread principal espera as duas terminarem antes de mostrar o resultado.
        t1.join();
        t2.join();

        // Cada thread soma VEZES, então o valor correto é 2 * VEZES.
        System.out.println();
        System.out.println(comExclusao ? "=== COM exclusao mutua (EXCHANGE) ===" : "=== SEM exclusao mutua ===");
        System.out.println("Esperado: " + (2 * VEZES));
        System.out.println("Obtido:   " + contador);
        System.out.println(contador == 2 * VEZES ? "Resultado CORRETO" : "Resultado ERRADO (condicao de corrida)");
    }

    public static void main(String[] args) throws InterruptedException {
        // Primeiro teste: sem sincronização, mostrando a condição de corrida.
        rodar(false);

        // Segundo teste: com exclusão mútua, mostrando a correção do problema.
        rodar(true);
    }
}
