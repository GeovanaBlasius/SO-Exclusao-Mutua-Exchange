import java.util.concurrent.atomic.AtomicInteger;

//Duas threads somam no mesmo número ao mesmo tempo.
//Sem controle, elas se atrapalham e algumas somas se perdem.
//O exchange funciona como uma chave: só quem pega a chave pode somar.
//Quem chega e a chave está ocupada espera, e quem termina devolve a chave.
//(clique em Run) Sem a chave o resultado sai errado, com a chave sai certo. Isso é exclusão mútua.

public class Analogia {

    // Representa a 'chave' do banheiro: 0 = livre, 1 = ocupado.
    // A operação de troca é atômica, então duas threads nunca alteram o estado ao mesmo tempo.
    static AtomicInteger chave = new AtomicInteger(0);

    // Quantas pessoas estão dentro do banheiro no momento.
    static int pessoasDentro = 0;

    // Indica se o código deve usar exclusão mútua ou não.
    static boolean usarChave;

    // Troca o valor da chave atomicamente e retorna o valor antigo.
    // Isso funciona como um bloqueio simples: se o valor antigo era 0, a thread pega a chave.
    static int exchange(int novoValor) {
        return chave.getAndSet(novoValor);
    }

    // Tenta pegar a chave do banheiro.
    // Se outra thread já estiver usando, o laço continua até a chave ficar livre.
    static void pegarChave() {
        while (exchange(1) == 1) {
            // Espera ocupada: fica repetindo até conseguir entrar.
        }
    }

    // Libera a chave quando a pessoa sai do banheiro.
    static void devolverChave() {
        exchange(0);
    }

    // Simula o uso do banheiro por uma pessoa.
    // A ideia é mostrar o problema de duas pessoas entrarem ao mesmo tempo.
    static void usarBanheiro(String nome) throws InterruptedException {
        for (int vez = 1; vez <= 3; vez++) {
            // Se a exclusão estiver ativa, a thread só entra quando conseguir a chave.
            if (usarChave) pegarChave();

            // Acesso à variável compartilhada que representa a quantidade de pessoas dentro.
            pessoasDentro++;
            System.out.println(nome + " entrou no banheiro.");
            Thread.sleep(150);

            // Se duas pessoas estiverem dentro ao mesmo tempo, há um problema de sincronização.
            if (pessoasDentro > 1) {
                System.out.println("   PROBLEMA: " + nome + " encontrou outra pessoa no banheiro!");
            }

            Thread.sleep(150);
            pessoasDentro--;
            System.out.println(nome + " saiu do banheiro.");

            // Libera a chave após usar o recurso.
            if (usarChave) devolverChave();

            Thread.sleep(100);
        }
    }

    // Executa a simulação com ou sem chave para comparar os resultados.
    static void rodar(boolean comChave) throws InterruptedException {
        usarChave = comChave;
        pessoasDentro = 0;
        chave.set(0);

        System.out.println();
        System.out.println(comChave ? "=== COM chave (EXCHANGE) ===" : "=== SEM chave ===");

        Thread julia = new Thread(() -> {
            try { usarBanheiro("Julia"); } catch (InterruptedException e) { }
        });
        Thread bruno = new Thread(() -> {
            try { usarBanheiro("Bruno"); } catch (InterruptedException e) { }
        });

        julia.start();
        bruno.start();
        julia.join();
        bruno.join();
    }

    public static void main(String[] args) throws InterruptedException {
        // Sem sincronização: mostra a condição de corrida.
        rodar(false);

        // Com sincronização: evita que duas pessoas entrem ao mesmo tempo.
        rodar(true);
    }
}