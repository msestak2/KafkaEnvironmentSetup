# Navodila za vzpostavitev Kafka gruče v sistemu

**Sistemske zahteve**

Za uspešen zagon potrebnih komponent je potrebno v sistemu imeti pripravljeno naslednje:
-	Nameščena Java v sistemu (JDK 1.8+)
-	Vzpostavljeno Docker okolje za virtualizacijo
-	Nameščeno orodje oz. IDE za razvoj aplikacij v programskem jeziku Java (zaželen JetBrains IntelliJ IDEA Community Edition)
-	Nameščeno orodje Offset Explorer za vizualni pregled nad Kafka gručo (povezava za prevzem orodja: https://www.kafkatool.com/download.html)

Za zagon osnovne Kafka gruče v sistemu potrebujemo:
- **Zookeeper** - koordinira komponente in splošno delovanje gruče
- **Kafka posrednik** (1 ali več njih) - sprejema podatkovne toke oz. prispela sporočila proizvajalcev v teme in particije

Za lažjo izvedbo (de)serializacije podatkovnih tokov se uporablja tudi **register shem** (ang. schema registry), ki ga tudi zaženemo v sistemu. V registru shem shranimo metapodatke oz. opis sheme objektov, ki jih v Kafka temo pošilja proizvajalec.

Za zagon Kafka gruče z enim posrednikom, najprej zaženemo tri Docker servisa v sistemu z naslednjim ukazom:
```
docker-compose up zookeeper broker schema-registry
```

Po uspešnem zagonu servisa Zookeeper se prikaže naslednje:
![zookeeper](Posnetki/zookeeper.png?raw=true "Uspešen zagon Zookeeperja v sistemu.")

Posrednik se ob zagonu more povezati na Zookeeper in se na ta način registrira v gruči. Po uspešnem zagonu posrednika dobimo naslednji izpis, iz kataerega je razvidno, da se uspešno povezal z Zookeeperjem:
![broker](Posnetki/broker.png?raw=true "Uspešen zagon posrednika v sistemu.")

### Ustvarjanje Kafka tem

Medtem ko je gruča aktivna, lahko ustvarimo teme na zagnanem posredniku. Teme lahko ustvarimo preko komandne linije ali grafičnega orodja Offset Explorer. 
Za ustvarjanje teme v orodju Offset Explorer, najprej konfiguriramo povezavo do Kafka gruče:
1. desni klik na Clusters -> Add new Connection
2. v oknu za konfiguracijo povezave, vnesemo naslednje podatke:
  - zavihek Properties:
    - Cluster name: poljubno ime
    - Zookeeper Host: localhost
    - Zookeper Port : 2181
  - zavihek Advanced: 
    - Bootstrap servers: localhost:9092
3. kliknemo Add na koncu

Ko je povezava uspešno ustvarjena, razširimo meni pod povezavo z leve strani in na opcijo Topics naredimo desni klik -> Create Topic. Ustvarili bomo dve temi: *measurements-pm* (za meritve onesneževalca pm10) in *measurements-o* (za meritve ozona, no2 in co). Ime teme vnesemo pod Topic Name in kliknemo Add.

Če smo vse uspešno izvedli, bomo v seznamu Kafka tem dobili 4 teme kot je razvidno na spodnji sliki:
![topics](Posnetki/topics.png?raw=true "Nabor Kafka tem v orodju Offset Explorer.")

### Pošiljanje in branje sporočil v/iz Kafka teme (proizvajalec - potrošnik)

Ko smo ustvarili Kafka teme na posredniku, lahko začnemo pošiljati sporočila oz. meritve čestic na določenih postajah. V mapi `MeasurementProducer\out\artifacts\MeasurementProducer_jar` se nahaja vnaprej pripravljena JAR aplikacija, ki predstavlja Kafka proizvajalca, ki neprekinjeno pošilja meritve v obadve temi po določenem intervalu. Proizvajalca zaženemo v komandni liniji z ukazom:
```
java -jar MeasurementProducer.jar
```

![producer](Posnetki/producer.png?raw=true "Izpis Kafka proizvajalca.")

Na drugi strani pa zaženemo Kafka potrošnika, ki bere podatke iz teme *measurements-o* in jih izpisuje na zaslon.





    
