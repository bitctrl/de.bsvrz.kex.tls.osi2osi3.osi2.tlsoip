/*
 * Copyright (c) 2010 - 2014 by inovat, innovative systeme - verkehr - tunnel - technik, Dipl.-Ing. H. C. Kniss
 *
 * This file is part of de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.Server
 *
 * de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.Server is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.Server; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Contact Information:
 * inovat, Dipl.-Ing. H. C. Kniss
 * An der Krautwiese 37
 * D-53783 Eitorf
 * +49 (0)2243 8464 193
 * info@inovat.de
 * www.inovat.de
 */



package de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip;

//~ NICHT JDK IMPORTE =========================================================

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.kex.tls.osi2osi3.osi2.api.AbstractDataLinkLayer;
import de.bsvrz.kex.tls.osi2osi3.osi2.api.DataLinkLayer;
import de.bsvrz.kex.tls.osi2osi3.osi2.api.DataLinkLayerEvent;
import de.bsvrz.kex.tls.osi2osi3.osi2.api.LinkState;
import de.bsvrz.kex.tls.osi2osi3.properties.PropertyConsultant;
import de.bsvrz.kex.tls.osi2osi3.properties.PropertyQueryInterface;
import de.bsvrz.sys.funclib.concurrent.PriorityChannel;
import de.bsvrz.sys.funclib.concurrent.PriorizedObject;
import de.bsvrz.sys.funclib.concurrent.UnboundedQueue;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.hexdump.HexDumper;

//~ JDK IMPORTE ===============================================================

import java.io.IOException;

import java.net.InetSocketAddress;
import java.net.Socket;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

//~ KLASSEN ===================================================================

