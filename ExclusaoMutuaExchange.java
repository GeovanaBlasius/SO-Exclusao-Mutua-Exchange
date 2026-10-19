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

    public static void main(String[] args) throws InterruptedException {
        if (args.length != 1 || !(args[0].equals("0") || args[0].equals("1"))) {
            System.out.println("Uso: java ExclusaoMutuaExchange <0|1>");
            System.out.println("  0 = sem exclusao mutua | 1 = com exclusao mutua (EXCHANGE)");
            return;
        }
        usarExclusao = args[0].equals("1");

        Thread t1 = new Thread(ExclusaoMutuaExchange::trabalhar);
        Thread t2 = new Thread(ExclusaoMutuaExchange::trabalhar);

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println(usarExclusao ? "COM exclusao mutua (EXCHANGE)" : "SEM exclusao mutua");
        System.out.println("Esperado: " + (2 * VEZES));
        System.out.println("Obtido:   " + contador);
        System.out.println(contador == 2 * VEZES ? "Resultado CORRETO" : "Resultado ERRADO (condicao de corrida)");
    }
}
