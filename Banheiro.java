import java.util.concurrent.atomic.AtomicInteger;

public class Banheiro {

    static AtomicInteger chave = new AtomicInteger(0);

    static int pessoasDentro = 0;

    static boolean usarChave;

    static int exchange(int novoValor) {
        return chave.getAndSet(novoValor);
    }

    static void pegarChave() {
        while (exchange(1) == 1) {
        }
    }

    static void devolverChave() {
        exchange(0);
    }

    static void usarBanheiro(String nome) throws InterruptedException {
        for (int vez = 1; vez <= 3; vez++) {
            if (usarChave) pegarChave();

            pessoasDentro++;
            System.out.println(nome + " entrou no banheiro.");
            Thread.sleep(150);
            if (pessoasDentro > 1) {
                System.out.println("   PROBLEMA: " + nome + " encontrou outra pessoa no banheiro!");
            }
            Thread.sleep(150);
            pessoasDentro--;
            System.out.println(nome + " saiu do banheiro.");

            if (usarChave) devolverChave();

            Thread.sleep(100);
        }
    }

    static void rodar(boolean comChave) throws InterruptedException {
        usarChave = comChave;
        pessoasDentro = 0;
        chave.set(0);

        System.out.println();
        System.out.println(comChave ? "=== COM chave (EXCHANGE) ===" : "=== SEM chave ===");

        Thread ana = new Thread(() -> {
            try { usarBanheiro("Ana"); } catch (InterruptedException e) { }
        });
        Thread bruno = new Thread(() -> {
            try { usarBanheiro("Bruno"); } catch (InterruptedException e) { }
        });

        ana.start();
        bruno.start();
        ana.join();
        bruno.join();
    }

    public static void main(String[] args) throws InterruptedException {
        rodar(false);
        rodar(true);
    }
}
