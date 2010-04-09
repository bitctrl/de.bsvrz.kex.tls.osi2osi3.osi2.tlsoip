/*
 * Copyright (c) 2010 by inovat, innovative systeme - verkehr - tunnel - technik, Dipl.-Ing. H. C. Kniss
 *
 * This file is part of de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.Client
 *
 * de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.Client is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.Client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.Client; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Contact Information:
 * inovat, Dipl.-Ing. H. C. Kniss
 * Koelner Strasse 30
 * D-50859 K�ln
 * +49 (0)2234 4301 800
 * info@invat.de
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

//~ KLASSEN ===================================================================

/**
 * Klasse, die als OSI-2 Protokollmodul f�r den Client-seitigen Teil einer TLSoIP-Verbindung eingesetzt werden kann.
 * <p>
 * Zur Verwendung dieses Protokollmoduls ist an dem jeweiligen Anschlusspunkt in der Konfiguration in
 * der Attributgruppe "atg.anschlussPunkt" im Attribut "ProtokollTyp" der Wert
 * "de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.Client" einzutragen.
 * <p>
 * Im Parameter "atg.protokollEinstellungenStandard" des Anschlu�punkts k�nnen
 * die Standardwerte f�r alle Verbindungen an diesem Anschlu�punkt eingestellt.
 * <p>
 * Im Parameter "atg.protokollEinstellungenPrimary" der dem Anschlu�punkt
 * zugeordneten Anschlu�PunktKommunikationsPartner k�nnen individuelle
 * Werte f�r die Verbindung zum jeweiligen Kommunikationspartner eingestellt werden.
 * <p>
 * Die Parameterdatens�tze k�nnen dabei mehrere Eintr�ge enthalten, die jeweils aus
 * einem Namen und einem Wert bestehen.
 * <p>
 * Folgende Eintr�ge werden unterst�tzt (siehe auch TLS 2009, Teil 2,
 * Daten�bertragung �ber TCP/IP (TLSoIP):
 * <p>
 * Verbindungsparameter f�r beide Verbindungspartner:
 * <p>
 * <table cellpadding="2" cellspacing="2" border="1">
 * <tr> <th> Name </th> <th> Defaultwert </th> <th> Beschreibung </th> </tr>
 * <tr> <td> tlsoip.C_HelloDelay </td> <td> 30 </td> <td> Zeit [s], nach der ein Keep-Alive-Telegramm an die Gegenstelle versendet werden muss (0=ausgeschaltet f�r Testzwecke, 1...3599). </td> </tr>
 * <tr> <td> tlsoip.C_HelloTimeout </td> <td> 60 </td> <td> Zeit [s], nach der sp�testens ein Keep-Alive-Telegramm der Gegenstelle erwartet wird ( > C_HelloDelay der Gegenstelle), (0=ausgeschaltet f�r Testzwecke, 1...3600). </td> </tr>
 * <tr> <td> tlsoip.C_ReceiptCount </td> <td> 10 </td> <td> Anzahl empfangener/gesendeter Telegramme, nach der sp�testens ein Quittungstelegramm versendet werden muss/erwartet wird (1..255). </td> </tr>
 * <tr> <td> tlsoip.C_ReceiptDelay </td> <td> 15 </td> <td> Zeit [s], nach der nach Erhalt eines Telegramms sp�tenstens ein Quittierungstelegramm an die Gegenstelle versendet werden muss (1..59). </td> </tr>
 * <tr> <td> tlsoip.C_ReceiptTimeout </td> <td> 30 </td> <td> Zeit [s], nach der sp�testens ein Quittungstelegramm von der Gegenstelle erwartet wird (> C_ReceiptDelay der Gegenstelle) (1..60). </td> </tr>
 * <tr> <td> tlsoip.C_SecureConnection </td> <td> nein </td> <td> WIRD AKTUELL NICHT UNTERST�TZT (immer nein): Verbindung wird ohne SSL betrieben (nein), Verbindung wird mit SSL betrieben (ja). </td> </tr>
 * </table>
 * <p/>
 * <p>
 * Verbindungsparameter f�r den Client:
 * <p>
 * <table cellpadding="2" cellspacing="2" border="1">
 * <tr> <th> Name </th> <th> Defaultwert </th> <th> Beschreibung </th> </tr>
 * <tr> <td> tlsoip.C_ServerAdrA </td> <td>  </td> <td> IP-Adresse des Servers. </td> </tr>
 * <tr> <td> tlsoip.C_AcceptPortA </td> <td>  </td>  <td> Portnummer des Servers. </td> </tr>
 * <tr> <td> tlsoip.C_ReconnectDelay </td> <td> 20 </td> <td> Zeit [s], nach der bei Nichtbestehen einer Verbindung sp�testens ein neuer Verbindungsaufbau initiiert werden muss (0=sofort, 1...3600). </td> </tr>
 * <tr> <td> tlsoip.C_ConnectDuration </td> <td> 0 </td> <td> AKTUELL WIRD NUR "0=immer" UNTERST�TZT!<br> Dauer [s], f�r die eine Verbindung vom Client aufrecht erhalten werden soll (0=immer, 1...3600). </td> </tr>
 * <tr> <td> tlsoip.C_ConnectDelay </td> <td> 00 05 00 </td> <td> WIRD AKTUELL NICHT UNTERST�TZT!<br> Zeit [hh mm ss], nach der zur Pr�fung der Erreichbarkeit des Servers ein Verbindungsaufbau stattfinden muss (optional, nur f�r tempor�re Verbindungen) (00 00 01...23 59 59). </td> </tr>
 * <tr> <td> tlsoip.C_ServerAdrB </td> <td>  </td><td> WIRD AKTUELL NICHT UNTERST�TZT!<br> IP-Adresse des Alternativ-Servers </td> </tr>
 * <tr> <td> tlsoip.C_AcceptPortB </td> <td>  </td><td> WIRD AKTUELL NICHT UNTERST�TZT!<br> Portnummer des Alternativ-Servers </td> </tr>
 * <tr> <td> tlsoip.waitForInitialReceive </td> <td> nein </td> <td> Wenn "ja", dann wartet das Protokoll nach dem Aufbau der TCP-Verbindung auf den Empfang eines initialen Telegramms, bevor eine Verbindung als "lebt" gemeldet wird. </td> </tr>
 * </table>
 * <p/>
 *
 * @author inovat, innovative systeme - verkehr - tunnel - technik
 * @author Dipl.-Ing. Hans Christian Kni� (HCK)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class Client extends TLSoIP implements PropertyQueryInterface {

    /** Logger f�r Debugausgaben */
    private static final Debug DEBUG = Debug.getLogger();

    //~ FELDER ================================================================

    /** Thread des Protokolls */
    private final Thread _workThread;

    /** Runnable Objekt, das vom Protokollthread ausgef�hrt wird und den Protokollablauf steuert */
    final Client.Worker _worker = new Client.Worker();

    /** Verbindung zum Datenverteiler */
    ClientDavInterface _connection;

    /** Aktueller Zustand des Protokolls */
    private ProtocolState _protocolState = ProtocolState.CREATED;

    /** Verbindungen zu Kommunikationspartnern, die durch das Protokoll verwaltet werden */
    private List<Link> _links = new LinkedList<Client.Link>();

    /** Monitor Objekt, das zur Synchronisation des Protokoll-Threads und den API-Zugriffen von fremden Threads auf das Protokoll koordiniert */
    private final Object _protocolLock = new Object();

    //~ KONSTRUKTOREN  (und vom Konstruktor verwendete Methoden) ==============

    /**
     * Default-Konstruktor, mit dem neue TLSoverIP-Client Protokolle instanziiert werden k�nnen.
     *
     * @throws IOException wenn eine nicht abgefangene Ausnahme auftritt.
     */
    public Client() throws IOException {
        DEBUG.fine("TLSoIP-CLient (Konstruktoraufruf)");
        _workThread = new Thread(_worker, "TLSoIP.Client.Worker");
    }

    //~ METHODEN ==============================================================

    /** Bricht die Kommunikation auf allen Verbindungen des Protokolls sofort ab und beendet anschlie�end das Protokoll. */
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
        return new Client.Link(remoteAddress);
    }

    //~ GET METHODEN ==========================================================

    /**
     * Bestimmt, ob die Kommunikation dieses Protokolls bereits mit der Methode {@link #start} aktiviert wurde.
     *
     * @return <code>true</code>, wenn die Kommunikation dieses Protokolls bereits aktiviert wurde,
     *         sonst <code>false</code>.
     */
    public boolean isStarted() {
        synchronized (_protocolLock) {
            return (_protocolState == ProtocolState.STARTING) || (_protocolState == ProtocolState.STARTED);
        }
    }

    //~ SET METHODEN ==========================================================

    /**
     * Nimmmt die Verbindung zum Datenverteiler entgegen. Diese Methode wird vom OSI-3 Modul nach dem
     * Erzeugen des OSI-2 Moduls durch den jeweiligen Konstruktor aufgerufen. Eine Implementierung
     * eines Protokollmoduls kann sich bei Bedarf die �bergebene Datenverteilerverbindung intern merken, um zu sp�teren
     * Zeitpunkten auf die Datenverteiler-Applikationsfunktionen zuzugreifen.
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
        DEBUG.fine(String.format("Neue Einstellungen f�r: %s, properties = %s", toString(), properties));

        synchronized (_protocolLock) {
            for (Link link : _links) {
                link.reload();
            }
        }
    }

    //~ METHODEN ==============================================================

    /** Terminiert alle Verbindungen des Protokolls und beendet anschlie�end das Protokoll. */
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

            _workThread.setName(String.format("TLSoIP.Client.Worker(%d)", localAddress));
            _workThread.start();
            _protocolState = ProtocolState.STARTING;
            _protocolLock.notifyAll();
        }
    }

    /**
     * Gibt Informationen des Protokolls f�r Debugzwecke zur�ck.
     * Das genaue Format ist nicht festgelegt und kann sich �ndern..
     *
     * @return Gibt Informationen des Protokolls f�r Debugzwecke zur�ck
     */
    public String toString() {
        return String.format("TLSoIP-Client(%d, %s) ", getLocalAddress(), _protocolState);
    }

    //~ INNERE KLASSEN ========================================================

    /** Definiert die Aktionscodes, die von den API-Methoden zur Steuerung des Protokoll-Threads versendet werden */
    public static class ActionType {

        /** Signalisiert dem Protokoll-Thread, das die Kommunikation auf einer Verbindung gestartet werden soll */
        public static final Client.ActionType CONNECT_CALLED = new Client.ActionType("CONNECT_CALLED");

        /** Signalisiert dem Protokoll-Thread, das die Kommunikation auf einer Verbindung geschlossen werden soll */
        public static final Client.ActionType SHUTDOWN_CALLED = new Client.ActionType("SHUTDOWN_CALLED");

        /** Signalisiert dem Protokoll-Thread, das ein Telegramm auf einer Verbindung versendet werden soll */
        public static final Client.ActionType SEND_CALLED = new Client.ActionType("SEND_CALLED");

        /** Signalisiert dem Protokoll-Thread, das ein erneuter Verbindungsversuch durchgef�hrt werden soll */
        public static final Client.ActionType RETRY_CONNECT = new Client.ActionType("RETRY_CONNECT");

        /** Signalisiert dem Protokoll-Thread, das die Kommunikation auf einer Verbindung mit Ber�cksichtigung von evtl. Parameter�nderungen neu aufgebaut werden soll */
        public static final Client.ActionType RELOAD_CALLED = new Client.ActionType("RELOAD_CALLED");

        /** Signalisiert dem Protokoll-Thread, das ein Quittierungs-Telegramm gesendet werden soll */
        public static final Client.ActionType QUITT_TIMER_SEND = new Client.ActionType("QUITT_TIMER_SEND");

        /** Signalisiert dem Protokoll-Thread, das ein der Empfang eines Quittierungs-Telegramms gepr�ft werden soll */
        public static final Client.ActionType QUITT_TIMER_RECEIVE = new Client.ActionType("QUITT_TIMER_RECEIVE");

        /** Signalisiert dem Protokoll-Thread, das ein Keep-Alive-Telegramm gesendet werden soll */
        public static final Client.ActionType KEEPALIVE_TIMER_SEND = new Client.ActionType("KEEPALIVE_TIMER_SEND");

        /** Signalisiert dem Protokoll-Thread, das ein der Empfang von Keep-Alive-Telegrammen gepr�ft werden soll */
        public static final Client.ActionType KEEPALIVE_TIMER_RECEIVE = new Client.ActionType("KEEPALIVE_TIMER_RECEIVE");

        /** Signalisiert dem Protokoll-Thread, das die Kommunikation auf einer Verbindung abgebrochen werden soll */
        public static final Client.ActionType ABORT_CALLED = new Client.ActionType("ABORT_CALLED");

        //~ FELDER ============================================================

        /** Name der Aktion f�r Debugzwecke */
        private final String _name;  // for debug only

        //~ KONSTRUKTOREN  (und vom Konstruktor verwendete Methoden) ==========

        /**
         * Erzeugt ein neues Aktionsobjekt mit vorgegebenem Namen
         *
         * @param name Name der Aktion f�r Debugzwecke
         */
        private ActionType(String name) {
            _name = name;
        }

        //~ METHODEN ==========================================================

        /**
         * Gibt Informationen des ActionType f�r Debugzwecke zur�ck.
         * Das genaue Format ist nicht festgelegt und kann sich �ndern..
         *
         * @return Name der Aktion f�r Debugzwecke
         */
        public String toString() {
            return _name;
        }
    }


    /** Signalisiert fehlerhafte Zust�nde in empfangenen Telegrammen */
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

        /** Wrapper-Objekt zum bequemen Zugriff auf die online �nderbaren Parameter dieser Verbindung */
        private final PropertyConsultant _propertyConsultant;

        /** Priorisierte Queue mit den noch zu versendenden Telegrammen */
        private final PriorityChannel _sendChannel;

        /** Anzahl der empfangenen Daten-Telegramme seit des letzten Quittungstelegramm-Versands */
        private int _countReceiptDataTel;

        /** Anzahl des gesendeten Daten-Telegramme seit des letzten Quittungstelegramm-Empfangs */
        private int _countSendDataTel;

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
         * Enth�lt w�hrend einer bestehenden Verbindung das Kommunikationsobjekt mit internem Server,
         * �ber den der Datenaustausch mit dem Client abgewickelt wird; sonst <code>null</code>.
         */
        private SocketChannel _socketChannel;

        /** Portnummer des Servers */
        private int _tlsoipCAcceptPortA;

        /** Zeit [s], nach der ein Keep-Alive-Telegramm an die Gegenstelle versendet werden muss (0=ausgeschaltet f�r Testzwecke, 1...3599) */
        private int _tlsoipCHelloDelay;

        /** Zeit [s], nach der sp�testens ein Keep-Alive-Telegramm der Gegenstelle erwartet wird ( > C_HelloDelay der Gegenstelle), (0=ausgeschaltet f�r Testzwecke, 1...3600) */
        private int _tlsoipCHelloTimeout;

        /** Anzahl empfangener/gesendeter Telegramme, nach der sp�testens ein Quittungstelegramm versendet werden muss/erwartet wird (1..255) */
        private int _tlsoipCReceiptCount;

        /** Zeit [s], nach der nach Erhalt eines Telegramms sp�tenstens ein Quittierungstelegramm an die Gegenstelle versendet werden muss (1..59) */
        private int _tlsoipCReceiptDelay;

        /** Zeit [s], nach der sp�testens ein Quittungstelegramm von der Gegenstelle erwartet wird (> C_ReceiptDelay der Gegenstelle) (1..60) */
        private int _tlsoipCReceiptTimeout;

        /** Zeit [s], nach der bei Nichtbestehen einer Verbindung sp�testens ein neuer Verbindungsaufbau initiiert werden muss  (0=sofort, 1...3600). */
        private int _tlsoipCReconnectDelay;

        /**
         * Wenn <code>true</code>, dann wartet das Protokoll nach dem Aufbau der TCP-Verbindung auf
         * den Empfang eines initialenTelegramms, bevor eine Verbindung als "lebt" gemeldet wird.
         */
        private boolean _tlsoipWaitForInitialReceive;

        /** Aktuell asynchron zu sendendes Telegramm */
        byte[] _packetOnTheAir = null;

        /** Enth�lt die online �nderbaren Parameter f�r diese Verbindung */
        private Properties _properties = null;

        /** IP-Adresse des Servers */
        private String _tlsoipCServerAdrA = null;

        /** Internes Flag f�r die Verwaltung des Initialen Telegramms. */
        private boolean _tcpConnectedWaitingForFirstReceive = false;

        /** Timerobjekt mit dem zuk�nftige Aktionen geplant und ausgef�hrt werden */
        private final Timer _timer = new Timer(true);

        /** Sendepuffer f�r versendete Telegramme */
        private final ByteBuffer _sendBuffer = ByteBuffer.allocateDirect(28 + getMaximumDataSize());

        /** Empfangspuffer f�r empfangene Telegramme */
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
                _sendChannel.put(new Client.PriorizedByteArray(null, 0));
            }
            catch (InterruptedException e) {
                e.printStackTrace();

                throw new RuntimeException(e);
            }

            notifyWorker(Client.ActionType.ABORT_CALLED);
        }

        /**
         * Schlie�t den Kommunikationskanal zum Server und plant den erneuten Aufbau der Kommunikationsverbindung
         * nach der durch den Parameter "tlsoip.C_ReconnectDelay" parameterierbaren Wartezeit ein.
         */
        private void closeChannel() {
            closeChannel(_tlsoipCReconnectDelay);
        }

        /**
         * Schlie�t den Kommunikationskanal zum Server und plant den erneuten Aufbau der Kommunikationsverbindung nach einer vorgebbaren Wartezeit ein.
         *
         * @param reconnectDelay Wartezeit nach der die Verbindung wieder aufgebaut werden soll.
         */
        private void closeChannel(int reconnectDelay) {
            synchronized (_linkLock) {

                // DIFFCLIENT ANFANG
                _tcpConnectedWaitingForFirstReceive = false;

                // DIFFCLIENT ENDE
                if (_socketChannel != null) {
                    try {
                        _socketChannel.close();
                    }
                    catch (IOException e) {
                        DEBUG.warning(String.format("Fehler beim Schlie�en des SocketChannels: %s", e));
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
                    DEBUG.fine(String.format("N�chster Verbundungsversuch in %d Sekunden; %s", reconnectDelay, this));
                    scheduleActionTimer(Client.ActionType.RETRY_CONNECT, reconnectDelay);
                    notifyEvent(DataLinkLayerEvent.Type.DISCONNECTED, null);
                } else if (_linkState == LinkState.CONNECTING) {
                    DEBUG.fine(String.format("N�chster Verbundungsversuch in %d Sekunden; %s", reconnectDelay, this));
                    scheduleActionTimer(Client.ActionType.RETRY_CONNECT, reconnectDelay);
                } else {
                    DEBUG.error(String.format("closeChannel: Unm�glicher Zustand: Fehler ohne bestehende Verbindung; %s", this));
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

            notifyWorker(Client.ActionType.CONNECT_CALLED);
        }

        /**
         * Initialisiert den Kommunikationskanal f�r den Datenaustausch.
         *
         * @param selector Selektor des Protokoll-Threads zum asynchronen Zugriff auf die Kommunikationskan�le.
         */
        private void connectSocketChannel(Selector selector) {
            synchronized (_linkLock) {
                if (_linkState == LinkState.CONNECTING) {
                    try {
                        final boolean connectFinished;

                        if (_socketChannel == null) {
                            _readBuffer.clear();
                            _sendBuffer.clear().flip();

                            // noinspection AssignmentToNull
                            _packetOnTheAir = null;

                            // Verbindungs- und Kommunikationsparameter einlesen
                            _tlsoipCServerAdrA           = _propertyConsultant.getProperty("tlsoip.C_ServerAdrA");
                            _tlsoipCAcceptPortA          = _propertyConsultant.getIntProperty("tlsoip.C_AcceptPortA");
                            _tlsoipCHelloDelay           = _propertyConsultant.getIntProperty("tlsoip.C_HelloDelay");
                            _tlsoipCHelloTimeout         = _propertyConsultant.getIntProperty("tlsoip.C_HelloTimeout");
                            _tlsoipCReceiptCount         = _propertyConsultant.getIntProperty("tlsoip.C_ReceiptCount");
                            _tlsoipCReceiptDelay         = _propertyConsultant.getIntProperty("tlsoip.C_ReceiptDelay");
                            _tlsoipCReceiptTimeout       = _propertyConsultant.getIntProperty("tlsoip.C_ReceiptTimeout");
                            _tlsoipCReconnectDelay       = _propertyConsultant.getIntProperty("tlsoip.C_ReconnectDelay");
                            _tlsoipWaitForInitialReceive = _propertyConsultant.getBooleanProperty("tlsoip.waitForInitialReceive");
                            _socketChannel               = SocketChannel.open();
                            _socketChannel.configureBlocking(false);
                            DEBUG.info(String.format("Verbindungsversuch zu %s:%d wird gestartet; %s", _tlsoipCServerAdrA, _tlsoipCAcceptPortA, this));
                            connectFinished = _socketChannel.connect(new InetSocketAddress(_tlsoipCServerAdrA, _tlsoipCAcceptPortA));
                        } else {
                            connectFinished = _socketChannel.finishConnect();
                        }

                        if (connectFinished) {
                            DEBUG.info(String.format("Verbindungsaufbau abgeschlossen; %s", this));
                            _linkState                = LinkState.CONNECTED;
                            _lastReceiptTimeAllTel    = System.currentTimeMillis();
                            _lastSendTimeAllTel       = System.currentTimeMillis();
                            _lastReceiptTimeDataTel   = System.currentTimeMillis();
                            _lastSendTimeDataTel      = System.currentTimeMillis();
                            _lastReceiptSeqNumDataTel = 0xffff;  // Als n�chstes wird die 0 erwartet
                            _lastSendSeqNumDataTel    = 0xffff;  // Als n�chstes wird die 0 erwartet
                            _countReceiptDataTel      = 0;
                            _countSendDataTel         = 0;
                            _sendKeepAliveTel         = true;
                            _sendQuittTel             = false;

                            // Kanal zum Lesen registrieren
                            _socketChannel.register(selector, SelectionKey.OP_READ, this);

                            if (!_tlsoipWaitForInitialReceive) {
                                _tcpConnectedWaitingForFirstReceive = false;
                                _linkState                          = LinkState.CONNECTED;
                                notifyEvent(DataLinkLayerEvent.Type.CONNECTED, null);
                            } else {
                                _tcpConnectedWaitingForFirstReceive = true;
                            }

                            scheduleActionTimer(ActionType.KEEPALIVE_TIMER_RECEIVE, _tlsoipCHelloTimeout);
                            scheduleActionTimer(ActionType.KEEPALIVE_TIMER_SEND, _tlsoipCHelloDelay);
                        } else {
                            DEBUG.info(String.format("Verbindungsaufbau ist noch nicht abgeschlossen und wird asynchron durchgef�hrt; %s", this));
                            _socketChannel.register(selector, SelectionKey.OP_CONNECT, this);
                        }
                    }
                    catch (Exception e) {

                        // Um einzelne Verbindungen zu Testzwecken auszuschalten, muss die IP-Host-Adresse (tlsoip.C_ServerAdrA) auf "" gesetzt werden,
                        if ((_tlsoipCServerAdrA == null) || _tlsoipCServerAdrA.equals("")) {
                            DEBUG.warning(String.format("Verbindungsversuch wird dauerhaft deaktiviert, da Parameter 'tlsoip.C_ServerAdrA' nicht gesetzt ist: %s", this));
                            closeChannel(Integer.MAX_VALUE);  // Reconnect auf ewig verz�gern ...
                        } else {
                            DEBUG.warning(String.format("Verbindung wird deaktiviert: %s. N�chster Verbindungsaufbau in %s Sekunden.", this, _tlsoipCReconnectDelay));
                            closeChannel();
                        }
                    }
                }
            }
        }

        //~ GET METHODEN ======================================================

        /** @return Das zu dieser Verbindung geh�rende Protokollmodul */
        public DataLinkLayer getDataLinkLayer() {
            return Client.this;
        }

        /**
         * Ermittelt auf Basis der aktullen SeqNum des Daten-Telegramm die n�chste folgende Nummer.
         * @param num Aktuelle SeqNum des Daten Telegramms
         * @return N�chste Nummer (num = num+1), wobei bei num = 65535 die n�chste Nummer 0 ist.
         */
        public int getNextSeqNum(int num) {
            if (num == 65535) {
                return 0;
            } else {
                return num + 1;
            }
        }

        /**
         * Liefert einen Parameterwert zur�ck.
         *
         * @param name Name des gew�nschten Parameterwerts.
         *
         * @return Gew�nschter Parameterwert.
         */
        public String getProperty(String name) {
            synchronized (_linkLock) {
                String value = (_properties == null) ? null : _properties.getProperty(name);

                return (value == null) ? Client.this.getProperty(name) : value;
            }
        }

        //~ METHODEN ==========================================================

        /**
         * F�hrt eine Aktion f�r dieses Verbindungsobjekt aus. Diese Methode wird vom Protokoll-Thread zur Verarbeitung einer Aktion aufgerufen.
         *
         * @param action   Auszuf�hrende Aktion
         * @param selector Selektor des Protokoll-Threads zum asynchronen Zugriff auf die Kommunikationskan�le.
         */
        public void handleAction(Client.ActionType action, Selector selector) {
            DEBUG.finer(String.format("handleAction(%s): %s", action, this));

            if ((action == Client.ActionType.CONNECT_CALLED) || (action == Client.ActionType.RETRY_CONNECT)) {
                DEBUG.fine("Verbindung aufbauen");
                connectSocketChannel(selector);
            } else if (action == Client.ActionType.SEND_CALLED) {

                // handleAsyncSend() wird auf jeden Fall aufgerufen (s.u.)
            } else if (action == Client.ActionType.RELOAD_CALLED) {
                closeChannel(2);
            } else if (action == Client.ActionType.ABORT_CALLED) {

                // nichts zu tun
            } else if (action == Client.ActionType.SHUTDOWN_CALLED) {

                // nichts zu tun
            } else if (action == Client.ActionType.KEEPALIVE_TIMER_RECEIVE) {
                synchronized (_linkLock) {
                    if (_linkState == LinkState.CONNECTED) {

                        // Pr�fen, ob KeepAlive-�berwachung f�r den Empfang nicht ausgeschaltet ist (zu Testzwecken)
                        if (_tlsoipCHelloTimeout == 0) {
                            DEBUG.info("KeepAlive-�berwachung f�r den Empfang ist ausgeschaltet (tlsoip.C_HelloTimeout = 0)");
                        } else {

                            // Zeitraum [s], seit letztes Telegramm empfangen wurde
                            int durationSinceLastReceiptAllTel = (int) ((System.currentTimeMillis() - _lastReceiptTimeAllTel) / 1000L);

                            // H�tte KeepAlive-Telegramm bereits empfangen werden m�ssen?
                            if (_tlsoipCHelloTimeout < durationSinceLastReceiptAllTel) {

                                // Ja. Verbindung abbrechen und nach Wartezeit neu aufbauen.
                                DEBUG.warning(String.format("Verbindung wird wegen Zeit�berschreibung initialisiert (Empfang letztes KeepAlive vor %ds > Parameter tlsoip.C_HelloTimeout %d", durationSinceLastReceiptAllTel, _tlsoipCHelloTimeout));
                                closeChannel();
                            } else {

                                // Nein, aber bald. Timer neu setzen
                                scheduleActionTimer(ActionType.KEEPALIVE_TIMER_RECEIVE, _tlsoipCHelloTimeout - durationSinceLastReceiptAllTel);
                            }
                        }
                    }

                    return;
                }
            } else if (action == Client.ActionType.KEEPALIVE_TIMER_SEND) {
                synchronized (_linkLock) {
                    if (_linkState == LinkState.CONNECTED) {

                        // Pr�fen, ob KeepAlive-�berwachung f�r das Senden nicht ausgeschaltet ist (zu Testzwecken)
                        if (_tlsoipCHelloDelay == 0) {
                            DEBUG.info("KeepAlive-�berwachung f�r das Senden ist ausgeschaltet (tlsoip.C_HelloDelay = 0)");
                            _sendKeepAliveTel = false;
                        } else {

                            // Zeitraum [s], seit letztes Telegramm gesendet wurde
                            int durationSinceLastSendAllTel = (int) ((System.currentTimeMillis() - _lastSendTimeAllTel) / 1000L);

                            // Muss KeepAlive-Telegramm versendet werden ?
                            if (_tlsoipCHelloDelay < durationSinceLastSendAllTel) {

                                // Ja (erfolgt in handleAsyncSend()). Zus�tzlich Timer f�r n�chsten sp�testen Versand setzten.
                                _sendKeepAliveTel = true;
                                scheduleActionTimer(Client.ActionType.KEEPALIVE_TIMER_SEND, _tlsoipCHelloDelay);
                            } else {

                                // Nein, aber bald. Timer neu setzen.
                                scheduleActionTimer(ActionType.KEEPALIVE_TIMER_RECEIVE, _tlsoipCHelloDelay - durationSinceLastSendAllTel);
                            }
                        }
                    }
                }
            } else if (action == Client.ActionType.QUITT_TIMER_RECEIVE) {
                synchronized (_linkLock) {
                    if (_linkState == LinkState.CONNECTED) {

                        // Pr�fen, ob Quittierungs-�berwachung f�r den Empfang nicht ausgeschaltet ist (zu Testzwecken)
                        if (_tlsoipCReceiptTimeout == 0) {
                            DEBUG.info("Quittierungs-�berwachung f�r versandte Daten-Telegramme ist ausgeschaltet (tlsoip.C_ReceiptTimeout = 0)");
                        } else {

                            // Zeitraum [s], seit letztes Daten-Telegramm verschickt wurde
                            int durationSinceLastSendDataTel = (int) ((System.currentTimeMillis() - _lastSendTimeDataTel) / 1000L);

                            // H�tte Quittierungs-Telegramm bereits empfangen werden m�ssen?
                            if (_tlsoipCReceiptTimeout < durationSinceLastSendDataTel) {

                                // Ja. Verbindung abbrechen und nach Wartezeit neu aufbauen.
                                DEBUG.warning(String.format("Verbindung wird wegen Zeit�berschreibung initialisiert (Empfang letztes Quittungs-Telegramm vor %ds > Parameter tlsoip.C_ReceiptTimeout %d", durationSinceLastSendDataTel, _tlsoipCReceiptTimeout));
                                closeChannel();
                            } else {

                                // Nein, aber bald. Timer neu setzen
                                scheduleActionTimer(ActionType.QUITT_TIMER_RECEIVE, _tlsoipCReceiptTimeout - durationSinceLastSendDataTel);
                            }
                        }
                    }

                    return;
                }
            } else if (action == Client.ActionType.QUITT_TIMER_SEND) {
                synchronized (_linkLock) {
                    if (_linkState == LinkState.CONNECTED) {

                        // Pr�fen, ob Quittungs-�berwachung f�r das Senden nicht ausgeschaltet ist (zu Testzwecken)
                        if (_tlsoipCReceiptDelay == 0) {
                            DEBUG.info("Quittierungs-�berwachung f�r empfangene Daten-Telegramme ist ausgeschaltet (tlsoip.C_ReceiptDelay = 0)");
                            _sendQuittTel = false;
                        } else {

                            // Zeitraum [s], seit letztes Daten-Telegramm empfangen wurde
                            int durationSinceLastReceiptDataTel = (int) ((System.currentTimeMillis() - _lastReceiptTimeDataTel) / 1000L);

                            // Muss Quittierungs-Telegramm versendet werden ?
                            if ((_tlsoipCReceiptDelay < durationSinceLastReceiptDataTel) || (_countReceiptDataTel >= _tlsoipCReceiptCount)) {

                                // Ja (erfolgt in handleAsyncSend()). Zus�tzlich Timer f�r n�chsten sp�testen Versand setzten.
                                _sendQuittTel = true;
                                scheduleActionTimer(Client.ActionType.QUITT_TIMER_SEND, _tlsoipCReceiptDelay);
                            } else {

                                // Nein, aber bald. Timer neu setzen.
                                scheduleActionTimer(Client.ActionType.QUITT_TIMER_SEND, _tlsoipCReceiptDelay - durationSinceLastReceiptDataTel);
                            }
                        }
                    }
                }
            } else {
                DEBUG.warning(String.format("Unbekannter ActionType: %s", action));
            }

            // In jedem Fall werden eventuell anliegende Sendeauftr�ge (KeepAlive, Quittungen, Daten) bearbeitet
            handleAsyncSend(selector);
        }

        /**
         * F�hrt den asynchronen Versand von noch zu versendenden Telegrammen aus.
         *
         * @param selector Selektor des Protokoll-Threads zum asynchronen Zugriff auf die Kommunikationskan�le.
         */
        private void handleAsyncSend(Selector selector) {
            if (_socketChannel == null) {
                return;
            }

            try {
                do {
                    if (!_sendBuffer.hasRemaining()) {
                        if (_sendQuittTel) {

                            // Quittungstelegramm versenden
                            DEBUG.fine(String.format("Quittungs-Telegramm wird versendet f�r SeqNum [%d]", _lastReceiptSeqNumDataTel));
                            _countReceiptDataTel    = 0;
                            _sendKeepAliveTel       = false;
                            _sendQuittTel           = false;
                            _lastReceiptTimeDataTel = System.currentTimeMillis();  // Zeit�berwachung f�r Quittung zur�cksetzen
                            _lastSendTimeAllTel     = System.currentTimeMillis();

                            TLSoIPFrame tlsoIPFrame = new TLSoIPFrame(_lastReceiptSeqNumDataTel, TLSoIPFrame.TELTYPE_QUITT, null);

                            _sendBuffer.clear();
                            _sendBuffer.put(tlsoIPFrame.getTel());
                            _sendBuffer.flip();
                        }

                        if (_sendKeepAliveTel) {

                            // KeepAlive-Telegramm versenden
                            DEBUG.fine("KeepAlive-Telegramm wird versendet.");
                            _sendKeepAliveTel   = false;
                            _lastSendTimeAllTel = System.currentTimeMillis();

                            TLSoIPFrame tlsoIPFrame = new TLSoIPFrame(0, TLSoIPFrame.TELTYPE_KEEPALIVE, null);

                            _sendBuffer.clear();
                            _sendBuffer.put(tlsoIPFrame.getTel());
                            _sendBuffer.flip();
                        } else {

                            // Daten-Telegramm versenden
                            // Pr�fen, ob �berhaupt noch weitere Datentelegramme versand werden d�rfen
                            // Wenn tlsoip.C_ReceiptCount = 0, dann ist Quittierungs�berwachung beim Versand ausgeschaltet.
                            if ((_countSendDataTel >= _tlsoipCReceiptCount) && (_tlsoipCReceiptCount > 0)) {
                                DEBUG.warning(String.format("Datenversand wegen fehlender Quittung angehalten. Versandte Datentelegramme %d >= Parameter tlsoip.C_ReceiptCount %d", _countSendDataTel, _tlsoipCReceiptCount));
                            } else {

                                // Pr�fen, ob Daten zum Versand anliegen
                                PriorizedByteArray priorizedByteArray = (Client.PriorizedByteArray) _sendChannel.poll(0L);

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
                                    }
                                }
                            }
                        }
                    }

                    if (_sendBuffer.hasRemaining()) {
                        DEBUG.finest(String.format("Sendeversuch f�r verbleibende %d Bytes", _sendBuffer.remaining()));

                        int sent = _socketChannel.write(_sendBuffer);

                        DEBUG.finest(String.format("erfolgreich gesendete Bytes %d", sent));
                    }

                    if (_sendBuffer.hasRemaining()) {
                        DEBUG.finer("Versand wird sobald m�glich fortgesetzt");
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
         * Verarbeitet asynchrone Kommunikationsoperationen anhand der vom Selektor des Protokoll-Threads gelieferten M�glichkeiten
         *
         * @param selectionKey Vom Selektor des Protokoll-Threads gelieferte Kommunikationsm�glichkeiten
         * @param selector     Selektor des Protokoll-Threads zum asynchronen Zugriff auf die Kommunikationskan�le.
         */
        public void handleSelection(SelectionKey selectionKey, Selector selector) {
            DEBUG.finer(String.format("handleSelection(%d/%d): %s", selectionKey.readyOps(), selectionKey.interestOps(), this));

            if (selectionKey.isConnectable()) {
                DEBUG.fine("Verbindungsaufbau abschlie�en");
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
                        DEBUG.info(String.format("Verbindung wurde von der Gegenseite terminiert; %s", this));
                        closeChannel();
                    } else {
                        DEBUG.finest(String.format("Anzahl gelesener Bytes: %d", count));
                        _readBuffer.flip();

                        int remaining;

                        // DEBUG.finest(String.format("_readBuffer nach dem Lesen: (%s), Daten: (%s)", _readBuffer, HexDumper.toString(_readBuffer.array(), 0, _readBuffer.position())));
                        DEBUG.finest(String.format("_readBuffer vor dem Lesen: (%s)", _readBuffer));

                        while ((remaining = _readBuffer.remaining()) >= 10) {

                            // Zeit f�r letztes empfangenes Telegramm setzten
                            _lastReceiptTimeAllTel = System.currentTimeMillis();

                            // Header einlesen
                            TLSoIPFrame tlSoIPFrame = new TLSoIPFrame(_readBuffer);

                            try {

                                // Test, ob erstes Byte 0x68 enth�lt, sonst wurde kein TLS over IP-Telegramm empfangen
                                if (!tlSoIPFrame.isTLSoIPFrame()) {
                                    throw new Client.IllegalTelegramException(String.format("Telegramm ist kein TLSoIP-Telegramm, Byte Sync ist nicht (68h bzw. 104dez) sondern (%ddez)", tlSoIPFrame.getSync()));
                                }

                                // Quittungs-Telegramm pr�fen und entsprechende Quittungs-Zust�nde setzten
                                if (tlSoIPFrame.isQuittTel()) {

                                    // Zeitpunkt der letzten Quittierung setzen (wird einfach als letztes gesendetes Datentelegramm gewertet f�r die Zeit�berwachung)
                                    _lastSendTimeDataTel = System.currentTimeMillis();

                                    // Pr�fen, ob SeqNummer korrekt ist
                                    if (tlSoIPFrame.getSeqNum() == _lastSendSeqNumDataTel) {

                                        // Anzahl der nicht quittierten gesendeten Telegramm zur�cksetzten.
                                        _countSendDataTel = 0;
                                    }
                                }

                                // KeepAlive-Telegramm pr�fen und entsprechende KeepAlive-Zust�nde setzten
                                if (tlSoIPFrame.isKeepAliveTel()) {

                                    // Nichts tun: Beim Verbindungsaufbau werden die KeepAliveTimer gesetzt und bei Zeiten aufgerufen.
                                    // Beim Aufruf wird entschieden, ob KeepAlive-Empfangs-Telegramm notwendig gewesen w�re und
                                    // ggf. der n�chste sp�teste Zeitpunkt registriert.
                                    DEBUG.finer("KeepAlive-Telegramm empfangen");
                                }

                                // Datentelegramm empfangen
                                if (tlSoIPFrame.isDataTel()) {

                                    // Test, ob maximale Telegramml�nge nicht �berschritten wurde (> 253 Byte)
                                    if (tlSoIPFrame.getLen() > getMaximumDataSize()) {
                                        throw new Client.IllegalTelegramException(String.format("Empfangene TLSoIP-Datenl�nge ist zu gro� (maximal %d Byte) : %s", tlSoIPFrame.getLen(), remaining));
                                    }

                                    // Falls genug Daten zum Lesen vorhanden sind
                                    if ((remaining = _readBuffer.remaining()) >= tlSoIPFrame.getLen()) {
                                        tlSoIPFrame.addData(_readBuffer);
                                        DEBUG.finer(String.format("TLS Datentelegramm empfangen: %s", tlSoIPFrame.toString()));

                                        // Empfangene Sequenznummer pr�fen
                                        if (!(tlSoIPFrame.getSeqNum() == getNextSeqNum(_lastReceiptSeqNumDataTel))) {
                                            int lastReceiptSeqNumDataTelOnlyForDebug = _lastReceiptSeqNumDataTel;

                                            _lastReceiptSeqNumDataTel = tlSoIPFrame.getSeqNum();

                                            throw new Client.IllegalTelegramException(String.format("Empfangene SeqNum (%d) entspricht nicht der erwarteten SeqNum (%d)!", tlSoIPFrame.getSeqNum(), getNextSeqNum(lastReceiptSeqNumDataTelOnlyForDebug)));
                                        } else {

                                            // Neue Nummer als letzte Nummer merken
                                            _lastReceiptSeqNumDataTel = tlSoIPFrame.getSeqNum();

                                            // Zeit f�r letztes empfangenes Daten-Telegramm setzten (erst hier, weil jetzt als g�ltig akzeptiert)
                                            _lastReceiptTimeDataTel = System.currentTimeMillis();

                                            // Anzahl der empfangenen Datentelegramme seit der letzten Quittung pr�fen und ggf. Quittung versenden
                                            _countReceiptDataTel++;

                                            if (_countReceiptDataTel == _tlsoipCReceiptCount) {

                                                // Wenn Anzahl der Datentelegramme, nach der Quittiert werden muss, erreicht ist, sofort Quittieren...
                                                scheduleActionTimer(ActionType.QUITT_TIMER_SEND, 1);
                                            } else {

                                                // ...sonst sp�tenstens nach Ablauf der EmpfangDelays Quittieren
                                                scheduleActionTimer(ActionType.QUITT_TIMER_SEND, _tlsoipCReceiptDelay);
                                            }

                                            // Datenempfang melden...
                                            notifyEvent(DataLinkLayerEvent.Type.DATA_RECEIVED, tlSoIPFrame.getData());
                                        }
                                    } else {
                                        throw new Client.IllegalTelegramException(String.format("L�nge des Datenblocks laut Header (%d) kleiner als vorhandene Anzahl der Nutzdaten (%d)", tlSoIPFrame.getLen(), remaining));
                                    }
                                }
                            }
                            catch (Client.IllegalTelegramException e) {
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

        /**
         * Sendet eine Aktion f�r dieses Verbindungsobjekt zur Ausf�hrung an den Protokoll-Thread.
         *
         * @param action Auszuf�hrende Aktion
         */
        private void notifyWorker(Client.ActionType action) {
            _worker.notify(this, action);
        }

        /** Initiiert den Abbruch und erneuten Verbindungsaufbau einer bestehenden Verbindung mit evtl. ge�nderten Parametern */
        public void reload() {
            DEBUG.fine(String.format("reload %s", this));

            synchronized (_protocolLock) {
                if ((_protocolState == ProtocolState.STARTED) || (_protocolState == ProtocolState.STARTING)) {
                    notifyWorker(ActionType.RELOAD_CALLED);
                }
            }
        }

        /**
         * Plant eine Aktion mit Hilfe eines Timer-Objekts zur sp�teren Ausf�hrung ein.
         *
         * @param actionType   Auszuf�hrende Aktion
         * @param delaySeconds Verz�gerungszeit in Sekunden nach der die Aktion ausgef�hrt werden soll.
         */
        private void scheduleActionTimer(final Client.ActionType actionType, int delaySeconds) {
            final TimerTask timerTask = new TimerTask() {
                public void run() {
                    notifyWorker(actionType);
                }
            };

            _timer.schedule(timerTask, delaySeconds * 1000L);
        }

        /**
         * Initiiert den Versand eines Telegramms.
         *
         * @param bytes    Bytearray mit den Bytes des zu sendenden Telegramms.
         * @param priority Priorit�t des zu sendenden Telegramms.
         *
         * @throws InterruptedException Wenn der aktuelle Thread unterbrochen wurde.
         */
        public void send(byte[] bytes, int priority) throws InterruptedException {
            DEBUG.finer(String.format("Telegramm soll gesendet werden:%nPriorit�t: %d%nDaten: %s", priority, HexDumper.toString(bytes)));

            synchronized (_linkLock) {
                if (_linkState != LinkState.CONNECTED) {
                    throw new IllegalStateException(String.format("Telegramm kann in diesem Verbindungszustand nicht versendet werden: %s", _linkState));
                }
            }

            DEBUG.finest(String.format("Telegramm wird zum Versand gepuffert, Priorit�t: %d", priority));
            _sendChannel.put(new Client.PriorizedByteArray(bytes, priority));
            notifyWorker(Client.ActionType.SEND_CALLED);
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

        /** Initiiert das Schlie�en der bestehenden Verbindung dieses Verbindungsobjekts */
        public void shutdown() {
            DEBUG.fine(String.format("shutdown %s", this));

            synchronized (_linkLock) {
                if ((_linkState == LinkState.DISCONNECTED) || (_linkState == LinkState.DISCONNECTING)) {
                    return;
                }

                _linkState = LinkState.DISCONNECTING;
            }

            try {
                _sendChannel.put(new Client.PriorizedByteArray(null, 2));
            }
            catch (InterruptedException e) {
                e.printStackTrace();

                throw new RuntimeException(e);
            }

            notifyWorker(Client.ActionType.SHUTDOWN_CALLED);
        }
    }


    /** Dient zur Speicherung eines zu versendenden Telegramms mit einer zugeordneten Priorit�t */
    private static class PriorizedByteArray implements PriorizedObject {

        /**
         * Array mit den einzelnen Bytes des zu versendenden Telegramms. Der Wert <code>null</code> signalisiert, dass keine weiteren Telegramme mehr versendet werden
         * sollen.
         */
        private final byte[] _bytes;

        /** Priorit�t des zu versendenden Telegramms. Kleinere Werte haben h�here Priorit�t. */
        private final int _priority;

        //~ KONSTRUKTOREN  (und vom Konstruktor verwendete Methoden) ==========

        /**
         * Erzeugt ein neues Objekt mit den angegebenen Eigenschaften.
         *
         * @param bytes    Array mit den einzelnen Bytes des zu versendenden Telegramms. Der Wert <code>null</code> signalisiert, dass keine weiteren Telegramme mehr
         *                 versendet werden sollen.
         * @param priority Priorit�t des zu versendenden Telegramms. Kleinere Werte haben h�here Priorit�t.
         */
        public PriorizedByteArray(byte[] bytes, int priority) {
            _bytes    = bytes;
            _priority = priority;
        }

        //~ GET METHODEN ======================================================

        /**
         * @return Array mit den einzelnen Bytes des zu versendenden Telegramms. Der Wert <code>null</code> signalisiert, dass keine weiteren Telegramme mehr
         *         versendet werden sollen.
         */
        public byte[] getBytes() {
            return _bytes;
        }

        /** @return Priorit�t des zu versendenden Telegramms. Kleinere Werte haben h�here Priorit�t. */
        public int getPriorityClass() {
            return _priority;
        }
    }


    /** Klasse die das Runnable-Interface implementiert, vom Protokollthread ausgef�hrt wird und den Protokollablauf steuert */
    private class Worker implements Runnable {

        /**
         * Selektor-Objekt, mit dessen Hilfe alle Kommunikationsoperationen (Verbindungsaufbau, Versand und
         * Empfang von Daten) ohne zus�tzliche Threads asynchron ausgef�hrt werden.
         */
        private final Selector _selector;

        /** Queue zur �bermittlung von Aktionen an den Protokoll-Thread */
        private final UnboundedQueue<WorkAction> _workQueue;

        //~ KONSTRUKTOREN  (und vom Konstruktor verwendete Methoden) ==========

        /**
         * Konstruktor initialisiert den Selektor und die Queue zur �bermittlung von Aktionen.
         *
         * @throws IOException wenn eine nicht abgefangene Ausnahme auftritt.
         */
        public Worker() throws IOException {
            _selector  = Selector.open();
            _workQueue = new UnboundedQueue<WorkAction>();
        }

        //~ METHODEN ==========================================================

        /**
         * Kann von einem beliebigen Thread aufgerufen werden, um dem Protokoll-Thread zu signalisieren,
         * dass eine bestimmte Aktion ausgef�hrt werden soll.
         *
         * @param link   Verbindung, auf die sich die Aktion bezieht.
         * @param action Durchzuf�hrende Aktion
         */
        public void notify(Client.Link link, Client.ActionType action) {
            _workQueue.put(new WorkAction(link, action));
            DEBUG.finer("Aufruf von _selector.wakeup()");
            _selector.wakeup();
        }

        /**
         * Methode, die beim Start des Protokoll-Threads aufgerufen wird und die asynchrone Protokollsteuerung
         * implementiert.
         */
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

            // Hier wird 30 Sekunden gewartet, um der TLS-OSI-7 gen�gend Zeit zur Initialisierung zu geben
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

                        // Instabile Zwischenzust�nde werden innerhalb des synchronized Block �berpr�ft,
                        // da eine Zustands�nderung notwendig sein k�nnte
                        if (_protocolState == ProtocolState.STARTING) {
                            _protocolState = ProtocolState.STARTED;
                        } else if (_protocolState == ProtocolState.STOPPING) {}

                        state = _protocolState;
                    }

                    if (state == ProtocolState.STARTING) {

                        // wird beim n�chsten Schleifendurchlauf im synchronized Block (oben) behandelt
                    } else if ((state == ProtocolState.STARTED) || (state == ProtocolState.STOPPING)) {
                        DEBUG.finest(String.format("Protokoll arbeitet: %s", this));

                        Client.Worker.WorkAction action;

                        while (null != (action = _workQueue.poll(0))) {
                            action._link.handleAction(action._action, _selector);
                        }

                        try {
                            DEBUG.finest("Aufruf von select()");

                            int count = _selector.select();  // ACHTUNG: Aufruf blockiert, bis Anforderung anliegt.

                            DEBUG.finest(String.format("R�ckgabe von select(): %d", count));

                            Set<SelectionKey> selectedKeys = _selector.selectedKeys();

                            for (Iterator<SelectionKey> iterator = selectedKeys.iterator(); iterator.hasNext(); ) {
                                SelectionKey selectionKey = iterator.next();

                                iterator.remove();

                                Client.Link selectedLink = (Client.Link) selectionKey.attachment();

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
                        DEBUG.error(String.format("Ung�ltiger Zustand: %s; %s", state, this));
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
         * Ausgabe von Informationen f�r dieses Objekt f�r Debug-Zwecke.
         * Das genaue Format ist nicht festgelegt und kann sich �ndern..
         *
         * @return Informationen dieses Objekts f�r Debug-Zwecke
         */
        public String toString() {
            return String.format("Worker f�r %s", Client.this.toString());
        }

        //~ INNERE KLASSEN ====================================================

        /**
         * Hilfsklasse, die zur Speicherung einer Aktion zusammen mit der Verbindung,
         * auf die sich die Aktion bezieht, eingesetzt wird.
         */
        class WorkAction {

            /** Zur Speicherung der Aktion */
            public final Client.ActionType _action;

            /** Zur Speicherung der Verbindung */
            public final Client.Link _link;

            //~ KONSTRUKTOREN  (und vom Konstruktor verwendete Methoden) ======

            /**
             * Erzeugt ein neues Hilfsobjekt f�r eine Aktion, die an einer Verbindung auszuf�hren ist.
             *
             * @param link   Zu speichernde Verbindung
             * @param action Zu speichernde Aktion
             */
            public WorkAction(Client.Link link, Client.ActionType action) {
                _link   = link;
                _action = action;
            }

            //~ METHODEN ======================================================

            /**
             * Ausgabe von Informationen f�r dieses Objekt f�r Debug-Zwecke.
             * Das genaue Format ist nicht festgelegt und kann sich �ndern..
             *
             * @return Informationen dieses Objekts f�r Debug-Zwecke
             */
            public String toString() {
                return String.format("WorkAction(link: %s, action: %s", _link, _action);
            }
        }
    }
}


//~Formatiert mit 'inovat Kodierkonvention' am 09.04.10
