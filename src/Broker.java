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
    private final Semaphore places;                         // contrôle connect_pub
    private final Semaphore messagesIndemnisation;          // msgs présents I
    private final Semaphore messagesTarification;           // msgs présents T
    private final Semaphore mutex;                          // protège ArrayList
    private boolean running = true;
    private ArrayList<String> listeMessagesIndemnisation;
    private ArrayList<String> listeMessagesTarification;

    public Broker(int n, Semaphore mutex) {
        this.N = n;
        this.places = new Semaphore(N, true);
        this.messagesIndemnisation = new Semaphore(0, true);
        this.messagesTarification = new Semaphore(0, true);
        this.mutex = mutex;
        this.listeMessagesIndemnisation = new ArrayList<String>();
        this.listeMessagesTarification = new ArrayList<String>();

    }

    private void log(String threadName, String action, String message) {
        synchronized (System.out) {
            int nbMessagesReels = listeMessagesIndemnisation.size() + listeMessagesTarification.size();

            System.out.printf("[Broker %s | [%d/%d] | %-15s | %-12s | %s%n",
                    threadName.charAt(0), nbMessagesReels, N, threadName, action, message);
        }
    }
    /**
     * connect_pub : le publisher attend qu'il y ait de la place
     */
    public void connectPub(String threadName) throws InterruptedException {
        places.acquire();
        if (!isRunning()) {
            places.release();
            throw new InterruptedException();
        }
        log(threadName, "CONNECT_PUB", "connexion");
    }

    public void pub(String threadName, String message) {
        try {
            mutex.acquire(); // On entre dans la zone critique
            if (threadName.startsWith("i")) {
                listeMessagesIndemnisation.add(message);
            } else {
                listeMessagesTarification.add(message);
            }
            log(threadName, "PUB", message);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
        if (threadName.startsWith("i")) {
            messagesIndemnisation.release();
        } else {
            messagesTarification.release();
        }
    }

    public void closePub(String threadName) {
        // action observable dans les traces données par le prof
        // i.publisher.1 CLOSE_PUB
        log(threadName, "CLOSE_PUB", "fermeture");
    }

    public void connectSub(String threadName) throws InterruptedException {
        // On choisit le sémaphore selon le préfixe du thread
        if (threadName.startsWith("i")) messagesIndemnisation.acquire();
        else messagesTarification.acquire();

        if (!isRunning()) throw new InterruptedException("Broker arrêté");
        log(threadName, "CONNECT_SUB", "subscription");
    }
    public String sub(String threadName) {
        String msg = null;
        try {
            mutex.acquire();
            if (threadName.startsWith("i")) msg = listeMessagesIndemnisation.removeFirst();
            else msg = listeMessagesTarification.removeFirst();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
        places.release(); // i--
        log(threadName, "SUB", msg);
        return msg;
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
        places.release(N);
        messagesIndemnisation.release(N);
        messagesTarification.release(N);
    }
}