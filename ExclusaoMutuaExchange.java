import java.util.concurrent.atomic.AtomicInteger;

public class ExclusaoMutuaExchange {

    static final int VEZES = 1_000_000;

    static int contador = 0;

    static AtomicInteger lock = new AtomicInteger(0);

    static boolean usarExclusao;

    static int exchange(int novoValor) {
        return lock.getAndSet(novoValor);
    }

    static void entrarRegiaoCritica() {
        while (exchange(1) == 1) {
        }
    }

    static void sairRegiaoCritica() {
        exchange(0);
    }

    static void trabalhar() {
        for (int i = 0; i < VEZES; i++) {
            if (usarExclusao) entrarRegiaoCritica();

            contador++;

            if (usarExclusao) sairRegiaoCritica();
        }
    }

    static void rodar(boolean comExclusao) throws InterruptedException {
        usarExclusao = comExclusao;
        contador = 0;
        lock.set(0);

        Thread t1 = new Thread(ExclusaoMutuaExchange::trabalhar);
        Thread t2 = new Thread(ExclusaoMutuaExchange::trabalhar);

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println();
        System.out.println(comExclusao ? "=== COM exclusao mutua (EXCHANGE) ===" : "=== SEM exclusao mutua ===");
        System.out.println("Esperado: " + (2 * VEZES));
        System.out.println("Obtido:   " + contador);
        System.out.println(contador == 2 * VEZES ? "Resultado CORRETO" : "Resultado ERRADO (condicao de corrida)");
    }

    public static void main(String[] args) throws InterruptedException {
        rodar(false);
        rodar(true);
    }
}
