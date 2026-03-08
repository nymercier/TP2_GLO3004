import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Subscriber extends Thread {

    private String prefixe;
    private String app;
    private CyclicBarrier barriereConnectSub;
    private CyclicBarrier barriereSub;

    public Subscriber(String prefixe, String app, CyclicBarrier barriereConnectSub, CyclicBarrier barriereSub) {
        this.prefixe = prefixe;
        this.app = app;
        this.barriereConnectSub = barriereConnectSub;
        this.barriereSub = barriereSub;
    }

    private void connect_sub() {
        synchronized (Subscriber.class) {
            try {
                this.barriereConnectSub.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                System.out.printf("PROBLÈME: le thread %s (%s) s'est interrompu ou la barrière s'est brisée.", this.prefixe, this.app);
            }
            System.out.printf("Connection au broker par %s (%s).%n", this.prefixe, this.app);
            sub();
        }
    }

    private void sub() {
        try {
            barriereSub.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            System.out.printf("PROBLÈME: le thread %s (%s) s'est interrompu ou la barrière s'est brisée.", this.prefixe, this.app);
        }
        System.out.printf("Réception d'un message par %s (%s).%n", this.prefixe, this.app);
    }

    private void consume() {
        System.out.printf("Consommation d'un message par %s (%s).%n", this.prefixe, this.app);
    }

    public void run() {
        connect_sub();
        consume();
    }
}
