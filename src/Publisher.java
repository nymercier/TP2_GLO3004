/*
 * Modélise PUB3 du FSP :
 *   PUB3 = (supply -> connect_pub -> pub -> PUB3)
 *
 * Selon la trace on demande : <app>.<prefixe>.<numéro> <ACTION>
 * i.publisher.2 SUPPLY
 */
import java.util.concurrent.ThreadLocalRandom;

public class Publisher extends Thread {

    private final String app;      // "i" ou "t"
    private final String prefixe;  // "publisher" ou "subscriber"
    private final int numero;
    private final Broker broker;
    private volatile boolean running = true;
    private String message;

    public Publisher(String app, String prefixe, int numero, Broker broker) {
        this.app     = app;
        this.prefixe = prefixe;
        this.numero  = numero;
        this.broker  = broker;
        this.message = "";
        setName(app + "." + prefixe + "." + numero);
    }

    private String label(String action) {
        return app + "." + prefixe + "." + numero + " " + action;
    }

    private void supply() {
        if (!running || Thread.currentThread().isInterrupted()) {
            return;
        }
        String tempMessage = "";
        int longueur = 10 + ThreadLocalRandom.current().nextInt(15);
        for (int i = 0; i <= longueur; i++) {
            tempMessage += (char)(ThreadLocalRandom.current().nextInt(26) + 97);
        }
        this.message = tempMessage;
        synchronized (System.out) {
            System.out.println(label("SUPPLY message \"" + this.message + "\""));
        }
    }

    @Override
    public void run() {
        try {
            while (running && !isInterrupted()) {
                if (this.message == null || this.message.isEmpty()) {
                    supply();
                }
                broker.connectPub(getName());
                if (!running || Thread.currentThread().isInterrupted()) break;
                broker.pub(getName(), this.message);
                this.message = "";
                broker.closePub(getName());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println(label("TERMINÉ"));
    }

    public void arreter() {
        running = false;
        interrupt();
    }
}