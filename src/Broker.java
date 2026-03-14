import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
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
    private volatile boolean running = true;
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
            System.out.printf("[Broker %s | %d/%d msgs] %s %s, Message: %s%n",
                    threadName.charAt(0), nbMessagesReels, N, threadName, action, message);
        }
    }
    /**
     * connect_pub : Un publisher demande l'accès au broker pour publier un message.
     * Implémentation :
     *  - le sémaphore "places" garantit qu'il reste de la capacité
     *  - le mutex garantit l'exclusion mutuelle pour accéder aux structures internes
     */
    public void connectPub(String threadName) throws InterruptedException {
        // On attend qu'il y ait une place (i < N)
        places.acquire();
        mutex.acquire();

        if (!isRunning()) {
            mutex.release();
            places.release();
            throw new InterruptedException();
        }
        log(threadName, "CONNECT_PUB", "connexion");
    }

    /**
     * pub : Un publisher publie un message dans le broker.
     * Le message est ajouté à la file correspondant à l'application :
     * - "i" pour indemnisation
     * - "t" pour tarification
     * (queue dans la spécification).
     */

    public void pub(String threadName, String message) {
        if (threadName.startsWith("i")) {
            listeMessagesIndemnisation.add(message);
            messagesIndemnisation.release();
        } else {
            listeMessagesTarification.add(message);
            messagesTarification.release();
        }
        log(threadName, "PUB", message);
    }

    public void closePub(String threadName) {
        log(threadName, "CLOSE_PUB", "fermeture");
        mutex.release();
    }

    /**
     * connect_sub : Un subscriber demande l'accès au broker pour récupérer un message
     * Implémentation :
     *   - attente sur le sémaphore correspondant au type d'application
     *   - acquisition du mutex pour garantir l'exclusion mutuelle
     */

    public void connectSub(String threadName) throws InterruptedException {
        if (threadName.startsWith("i")) {
            messagesIndemnisation.acquire();
        } else {
            messagesTarification.acquire();
        }
        // On verrouille le broker pour l'exclusivité
        mutex.acquire();
        try {
            if (!isRunning()) {
                throw new InterruptedException("Broker arrêté");
            }
            log(threadName, "CONNECT_SUB", "subscription");
        } catch (InterruptedException e) {
            mutex.release();
            throw e;
        }
    }

    /**
     * sub : Un subscriber récupère un message depuis la file du broker.
     * Le message est retiré de la liste correspondante.
     * (dequeue dans la spécification).
     */

    public String sub(String threadName) {
        String msg = null;
        if (threadName.startsWith("i")) {
            msg = listeMessagesIndemnisation.remove(0);
        } else {
            msg = listeMessagesTarification.remove(0);
        }

        log(threadName, "SUB", msg);
        return msg;
    }

    public void closeSub(String threadName) {
        log(threadName, "CLOSE_SUB", "fermeture");
        places.release();
        mutex.release();
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * Arrêt "propre" : on met la variable "running" à false et on relâche tous les threads bloqués
     * en libérant les deux sémaphores avec N permits.
     */
    public void arreter() {
        running = false;
        places.release(N);
        messagesIndemnisation.release(N);
        messagesTarification.release(N);
    }
}