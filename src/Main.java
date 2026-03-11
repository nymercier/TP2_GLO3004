import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;


// Je pense que c'est plus un sémaphore qu'une barrière?
// Compteur [0..N] du FSP :

public class Main {
    public static void main(String[] args) throws Exception {
        int N = 8;
        int NB_P = 3;
        int NB_S = 4;
        int tempsExecution = 40;

//        int N = Integer.parseInt(System.getProperty("n", "1"));
//        int NB_P = Integer.parseInt(System.getProperty("p", "1"));
//        int NB_S = Integer.parseInt(System.getProperty("s", "1"));
//        int tempsExecution = Integer.parseInt(System.getProperty("t", "5"));

        System.out.println("=== SYSTEM démarre ===");
        System.out.println("  n=" + N + "  (capacité broker)");
        System.out.println("  p=" + NB_P + "  (publishers)");
        System.out.println("  s=" + NB_S + "  (subscribers)");
        System.out.println("  t=" + tempsExecution + " ms  (durée)");
        System.out.println("========================");

        String[] apps = {"i", "t"};

        // Créer le mutex
        Semaphore mutex = new Semaphore(1, true);

        // Créer le broker
        Broker broker = new Broker(N, mutex);

        // NB_P publishers par app
        ArrayList<Publisher> publishers = new ArrayList<>();
        for (String app : apps) {
            for (int i = 1; i <= NB_P; i++) {
                publishers.add(new Publisher(app, "publisher", i, broker, mutex));
            }
        }

        // NB_S subscribers par app
        ArrayList<Subscriber> subscribers = new ArrayList<>();
        for (String app : apps) {
            for (int i = 1; i <= NB_S; i++) {
                subscribers.add(new Subscriber(app, "subscriber", i, broker, mutex));
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

        System.out.println("=== Arrêt après " + tempsExecution + " ms ===");

        // Arrêt propre (graceful shutdown)
        for (Publisher p : publishers) p.arreter();
        for (Subscriber s : subscribers) s.arreter();
        broker.arreter();

        for (Publisher p : publishers) {
            p.arreter();
            p.join(50);
            System.out.println("[Main] Publisher " + p.getName() + " état: " + p.getState());
        }
        for (Subscriber s : subscribers) {
            s.arreter();
            s.join(50);
            System.out.println("[Main] Subscriber " + s.getName() + " état: " + s.getState());
        }

        System.out.println("=== Système arrêté ===");


//        CyclicBarrier barriereConnectPub = new CyclicBarrier(2);
//        CyclicBarrier barrierePub = new CyclicBarrier(2);
//        CyclicBarrier barriereConnectSub = new CyclicBarrier(2);
//        CyclicBarrier barriereSub = new CyclicBarrier(2);
//
//        ArrayList<Publisher> publishers = new ArrayList<Publisher>();
//        for (int i = 0; i < NB_P; i++) {
//            publishers.add(new Publisher("publisher" + i, "indemnisation", barriereConnectPub, barrierePub));
//            publishers.add(new Publisher("publisher" + i, "tarification", barriereConnectPub, barrierePub));
//        }
//
//        ArrayList<Subscriber> subscribers = new ArrayList<Subscriber>();
//        for (int i = 0; i < NB_S; i++) {
//            subscribers.add(new Subscriber("subscriber" + i, "indemnisation", barriereConnectSub, barriereSub));
//            subscribers.add(new Subscriber("subscriber" + i, "tarification", barriereConnectSub, barriereSub));
//        }
//
//        Broker broker = new Broker(tempsExecution, N, barriereConnectPub, barrierePub, barriereConnectSub, barriereSub);
//
//        for (int i = 0; i < publishers.size(); i++) {
//            publishers.get(i).start();
//        }
//
//        for (int i = 0; i < subscribers.size(); i++) {
//            subscribers.get(i).start();
//        }
//
//        broker.start();
//        broker.join();
//
//        for (int i = 0; i < publishers.size(); i++) {
//            publishers.get(i).arreter();
//            if (publishers.get(i).getState() == Thread.State.WAITING || publishers.get(i).getState() == Thread.State.BLOCKED) {
//                publishers.get(i).interrupt();
//            }
//        }
//
//        for (int i = 0; i < subscribers.size(); i++) {
//            subscribers.get(i).arreter();
//            if (subscribers.get(i).getState() == Thread.State.WAITING || subscribers.get(i).getState() == Thread.State.BLOCKED) {
//                subscribers.get(i).interrupt();
//            }
//        }
    }
}