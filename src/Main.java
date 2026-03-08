import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;

public class Main {
    public static void main(String[] args) throws Exception {
        int N = 2;
        int NB_P = 1;
        int NB_S = 1;
        int tempsExecution = 1000;

        CyclicBarrier barriereConnectPub = new CyclicBarrier(2);
        CyclicBarrier barrierePub = new CyclicBarrier(2);
        CyclicBarrier barriereConnectSub = new CyclicBarrier(2);
        CyclicBarrier barriereSub = new CyclicBarrier(2);

        ArrayList<Publisher> publishers = new ArrayList<>();
        for (int i = 0; i < NB_P; i++) {
            publishers.add(new Publisher("publisher" + i, "indemnisation", barriereConnectPub, barrierePub));
            publishers.add(new Publisher("publisher" + i, "tarification", barriereConnectPub, barrierePub));
        }

        ArrayList<Subscriber> subscribers = new ArrayList<>();
        for (int i = 0; i < NB_S; i++) {
            subscribers.add(new Subscriber("subscriber" + i, "indemnisation", barriereConnectSub, barriereSub));
            subscribers.add(new Subscriber("subscriber" + i, "tarification", barriereConnectSub, barriereSub));
        }

        Broker broker = new Broker(tempsExecution, N, barriereConnectPub, barrierePub, barriereConnectSub, barriereSub);

        for (Publisher publisher : publishers) {
            publisher.start();
        }

        for (Subscriber subscriber : subscribers) {
            subscriber.start();
        }

        broker.start();
        broker.join();

        for (Publisher publisher : publishers) {
            publisher.arreter();
            if (publisher.getState() == Thread.State.WAITING || publisher.getState() == Thread.State.BLOCKED) {
                publisher.interrupt();
            }
        }

        for (Subscriber subscriber : subscribers) {
            subscriber.arreter();
            if (subscriber.getState() == Thread.State.WAITING || subscriber.getState() == Thread.State.BLOCKED) {
                subscriber.interrupt();
            }
        }
    }
}