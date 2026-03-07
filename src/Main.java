import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;

public class Main {
    public static void main(String[] args) throws Exception {
        int N = 2;
        int NB_P = 2;
        int NB_S = 3;
        int tempsExecution = 5000;

        CyclicBarrier barriereConnectPub = new CyclicBarrier(2);
        CyclicBarrier barriereConnectSub = new CyclicBarrier(2);

        ArrayList<Publisher> publishers = new ArrayList<Publisher>();
        for (int i = 0; i < NB_P; i++) {
            publishers.add(new Publisher("publisher" + i, "indemnisation", barriereConnectPub));
            publishers.add(new Publisher("publisher" + i, "tarification", barriereConnectPub));
        }

        ArrayList<Subscriber> subscribers = new ArrayList<Subscriber>();
        for (int i = 0; i < NB_S; i++) {
            subscribers.add(new Subscriber("subscriber" + i, "indemnisation", barriereConnectSub));
            subscribers.add(new Subscriber("subscriber" + i, "tarification", barriereConnectSub));
        }

        Broker broker = new Broker(tempsExecution, barriereConnectPub, barriereConnectSub);
    }
}