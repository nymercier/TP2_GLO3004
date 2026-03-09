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
    private final Semaphore places;    // contrôle connect_pub
    private final Semaphore messages;  // msgs présents → contrôle connect_sub
    private boolean running = true;

    public Broker(int n) {
        this.N        = n;
        this.places   = new Semaphore(N, true);             // éviter la famine
        this.messages = new Semaphore(0, true);
    }

//    private void log(String threadName, String action) {
//        System.out.println("[Broker " + N + "] " + threadName + " " + action);
//    }

    /**
     * connect_pub : le publisher attend qu'il y ait de la place
     * File est pleine (i == N).
     */
    public void connectPub() throws InterruptedException {
        places.acquire();
    }

    /**
     * pub + queue : dépôt effectif du message
     * Incrémente le compteur de messages disponibles.
     * Production
     */
    public void pub() {
        messages.release(); // i → i+1
    }

    public void closePub() {
        // action observable dans les traces données par le prof
        // i.publisher.1 CLOSE_PUB
    }

    /**
     * connect_sub : le subscriber attend qu'il y ait un message
     * File est vide (i == 0).
     */
    public void connectSub() throws InterruptedException {
        messages.acquire();
    }

    /**
     * sub + dequeue : consommation effective
     * Libère un slot pour les publishers.
     * Consommation
     */
    public void sub() {
        places.release();   // i → i-1
    }

    public void closeSub() {
        // action observable dans les traces données par le prof
        // i.subscriber.2 CLOSE_SUB
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
        places.release(N);
        messages.release(N);
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
