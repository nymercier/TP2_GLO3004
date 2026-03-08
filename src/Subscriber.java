import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Subscriber extends Thread {

    private final String prefixe;
    private final String app;
    private final CyclicBarrier barriereConnectSub;
    private final CyclicBarrier barriereSub;
    private boolean interrupted;

    public Subscriber(String prefixe, String app, CyclicBarrier barriereConnectSub, CyclicBarrier barriereSub) {
        this.prefixe = prefixe;
        this.app = app;
        this.barriereConnectSub = barriereConnectSub;
        this.barriereSub = barriereSub;
        this.interrupted = false;
    }

    private void connect_sub() {
        synchronized (Subscriber.class) {
            try {
                this.barriereConnectSub.await();
            } catch (BrokenBarrierException | InterruptedException e) {
                System.exit(0);
            }
            System.out.printf("Connection au broker par %s (%s).%n", this.prefixe, this.app);
            sub();
        }
    }

    private void sub() {
        try {
            barriereSub.await();
        } catch (BrokenBarrierException | InterruptedException e) {
            System.exit(0);
        }
        System.out.printf("Réception d'un message par %s (%s).%n", this.prefixe, this.app);
    }

    private void consume() {
        System.out.printf("Consommation d'un message par %s (%s).%n", this.prefixe, this.app);
    }

    public void arreter() {
        this.interrupted = true;
    }

    public void run() {
        while (!this.interrupted) {
            connect_sub();
            consume();
        }
    }
}
