import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;

public class Main {
    public static void main(String[] args) throws Exception {
        int N = 2;
        int NB_P = 1;
        int NB_S = 1;
        int tempsExecution = 5000;

        CyclicBarrier barriereConnectPub = new CyclicBarrier(2);
        CyclicBarrier barrierePub = new CyclicBarrier(2);
        CyclicBarrier barriereConnectSub = new CyclicBarrier(2);
        CyclicBarrier barriereSub = new CyclicBarrier(2);

        ArrayList<Publisher> publishers = new ArrayList<Publisher>();
        for (int i = 0; i < NB_P; i++) {
            publishers.add(new Publisher("publisher" + i, "indemnisation", barriereConnectPub, barrierePub));
            publishers.add(new Publisher("publisher" + i, "tarification", barriereConnectPub, barrierePub));
        }

        ArrayList<Subscriber> subscribers = new ArrayList<Subscriber>();
        for (int i = 0; i < NB_S; i++) {
            subscribers.add(new Subscriber("subscriber" + i, "indemnisation", barriereConnectSub, barriereSub));
            subscribers.add(new Subscriber("subscriber" + i, "tarification", barriereConnectSub, barriereSub));
        }

        Broker broker = new Broker(tempsExecution, barriereConnectPub, barrierePub, barriereConnectSub, barriereSub);

        for (int i = 0; i < publishers.size(); i++) {
            publishers.get(i).start();
        }

        for (int i = 0; i < subscribers.size(); i++) {
            subscribers.get(i).start();
        }

        broker.start();
    }
}