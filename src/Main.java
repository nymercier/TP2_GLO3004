import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Main {
    public static void main(String[] args) throws Exception {

        /**
         *
         * SYSTEM11 de la spécification.
         * Elle crée les différents processus (publishers, subscribers et broker),
         * démarre les threads, puis arrête le système après un certain temps.
         */

        /**
         * - n : capacité maximale du broker
         * - p : nombre de publishers
         * - s : nombre de subscribers
         * - t : temps d'exécution du système (en millisecondes)
         *
         * Des valeurs par défaut sont utilisées.
         */

        int N = Integer.parseInt(System.getProperty("n", "1"));
        int NB_P = Integer.parseInt(System.getProperty("p", "1"));
        int NB_S = Integer.parseInt(System.getProperty("s", "1"));
        int tempsExecution = Integer.parseInt(System.getProperty("t", "35"));

        System.out.println("=== SYSTEM démarre ===");
        long startTime = System.currentTimeMillis();
        System.out.println("  n=" + N + "  (capacité broker)");
        System.out.println("  p=" + NB_P + "  (publishers)");
        System.out.println("  s=" + NB_S + "  (subscribers)");
        System.out.println("  t=" + tempsExecution + " ms  (durée)");
        System.out.println("========================");

        String[] apps = {"i", "t"};

        // Créer le mutex pour protéger les structures partagées dans le broker.
        Semaphore mutex = new Semaphore(1, true);

        // Créer le broker, BROKER4
        Broker broker = new Broker(N, mutex);

        // NB_P publishers par app
        ArrayList<Publisher> publishers = new ArrayList<>();
        for (String app : apps) {
            for (int i = 1; i <= NB_P; i++) {
                publishers.add(new Publisher(app, "publisher", i, broker));
            }
        }

        // NB_S subscribers par app
        ArrayList<Subscriber> subscribers = new ArrayList<>();
        for (String app : apps) {
            for (int i = 1; i <= NB_S; i++) {
                subscribers.add(new Subscriber(app, "subscriber", i, broker));
            }
        }

        // On start les threads
        for (Publisher p  : publishers)  p.start();
        for (Subscriber s : subscribers) s.start();

        // Pour le paramètre t (exécution pendant t ms)
        try {
            Thread.sleep(tempsExecution);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Arrêt propre (graceful shutdown)
        for (Publisher p : publishers) p.arreter();
        for (Subscriber s : subscribers) s.arreter();
        broker.arreter();

        System.out.println("=== Arrêt après " + tempsExecution + " ms ===");


        for (Publisher p : publishers) {
//            p.arreter();
            p.join(500);
            System.out.println("[Main] Publisher : " + p.getName() + " état: " + p.getState());
        }
        for (Subscriber s : subscribers) {
//            s.arreter();
            s.join(500);
            System.out.println("[Main] Subscriber : " + s.getName() + " état: " + s.getState());
        }


        System.out.println("=== Système arrêté ===");
        long elapsed = System.currentTimeMillis() - startTime;
        System.out.printf("=== Temps total d'exécution : %d ms ===%n", elapsed);
    }
}