**EXÉCUTION DU JAR**

Pour exécuter le jar, il suffit de lui passer les options n (capacité du broker), p (nombre de publishers), s (nombre de subscribers) et t (temps d'exécution) de la manière indiquée dans l'énoncé du tp. Par exemple:
java -Dn=2 -Dp=2 -Ds=3 -Dt=100 -jar tp2.jar <br />
Nous recommendons de passer une valeur d'au moins 30ms pour le temps d'exécution, sinon la sortie ne sera pas assez longue pour être très intéressante.


**SORTIE**

Le jar produira comme sortie une liste des actions effectuées dans l'ordre. Voici un exemple d'une ligne indiquant la production d'un message par un publisher:
i.publisher.2 SUPPLY message "spjunrxjyjmw"
Les messages sont aléatoires.

Voici un exemple d'une ligne indiquant la connexion d'un publisher au broker:
[Broker i | 0/2 msgs] i.publisher.2 CONNECT_PUB, Message: connexion
"Broker i"/"Broker t" est pour indiquer si la connexion vient d'un thread d'indemnisation ou de tarification (ce n'est pas parce qu'il y a deux brokers dans le programme, il y en a un seul).
0/2 indique le nombre de messages (au total) présentement dans le broker.

Voici un exemple d'une ligne indiquant la publication d'un message au broker:
[Broker i | 1/2 msgs] i.publisher.2 PUB, Message: spjunrxjyjmw
On voit maintenant 1/2, ce qui inclut le message "spjunrxjyjmw".

Voici un exemple d'une ligne indiquant la fin de la connexion du publisher:
[Broker i | 1/2 msgs] i.publisher.2 CLOSE_PUB, Message: fermeture

Voici un exemple d'une ligne indiquant la connexion d'un subscriber:
[Broker i | 2/2 msgs] i.subscriber.2 CONNECT_SUB, Message: subscription

Voici un exemple d'une ligne indiquant la réception d'un message par le subscriber:
[Broker i | 1/2 msgs] i.subscriber.2 SUB, Message: spjunrxjyjmw
On voit maintenant 1/2, ce qui n'inclut pas le message "spjunrxjyjmw", car il vient d'être retiré (il y a un autre message dans le broker).

Voici un exemple d'une ligne indiquant la fin de la connexion du subscriber:
[Broker i | 1/2 msgs] i.subscriber.2 CLOSE_SUB, Message: fermeture

Voici un exemple d'une ligne indiquant la consommation d'un message par le subscriber:
i.subscriber.2 CONSUME message "spjunrxjyjmw"

Une fois le temps alloué passé, le programme affiche une ligne comme celle-ci:
=== Arrêt après 100 ms ===
Le programme continue quand même de rouler pendant un peu de temps après avoir affiché cette ligne pour faire un "graceful shutdown". Le programme affiche une deuxième ligne une fois l'exécution terminée. Ex:
=== Temps total d'exécution : 132 ms ===
Cela correspond au temps total d'exécution, incluant le graceful shutdown.

**IMPLÉMENTATION**

Le processus PUB3 correspond à la classe Publisher, qui hérite de la classe Thread. Pendant son exécution, un thread publisher fait (en boucle) les actions supply, connect_pub, pub et close_pub. <br />
Le processus SUB3 correspond à la classe Subscriber, qui hérite de la classe Thread. Pendant son exécution, un thread subscriber fait (en boucle) les actions connect_sub, sub, close_sub et consume. <br />
Nous avons choisi d'ajouter close_pub et close_sub (qui ne sont pas dans la spécification), pour aider à voir la durée de la connexion de chaque thread au broker. Ça permet de voir que les threads se connectent et se déconnectent au bon moment, donc qu'ils ne bloquent pas les autres threads quand ils ne sont pas supposé. 
Le processus BROKER4 correspond à la classe Broker. Elle contient les méthodes connect_pub, pub et close_pub (partagées avec les publishers), ainsi que connect_sub, sub et close_sub (partagées avec les subscribers).
Nous avons respecté 1 seul broker, mais nous avons exploré l'avenue d'avoir deux files pour gérer les "i" et "t", donc cela modifierait légèrement le FSP : <br />
BROKER_DUAL = PUBSUB_I[0][0], <br />
PUBSUB_I[i:0..N][t:0..N] = ( <br />
    when (i < N) connect_pub_i -> pub_i -> close_pub_i -> PUBSUB_I[i+1][t] <br />
  | when (i > 0) connect_sub_i -> sub_i -> close_sub_i -> PUBSUB_I[i-1][t] <br />
  | when (t < N) connect_pub_t -> pub_t -> close_pub_t -> PUBSUB_I[i][t+1] <br />
  | when (t > 0) connect_sub_t -> sub_t -> close_sub_t -> PUBSUB_I[i][t-1] <br />
).
<br />
La propriété FORBIDDEN est naturellement respectée, car les threads de la classe Publisher n'ont pas l'option de faire connect_sub, sub, close_sub ou consume et vice-versa.<br />
Le processus CONTROLLER est implémenté à même les méthodes pub et sub qui incrémentent et décrémentent la variable contenant le nombre de messages stockés dans le broker.<br />
La classe Main met le tout en commun, ça correspond au processus SYSTEM11.<br />


