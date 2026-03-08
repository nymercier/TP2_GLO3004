import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Broker extends Thread {

    private int nombreMessagesEnTraitement;
    private int N;
    private int tempsExecution;
    private CyclicBarrier barriereConnectPub;
    private CyclicBarrier barrierePub;
    private CyclicBarrier barriereConnectSub;
    private CyclicBarrier barriereSub;

    public Broker(int tempsExecution, int N, CyclicBarrier barriereConnectPub, CyclicBarrier barrierePub,
                  CyclicBarrier barriereConnectSub, CyclicBarrier barriereSub) {
        this.nombreMessagesEnTraitement = 0;
        this.tempsExecution = tempsExecution;
        this.N = N;
        this.barriereConnectPub = barriereConnectPub;
        this.barrierePub = barrierePub;
        this.barriereConnectSub = barriereConnectSub;
        this.barriereSub = barriereSub;
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
        try {
            this.barrierePub.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            System.out.println("PROBLÈME: le broker s'est interrompu ou la barrière s'est brisée.");
        }
        nombreMessagesEnTraitement++;
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
        try {
            this.barriereSub.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            System.out.println("PROBLÈME: le broker s'est interrompu ou la barrière s'est brisée.");
        }
        nombreMessagesEnTraitement--;
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
