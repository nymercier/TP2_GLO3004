import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Publisher extends Thread {

    private String prefixe;
    private String app;
    private CyclicBarrier barriereConnectPub;
    private CyclicBarrier barrierePub;

    public Publisher(String prefixe, String app, CyclicBarrier barriereConnectPub, CyclicBarrier barrierePub) throws Exception {
        if (!app.equals("indemnisation") && !app.equals("tarification")) {
            throw new Exception("Doit être indemnisation ou tarification");
        }
        this.prefixe = prefixe;
        this.app = app;
        this.barriereConnectPub = barriereConnectPub;
        this.barrierePub = barrierePub;
    }

    private void supply() {
        System.out.printf("Production d'un message par %s (%s).%n", this.prefixe, this.app);
    }

    private synchronized void connect_pub() {
        try {
            this.barriereConnectPub.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            System.out.printf("PROBLÈME: le thread %s (%s) s'est interrompu ou la barrière s'est brisée.", this.prefixe, this.app);
        }
        System.out.printf("Connection au broker par %s (%s).%n", this.prefixe, this.app);
    }

    private synchronized void pub() {
        try {
            this.barrierePub.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            System.out.printf("PROBLÈME: le thread %s (%s) s'est interrompu ou la barrière s'est brisée.", this.prefixe, this.app);
        }
        System.out.printf("Publication d'un message par %s (%s).%n", this.prefixe, this.app);
    }

    public void run() {
        supply();
        connect_pub();
        pub();
    }
}
