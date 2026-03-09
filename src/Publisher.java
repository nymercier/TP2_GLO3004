/*
 * Modélise PUB3 du FSP :
 *   PUB3 = (supply -> connect_pub -> pub -> PUB3)
 *
 * Selon la trace on demande : <app>.<prefixe>.<numéro> <ACTION>
 * i.publisher.2 SUPPLY
 */

public class Publisher extends Thread {

    private final String app;      // "i" ou "t"
    private final String prefixe;  // "publisher"
    private final int numero;
    private final Broker broker;
    private boolean running = true;

    public Publisher(String app, String prefixe, int numero, Broker broker) {
        this.app     = app;
        this.prefixe = prefixe;
        this.numero  = numero;
        this.broker  = broker;
        setName(app + "." + prefixe + "." + numero);
    }

    private String label(String action) {
        return app + "." + prefixe + "." + numero + " " + action;
    }

    private void supply() {
        System.out.println(label("SUPPLY"));
    }

    // Sortir "proprement"
    private void connect_pub() throws InterruptedException {
        broker.connectPub();  // bloque si file pleine
        System.out.println(label("CONNECT_PUB"));
    }

    private void pub() {
        broker.pub();
        System.out.println(label("PUB"));
    }

    private void close_pub() {
        broker.closePub();
        System.out.println(label("CLOSE_PUB"));
    }

    @Override
    public void run() {
        try {
            while (running) {
                supply();
                connect_pub();
                if (!running) break;
                pub();
                close_pub();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
