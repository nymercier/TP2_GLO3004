import java.util.Timer;
import java.util.concurrent.CyclicBarrier;

public class Broker {

    private int nombreMessagesEnTraitement;
    private int tempsExecution;
    private CyclicBarrier barriereConnectPub;
    private CyclicBarrier barriereConnectSub;
    private Timer timer;

    public Broker(int tempsExecution, CyclicBarrier barriereConnectPub, CyclicBarrier barrierePub,
                  CyclicBarrier barriereConnectSub, CyclicBarrier barriereSub) {
        this.nombreMessagesEnTraitement = 0;
        this.tempsExecution = tempsExecution;
        this.barriereConnectPub = barriereConnectPub;
        this.barriereConnectSub = barriereConnectSub;
        this.timer = new Timer();
    }




}
