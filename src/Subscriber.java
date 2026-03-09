/*
 * Modélise SUB3 du FSP :
 *   SUB3 = (connect_sub -> sub -> consume -> SUB3)
 *
 * Selon la trace on demande : <app>.<prefixe>.<numéro> <ACTION>
 * i.subscriber.2 CONNECT_SUB
 */

public class Subscriber extends Thread {

    private final String app;      // "i" ou "t"
    private final String prefixe;  // "subscriber"
    private final int    numero;   // 1, 2, 3, ...
    private final Broker broker;
    private boolean running = true;

    public Subscriber(String app, String prefixe, int numero, Broker broker) {
        this.app     = app;
        this.prefixe = prefixe;
        this.numero  = numero;
        this.broker  = broker;
        setName(app + "." + prefixe + "." + numero);
    }

    private String label(String action) {
        return app + "." + prefixe + "." + numero + " " + action;
    }

    private void connect_sub() throws InterruptedException {
        broker.connectSub(getName());
        System.out.println(label("CONNECT_SUB"));
    }

    private void sub() {
        broker.sub(getName());
        System.out.println(label("SUB"));
    }

    private void consume() {
        System.out.println(label("CONSUME"));
    }
    private void close_sub() {
        broker.closeSub(getName());
        System.out.println(label("CLOSE_SUB"));
    }

    @Override
    public void run() {
        try {
            while (running) {
                connect_sub();
                if (!running) break;
                sub();
                close_sub();
                consume();
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


//public class Subscriber extends Thread {
//
//    private String prefixe;
//    private String app;
//    private CyclicBarrier barriereConnectSub;
//    private CyclicBarrier barriereSub;
//    private boolean interrupted;
//
//    public Subscriber(String prefixe, String app, CyclicBarrier barriereConnectSub, CyclicBarrier barriereSub) {
//        this.prefixe = prefixe;
//        this.app = app;
//        this.barriereConnectSub = barriereConnectSub;
//        this.barriereSub = barriereSub;
//        this.interrupted = false;
//    }
//
//    private void connect_sub() {
//        synchronized (Subscriber.class) {
//            try {
//                this.barriereConnectSub.await();
//            } catch (BrokenBarrierException | InterruptedException e) {
//                System.exit(0);
//            }
//            System.out.printf("Connection au broker par %s (%s).%n", this.prefixe, this.app);
//            sub();
//        }
//    }
//
//    private void sub() {
//        try {
//            barriereSub.await();
//        } catch (BrokenBarrierException | InterruptedException e) {
//            System.exit(0);
//        }
//        System.out.printf("Réception d'un message par %s (%s).%n", this.prefixe, this.app);
//    }
//
//    private void consume() {
//        System.out.printf("Consommation d'un message par %s (%s).%n", this.prefixe, this.app);
//    }
//
//    public void arreter() {
//        this.interrupted = true;
//    }
//
//    public void run() {
//        while (!this.interrupted) {
//            connect_sub();
//            consume();
//        }
//    }
//}
