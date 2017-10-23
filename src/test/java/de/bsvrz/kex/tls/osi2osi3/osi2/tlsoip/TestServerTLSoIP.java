/*
 * Copyright (c) 2010-2011 by inovat, innovative systeme - verkehr - tunnel - technik, Dipl.-Ing. H. C. Kniss
 *
 * This file is part of de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.TestServerTLSoIP
 *
 * de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.TestServerTLSoIP is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.TestServerTLSoIP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.TestServerTLSoIP; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Contact Information:
 * inovat, Dipl.-Ing. H. C. Kniss
 * Koelner Strasse 30
 * D-50859 Koeln
 * +49 (0)2234 4301 800
 * info@invat.de
 * www.inovat.de
 */



package de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip;

//~ NICHT JDK IMPORTE =========================================================

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.hexdump.HexDumper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;

//~ KLASSEN ===================================================================

/**
 * Implementiert einen TestServer für TLSoIP, mit dem ein SM simuliert werden kann, welches über TLSoIP angebunden wird
 * und als Server fungiert.
 * <p/>
 * Folgende Funktionen werden unterstützt: <ul> <li>Verbindungseinstellungen werden aus dem übergebenen Anschlusspunkt
 * übernommen.</li> <li></li> <li></li> <li></li> <li></li> </ul>
 *
 * @author inovat, innovative systeme - verkehr - tunnel - technik
 * @author Dipl.-Ing. Hans Christian Kniß (HCK)
 * @version $Revision: 0 $ / $Date: neu $ / ($Author: HCK $)
 */
public class TestServerTLSoIP implements StandardApplication {

    /** DebugLogger fuer Debug-Ausgaben. */
    private static final Debug debug = Debug.getLogger();

    /** Datumsformatierer fuer die Zeitausgabe */
    private static DateFormat zeitFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS:Z");

    //~ FELDER ================================================================

    /** Mamimale Anzahl von Nutzdatenbytes in einem OSI-2 Paket (Telegramm) */
    final private int MAX_ANZAHL_NUTZDATENBYTES_PRO_OIS2_PAKET = 253;

    /** Anzahl der empfangenen Daten-Telegramme seit des letzten Quittungstelegramm-Versands */
    private int _countReceiptDataTel;

    /** Anzahl des gesendeten Daten-Telegramme seit des letzten Quittungstelegramm-Empfangs */
    private int _countSendDataTel;

    /** Datenverteilerverbindung */
    private ClientDavInterface _dav;

    /** Sequenznummer des letzten empfangenen Daten-Telegramms */
    private int _lastReceiptSeqNumDataTel;

    /** Zeitpunkt des letzten empfangenen Telegramms (Keep-Alive, Quittung oder Daten) in Millisekunden */
    private long _lastReceiptTimeAllTel;

    /** Zeitpunkt des letzten empfangenen Daten-Telegramms in Millisekunden */
    private long _lastReceiptTimeDataTel;

    /** Sequenznummer des letzten gesendeten Daten-Telegramms */
    private int _lastSendSeqNumDataTel;

    /** Zeitpunkt des letzten gesendeten Telegramms (Keep-Alive, Quittung oder Daten) in Millisekunden */
    private long _lastSendTimeAllTel;

    /** Zeitpunkt des letzten gesendeten Daten-Telegramms in Millisekunden */
    private long _lastSendTimeDataTel;

    /**
     * PID des AnschlussPunktes des Clients, mit dem dieser Server kommunizieren soll. Von diesem AnschlussPunkt werden die
     * Kommunikationsparameter ermittelt.
     */
    String _pidAPClient;

    /** Flag das signalisiert, dass ein Keep-Alive-Telegramm versendet werden soll */
    private boolean _sendKeepAliveTel;

    /** Flag das signalisiert, dass ein Quittungs-Telegramm versendet werden soll */
    private boolean _sendQuittTel;

    /** Serverport, auf dem Anfragen entgegengenommen werden. Dynamische und oder Private Ports (49152 bis 65535) gemäß IANA konfigurierbar */
    private int _tlsoipCAcceptPort;

    /** Portnummer des Servers */
    private int _tlsoipCAcceptPortA;

    /** Zeit [s], nach der ein Keep-Alive-Telegramm an die Gegenstelle versendet werden muss (0=ausgeschaltet für Testzwecke, 1...3599) */
    private int _tlsoipCHelloDelay;

    /** Zeit [s], nach der spätestens ein Keep-Alive-Telegramm der Gegenstelle erwartet wird ( > C_HelloDelay der Gegenstelle), (0=ausgeschaltet für Testzwecke, 1...3600) */
    private int _tlsoipCHelloTimeout;

    /** Anzahl empfangener/gesendeter Telegramme, nach der spätestens ein Quittungstelegramm versendet werden muss/erwartet wird (1..255) */
    private int _tlsoipCReceiptCount;