/**
 * Klasse, die als OSI-2 Protokollmodul für den Server-seitigen Teil einer TLSoIP-Verbindung eingesetzt werden kann.
 * <p/>
 * Zur Verwendung dieses Protokollmoduls ist an dem jeweiligen Anschlusspunkt in der Konfiguration in der Attributgruppe
 * "atg.anschlussPunkt" im Attribut "ProtokollTyp" der Wert "de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.Server" einzutragen.
 * <p/>
 * Im Parameter "atg.protokollEinstellungenStandard" des Anschlußpunkts können die Standardwerte für alle Verbindungen
 * an diesem Anschlußpunkt eingestellt.
 * <p/>
 * Im Parameter "atg.protokollEinstellungenPrimary" der dem Anschlußpunkt zugeordneten
 * AnschlußPunktKommunikationsPartner können individuelle Werte für die Verbindung zum jeweiligen Kommunikationspartner
 * eingestellt werden.
 * <p/>
 * Die Parameterdatensätze können dabei mehrere Einträge enthalten, die jeweils aus einem Namen und einem Wert
 * bestehen.
 * <p/>
 * Folgende Einträge werden unterstützt (siehe auch TLS 2009, Teil 2, Datenübertragung über TCP/IP (TLSoIP):
 * <p/>
 * Verbindungsparameter für beide Verbindungspartner:
 * <p/>
 * <table cellpadding="2" cellspacing="2" border="1"> <tr> <th> Name </th> <th> Defaultwert </th> <th> Beschreibung
 * </th> </tr> <tr> <td> tlsoip.C_HelloDelay </td> <td> 30 </td> <td> Zeit [s], nach der ein Keep-Alive-Telegramm an die
 * Gegenstelle versendet werden muss (0=ausgeschaltet für Testzwecke, 1...3599). </td> </tr> <tr> <td>
 * tlsoip.C_HelloTimeout </td> <td> 60 </td> <td> Zeit [s], nach der spätestens ein Keep-Alive-Telegramm der Gegenstelle
 * erwartet wird ( > C_HelloDelay der Gegenstelle), (0=ausgeschaltet für Testzwecke, 1...3600). </td> </tr> <tr> <td>
 * tlsoip.C_ReceiptCount </td> <td> 10 </td> <td> Anzahl empfangener/gesendeter Telegramme, nach der spätestens ein
 * Quittungstelegramm versendet werden muss/erwartet wird (1..255). </td> </tr> <tr> <td> tlsoip.C_ReceiptDelay </td>
 * <td> 15 </td> <td> Zeit [s], nach der nach Erhalt eines Telegramms spätenstens ein Quittierungstelegramm an die
 * Gegenstelle versendet werden muss (1..59). </td> </tr> <tr> <td> tlsoip.C_ReceiptTimeout </td> <td> 30 </td> <td>
 * Zeit [s], nach der spätestens ein Quittungstelegramm von der Gegenstelle erwartet wird (> C_ReceiptDelay der
 * Gegenstelle) (1..60). </td> </tr> <tr> <td> tlsoip.C_SecureConnection </td> <td> nein </td> <td> WIRD AKTUELL NICHT
 * UNTERSTÜTZT (immer nein): Verbindung wird ohne SSL betrieben (nein), Verbindung wird mit SSL betrieben (ja). </td>
 * </tr> </table>
 * <p/>
 * <p/>
 * Verbindungsparameter für den Server:
 * <p/>
 * <table cellpadding="2" cellspacing="2" border="1"> <tr> <th> Name </th> <th> Defaultwert </th> <th> Beschreibung
 * </th> </tr> <tr> <td> tlsoip.C_AcceptPort </td> <td>  </td> <td> Dynamische und oder Private Ports (49152 bis 65535)
 * gemäß IANA konfigurierbar, auf dem der Server anfragen entgegen nimmt. </td> </tr> <tr> <td> tlsoip.C_PortMode </td>
 * <td> aktiv </td> <td> AKTUELL WIRD NUR "AKTIV" UNTERSTÜTZT!<br> Portmodus (aktiv/passiv). Passiv nur für
 * Mithörschnittstellen. </td> </tr> <tr> <td> tlsoip.C_ClientCount </td> <td> 1 </td> <td> AKTUELL WIRD NUR PortMode
 * "AKTIV" mit ClientClount "1" UNTERSTÜTZT!<br> Anzahl der unterstützten Ports (1..255), bei aktiven Ports immer gleich
 * 1. </td> </tr> <tr> <td> tlsoip.C_ActivePort </td> <td>  </td> <td> WIRD AKTUELL NICHT UNTERSTÜTZT!<br> Bei passiven
 * Ports: Zugeordneter aktiver Port. </td> </tr> <tr> <td> tlsoip.C_XmitRequest </td> <td> nein </td> <td> WIRD AKTUELL
 * NICHT UNTERSTÜTZT!<br> Bei passiven Ports: Übertragung der Telegramme in Abrufrichtung (ja/nein). </td> </tr> <tr>
 * <td> tlsoip.C_XmitAnswer </td> <td> nein </td> <td> WIRD AKTUELL NICHT UNTERSTÜTZT!<br> Bei passiven Ports:
 * Übertragung der Telegramme in Antwortrichtung (ja/nein). </td> </tr> <tr> <td> tlsoip.C_TelTypeList </td> <td>  </td>
 * <td> WIRD AKTUELL NICHT UNTERSTÜTZT!<br> Bei passiven Ports: Liste der erlaubten TelType (OSI-2 TLSoIP). </td> </tr>
 * </table>
 * <p/>
 *
 * @author inovat, innovative systeme - verkehr - tunnel - technik
 * @author Dipl.-Ing. Hans Christian Kniß (HCK)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class Server extends TLSoIP implements PropertyQueryInterface {

    /** Logger für Debugausgaben */
    private static final Debug DEBUG = Debug.getLogger();

    //~ FELDER ================================================================

    /** Thread des Protokolls */
    private final Thread _workThread;

    /** Runnable Objekt, das vom Protokollthread ausgeführt wird und den Protokollablauf steuert */
    final Worker _worker = new Worker();

    /** Verbindung zum Datenverteiler */
    ClientDavInterface _connection;

    /** Aktueller Zustand des Protokolls */
    private ProtocolState _protocolState = ProtocolState.CREATED;

    /** Verbindungen zu Kommunikationspartnern, die durch das Protokoll verwaltet werden */
    private List<Link> _links = new LinkedList<Link>();

    /**
     * Monitor Objekt, das zur Synchronisation des Protokoll-Threads und den API-Zugriffen von fremden Threads auf das
     * Protokoll koordiniert
     */
    private final Object _protocolLock = new Object();

    //~ KONSTRUKTOREN  (und vom Konstruktor verwendete Methoden) ==============

    /**
     * Default-Konstruktor, mit dem neue TLSoverIP-Server Protokolle instanziiert werden können.
     *
     * @throws java.io.IOException wenn eine nicht abgefangene Ausnahme auftritt.
     */
    public Server() throws IOException {
        DEBUG.fine("TLSoIP-Server (Konstruktoraufruf)");
        _workThread = new Thread(_worker, "TLSoIP.Server.Worker");
    }

    //~ METHODEN ==============================================================

    /** Bricht die Kommunikation auf allen Verbindungen des Protokolls sofort ab und beendet anschließend das Protokoll. */
    public void abort() {
        DEBUG.fine(String.format("abort(): %s", this));

        synchronized (_protocolLock) {
            for (Link link : _links) {
                link.abort();
            }

            if (_protocolState == ProtocolState.STARTED) {
                _protocolState = ProtocolState.STOPPING;
            }

            if (_protocolState != ProtocolState.STOPPING) {
                _protocolState = ProtocolState.STOPPED;
            }

            _protocolLock.notifyAll();
        }
    }

    /**
     * Erzeugt ein neues Verbindungsobjekt.
     *
     * @param remoteAddress OSI-2 Adresse des Kommunikationspartners
     *
     * @return Neues Verbindungsobjekt
     */
    public DataLinkLayer.Link createLink(int remoteAddress) {
        return new Link(remoteAddress);
    }

    //~ GET METHODEN ==========================================================

    /**
     * Bestimmt, ob die Kommunikation dieses Protokolls bereits mit der Methode {@link #start} aktiviert wurde.
     *
     * @return <code>true</code>, wenn die Kommunikation dieses Protokolls bereits aktiviert wurde, sonst
     *         <code>false</code>.
     */
    public boolean isStarted() {
        synchronized (_protocolLock) {
            return (_protocolState == ProtocolState.STARTING) || (_protocolState == ProtocolState.STARTED);
        }
    }

    //~ SET METHODEN ==========================================================

    /**
     * Nimmmt die Verbindung zum Datenverteiler entgegen. Diese Methode wird vom OSI-3 Modul nach dem Erzeugen des OSI-2
     * Moduls durch den jeweiligen Konstruktor aufgerufen. Eine Implementierung eines Protokollmoduls kann sich bei Bedarf
     * die übergebene Datenverteilerverbindung intern merken, um zu späteren Zeitpunkten auf die
     * Datenverteiler-Applikationsfunktionen zuzugreifen.
     *
     * @param connection Verbindung zum Datenverteiler
     */
    public void setDavConnection(ClientDavInterface connection) {
        _connection = connection;
    }

    /**
     * Setzt neue Protokollparameter. Alle Verbindungen werden mit den neuen Parametern reinitialisiert.
     *
     * @param properties Neue Protokoll und Verbindungsparameter
     */
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        DEBUG.fine(String.format("Neue Einstellungen für: %s, properties = %s", toString(), properties));

        synchronized (_protocolLock) {
            for (Link link : _links) {
                link.reload();
            }
        }
    }

    //~ METHODEN ==============================================================

    /** Terminiert alle Verbindungen des Protokolls und beendet anschließend das Protokoll. */
    public void shutdown() {
        DEBUG.fine(String.format("shutdown): %s", this));

        synchronized (_protocolLock) {
            for (Link link : _links) {
                link.shutdown();
            }

            if (_protocolState == ProtocolState.STARTED) {
                _protocolState = ProtocolState.STOPPING;
            }

            if (_protocolState != ProtocolState.STOPPING) {
                _protocolState = ProtocolState.STOPPED;
            }

            _protocolLock.notifyAll();
        }
    }

    /** Initialisiert das Protokoll und startet den Protokoll-Thread */
    public void start() {
        DEBUG.fine(String.format("start(): %s", this));

        synchronized (_protocolLock) {
            if (_protocolState != ProtocolState.CREATED) {
                throw new IllegalStateException("Protokoll kann nicht erneut gestartet werden: " + toString());
            }

            int localAddress = getLocalAddress();

            if ((localAddress < 1) || (localAddress > 254)) {
                throw new IllegalStateException(String.format("lokale OSI-2 Adresse muss zwischen 1 und 254 liegen, ist: %d", localAddress));
            }

            _workThread.setName(String.format("TLSoIP.Server.Worker(%d)", localAddress));
            _workThread.start();
            _protocolState = ProtocolState.STARTING;
            _protocolLock.notifyAll();
        }
    }

    /**
     * Gibt Informationen des Protokolls für Debugzwecke zurück. Das genaue Format ist nicht festgelegt und kann sich
     * ändern..
     *
     * @return Gibt Informationen des Protokolls für Debugzwecke zurück
     */
    public String toString() {
        return String.format("TLSoIP-Server(%d, %s) ", getLocalAddress(), _protocolState);
    }

    //~ INNERE KLASSEN ========================================================

    /** Definiert die Aktionscodes, die von den API-Methoden zur Steuerung des Protokoll-Threads versendet werden */
    public static class ActionType {

        /** Signalisiert dem Protokoll-Thread, das die Kommunikation auf einer Verbindung gestartet werden soll */
        public static final ActionType CONNECT_CALLED = new ActionType("CONNECT_CALLED");

        /** Signalisiert dem Protokoll-Thread, das die Kommunikation auf einer Verbindung geschlossen werden soll */
        public static final ActionType SHUTDOWN_CALLED = new ActionType("SHUTDOWN_CALLED");

        /** Signalisiert dem Protokoll-Thread, das ein Telegramm auf einer Verbindung versendet werden soll */
        public static final ActionType SEND_CALLED = new ActionType("SEND_CALLED");

        /** Signalisiert dem Protokoll-Thread, das ein erneuter Verbindungsversuch durchgeführt werden soll */
        public static final ActionType RETRY_CONNECT = new ActionType("RETRY_CONNECT");

        /**
         * Signalisiert dem Protokoll-Thread, das die Kommunikation auf einer Verbindung mit Berücksichtigung von evtl.
         * Parameteränderungen neu aufgebaut werden soll
         */
        public static final ActionType RELOAD_CALLED = new ActionType("RELOAD_CALLED");

        /** Signalisiert dem Protokoll-Thread, das ein Quittierungs-Telegramm gesendet werden soll */
        public static final ActionType QUITT_TIMER_SEND = new ActionType("QUITT_TIMER_SEND");

        /** Signalisiert dem Protokoll-Thread, das ein der Empfang eines Quittierungs-Telegramms geprüft werden soll */
        public static final ActionType QUITT_TIMER_RECEIVE = new ActionType("QUITT_TIMER_RECEIVE");

        /** Signalisiert dem Protokoll-Thread, das ein Keep-Alive-Telegramm gesendet werden soll */
        public static final ActionType KEEPALIVE_TIMER_SEND = new ActionType("KEEPALIVE_TIMER_SEND");

        /** Signalisiert dem Protokoll-Thread, das ein der Empfang von Keep-Alive-Telegrammen geprüft werden soll */
        public static final ActionType KEEPALIVE_TIMER_RECEIVE = new ActionType("KEEPALIVE_TIMER_RECEIVE");

        /** Signalisiert dem Protokoll-Thread, das die Kommunikation auf einer Verbindung abgebrochen werden soll */
        public static final ActionType ABORT_CALLED = new ActionType("ABORT_CALLED");

        //~ FELDER ============================================================

        /** Name der Aktion für Debugzwecke */
        private final String _name;  // for debug only

        //~ KONSTRUKTOREN  (und vom Konstruktor verwendete Methoden) ==========

        /**
         * Erzeugt ein neues Aktionsobjekt mit vorgegebenem Namen
         *
         * @param name Name der Aktion für Debugzwecke
         */
        private ActionType(String name) {
            _name = name;
        }

        //~ METHODEN ==========================================================

        /**
         * Gibt Informationen des ActionType für Debugzwecke zurück. Das genaue Format ist nicht festgelegt und kann sich
         * ändern..
         *
         * @return Name der Aktion für Debugzwecke
         */
        public String toString() {
            return _name;
        }
    }


    /** Signalisiert fehlerhafte Zustände in empfangenen Telegrammen */
    private static class IllegalTelegramException extends Exception {

        /**
         * Erzeugt eine neue Ausnahme mit einer spezifischen Fehlerbeschreibung
         *
         * @param message Spezifische Fehlerbeschreibung der Ausnahme
         */
        public IllegalTelegramException(String message) {
            super(message);
        }
    }


    /** Realisiert ein Verbindungsobjekt, das die Kommunikation mit einem einzelnen Kommunikationspartner verwaltet. */
    private class Link extends AbstractDataLinkLayer.Link implements DataLinkLayer.Link, PropertyQueryInterface {

        /** Wrapper-Objekt zum bequemen Zugriff auf die online änderbaren Parameter dieser Verbindung */
        private final PropertyConsultant _propertyConsultant;

        /** Priorisierte Queue mit den noch zu versendenden Telegrammen */
        private final PriorityChannel _sendChannel;

        /** Anzahl der empfangenen Daten-Telegramme seit des letzten Quittungstelegramm-Versands */
        private int _countReceiptDataTel;

        /** Anzahl des gesendeten Daten-Telegramme seit des letzten Quittungstelegramm-Empfangs */
        private int _countSendDataTel;

        /** Sequenznummer des letzten quittierten Daten-Telegramms */
        private int _lastQuittSeqNumDataTel;

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

        /** Flag das signalisiert, dass ein Keep-Alive-Telegramm versendet werden soll */
        private boolean _sendKeepAliveTel;

        /** Flag das signalisiert, dass ein Quittungs-Telegramm versendet werden soll */
        private boolean _sendQuittTel;

        /**
         * Enthält während des Verbindungsaufbau das Kommunikationsobjekt mit internem Serversocket, über den die Verbindung
         * des Clients entgegengenommen wird; sonst <code>null</code>.
         */
        private ServerSocketChannel _serverSocketChannel;

        /**
         * Enthält während einer bestehenden Verbindung das Kommunikationsobjekt mit internem Server, über den der
         * Datenaustausch mit dem Client abgewickelt wird; sonst <code>null</code>.
         */
        private SocketChannel _socketChannel;

        /**
         * Serverport, auf dem Anfragen entgegengenommen werden. Dynamische und oder Private Ports (49152 bis 65535) gemäß
         * IANA konfigurierbar
         */
        private int _tlsoipCAcceptPort;

        /**
         * Zeit [s], nach der ein Keep-Alive-Telegramm an die Gegenstelle versendet werden muss (0=ausgeschaltet für
         * Testzwecke, 1...3599)
         */
        private int _tlsoipCHelloDelay;

        /**
         * Zeit [s], nach der spätestens ein Keep-Alive-Telegramm der Gegenstelle erwartet wird ( > C_HelloDelay der
         * Gegenstelle), (0=ausgeschaltet für Testzwecke, 1...3600)
         */
        private int _tlsoipCHelloTimeout;

        /**
         * Anzahl empfangener/gesendeter Telegramme, nach der spätestens ein Quittungstelegramm versendet werden muss/erwartet
         * wird (1..255)
         */
        private int _tlsoipCReceiptCount;

        /**
         * Zeit [s], nach der nach Erhalt eines Telegramms spätenstens ein Quittierungstelegramm an die Gegenstelle versendet
         * werden muss (1..59)
         */
        private int _tlsoipCReceiptDelay;

        /**
         * Zeit [s], nach der spätestens ein Quittungstelegramm von der Gegenstelle erwartet wird (> C_ReceiptDelay der
         * Gegenstelle) (1..60)
         */
        private int _tlsoipCReceiptTimeout;

        /** Flag das signalisiert, dass Timer für die Quittungsüberwachung nach Datenversand einmal gestartet werden muss. */
        private boolean _tlsoipCReceiptTimeoutGestartet;

        /** Aktuell asynchron zu sendendes Telegramm */
        byte[] _packetOnTheAir = null;

        /** Enthält die online änderbaren Parameter für diese Verbindung */
        private Properties _properties = null;

        /** Timerobjekt mit dem zukünftige Aktionen geplant und ausgeführt werden */
        private final Timer _timer = new Timer(true);

        /** Sendepuffer für versendete Telegramme */
        private final ByteBuffer _sendBuffer = ByteBuffer.allocateDirect(28 + getMaximumDataSize());

        /** Empfangspuffer für empfangene Telegramme */
        private final ByteBuffer _readBuffer = ByteBuffer.allocateDirect(3000);

        //~ KONSTRUKTOREN  (und vom Konstruktor verwendete Methoden) ==========

        /**
         * Erzeugt ein neues Verbindungsobjekt.
         *
         * @param remoteAddress OSI-2 Adresse des Kommunikationspartners
         */
        private Link(int remoteAddress) {
            super(remoteAddress);
            _propertyConsultant = new PropertyConsultant(this);

            if ((remoteAddress < 1) || (remoteAddress > 255)) {
                throw new IllegalArgumentException(String.format("OSI-2 Adresse muss zwischen 1 und 254 liegen oder den speziellen Wert 255 (Broadcastaddresse) haben, versorgt ist: %d", remoteAddress));
            }

            _sendChannel = new PriorityChannel(3, 1000);
            _readBuffer.order(ByteOrder.LITTLE_ENDIAN);
            _sendBuffer.order(ByteOrder.LITTLE_ENDIAN);
            _linkState = LinkState.DISCONNECTED;

            synchronized (_protocolLock) {
                for (Link link : _links) {
                    if (link.getRemoteAddress() == _remoteAddress) {
                        throw new IllegalStateException(String.format("Es gibt bereits ein Verbindung mit dieser Secondary-Adresse: %d", _remoteAddress));
                    }
                }

                _links.add(this);
            }
        }

        //~ METHODEN ==========================================================

        /** Initiiert den sofortigen Abbruch der bestehenden Verbindung dieses Verbindungsobjekts */
        public void abort() {
            DEBUG.fine(String.format("abort %s", this));

            synchronized (_linkLock) {
                if ((_linkState == LinkState.DISCONNECTED) || (_linkState == LinkState.DISCONNECTING)) {
                    return;
                }

                _linkState = LinkState.DISCONNECTING;
            }

            try {
                _sendChannel.put(new PriorizedByteArray(null, 0));
            }
            catch (InterruptedException e) {
                e.printStackTrace();

                throw new RuntimeException(e);
            }

            notifyWorker(ActionType.ABORT_CALLED, 0, false);
        }

        /** Schließt den Kommunikationskanal zum Client und wartet auf neuen Verbindungsaufbau seitens des Client. */
        private void closeChannel() {
            closeChannel(0);  // Verbindung sofort wieder akzeptieren
        }

        /**
         * Schließt den Kommunikationskanal zum Client und plant den erneuten Aufbau der Kommunikationsverbindung nach einer
         * vorgebbaren Wartezeit ein.
         *
         * @param reconnectDelay Wartezeit nach der die Verbindung wieder aufgebaut werden soll.
         */
        private void closeChannel(int reconnectDelay) {
            synchronized (_linkLock) {

                // DIFFCLIENT ANFANG
                if (_serverSocketChannel != null) {
                    try {
                        _serverSocketChannel.close();
                    }
                    catch (IOException e) {
                        DEBUG.warning("Fehler beim Schließen des ServerSocketChannels: ", e);
                    }
                    finally {

                        // noinspection AssignmentToNull
                        _serverSocketChannel = null;
                    }
                }

                // DIFFCLIENT ENDE
                if (_socketChannel != null) {
                    try {
                        _socketChannel.close();
                    }
                    catch (IOException e) {
                        DEBUG.warning(String.format("Fehler beim Schließen des SocketChannels: %s", e));
                    }
                    finally {

                        // noinspection AssignmentToNull
                        _socketChannel = null;
                    }
                }

                if (_linkState == LinkState.DISCONNECTING) {
                    _linkState = LinkState.DISCONNECTED;
                    notifyEvent(DataLinkLayerEvent.Type.DISCONNECTED, null);
                } else if (_linkState == LinkState.CONNECTED) {
                    _linkState = LinkState.CONNECTING;
                    DEBUG.fine(String.format("Nächster Verbundungsversuch in %d Sekunden möglich; %s", reconnectDelay, this));
                    scheduleActionTimer(ActionType.RETRY_CONNECT, reconnectDelay);
                    notifyEvent(DataLinkLayerEvent.Type.DISCONNECTED, null);
                } else if (_linkState == LinkState.CONNECTING) {
                    DEBUG.fine(String.format("Nächster Verbundungsversuch in %d Sekunden möglich; %s", reconnectDelay, this));
                    scheduleActionTimer(ActionType.RETRY_CONNECT, reconnectDelay);
                } else {
                    DEBUG.error(String.format("closeChannel: Unmöglicher Zustand: Fehler ohne bestehende Verbindung; %s", this));
                    _linkState = LinkState.DISCONNECTED;
                }
            }
        }

        /** Initiiert den Verbindungsaufbau mit dem Kommunikationspartner dieses Verbindungsobjekts */
        public void connect() {
            DEBUG.fine(String.format("connect %s", this));

            synchronized (_protocolLock) {
                synchronized (_linkLock) {
                    if ((_linkState == LinkState.CONNECTED) || (_linkState == LinkState.CONNECTING)) {
                        return;
                    }

                    if (_linkState != LinkState.DISCONNECTED) {
                        throw new IllegalStateException(String.format("Verbindung kann in diesem Zustand nicht aufgebaut werden: %s", _linkState));
                    }

                    _linkState = LinkState.CONNECTING;
                }

                _protocolLock.notifyAll();
            }

            // ToDo freigeben nach Test: Geändert inovat auf Server
            notifyWorker(ActionType.CONNECT_CALLED, 0, false);
        }

        /**
         * Asynchroner Verbindungsaufbau. Zum Aufbau der Kommunikationsverbindung wird ein Kommunikationskanal mit einem
         * Serversocket initialisiert, der auf dem gewünschten TCP-Port Verbindungen entgegennimmt. Nachdem Verbindungsaufbau
         * wird der Serversocket wieder geschlossen und ein neuer Kommunikationskanal für den Datenaustausch initialisiert.
         *
         * @param selector Selektor des Protokoll-Threads zum asynchronen Zugriff auf die Kommunikationskanäle.
         */
        private void connectSocketChannel(Selector selector) {
            synchronized (_linkLock) {
                if (_linkState == LinkState.CONNECTING) {
                    try {
                        if (_serverSocketChannel == null) {
                            _readBuffer.clear();
                            _sendBuffer.clear().flip();

                            // noinspection AssignmentToNull
                            _packetOnTheAir = null;

                            // Verbindungs- und Kommunikationsparameter einlesen
                            _tlsoipCAcceptPort     = _propertyConsultant.getIntProperty("tlsoip.C_AcceptPort");
                            _tlsoipCHelloDelay     = _propertyConsultant.getIntProperty("tlsoip.C_HelloDelay");
                            _tlsoipCHelloTimeout   = _propertyConsultant.getIntProperty("tlsoip.C_HelloTimeout");
                            _tlsoipCReceiptCount   = _propertyConsultant.getIntProperty("tlsoip.C_ReceiptCount");
                            _tlsoipCReceiptDelay   = _propertyConsultant.getIntProperty("tlsoip.C_ReceiptDelay");
                            _tlsoipCReceiptTimeout = _propertyConsultant.getIntProperty("tlsoip.C_ReceiptTimeout");
                            _serverSocketChannel   = ServerSocketChannel.open();

                            // ToDo Prüfen, wie der richtige BlockingMode ist und wie die Links aufgebaut werden
                            // _serverSocketChannel.configureBlocking(true);
                            _serverSocketChannel.configureBlocking(false);
                            _serverSocketChannel.socket().bind(new InetSocketAddress(_tlsoipCAcceptPort));
                            DEBUG.info(String.format("Server aktzeptiert Verbindung auf Port %d; %s", _tlsoipCAcceptPort, this));
                        }

                        if (_serverSocketChannel != null) {
                            _socketChannel = _serverSocketChannel.accept();

                            if (_socketChannel != null) {
                                final Socket socket = _socketChannel.socket();

                                _socketChannel.configureBlocking(false);
                                _serverSocketChannel.close();

                                // noinspection AssignmentToNull
                                _serverSocketChannel      = null;
                                _linkState                = LinkState.CONNECTED;
                                _lastReceiptTimeAllTel    = System.currentTimeMillis();
                                _lastSendTimeAllTel       = System.currentTimeMillis();
                                _lastReceiptTimeDataTel   = System.currentTimeMillis();
                                _lastSendTimeDataTel      = System.currentTimeMillis();
                                _lastReceiptSeqNumDataTel = 0xffff;  // Als nächstes wird die 0 erwartet
                                _lastQuittSeqNumDataTel   = 0xffff;  // Erstes Telegramm hat die 0 und kann theoretisch auch quittiert werden.
                                _lastSendSeqNumDataTel    = 0xffff;  // Als nächstes wird die 0 erwartet
                                _countReceiptDataTel      = 0;
                                _countSendDataTel         = 0;
                                _sendKeepAliveTel         = true;
                                _sendQuittTel             = false;
                                DEBUG.info(String.format("Verbindungsaufbau von %s akzeptiert; %s", socket.getInetAddress().getHostName(), this));

                                // Kanal zum Lesen registrieren
                                _socketChannel.register(selector, SelectionKey.OP_READ, this);
                                notifyEvent(DataLinkLayerEvent.Type.CONNECTED, null);
                                scheduleActionTimer(ActionType.KEEPALIVE_TIMER_RECEIVE, _tlsoipCHelloTimeout);
                                scheduleActionTimer(ActionType.KEEPALIVE_TIMER_SEND, _tlsoipCHelloDelay);
                            } else {
                                DEBUG.info(String.format("Verbindungsaufbau ist noch nicht abgeschlossen und wird asynchron durchgeführt; %s", this));
                                _serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, this);
                            }
                        }
                    }
                    catch (Exception e) {
                        DEBUG.warning(String.format("Verbindungsaufbau ist fehlgeschlagen: %s.", this), e);
                        closeChannel();
                    }
                }
            }
        }

        //~ GET METHODEN ======================================================

        /** @return Das zu dieser Verbindung gehörende Protokollmodul */
        public DataLinkLayer getDataLinkLayer() {
            return Server.this;
        }

        /**
         * Ermittelt auf Basis der aktullen SeqNum des Daten-Telegramm die nächste folgende Nummer.
         *
         * @param num Aktuelle SeqNum des Daten Telegramms
         *
         * @return Nächste Nummer (num = num+1), wobei bei num = 65535 die nächste Nummer 0 ist.
         */
        public int getNextSeqNum(int num) {
            if (num == 65535) {
                return 0;
            } else {
                return num + 1;
            }
        }

        /**
         * Liefert einen Parameterwert zurück.
         *
         * @param name Name des gewünschten Parameterwerts.
         *
         * @return Gewünschter Parameterwert.
         */
        public String getProperty(String name) {
            synchronized (_linkLock) {
                String value = (_properties == null) ? null : _properties.getProperty(name);

                return (value == null) ? Server.this.getProperty(name) : value;
            }
        }

        //~ METHODEN ==========================================================

        /**
         * Führt eine Aktion für dieses Verbindungsobjekt aus. Diese Methode wird vom Protokoll-Thread zur Verarbeitung einer
         * Aktion aufgerufen.
         *
         * @param action           Auszuführende Aktion
         * @param selector         Selektor des Protokoll-Threads zum asynchronen Zugriff auf die Kommunikationskanäle.
         * @param seqNum           SequenzNummer des Telegramms, welches quittiert werden soll
         * @param sofortQuittieren <code>true</code>, wenn Quittierung in jedem Fall sofort erfolgen soll, sonst
         */

        // ToDo freigeben nach Test: Geändert inovat auf Server
        public void handleAction(ActionType action, Selector selector, int seqNum, boolean sofortQuittieren) {
            DEBUG.finer(String.format("handleAction(%s): %s", action, this));

            if ((action == ActionType.CONNECT_CALLED) || (action == ActionType.RETRY_CONNECT)) {
                DEBUG.fine(String.format("--> %s %s Verbindung aufbauen", this, new Date().toString()));
                connectSocketChannel(selector);
            } else if (action == ActionType.SEND_CALLED) {

                // handleAsyncSend() wird auf jeden Fall aufgerufen (s.u.)
            } else if (action == ActionType.RELOAD_CALLED) {
                closeChannel(2);
            } else if (action == ActionType.ABORT_CALLED) {

                // nichts zu tun
            } else if (action == ActionType.SHUTDOWN_CALLED) {

                // nichts zu tun
            } else if (action == ActionType.KEEPALIVE_TIMER_RECEIVE) {
                synchronized (_linkLock) {
                    if (_linkState == LinkState.CONNECTED) {

                        // Prüfen, ob KeepAlive-Überwachung für den Empfang nicht ausgeschaltet ist (zu Testzwecken)
                        if (_tlsoipCHelloTimeout == 0) {
                            DEBUG.info("KeepAlive-Überwachung für den Empfang ist ausgeschaltet (tlsoip.C_HelloTimeout = 0)");
                        } else {

                            // Zeitraum [s], seit letztes Telegramm empfangen wurde
                            int durationSinceLastReceiptAllTel = (int) ((System.currentTimeMillis() - _lastReceiptTimeAllTel) / 1000L);

                            // Hätte KeepAlive-Telegramm bereits empfangen werden müssen?
                            if (_tlsoipCHelloTimeout < durationSinceLastReceiptAllTel) {

                                // Ja. Verbindung abbrechen und nach Wartezeit neu aufbauen.
                                DEBUG.warning(String.format("--> %s %s Verbindung wird wegen Zeitüberschreibung initialisiert (Empfang letztes KeepAlive vor %ds > Parameter tlsoip.C_HelloTimeout %d", this, new Date().toString(), durationSinceLastReceiptAllTel, _tlsoipCHelloTimeout));
                                closeChannel();
                            } else {

                                // Nein, aber bald. Timer neu setzen
                                scheduleActionTimer(ActionType.KEEPALIVE_TIMER_RECEIVE, _tlsoipCHelloTimeout - durationSinceLastReceiptAllTel);
                            }
                        }
                    }

                    return;
                }
            } else if (action == ActionType.KEEPALIVE_TIMER_SEND) {
                synchronized (_linkLock) {
                    if (_linkState == LinkState.CONNECTED) {

                        // Prüfen, ob KeepAlive-Überwachung für das Senden nicht ausgeschaltet ist (zu Testzwecken)
                        if (_tlsoipCHelloDelay == 0) {
                            DEBUG.info("KeepAlive-Überwachung für das Senden ist ausgeschaltet (tlsoip.C_HelloDelay = 0)");
                            _sendKeepAliveTel = false;
                        } else {

                            // Zeitraum [s], seit letztes Telegramm gesendet wurde
                            int durationSinceLastSendAllTel = (int) ((System.currentTimeMillis() - _lastSendTimeAllTel) / 1000L);

                            // Muss KeepAlive-Telegramm versendet werden ?
                            if (_tlsoipCHelloDelay < durationSinceLastSendAllTel) {

                                // Ja (erfolgt in handleAsyncSend()). Zusätzlich Timer für nächsten spätesten Versand setzten.
                                _sendKeepAliveTel = true;
                                scheduleActionTimer(ActionType.KEEPALIVE_TIMER_SEND, _tlsoipCHelloDelay);
                            } else {

                                // Nein, aber bald. Timer neu setzen.
                                scheduleActionTimer(ActionType.KEEPALIVE_TIMER_SEND, _tlsoipCHelloDelay - durationSinceLastSendAllTel);
                            }
                        }
                    }
                }
            } else if (action == ActionType.QUITT_TIMER_RECEIVE) {
                synchronized (_linkLock) {
                    if (_linkState == LinkState.CONNECTED) {

                        // Prüfen, ob Quittierungs-Überwachung für den Empfang nicht ausgeschaltet ist (zu Testzwecken)
                        if (_tlsoipCReceiptTimeout == 0) {
                            DEBUG.info("Quittierungs-Überwachung für versandte Daten-Telegramme ist ausgeschaltet (tlsoip.C_ReceiptTimeout = 0)");
                        } else {

                            // Zeitraum [s], seit letztes Daten-Telegramm verschickt wurde
                            int durationSinceLastSendDataTel = (int) ((System.currentTimeMillis() - _lastSendTimeDataTel) / 1000L);

                            // Hätte Quittierungs-Telegramm bereits empfangen werden müssen?
                            if (_tlsoipCReceiptTimeout < durationSinceLastSendDataTel) {

                                // ..aber nur, wenn überhaupt Quittung erwartet werden darf
                                if (_countSendDataTel > 0) {

                                    // Ja. Verbindung abbrechen ....
                                    DEBUG.warning(String.format("Verbindung wird wegen Zeitüberschreibung initialisiert (Empfang letztes Quittungs-Telegramm vor %ds > Parameter tlsoip.C_ReceiptTimeout %d", durationSinceLastSendDataTel, _tlsoipCReceiptTimeout));

                                    // ...und nach Wartezeit neu aufbauen (passiert jetzt wieder indirekt beim Verschicken des ersten Datentelegramms).
                                    _tlsoipCReceiptTimeoutGestartet = false;
                                    closeChannel();
                                } else {

                                    // Nein, aber Timer wieder auf Default setzen
                                    scheduleActionTimer(ActionType.QUITT_TIMER_RECEIVE, _tlsoipCReceiptTimeout);
                                }
                            } else {

                                // Nein, aber bald. Timer neu setzen
                                scheduleActionTimer(ActionType.QUITT_TIMER_RECEIVE, _tlsoipCReceiptTimeout - durationSinceLastSendDataTel);
                            }
                        }
                    }

                    return;
                }
            } else if (action == ActionType.QUITT_TIMER_SEND) {
                synchronized (_linkLock) {
                    if (_linkState == LinkState.CONNECTED) {

                        // ToDo freigeben nach Test: ###Start: Geändert inoat auf Server
                        boolean actionBearbeiten = true;

                        if (seqNum != _lastReceiptSeqNumDataTel) {  // Actions für nicht mehr aktuelle Sequenz-Nummern nicht bearbeiten
                            actionBearbeiten = false;
                        }

                        if (sofortQuittieren) {                     // sofortige Quittierung unbedingt bearbeiten
                            actionBearbeiten = true;
                        }

                        if (actionBearbeiten) {

                            // Prüfen, ob Quittungs-Überwachung für das Senden nicht ausgeschaltet ist (zu Testzwecken)
                            if (_tlsoipCReceiptDelay == 0) {
                                DEBUG.info("Quittierungs-Überwachung für empfangene Daten-Telegramme ist ausgeschaltet (tlsoip.C_ReceiptDelay = 0)");
                                _sendQuittTel = false;
                            } else {

                                // Zeitraum [s], seit letztes Daten-Telegramm empfangen wurde
                                int durationSinceLastReceiptDataTel = (int) ((System.currentTimeMillis() - _lastReceiptTimeDataTel) / 1000L);

                                // Muss Quittierungs-Telegramm versendet werden ?
                                if ((_lastReceiptSeqNumDataTel != _lastQuittSeqNumDataTel) && ((_tlsoipCReceiptDelay < durationSinceLastReceiptDataTel) || (_countReceiptDataTel >= _tlsoipCReceiptCount) || (sofortQuittieren))) {

                                    // Ja (erfolgt in handleAsyncSend()).
                                    _sendQuittTel = true;

                                    // ToDo Prüfen ob notwendig: Zusätzlich Timer für nächsten spätesten Versand setzten.
                                    scheduleActionTimer(ActionType.QUITT_TIMER_SEND, _tlsoipCReceiptDelay, seqNum);
                                } else {

                                    // Nein, aber bald. Timer neu setzen.
                                    if (_tlsoipCReceiptDelay < durationSinceLastReceiptDataTel) {
                                        scheduleActionTimer(ActionType.QUITT_TIMER_SEND, _tlsoipCReceiptDelay, seqNum);
                                    } else {
                                        scheduleActionTimer(ActionType.QUITT_TIMER_SEND, _tlsoipCReceiptDelay - durationSinceLastReceiptDataTel, seqNum);
                                    }
                                }
                            }
                        }
                    }
                }

                // ToDo freigeben nach Test: ###Ende: Geändert inovat auf Server
            } else {
                DEBUG.warning(String.format("Unbekannter ActionType: %s", action));
            }

            // In jedem Fall werden eventuell anliegende Sendeaufträge (KeepAlive, Quittungen, Daten) bearbeitet
            handleAsyncSend(selector);
        }

        /**
         * Führt den asynchronen Versand von noch zu versendenden Telegrammen aus.
         *
         * @param selector Selektor des Protokoll-Threads zum asynchronen Zugriff auf die Kommunikationskanäle.
         */
        private void handleAsyncSend(Selector selector) {
            if (_socketChannel == null) {
                return;
            }

            try {
                do {
                    if (!_sendBuffer.hasRemaining()) {
                        if (_sendQuittTel && (_lastReceiptSeqNumDataTel != _lastQuittSeqNumDataTel)) {

                            // Quittungstelegramm versenden
                            DEBUG.fine(String.format("Quittungs-Telegramm wird versendet für SeqNum [%d], letzte Quittung war für SeqNum [%d]", _lastReceiptSeqNumDataTel, _lastQuittSeqNumDataTel));
                            _countReceiptDataTel    = 0;
                            _sendKeepAliveTel       = false;
                            _sendQuittTel           = false;
                            _lastReceiptTimeDataTel = System.currentTimeMillis();  // Zeitüberwachung für Quittung zurücksetzen
                            _lastSendTimeAllTel     = System.currentTimeMillis();
                            _lastQuittSeqNumDataTel = _lastReceiptSeqNumDataTel;

                            TLSoIPFrame tlsoIPFrame = new TLSoIPFrame(_lastReceiptSeqNumDataTel, TLSoIPFrame.TELTYPE_QUITT, null);

                            _sendBuffer.clear();
                            _sendBuffer.put(tlsoIPFrame.getTel());
                            _sendBuffer.flip();
                            DEBUG.fine(String.format("--> %s %s Quittierungs-Telegramm versendet %d", this, new Date().toString(), _lastReceiptSeqNumDataTel));
                        } else if (_sendKeepAliveTel) {

                            // KeepAlive-Telegramm versenden
                            DEBUG.fine("KeepAlive-Telegramm wird versendet.");
                            _sendKeepAliveTel   = false;
                            _lastSendTimeAllTel = System.currentTimeMillis();

                            TLSoIPFrame tlsoIPFrame = new TLSoIPFrame(0, TLSoIPFrame.TELTYPE_KEEPALIVE, null);

                            _sendBuffer.clear();
                            _sendBuffer.put(tlsoIPFrame.getTel());
                            _sendBuffer.flip();
                            DEBUG.fine(String.format("--> %s %s KeepAlive-Telegramm versendet", this, new Date().toString()));
                        } else {

                            // Daten-Telegramm versenden
                            // Prüfen, ob überhaupt noch weitere Datentelegramme versand werden dürfen
                            // Wenn tlsoip.C_ReceiptCount = 0, dann ist Quittierungsüberwachung beim Versand ausgeschaltet.
                            if ((_countSendDataTel >= _tlsoipCReceiptCount) && (_tlsoipCReceiptCount > 0)) {
                                DEBUG.warning(String.format("Datenversand wegen fehlender Quittung angehalten. Versandte Datentelegramme %d >= Parameter tlsoip.C_ReceiptCount %d", _countSendDataTel, _tlsoipCReceiptCount));

                                break;  // Schleife unbedingt verlassen, damit z. B. Quittungstelegramme berhaupt empfangen werden knnen.
                            } else {

                                // Prüfen, ob Daten zum Versand anliegen
                                PriorizedByteArray priorizedByteArray = (PriorizedByteArray) _sendChannel.poll(0L);

                                if (priorizedByteArray != null) {
                                    final byte[] bytes = priorizedByteArray.getBytes();

                                    if (bytes == null) {
                                        closeChannel();
                                    } else {

                                        // TLS-Telegramm verschicken
                                        DEBUG.fine("Senden eines TLS-Telegramms");
                                        _packetOnTheAir = bytes;

                                        int nextSendSeqNumDataTel = getNextSeqNum(_lastSendSeqNumDataTel);

                                        _lastSendSeqNumDataTel = nextSendSeqNumDataTel;
                                        _countSendDataTel++;
                                        _lastSendTimeDataTel = System.currentTimeMillis();
                                        _lastSendTimeAllTel  = System.currentTimeMillis();

                                        TLSoIPFrame tlsoIPFrame = new TLSoIPFrame(nextSendSeqNumDataTel, TLSoIPFrame.TELTYPE_IB_V1, bytes);

                                        _sendBuffer.clear();
                                        _sendBuffer.put(tlsoIPFrame.getTel());
                                        _sendBuffer.flip();
                                        DEBUG.fine(String.format("--> %s %s Datentelegramm gesendet %d", this, new Date().toString(), tlsoIPFrame.getSeqNum()));

                                        if (!_tlsoipCReceiptTimeoutGestartet) {
                                            DEBUG.fine("inovat: ActionType.QUITT_TIMER_RECEIVE gestartet");
                                            scheduleActionTimer(ActionType.QUITT_TIMER_RECEIVE, _tlsoipCReceiptTimeout);
                                            _tlsoipCReceiptTimeoutGestartet = true;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (_sendBuffer.hasRemaining()) {
                        DEBUG.finest(String.format("Sendeversuch für verbleibende %d Bytes", _sendBuffer.remaining()));

                        int sent = _socketChannel.write(_sendBuffer);

                        DEBUG.finest(String.format("erfolgreich gesendete Bytes %d", sent));
                    }

                    if (_sendBuffer.hasRemaining()) {
                        DEBUG.finer("Versand wird sobald möglich fortgesetzt");
                        _socketChannel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, this);

                        break;
                    } else {
                        if (_packetOnTheAir != null) {
                            byte[] sentPacket = _packetOnTheAir;

                            // noinspection AssignmentToNull
                            _packetOnTheAir = null;
                            notifyEvent(DataLinkLayerEvent.Type.DATA_SENT, sentPacket);
                        }
                    }
                } while (!_sendChannel.isEmpty());
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                DEBUG.warning(String.format("Unerwartete Exception: %s", e));
            }
            catch (IOException e) {
                DEBUG.warning(String.format("Verbindung wird wegen Fehler beim Senden initialisiert: %s", e));
                closeChannel();
            }
        }

        /**
         * Verarbeitet asynchrone Kommunikationsoperationen anhand der vom Selektor des Protokoll-Threads gelieferten
         * Möglichkeiten
         *
         * @param selectionKey Vom Selektor des Protokoll-Threads gelieferte Kommunikationsmöglichkeiten
         * @param selector     Selektor des Protokoll-Threads zum asynchronen Zugriff auf die Kommunikationskanäle.
         */
        public void handleSelection(SelectionKey selectionKey, Selector selector) {
            DEBUG.finer(String.format("handleSelection(%d/%d): %s", selectionKey.readyOps(), selectionKey.interestOps(), this));

            if (selectionKey.isAcceptable()) {
                DEBUG.fine("Verbindungsaufbau akzeptieren");
                connectSocketChannel(selector);
            }

            if (!selectionKey.isValid()) {
                return;
            }

            // Es liegen Daten zum Lesen an...
            if (selectionKey.isReadable()) {
                DEBUG.finest("Telegramm-Daten empfangen");

                try {

                    // DEBUG.finest(String.format("_readBuffer vor dem Lesen: (%s), Daten: (%s)", _readBuffer, HexDumper.toString(_readBuffer.array(), 0, _readBuffer.position())));
                    DEBUG.finest(String.format("_readBuffer vor dem Lesen: (%s)", _readBuffer));

                    int count = _socketChannel.read(_readBuffer);

                    if (count == -1) {
                        DEBUG.info(String.format("--> %s %s Verbindung wurde von der Gegenseite terminiert", this, new Date().toString()));
                        closeChannel();
                    } else {
                        DEBUG.finest(String.format("Anzahl gelesener Bytes: %d", count));
                        _readBuffer.flip();

                        int remaining;

                        // DEBUG.finest(String.format("_readBuffer nach dem Lesen: (%s), Daten: (%s)", _readBuffer, HexDumper.toString(_readBuffer.array(), 0, _readBuffer.position())));
                        DEBUG.finest(String.format("_readBuffer vor dem Lesen: (%s)", _readBuffer));

                        while ((remaining = _readBuffer.remaining()) >= 10) {

                            // Position des Telegrammstarts merken, damit wieder auf diese Position zurückgesprungen
                            // werden kann, wenn erst ein Teil des Telegramms verfügbar ist. (Bugfix KS/RS)
                            int telegramStartPosition = _readBuffer.position();

                            // Zeit für letztes empfangenes Telegramm setzten
                            _lastReceiptTimeAllTel = System.currentTimeMillis();

                            // Header einlesen
                            TLSoIPFrame tlSoIPFrame = new TLSoIPFrame(_readBuffer);

                            try {

                                // Test, ob erstes Byte 0x68 enthält, sonst wurde kein TLS over IP-Telegramm empfangen
                                if (!tlSoIPFrame.isTLSoIPFrame()) {
                                    throw new IllegalTelegramException(String.format("Telegramm ist kein TLSoIP-Telegramm, Byte Sync ist nicht (68h bzw. 104dez) sondern (%ddez)", tlSoIPFrame.getSync()));
                                }

                                // Quittungs-Telegramm prüfen und entsprechende Quittungs-Zustände setzten
                                if (tlSoIPFrame.isQuittTel()) {
                                    DEBUG.fine(String.format("--> %s %s Quittungs-Telegramm empfangen %d", this, new Date().toString(), tlSoIPFrame.getSeqNum()));

                                    // Zeitpunkt der letzten Quittierung setzen (wird einfach als letztes gesendetes Datentelegramm gewertet für die Zeitüberwachung)
                                    _lastSendTimeDataTel = System.currentTimeMillis();

                                    // Prüfen, ob SeqNummer korrekt ist
                                    if (tlSoIPFrame.getSeqNum() == _lastSendSeqNumDataTel) {

                                        // Anzahl der nicht quittierten gesendeten Telegramm zurücksetzten.
                                        _countSendDataTel = 0;
                                    }
                                }

                                // KeepAlive-Telegramm prüfen und entsprechende KeepAlive-Zustände setzten
                                if (tlSoIPFrame.isKeepAliveTel()) {

                                    // Nichts tun: Beim Verbindungsaufbau werden die KeepAliveTimer gesetzt und nei Zeiten aufgerufen.
                                    // Beim Aufruf wird entschieden, ob KeepAlive-Empfangs-Telegramm notwendig gewesen wäre und
                                    // ggf. der nächste späteste Zeitpunkt registriert.
                                    DEBUG.finer(String.format("--> %s %s KeepAlive-Telegramm empfangen", this, new Date().toString()));
                                }

                                // Datentelegramm empfangen
                                if (tlSoIPFrame.isDataTel()) {

                                    // Test, ob maximale Telegrammlänge nicht überschritten wurde (> 253 Byte)
                                    if (tlSoIPFrame.getLen() > getMaximumDataSize()) {
                                        throw new IllegalTelegramException(String.format("Empfangene TLSoIP-Datenlänge ist zu groß (maximal %d Byte) : %s", tlSoIPFrame.getLen(), remaining));
                                    }

                                    // Falls genug Daten zum Lesen vorhanden sind
                                    if ((remaining = _readBuffer.remaining()) >= tlSoIPFrame.getLen()) {
                                        tlSoIPFrame.addData(_readBuffer);
                                        DEBUG.finer(String.format("TLS Datentelegramm empfangen: %s", tlSoIPFrame.toString()));

                                        // Empfangene Sequenznummer prüfen
                                        if (!(tlSoIPFrame.getSeqNum() == getNextSeqNum(_lastReceiptSeqNumDataTel))) {
                                            int lastReceiptSeqNumDataTelOnlyForDebug = _lastReceiptSeqNumDataTel;

                                            _lastReceiptSeqNumDataTel = tlSoIPFrame.getSeqNum();

                                            throw new IllegalTelegramException(String.format("Empfangene SeqNum (%d) entspricht nicht der erwarteten SeqNum (%d)!", tlSoIPFrame.getSeqNum(), getNextSeqNum(lastReceiptSeqNumDataTelOnlyForDebug)));
                                        } else {

                                            // Neue Nummer als letzte Nummer merken
                                            _lastReceiptSeqNumDataTel = tlSoIPFrame.getSeqNum();
                                            DEBUG.fine(String.format("--> %s %s Datentelegramm empfangen %d", this, new Date().toString(), tlSoIPFrame.getSeqNum()));

                                            // Zeit für letztes empfangenes Daten-Telegramm setzten (erst hier, weil jetzt als gültig akzeptiert)
                                            _lastReceiptTimeDataTel = System.currentTimeMillis();

                                            // Anzahl der empfangenen Datentelegramme seit der letzten Quittung prüfen und ggf. Quittung versenden
                                            _countReceiptDataTel++;

                                            if (_countReceiptDataTel == _tlsoipCReceiptCount) {

                                                // Wenn Anzahl der Datentelegramme, nach der Quittiert werden muss, erreicht ist, sofort Quittieren...
                                                // ToDo freigeben nach Test: Geändert inovat auf Server
                                                scheduleActionTimer(ActionType.QUITT_TIMER_SEND, 0, _lastReceiptSeqNumDataTel, true);
                                            } else {

                                                // ...sonst spätenstens nach Ablauf der EmpfangDelays Quittieren
                                                // ToDo freigeben nach Test: Geändert inovat auf Server
                                                scheduleActionTimer(ActionType.QUITT_TIMER_SEND, _tlsoipCReceiptDelay, _lastReceiptSeqNumDataTel);
                                            }

                                            // Datenempfang melden...
                                            notifyEvent(DataLinkLayerEvent.Type.DATA_RECEIVED, tlSoIPFrame.getData());
                                        }
                                    } else {

                                        // throw new Server.IllegalTelegramException(String.format("Länge des Datenblocks laut Header (%d) kleiner als vorhandene Anzahl der Nutzdaten (%d)", tlSoIPFrame.getLen(), remaining));
                                        // TBD warning durch finer ersetzen
                                        DEBUG.finer(String.format("Länge des Datenblocks laut Header (%d) größer als vorhandene Anzahl der Nutzdaten (%d), Telegrammfragment wird gespeichert.", tlSoIPFrame.getLen(), remaining));

                                        // Das ganze Telegramm ist noch nicht verfügbar. Position des Lesepuffers wird
                                        // wieder an den Telegrammanfang zurückgesetzt. Die bereits gelesenen Bytes
                                        // verbleiben im Lesepuffer und werden bei späteren Leseoperation erneut
                                        // ausgewertet. (Bugfix KS/RS)
                                        _readBuffer.position(telegramStartPosition);

                                        break;
                                    }
                                }
                            }
                            catch (IllegalTelegramException e) {
                                DEBUG.error(e.getLocalizedMessage(), e);
                                closeChannel();

                                return;
                            }
                        }

                        _readBuffer.compact();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (!selectionKey.isValid()) {
                return;
            }

            if (selectionKey.isWritable()) {
                if (!_sendBuffer.hasRemaining()) {
                    selectionKey.interestOps(SelectionKey.OP_READ);
                }
            }

            if (!selectionKey.isValid()) {
                return;
            }

            handleAsyncSend(selector);
        }

        // ToDo freigeben nach Test: ###Start: Geändert inovat auf Server

        /**
         * Sendet eine Aktion für dieses Verbindungsobjekt zur Ausführung an den Protokoll-Thread.
         *
         * @param action           Auszuführende Aktion
         * @param seqNum           SequenzNummer des Telegramms, welches quittiert werden soll
         * @param sofortQuittieren <code>true</code>, wenn Quittierung in jedem Fall sofort erfolgen soll, sonst
         *                         <code>false</code>.
         */
        private void notifyWorker(ActionType action, int seqNum, boolean sofortQuittieren) {
            _worker.notify(this, action, seqNum, sofortQuittieren);
        }

        /** Initiiert den Abbruch und erneuten Verbindungsaufbau einer bestehenden Verbindung mit evtl. geänderten Parametern */
        public void reload() {
            DEBUG.fine(String.format("reload %s", this));

            synchronized (_protocolLock) {
                if ((_protocolState == ProtocolState.STARTED) || (_protocolState == ProtocolState.STARTING)) {
                    notifyWorker(ActionType.RELOAD_CALLED, 0, false);
                }
            }
        }

        /**
         * Plant eine Aktion mit Hilfe eines Timer-Objekts zur späteren Ausführung ein.
         *
         * @param actionType   Auszuführende Aktion
         * @param delaySeconds Verzögerungszeit in Sekunden nach der die Aktion ausgeführt werden soll.
         */
        private void scheduleActionTimer(final ActionType actionType, int delaySeconds) {
            scheduleActionTimer(actionType, delaySeconds, 0, false);
        }

        /**
         * Plant eine Aktion mit Hilfe eines Timer-Objekts zur späteren Ausführung ein.
         *
         * @param actionType   Auszufhrende Aktion
         * @param delaySeconds Verzögerungszeit in Sekunden nach der die Aktion ausgeführt werden soll.
         * @param seqNum       SequenzNummer des letzten empfangenen Datentelegramms
         */
        private void scheduleActionTimer(final ActionType actionType, int delaySeconds, final int seqNum) {
            scheduleActionTimer(actionType, delaySeconds, seqNum, false);
        }

        /**
         * Plant eine Aktion mit Hilfe eines Timer-Objekts zur späteren Ausführung ein.
         *
         * @param actionType       Auszufhrende Aktion
         * @param delaySeconds     Verzögerungszeit in Sekunden nach der die Aktion ausgeführt werden soll.
         * @param seqNum           SequenzNummer des Telegramms, welches quittiert werden soll
         * @param sofortQuittieren <code>true</code>, wenn Quittierung in jedem Fall sofort erfolgen soll, sonst
         *                         <code>false</code>.
         */
        private void scheduleActionTimer(final ActionType actionType, int delaySeconds, final int seqNum, final boolean sofortQuittieren) {
            final TimerTask timerTask = new TimerTask() {
                public void run() {
                    notifyWorker(actionType, seqNum, sofortQuittieren);
                }
            };

            _timer.schedule(timerTask, delaySeconds * 1000L);
        }

        // ToDo freigeben nach Test: ###Ende: Geändert inovat auf Server

        /**
         * Initiiert den Versand eines Telegramms.
         *
         * @param bytes    Bytearray mit den Bytes des zu sendenden Telegramms.
         * @param priority Priorität des zu sendenden Telegramms.
         *
         * @throws InterruptedException Wenn der aktuelle Thread unterbrochen wurde.
         */
        public void send(byte[] bytes, int priority) throws InterruptedException {
            DEBUG.finer(String.format("Telegramm soll gesendet werden:%nPriorität: %d%nDaten: %s", priority, HexDumper.toString(bytes)));

            synchronized (_linkLock) {
                if (_linkState != LinkState.CONNECTED) {
                    throw new IllegalStateException(String.format("Telegramm kann in diesem Verbindungszustand nicht versendet werden: %s", _linkState));
                }
            }

            DEBUG.finest(String.format("Telegramm wird zum Versand gepuffert, Priorität: %d", priority));
            _sendChannel.put(new PriorizedByteArray(bytes, priority));

            // ToDo freigeben nach Test: Geändert inovat auf Server
            notifyWorker(ActionType.SEND_CALLED, 0, false);
        }

        //~ SET METHODEN ======================================================

        /**
         * Setzt neue Parameterwerte.
         *
         * @param properties Neue Parameterwerte.
         */
        public void setProperties(Properties properties) {
            synchronized (_linkLock) {
                _properties = properties;

                if ((_linkState == LinkState.CONNECTED) || (_linkState == LinkState.CONNECTING)) {
                    reload();
                }
            }
        }

        //~ METHODEN ==========================================================

        /** Initiiert das Schließen der bestehenden Verbindung dieses Verbindungsobjekts */
        public void shutdown() {
            DEBUG.fine(String.format("shutdown %s", this));

            synchronized (_linkLock) {
                if ((_linkState == LinkState.DISCONNECTED) || (_linkState == LinkState.DISCONNECTING)) {
                    return;
                }

                _linkState = LinkState.DISCONNECTING;
            }

            try {
                _sendChannel.put(new PriorizedByteArray(null, 2));
            }
            catch (InterruptedException e) {
                e.printStackTrace();

                throw new RuntimeException(e);
            }

            // ToDo freigeben nach Test: Geändert inovat auf Server
            notifyWorker(ActionType.SHUTDOWN_CALLED, 0, false);
        }
    }


    /** Dient zur Speicherung eines zu versendenden Telegramms mit einer zugeordneten Priorität */
    private static class PriorizedByteArray implements PriorizedObject {

        /**
         * Array mit den einzelnen Bytes des zu versendenden Telegramms. Der Wert <code>null</code> signalisiert, dass keine
         * weiteren Telegramme mehr versendet werden sollen.
         */
        private final byte[] _bytes;

        /** Priorität des zu versendenden Telegramms. Kleinere Werte haben höhere Priorität. */
        private final int _priority;

        //~ KONSTRUKTOREN  (und vom Konstruktor verwendete Methoden) ==========

        /**
         * Erzeugt ein neues Objekt mit den angegebenen Eigenschaften.
         *
         * @param bytes    Array mit den einzelnen Bytes des zu versendenden Telegramms. Der Wert <code>null</code>
         *                 signalisiert, dass keine weiteren Telegramme mehr versendet werden sollen.
         * @param priority Priorität des zu versendenden Telegramms. Kleinere Werte haben höhere Priorität.
         */
        public PriorizedByteArray(byte[] bytes, int priority) {
            _bytes    = bytes;
            _priority = priority;
        }

        //~ GET METHODEN ======================================================

        /**
         * @return Array mit den einzelnen Bytes des zu versendenden Telegramms. Der Wert <code>null</code> signalisiert, dass
         *         keine weiteren Telegramme mehr versendet werden sollen.
         */
        public byte[] getBytes() {
            return _bytes;
        }

        /** @return Priorität des zu versendenden Telegramms. Kleinere Werte haben höhere Priorität. */
        public int getPriorityClass() {
            return _priority;
        }
    }


    /**
     * Klasse die das Runnable-Interface implementiert, vom Protokollthread ausgeführt wird und den Protokollablauf
     * steuert
     */
    private class Worker implements Runnable {

        /**
         * Selektor-Objekt, mit dessen Hilfe alle Kommunikationsoperationen (Verbindungsaufbau, Versand und Empfang von Daten)
         * ohne zusätzliche Threads asynchron ausgeführt werden.
         */
        private final Selector _selector;

        /** Queue zur Übermittlung von Aktionen an den Protokoll-Thread */
        private final UnboundedQueue<WorkAction> _workQueue;

        //~ KONSTRUKTOREN  (und vom Konstruktor verwendete Methoden) ==========

        /**
         * Konstruktor initialisiert den Selektor und die Queue zur Übermittlung von Aktionen.
         *
         * @throws java.io.IOException wenn eine nicht abgefangene Ausnahme auftritt.
         */
        public Worker() throws IOException {
            _selector  = Selector.open();
            _workQueue = new UnboundedQueue<WorkAction>();
        }

        //~ METHODEN ==========================================================

        // ToDo freigeben nach Test: ###Start: Geändert inovat auf Server

        /**
         * Kann von einem beliebigen Thread aufgerufen werden, um dem Protokoll-Thread zu signalisieren, dass eine bestimmte
         * Aktion ausgeführt werden soll.
         *
         * @param link             Verbindung, auf die sich die Aktion bezieht.
         * @param action           Durchzuführende Aktion
         * @param seqNum           SequenzNummer des Telegramms, welches quittiert werden soll
         * @param sofortQuittieren <code>true</code>, wenn Quittierung in jedem Fall sofort erfolgen soll, sonst
         *                         <code>false</code>.
         */
        public void notify(Link link, ActionType action, int seqNum, boolean sofortQuittieren) {
            _workQueue.put(new WorkAction(link, action, seqNum, sofortQuittieren));
            DEBUG.finer("Aufruf von _selector.wakeup()");
            _selector.wakeup();
        }

        // ToDo freigeben nach Test: ###Ende: Geändert inovat auf Server

        /** Methode, die beim Start des Protokoll-Threads aufgerufen wird und die asynchrone Protokollsteuerung implementiert. */
        public void run() {
            synchronized (_protocolLock) {

                // Warten bis Protokoll gestartet wird
                while (_protocolState == ProtocolState.CREATED) {
                    DEBUG.fine(String.format("Warten auf den Start des Protokolls: %s", toString()));

                    try {
                        _protocolLock.wait();
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                        DEBUG.error(String.format("Fehler im %s: %s", toString(), e));
                    }
                }
            }

            // Hier wird 30 Sekunden gewartet, um der TLS-OSI-7 genügend Zeit zur Initialisierung zu geben
            DEBUG.fine("TSLoIP: 30 Sekunden warten: " + toString());

            try {
                Thread.sleep(30000);
            }
            catch (InterruptedException e) {

                // nichts zu tun
            }

            DEBUG.fine("TLSoIP: Beginn der Protokollabarbeitung: " + toString());

            while (true) {
                try {
                    final ProtocolState state;

                    synchronized (_protocolLock) {

                        // Instabile Zwischenzustände werden innerhalb des synchronized Block überprüft,
                        // da eine Zustandsänderung notwendig sein könnte
                        if (_protocolState == ProtocolState.STARTING) {
                            _protocolState = ProtocolState.STARTED;
                        } else if (_protocolState == ProtocolState.STOPPING) {}

                        state = _protocolState;
                    }

                    if (state == ProtocolState.STARTING) {

                        // wird beim nächsten Schleifendurchlauf im synchronized Block (oben) behandelt
                    } else if ((state == ProtocolState.STARTED) || (state == ProtocolState.STOPPING)) {
                        DEBUG.finest(String.format("Protokoll arbeitet: %s", this));

                        WorkAction action;

                        while (null != (action = _workQueue.poll(0))) {
                            action._link.handleAction(action._action, _selector, action._seqNum, action._sofortQuittieren);
                        }

                        try {
                            DEBUG.finest("Aufruf von select()");

                            int count = _selector.select();  // ACHTUNG: Aufruf blockiert, bis Anforderung anliegt.

                            DEBUG.finest(String.format("Rückgabe von select(): %d", count));

                            Set<SelectionKey> selectedKeys = _selector.selectedKeys();

                            for (Iterator<SelectionKey> iterator = selectedKeys.iterator(); iterator.hasNext(); ) {
                                SelectionKey selectionKey = iterator.next();

                                iterator.remove();

                                Link selectedLink = (Link) selectionKey.attachment();

                                selectedLink.handleSelection(selectionKey, _selector);
                            }
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (state == ProtocolState.STOPPED) {
                        DEBUG.fine(String.format("Protokoll wurde gestoppt: %s", this));

                        break;
                    } else {
                        DEBUG.error(String.format("Ungültiger Zustand: %s; %s", state, this));
                    }
                }
                catch (InterruptedException e) {
                    DEBUG.warning(String.format("InterruptedException: %s", this), e);
                }
                catch (RuntimeException e) {
                    DEBUG.warning(String.format("Unerwarteter Fehler: %s; %s", e.getLocalizedMessage(), this), e);
                }
            }

            DEBUG.warning(String.format("Thread wird terminiert: %s", this));
        }

        /**
         * Ausgabe von Informationen für dieses Objekt für Debug-Zwecke. Das genaue Format ist nicht festgelegt und kann sich
         * ändern..
         *
         * @return Informationen dieses Objekts für Debug-Zwecke
         */
        public String toString() {
            return String.format("Worker für %s", Server.this.toString());
        }

        //~ INNERE KLASSEN ====================================================

        // ToDo freigeben nach Test: ###Start: Geändert inovat auf Server

        /**
         * Hilfsklasse, die zur Speicherung einer Aktion zusammen mit der Verbindung, auf die sich die Aktion bezieht,
         * eingesetzt wird.
         */
        class WorkAction {

            /** Zur Speicherung der Aktion */
            public final ActionType _action;

            /** Zur Speicherung der Verbindung */
            public final Link _link;

            /** Zur Speicherung der SeqNum */
            public final int _seqNum;

            /** Zur Speicherung der Kennung, dass sofort quittiert werden soll */
            public final boolean _sofortQuittieren;

            //~ KONSTRUKTOREN  (und vom Konstruktor verwendete Methoden) ======

            /**
             * Erzeugt ein neues Hilfsobjekt für eine Aktion, die an einer Verbindung auszuführen ist.
             *
             * @param link   Zu speichernde Verbindung
             * @param action Zu speichernde Aktion
             */
            public WorkAction(Link link, ActionType action) {
                this(link, action, 0, false);
            }

            /**
             * Erzeugt ein neues Hilfsobjekt für eine Aktion, die an einer Verbindung auszuführen ist.
             *
             * @param link   Zu speichernde Verbindung
             * @param action Zu speichernde Aktion
             * @param seqNum Zu speichernde SeqNum
             */
            public WorkAction(Link link, ActionType action, int seqNum) {
                this(link, action, seqNum, false);
            }

            /**
             * Erzeugt ein neues Hilfsobjekt für eine Aktion, die an einer Verbindung auszuführen ist.
             *
             * @param link             Zu speichernde Verbindung
             * @param action           Zu speichernde Aktion
             * @param seqNum           Zu speichernde SeqNum
             * @param sofortQuittieren Zu speichernde Kennung sofort quittieren
             */
            public WorkAction(Link link, ActionType action, int seqNum, boolean sofortQuittieren) {
                _link             = link;
                _action           = action;
                _seqNum           = seqNum;
                _sofortQuittieren = sofortQuittieren;
            }

            //~ METHODEN ======================================================

            // ToDo freigeben nach Test: ###Ende: Geändert inovat auf Server

            /**
             * Ausgabe von Informationen für dieses Objekt für Debug-Zwecke. Das genaue Format ist nicht festgelegt und kann sich
             * ändern..
             *
             * @return Informationen dieses Objekts für Debug-Zwecke
             */
            public String toString() {
                return String.format("WorkAction(link: %s, action: %s", _link, _action);
            }
        }
    }
}


//~Formatiert mit 'inovat Kodierkonvention' am 22.07.14
