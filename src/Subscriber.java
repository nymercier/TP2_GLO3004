/*
 * Modélise SUB3 du FSP :
 *   SUB3 = (connect_sub -> sub -> consume -> SUB3)
 *
 * Selon la trace on demande : <app>.<prefixe>.<numéro> <ACTION>
 * i.subscriber.2 CONNECT_SUB
 */

public class Subscriber extends Thread {

    private final String app;      // "i" ou "t"
    private final String prefixe;  // "subscriber"
    private final int    numero;   // 1, 2, 3, ...
    private final Broker broker;
    private volatile boolean running = true;

    public Subscriber(String app, String prefixe, int numero, Broker broker) {
        this.app     = app;
        this.prefixe = prefixe;
        this.numero  = numero;
        this.broker  = broker;
        setName(app + "." + prefixe + "." + numero);
    }

    private String label(String action) {
        return app + "." + prefixe + "." + numero + " " + action;
    }

//    private void consume(String msg) {
//        this.message = msg;
//        System.out.println(label("CONSUME message \"" + this.message + "\""));
//    }

    @Override
    public void run() {
        try {
            while (running) {
                broker.connectSub(getName());
                String msg = broker.sub(getName());
                broker.closeSub(getName());

                if (msg != null) {
                    synchronized (System.out) {
                        System.out.println(label("CONSUME message \"" + msg + "\""));
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void arreter() {
        running = false;
        interrupt();
    }
}