    /** Zeit [s], nach der nach Erhalt eines Telegramms spätenstens ein Quittierungstelegramm an die Gegenstelle versendet werden muss (1..59) */
    private int _tlsoipCReceiptDelay;

    /** Zeit [s], nach der spätestens ein Quittungstelegramm von der Gegenstelle erwartet wird (> C_ReceiptDelay der Gegenstelle) (1..60) */
    private int _tlsoipCReceiptTimeout;

    /** Zeit [s], nach der bei Nichtbestehen einer Verbindung spätestens ein neuer Verbindungsaufbau initiiert werden muss  (0=sofort, 1...3600). */
    private int _tlsoipCReconnectDelay;

    /** IP-Adresse des Servers */
    private String  _tlsoipCServerAdrA                  = null;
    private boolean _tcpConnectedWaitingForFirstReceive = false;

    /** Sendepuffer für versendete Telegramme */
    private ByteBuffer _sendBuffer = ByteBuffer.allocateDirect(28 + MAX_ANZAHL_NUTZDATENBYTES_PRO_OIS2_PAKET);

    /** Empfangspuffer für empfangene Telegramme */
    private ByteBuffer _leseBuffer = ByteBuffer.allocate(3000);

    /** Timerobjekt mit dem zukünftige Aktionen geplant und ausgeführt werden */
    private final Timer _timer = new Timer(true);

	//~ KONSTRUKTOREN  (und vom Konstruktor verwendete Methoden) ==============

    public TestServerTLSoIP() {}

    //~ METHODEN ==============================================================

    /**
     * Main-Methode.
     *
     * @param args Aufrufargumente.
     * @throws java.io.IOException wenn die Socketverbindung nicht aufgebaut werden konnte.
     */
    public static void main(String[] args) throws IOException {
        TestServerTLSoIP testServerTLSoIP = new TestServerTLSoIP();

        StandardApplicationRunner.run(testServerTLSoIP, args);
        testServerTLSoIP.start();
        System.out.format("TestServerTLSoIP beendet [%s]", zeitFormat.format(new Date(System.currentTimeMillis())));
        System.exit(0);
    }

    public void initialize(ClientDavInterface dav) {
        _dav = dav;
    }

    public void parseArguments(ArgumentList argumentList) throws Exception {

        /** IP-Adresse des Servers */
        _tlsoipCServerAdrA = "localhost";

        // Serverport, auf dem Anfragen durch den Server entgegengenommen werden. Dynamische und oder Private Ports (49152 bis 65535) gemäß IANA konfigurierbar
        _tlsoipCAcceptPort = 20000;

        // Portnummer des Servers, auf dem der Client eine Verbindung herstellen soll.
        _tlsoipCAcceptPortA = 20000;

        // Zeit [s], nach der ein Keep-Alive-Telegramm an die Gegenstelle versendet werden muss (0=ausgeschaltet für Testzwecke, 1...3599)
        _tlsoipCHelloDelay = 20;

        // Zeit [s], nach der spätestens ein Keep-Alive-Telegramm der Gegenstelle erwartet wird ( > C_HelloDelay der Gegenstelle), (0=ausgeschaltet für Testzwecke, 1...3600) */
        _tlsoipCHelloTimeout = 60;

        // Anzahl empfangener/gesendeter Telegramme, nach der spätestens ein Quittungstelegramm versendet werden muss/erwartet wird (1..255)
        _tlsoipCReceiptCount = 10;

        // Zeit [s], nach der nach Erhalt eines Telegramms spätenstens ein Quittierungstelegramm an die Gegenstelle versendet werden muss (1..59)
        _tlsoipCReceiptDelay = 5;

        // Zeit [s], nach der spätestens ein Quittungstelegramm von der Gegenstelle erwartet wird (> C_ReceiptDelay der Gegenstelle) (1..60)
        _tlsoipCReceiptTimeout = 60;

        // Zeit [s], nach der bei Nichtbestehen einer Verbindung spätestens ein neuer Verbindungsaufbau initiiert werden muss  (0=sofort, 1...3600).
        _tlsoipCReconnectDelay = 60;

        // Über die PID den Anschlusspunkt ermitteln und die zugehörigen Parameter anmelden.
        _pidAPClient = argumentList.fetchArgument(String.format("-pidAPClient=%s", "ap.unbekannt")).asNonEmptyString();

        // ToDo: Objekt ermitteln
        // ToDo: Auf Parametersatz anmelden
    }

