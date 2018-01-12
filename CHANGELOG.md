Änderungsverfolgung KEx TLS
===========================

01.12.2015 - V 2.1.1 =============================================================================================================
----------------------------------------------------------------------------
Zeitoptimierung Timer im Client
----------------------------------------------------------------------------
Änderungen nach Vorgabe BitCtrl, Gieseler


22.07.2014 - V 2.1.0 =============================================================================================================
----------------------------------------------------------------------------
Fragmentierte Telegramme beim Empfang
----------------------------------------------------------------------------
Folgendes Problem wurden behoben:

Die TLS-Over-IP-Implementierung wirft manchmal eine Exception, die in Folge dann auch zur Terminierung der TLS-Over-IP-Verbindung führt.

Hier der Stacktrace:


FEHLER : KEx-TLS.de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.Client
Länge des Datenblocks laut Header (60) kleiner als vorhandene Anzahl der Nutzdaten (36):
     de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.Client$IllegalTelegramException: Länge des Datenblocks laut Header (60) kleiner als vorhandene Anzahl der Nutzdaten (36)
       at de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.Client$Link.handleSelection(Client.java:1194)
       at de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.Client$Worker.run(Client.java:1534)
       at java.lang.Thread.run(Thread.java:744)

Ursache für die Exception ist die Tatsache, dass ein Telegramm auf dem Weg vom Sender zum Empfänger
so zerstückelt werden kann, dass es in zwei Teilen beim Empfänger ankommen kann. Die bisherige
Implementierung wirft in diesem Fall eine Exception und terminiert die Verbindung.

TCP gibt keinerlei Garantien für den Erhalt von Blockgrenzen und eine TCP-Anwendung sollte damit
umgehen können, wenn Telegramme zerstückelt werden.

Die Implementierung wurde so geändert, das teilweise empfangene Telegramm im _readBuffer belassen werden
und ganz normal (ohne zusätzliche Timeouts) auf den Empfang von weiteren Daten gewartet wird. Wenn dann
weitere Daten kommen wird der Header des Telegramms erneut aus dem _readBuffer gelesen und erneut geprüft,
ob das Telegramm vollständig ist.

06.04.2012 - V 2.0.0 =============================================================================================================
----------------------------------------------------------------------------
Erste Veröffentlichte Version
----------------------------------------------------------------------------
Freigegeben in NI.
Einsatz u.a. in NI (seit 10.2011) und BW