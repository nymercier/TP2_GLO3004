/*
 * Modélise PUB3 du FSP :
 *   PUB3 = (supply -> connect_pub -> pub -> PUB3)
 *
 * Selon la trace on demande : <app>.<prefixe>.<numéro> <ACTION>
 * i.publisher.2 SUPPLY
 */

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Publisher extends Thread {

    private final String app;      // "i" ou "t"
    private final String prefixe;  // "publisher" ou "subscriber"
    private final int numero;
    private final Broker broker;
    private volatile boolean running = true;
    private final Object lock = new Object();
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
        String tempMessage = "";
        int longueur = 10 + ThreadLocalRandom.current().nextInt(15);
        for (int i = 0; i <= longueur; i++) {
            tempMessage += (char)(ThreadLocalRandom.current().nextInt(26) + 97);
        }
        this.message = tempMessage;
        System.out.println(label("SUPPLY message \"" + this.message + "\""));
    }

    private void connect_pub() throws InterruptedException {
        broker.connectPub(getName());  // bloque si file pleine
        System.out.println(label("CONNECT_PUB"));
    }

    private void pub() {
        broker.pub(getName(), this.message);
        checkAction(getName(), "PUB");
        System.out.println(label("PUB"));
    }

    // Est-ce FORBIDDEN fonctionne ?
    // Test pour le publisher seulement, juste pour voir.
    // Oui, on pourrait aussi le faire pour sub si on le voulait.
    private void checkAction(String name, String pub) {
        synchronized (lock) {
            // Règle FORBIDDEN : Un publisher ne fait pas de SUB, un subscriber ne fait pas de PUB
            if (name.contains("publisher") && (pub.contains("SUB") || pub.contains("CONNECT_SUB"))) {
                throw new IllegalStateException("VIOLATION FORBIDDEN: Publisher a tenté une action de Subscriber !");
            }
        }
    }

    private void close_pub() {
        broker.closePub(getName());
        System.out.println(label("CLOSE_PUB"));
    }

    @Override
    public void run() {
        try {
            while (running) {
                if (this.broker.nbMessagesApp(this.app.charAt(0)) != this.broker.getN()) {
                    supply();
                    if (!running) break;
                    connect_pub();
                    if (!running) {
                        broker.closePub(getName());
                        break;
                    }
                    pub();
                    close_pub();
                }
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

//public class Publisher extends Thread {
//
//    private String prefixe;
//    private String app;
//    private CyclicBarrier barriereConnectPub;
//    private CyclicBarrier barrierePub;
//    private boolean interrupted;
//
//    public Publisher(String prefixe, String app, CyclicBarrier barriereConnectPub, CyclicBarrier barrierePub) {
//        this.prefixe = prefixe;
//        this.app = app;
//        this.barriereConnectPub = barriereConnectPub;
//        this.barrierePub = barrierePub;
//        this.interrupted = false;
//    }
//
//    private void supply() {
//        System.out.printf("Production d'un message par %s (%s).%n", this.prefixe, this.app);
//    }
//
//    private void connect_pub() {
//        synchronized (Publisher.class) {
//            try {
//                this.barriereConnectPub.await();
//            } catch (BrokenBarrierException | InterruptedException e) {
//                System.exit(0);
//            }
//            System.out.printf("Connection au broker par %s (%s).%n", this.prefixe, this.app);
//            pub();
//        }
//    }
//
//    private void pub() {
//        try {
//            this.barrierePub.await();
//        } catch (BrokenBarrierException | InterruptedException e) {
//            System.exit(0);
//        }
//        System.out.printf("Publication d'un message par %s (%s).%n", this.prefixe, this.app);
//    }
//
//    public void arreter() {
//        this.interrupted = true;
//    }
//
//    public void run() {
//        while (!this.interrupted) {
//            supply();
//            connect_pub();
//        }
//    }
//}
