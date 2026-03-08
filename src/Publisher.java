import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Publisher extends Thread {

    private final String prefixe;
    private final String app;
    private final CyclicBarrier barriereConnectPub;
    private final CyclicBarrier barrierePub;
    private boolean interrupted;

    public Publisher(String prefixe, String app, CyclicBarrier barriereConnectPub, CyclicBarrier barrierePub) {
        this.prefixe = prefixe;
        this.app = app;
        this.barriereConnectPub = barriereConnectPub;
        this.barrierePub = barrierePub;
        this.interrupted = false;
    }

    private void supply() {
        System.out.printf("Production d'un message par %s (%s).%n", this.prefixe, this.app);
    }

    private void connect_pub() {
        synchronized (Publisher.class) {
            try {
                this.barriereConnectPub.await();
            } catch (BrokenBarrierException | InterruptedException e) {
                System.exit(0);
            }
            System.out.printf("Connection au broker par %s (%s).%n", this.prefixe, this.app);
            pub();
        }
    }

    private void pub() {
        try {
            this.barrierePub.await();
        } catch (BrokenBarrierException | InterruptedException e) {
            System.exit(0);
        }
        System.out.printf("Publication d'un message par %s (%s).%n", this.prefixe, this.app);
    }

    public void arreter() {
        this.interrupted = true;
    }

    public void run() {
        while (!this.interrupted) {
            supply();
            connect_pub();
        }
    }
}
