import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Broker extends Thread {

    private int nombreMessagesEnTraitement;
    private int N;
    private int tempsExecution;
    private CyclicBarrier barriereConnectPub;
    private CyclicBarrier barriereConnectSub;

    public Broker(int tempsExecution, CyclicBarrier barriereConnectPub, CyclicBarrier barriereConnectSub) {
        this.nombreMessagesEnTraitement = 0;
        this.tempsExecution = tempsExecution;
        this.barriereConnectPub = barriereConnectPub;
        this.barriereConnectSub = barriereConnectSub;
    }

    private void connect_pub() {
        try {
            this.barriereConnectPub.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            System.out.println("PROBLÈME: le broker s'est interrompu ou la barrière s'est brisée.");
        }
        pub();
    }

    private void pub() {
        nombreMessagesEnTraitement++;
        System.out.printf("Il y a maintenant %d messages en traitement.", this.nombreMessagesEnTraitement);
    }

    private void connect_sub() {
        try {
            this.barriereConnectSub.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            System.out.println("PROBLÈME: le broker s'est interrompu ou la barrière s'est brisée.");
        }
        sub();
    }

    private void sub() {
        nombreMessagesEnTraitement--;
        System.out.printf("Il y a maintenant %d messages en traitement.", this.nombreMessagesEnTraitement);
    }

    public void run() {
        Random choix = new Random();
        long start = System.currentTimeMillis();
        do {
            if (this.nombreMessagesEnTraitement == 0 || (this.barriereConnectSub.getNumberWaiting() == 0
                    && this.barriereConnectPub.getNumberWaiting() == 1)) {
                connect_pub();
            } else if (this.nombreMessagesEnTraitement == N || (this.barriereConnectPub.getNumberWaiting() == 0
                    && this.barriereConnectSub.getNumberWaiting() == 1)) {
                connect_sub();
            } else {
                if (choix.nextBoolean()) {
                    connect_pub();
                } else {
                    connect_sub();
                }
            }
        } while (System.currentTimeMillis() < start + this.tempsExecution);
    }

}
