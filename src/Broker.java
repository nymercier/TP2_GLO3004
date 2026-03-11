import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/*
 * Modélise BROKER4 du FSP :
 *      BROKER4 = PUBSUB[0],
 *      PUBSUB[i:0..N] = (when (i < N) connect_pub -> pub -> queue -> PUBSUB[i + 1]
 * 	                    | when (i > 0) connect_sub -> sub -> dequeue -> PUBSUB[i - 1]).
 * Le compteur [0..N] est modélisé par deux sémaphores :
 *   - places  : nombre de slots libres  (initialisé à N)
 *   - messages: nombre de messages (initialisé à 0)
 */

public class Broker {

    private final int N;
    private final String app;
    private final Semaphore places;    // contrôle connect_pub
    private final Semaphore messages;  // msgs présents → contrôle connect_sub
    private final Semaphore mutex = new Semaphore(1, true);
    private boolean running = true;
    private ArrayList<String> listeMessages;

    public Broker(String app, int n) {
        this.N        = n;
        this.app      = app;
        this.places   = new Semaphore(N, true);             // éviter la famine
        this.messages = new Semaphore(0, true);
        this.listeMessages = new ArrayList<String>();
    }

    public int nbMessages() {
        return Math.max(0, N - places.availablePermits());
    }


    private void log(String threadName, String action, String message) {
        System.out.printf("[Broker %s | %d/%d msgs] %s %s, Message: %s%n",
                app, nbMessages(), N, threadName, action, message);
    }

    /**
     * connect_pub : le publisher attend qu'il y ait de la place
     * File est pleine (i == N).
     */
    public void connectPub(String threadName) throws InterruptedException {
        places.acquire();
        if (!isRunning()) throw new InterruptedException("Broker arrêté");
        mutex.acquire();
        if (!isRunning()) {
            mutex.release();
            throw new InterruptedException("Broker arrêté");
        }
        log(threadName, "CONNECT_PUB", "connexion");
    }

    /**
     * pub + queue : dépôt effectif du message
     * Incrémente le compteur de messages disponibles.
     * Production
     */
    public void pub(String threadName, String message) {
        try {
            queue(message);
            messages.release(); // i → i+1
            log(threadName, "PUB", message);
        }
        finally {
            mutex.release();
        }

    }

    private void queue(String message) {
        this.listeMessages.add(message);
    }

    public void closePub(String threadName) {
        // action observable dans les traces données par le prof
        // i.publisher.1 CLOSE_PUB
        log(threadName, "CLOSE_PUB", "fermeture");
    }

    /**
     * connect_sub : le subscriber attend qu'il y ait un message
     * File est vide (i == 0).
     */
    public void connectSub(String threadName) throws InterruptedException {
        messages.acquire();
        if (!isRunning()) throw new InterruptedException("Broker arrêté");
        mutex.acquire();
        if (!isRunning()) {
            mutex.release();
            throw new InterruptedException("Broker arrêté");
        }
        log(threadName, "CONNECT_SUB", "subscription");
    }


    /**
     * sub + dequeue : consommation effective
     * Libère un slot pour les publishers.
     * Consommation
     */
    public String sub(String threadName) {
        try {
            log(threadName, "SUB", this.listeMessages.getFirst());
            places.release();          // i → i-1
            return this.listeMessages.removeFirst();
        }
        finally {
            mutex.release();
        }
    }

    public void closeSub(String threadName) {
        // action observable dans les traces données par le prof
        // i.subscriber.2 CLOSE_SUB
        log(threadName, "CLOSE_SUB", "fermeture");
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * Arrêt "propre" : on relâche tous les threads bloqués
     * en libérant les deux sémaphores avec N permits.
     */
    public void arreter() {
        running = false;
        System.out.println("[Broker " + app + "] Arrêt demandé — libération des sémaphores");
        places.release(N);
        messages.release(N);
        System.out.println("[Broker " + app + "] Sémaphores libérés");
    }
}

//import java.util.Random;
//import java.util.concurrent.BrokenBarrierException;
//import java.util.concurrent.CyclicBarrier;
//
//public class Broker extends Thread {
//
//    private int nombreMessagesEnTraitement;
//    private int N;
//    private int tempsExecution;
//    private CyclicBarrier barriereConnectPub;
//    private CyclicBarrier barrierePub;
//    private CyclicBarrier barriereConnectSub;
//    private CyclicBarrier barriereSub;
//
//    public Broker(int tempsExecution, int N, CyclicBarrier barriereConnectPub, CyclicBarrier barrierePub,
//                  CyclicBarrier barriereConnectSub, CyclicBarrier barriereSub) {
//        this.nombreMessagesEnTraitement = 0;
//        this.tempsExecution = tempsExecution;
//        this.N = N;
//        this.barriereConnectPub = barriereConnectPub;
//        this.barrierePub = barrierePub;
//        this.barriereConnectSub = barriereConnectSub;
//        this.barriereSub = barriereSub;
//    }
//
//    private void connect_pub() {
//        try {
//            this.barriereConnectPub.await();
//        } catch (InterruptedException | BrokenBarrierException e) {
//            System.out.println("PROBLÈME: le broker s'est interrompu ou la barrière s'est brisée.");
//        }
//        pub();
//    }
//
//    private void pub() {
//        try {
//            this.barrierePub.await();
//        } catch (InterruptedException | BrokenBarrierException e) {
//            System.out.println("PROBLÈME: le broker s'est interrompu ou la barrière s'est brisée.");
//        }
//        nombreMessagesEnTraitement++;
//    }
//
//    private void connect_sub() {
//        try {
//            this.barriereConnectSub.await();
//        } catch (InterruptedException | BrokenBarrierException e) {
//            System.out.println("PROBLÈME: le broker s'est interrompu ou la barrière s'est brisée.");
//        }
//        sub();
//    }
//
//    private void sub() {
//        try {
//            this.barriereSub.await();
//        } catch (InterruptedException | BrokenBarrierException e) {
//            System.out.println("PROBLÈME: le broker s'est interrompu ou la barrière s'est brisée.");
//        }
//        nombreMessagesEnTraitement--;
//    }
//
//    public void run() {
//        Random choix = new Random();
//        long start = System.currentTimeMillis();
//        do {
//            if (this.nombreMessagesEnTraitement == 0 || (this.barriereConnectSub.getNumberWaiting() == 0
//                    && this.barriereConnectPub.getNumberWaiting() == 1)) {
//                connect_pub();
//            } else if (this.nombreMessagesEnTraitement == N || (this.barriereConnectPub.getNumberWaiting() == 0
//                    && this.barriereConnectSub.getNumberWaiting() == 1)) {
//                connect_sub();
//            } else {
//                if (choix.nextBoolean()) {
//                    connect_pub();
//                } else {
//                    connect_sub();
//                }
//            }
//        } while (System.currentTimeMillis() < start + this.tempsExecution);
//    }
//
//}
