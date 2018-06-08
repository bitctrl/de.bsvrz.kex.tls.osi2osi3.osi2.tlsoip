Versionsverlauf
===============

## [Noch nicht veröffentlicht]

- Polling im Client ersetzt, um die erzeugte CPU-Last zu verringern

## [Version 2.1.2]

- NERZ: Umstellung auf Gradle, Build durch FTB und Bereitstellung auf NERZ-Repositories

## Version 2.1.1

- Stand 01.12.2015 

### Zeitoptimierung Timer im Client
  - Änderungen nach Vorgabe BitCtrl, Gieseler


## Version 2.1.0

- Stand 22.07.2014

### Fragmentierte Telegramme beim Empfang

Folgendes Problem wurden behoben:

- Die TLS-Over-IP-Implementierung wirft manchmal eine Exception, die in Folge dann auch 
zur Terminierung der TLS-Over-IP-Verbindung führt.

Hier der Stacktrace:

```java
FEHLER : KEx-TLS.de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.Client
Länge des Datenblocks laut Header (60) kleiner als vorhandene Anzahl der Nutzdaten (36):
     de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.Client$IllegalTelegramException: Länge des Datenblocks laut Header (60) kleiner als vorhandene Anzahl der Nutzdaten (36)
       at de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.Client$Link.handleSelection(Client.java:1194)
       at de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.Client$Worker.run(Client.java:1534)
       at java.lang.Thread.run(Thread.java:744)
```

Ursache für die Exception ist die Tatsache, dass ein Telegramm auf dem Weg vom Sender zum Empfänger
so zerstückelt werden kann, dass es in zwei Teilen beim Empfänger ankommen kann. Die bisherige
Implementierung wirft in diesem Fall eine Exception und terminiert die Verbindung.

TCP gibt keinerlei Garantien für den Erhalt von Blockgrenzen und eine TCP-Anwendung sollte damit
umgehen können, wenn Telegramme zerstückelt werden.

Die Implementierung wurde so geändert, das teilweise empfangene Telegramm im `_readBuffer` belassen werden
und ganz normal (ohne zusätzliche Timeouts) auf den Empfang von weiteren Daten gewartet wird. Wenn dann
weitere Daten kommen wird der Header des Telegramms erneut aus dem `_readBuffer` gelesen und erneut geprüft,
ob das Telegramm vollständig ist.

## Version 2.0.0

- Stand 06.04.2012 

### Erste Veröffentlichte Version
- Freigegeben in NI.
- Einsatz u.a. in NI (seit 10.2011) und BW

[Noch nicht veröffentlicht]: https://gitlab.nerz-ev.de/ERZ/SWE_de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip/compare/v2.1.2...HEAD
[Version 1.0.1]: https://gitlab.nerz-ev.de/ERZ/SWE_de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip/compare/v2.1.1...v2.1.2