    /**
     * Startet den Server und verarbeitet die Telegramme.
     *
     * @throws java.io.IOException wenn bei der Erstellung der Verbindung ein grundsätzliches Problem auftritt.
     */
    private void start() throws IOException {
        Selector            selector            = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        serverSocketChannel.configureBlocking(false);  // Asyncrone Verarbeitung ermöglichen

        ServerSocket serverSocket = serverSocketChannel.socket();

        // IP-Adresse und Port für den Server festlegen
        InetAddress       ipAddresse       = InetAddress.getByName(_tlsoipCServerAdrA);               // Server läuft beim Testen immer lokal !!
        InetSocketAddress ipSocketAddresse = new InetSocketAddress(ipAddresse, _tlsoipCAcceptPortA);  // es wird der PORT verwendet, der beim Client-AP eingetragen wurde !!

        /*
         *       // IP-Adresse und Port für den Client festlegen
         *           // Es wird die IP verwendet, der beim Server-AP eingetragen wurde.
         *           // Wird eigentlich nur für einen Client verwendet.
         *           // Zum Test eines Servers durch den TestClient muss diese IP aber eingetragen werden,
         *           // wenn nicht auf einem Rechner getestet wird (--> dann wird automatisch "localhost" verwendet)
         *           InetAddress adresse = InetAddress.getByName(_tlsoipCServerAdrA);
         *           InetSocketAddress address = new InetSocketAddress(adresse, _tlsoipCAcceptPort);  // es wird der PORT verwendet, der beim Server-AP eingetragen wurde !!
         */
        serverSocket.bind(ipSocketAddresse);

        SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println(String.format("TestServerTLSoIP wurde gestartet [%s:%d]", _tlsoipCServerAdrA, _tlsoipCAcceptPortA));
        debug.config(String.format("TestServerTLSoIP wurde gestartet [%s:%d]", _tlsoipCServerAdrA, _tlsoipCAcceptPortA));

        // debug.config(String.format("TestClientTLSoIP wurde gestartet [%s:%d]",_tlsoipCServerAdrA, _tlsoipCAcceptPort));
        // Komunikations abwickeln...
        while (true) {
            int    anzFunktionsSchluessel    = selector.select(60000);  // BLOCKIERT, BIS EIN KANAL DATEN HAT
            Set<SelectionKey>   mengeFunktionsSchlusessel = selector.selectedKeys();
            String aktZeit                   = zeitFormat.format(new Date(System.currentTimeMillis()));

            System.out.format("%s : %40d %40s%n", aktZeit, mengeFunktionsSchlusessel.size(), "========================================");

            Iterator<SelectionKey> it = mengeFunktionsSchlusessel.iterator();

            while (it.hasNext()) {
                SelectionKey funktionsSchluessel = it.next();

	            System.out.println("-----------------------------------------------------------------------------");
	            System.out.println("funktionsSchluessel.isValid()"+ funktionsSchluessel.isValid());
	            System.out.println("funktionsSchluessel.isAcceptable()"+ funktionsSchluessel.isAcceptable());
	            System.out.println("funktionsSchluessel.isConnectable()"+ funktionsSchluessel.isConnectable());
	            System.out.println("funktionsSchluessel.isReadable()"+ funktionsSchluessel.isReadable());
	            System.out.println("funktionsSchluessel.isWritable()"+ funktionsSchluessel.isWritable());
                if (funktionsSchluessel.isValid()) {
                    if (funktionsSchluessel.isAcceptable()) {

                        // Neue Verbindung akzeptieren
                        ServerSocketChannel ssc = (ServerSocketChannel) funktionsSchluessel.channel();
                        SocketChannel       sc  = ssc.accept();

                        sc.configureBlocking(false);

                        // Neue Verbindung dem Selektor zuordnen
                        sc.register(selector, SelectionKey.OP_READ);
                        it.remove();
                        System.out.format("%s : %40s %40s%n", aktZeit, String.format("Verbindungsaufbau von [%s]", sc), "");
                    } else if (funktionsSchluessel.isReadable()) {
                        System.out.format("%s : %40s %40s%n", aktZeit, funktionsSchluessel.toString(), "SelectionKey.OP_READ");

                        // Read the data
                        SocketChannel sc = (SocketChannel) funktionsSchluessel.channel();

                        // Echo data
                        int bytesEchoed = 0;

                        while (true) {
                            _leseBuffer.clear();

                            int anzahlBytes = sc.read(_leseBuffer);

                            if (anzahlBytes == 0) {
	                            System.out.println("anzahlBytes=" + anzahlBytes);
                                break;
                            } else if(anzahlBytes == -1){
	                            System.out.println("anzahlBytes=" + anzahlBytes);
	                            System.out.println(String.format("Verbindung wurde von der Gegenseite terminiert; %s", this));
	                                sc.close();

	                            break;
                            }

                            _leseBuffer.flip();

                            // sc.write(echoBuffer);
                            bytesEchoed += anzahlBytes;
                            System.out.printf("Echoed %d (%d) Byte from %s:%n", bytesEchoed, anzahlBytes, sc);
                            HexDumper.dumpTo(System.out, 0, _leseBuffer.array(), 0, _leseBuffer.limit());
                        }

                        it.remove();
                    } else {
                        System.out.format("%s : %40s %40s%n", aktZeit, funktionsSchluessel.toString(), "");
                        it.remove();
                    }
                }
            }
        }
    }
}


//~Formatiert mit 'inovat Kodierkonvention' am 10.02.10
