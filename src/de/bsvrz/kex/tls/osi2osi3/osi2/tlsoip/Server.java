/*
 * Copyright (c) 2009 by inovat, innovative systeme - verkehr - tunnel - technik, Dipl.-Ing. H. C. Kni�
 * ALL RIGHTS RESERVED.
 *
 * THIS SOFTWARE IS  PROVIDED  "AS IS"  AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF  MERCHANTABILITY  AND  FITNESS  FOR  A PARTICULAR  PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL inovat OR ITS CONTRIBUTORS BE
 * LIABLE FOR ANY  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES  (INCL., BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES;  LOSS OF USE, DATA, OR  PROFITS;
 * OR BUSINESS INTERRUPTION)  HOWEVER  CAUSED  AND  ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,  OR TORT (INCL.
 * NEGLIGENCE OR OTHERWISE)  ARISING  IN  ANY  WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Contact Information:
 * inovat, Dipl.-Ing. H. C. Kni�
 * K�lner Stra�e 30
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

//~ JDK IMPORTE ===============================================================

import java.io.IOException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
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
 * Klasse, die als OSI-2 Protokollmodul f�r den server-seitigen Teil einer WanCom-Verbindung eingesetzt werden kann. Zur Verwendung dieses Protokollmoduls als
 * Primary ist an dem jeweiligen Anschlu�punkt in der Konfiguration in der Attributgruppe "atg.anschlussPunkt" im Attribut "ProtokollTyp" der Wert
 * "de.bsvrz.kex.tls.osi2osi3.osi2.wancom.Server" einzutragen. Zur Verwendung dieses Protokollmoduls als Secondary ist an dem jeweiligen
 * Anschlu�punkt-Kommunikationspartner in der Konfiguration in der Attributgruppe "atg.anschlussPunktKommunikationsPartner" im Attribut "ProtokollTyp" der Wert
 * "de.bsvrz.kex.tls.osi2osi3.osi2.wancom.Server" einzutragen. Im Parameter "atg.protokollEinstellungenStandard" des Anschlu�punkts werden Defaultswerte f�r
 * alle Verbindungen an diesem Anschlu�punkt eingestellt. Im Parameter "atg.protokollEinstellungenPrimary" bzw. "atg.protokollEinstellungenSecondary" der dem
 * Anschlu�punkt zugeordneten Anschlu�PunktKommunikationsPartner werden individuelle Werte f�r die Verbindung zum jeweiligen Kommunikationspartner eingestellt.
 * Die Parameterdatens�tze k�nnen mehrere Eintr�ge enthalten die jeweils aus einem Namen und einem Wert bestehen. Folgende Tabelle enth�lt die Namen,
 * Defaultwerte und eine Beschreibung der unterst�tzten Eintr�ge: <table cellpadding="2" cellspacing="2" border="1"> <tr> <th> Name </th> <th> Defaultwert </th>
 * <th> Beschreibung </th> </tr> <tr> <td> wancom.port </td> <td> 7100 </td> <td> Lokale TCP-Portnummer auf der Verbindungen entgegengenommen werden. </td>
 * </tr> <tr> <td> wancom.version </td> <td> 35 </td> <td> Im WanCom-Header �bertragene Version des eingesetzten Protokolls. </td> </tr> <tr> <td>
 * wancom.keepAliveTime </td> <td> 20 </td> <td> Zeit in Sekunden zwischen dem Versand von 2 Keep-Alive Telegrammen. </td> </tr> <tr> <td>
 * wancom.keepAliveTimeoutCount </td> <td> 3 </td> <td> Anzahl von in Folge vergangenen keepAliveTime-Intervallen ohne Empfang eines KeepAlive-Telegramms bevor
 * die Verbindung abgebrochen wird. </td> </tr> <tr> <td> wancom.keepAliveType </td> <td> 50 </td> <td> WanCom-Type-Feld in KeepAlive-Telegrammen. </td> </tr>
 * <tr> <td> wancom.tlsType </td> <td> 600 </td> <td> WanCom-Type-Feld in versendeten TLS-Telegrammen. </td> </tr> <tr> <td> wancom.tlsTypeReceive </td> <td>
 * </td> <td> WanCom-Type-Feld in empfangenen TLS-Telegrammen. Dieser Wert muss nur angegeben werden, wenn er sich vom WanCom-Typen zum Versand (wancom.tlsType)
 * unterscheidet. Wenn dieser Wert nicht angegeben wurde, wird der Wert von wancom.tlsType auch zum Empfang verwendet </td> </tr> <tr> <td>
 * wancom.connectRetryDelay </td> <td> 60 </td> <td> Wartezeit in Sekunden, bevor ein fehlgeschlagener Verbindungsversuch wiederholt wird. </td> </tr> <tr> <td>
 * wancom.localAddress </td> <td> </td> <td> Lokale Adresse, die in Wan-Com-Header als Absender eingetragen werden soll. Ein leerer Text, wird automatisch durch
 * die aktuelle lokale Adresse der Wan-Com-Verbindung ersetzt. </td> </tr> </table>
 * <p/>
 *
 * @author inovat, innovative systeme - verkehr - tunnel - technik
 * @author Dipl.-Ing. Hans Christian Kni� (HCK)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class Server extends TLSoIP implements PropertyQueryInterface {

    /** Logger f�r Debugausgaben */
    private static final Debug _debug = Debug.getLogger();

    //~ FELDER ================================================================

    /** Runnable Objekt, das vom Protokollthread ausgef�hrt wird und den Protokollablauf steuert */
    final Server.Worker _worker = new Server.Worker();

    /** Aktueller Zustand des Protokolls */
    private ProtocolState _protocolState = ProtocolState.CREATED;

    /** Monitor Objekt, das zur Synchronisation des Protokoll-Threads und den API-Zugriffen von fremden Threads auf das Protokoll koordiniert */
    private final Object _protocolLock = new Object();

    /** Verbindungen zu Kommunikationspartnern, die durch das Protokoll verwaltet werden */
    private List<Link> _links = new LinkedList<Server.Link>();

    /** Thread des Protokolls */
    private final Thread _workThread;

    //~ KONSTRUKTOREN  (und vom Konstruktor verwendete Methoden) ==============

    /** Default-Konstruktor, mit dem neue WanCom-Server Protokolle instanziiert werden k�nnen */
    public Server() throws IOException {
        _debug.fine("WanComServer ");
        _workThread = new Thread(_worker, "wancom.Server.Worker");
    }

    //~ METHODEN ==============================================================

    /** Bricht die Kommunikation auf allen Verbindungen des Protokolls sofort ab und beendet anschlie�end das Protokoll. */
    public void abort() {
        _debug.fine("abort(): " + this);

        synchronized (_protocolLock) {
            for (Iterator<Server.Link> iterator = _links.iterator(); iterator.hasNext(); ) {
                Server.Link link = iterator.next();

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
        return new Server.Link(remoteAddress);
    }

    //~ GET METHODEN ==========================================================

    /**
     * Bestimmt, ob die Kommunikation dieses Protokolls bereits mit der Methode {@link #start} aktiviert wurde.
     *
     * @return <code>true</code>, wenn die Kommunikation dieses Protokolls bereits aktiviert wurde, sonst <code>false</code>.
     */
    public boolean isStarted() {
        synchronized (_protocolLock) {
            return (_protocolState == ProtocolState.STARTING) || (_protocolState == ProtocolState.STARTED);
        }
    }

    //~ SET METHODEN ==========================================================

    /**
     * Nimmmt die Verbindung zum Datenverteiler entgegen. Diese Methode wird vom OSI-3 Modul nach dem Erzeugen des OSI-2 Moduls durch den jeweiligen Konstruktor
     * aufgerufen. Eine Implementierung eines Protokollmoduls kann sich bei Bedarf die �bergebene Datenverteilerverbindung intern merken, um zu sp�teren
     * Zeitpunkten auf die Datenverteiler-Applikationsfunktionen zuzugreifen.
     *
     * @param connection Verbindung zum Datenverteiler
     */
    public void setDavConnection(ClientDavInterface connection) {

        // _connection = connection;
        return;
    }

    /**
     * Setzt neue Protokollparameter. Alle Verbindungen werden mit den neuen Parametern reinitialisiert.
     *
     * @param properties Neue Protokoll und Verbindungsparameter
     */
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        _debug.fine("Neue Einstellungen f�r: " + toString() + ", properties = " + properties);

        synchronized (_protocolLock) {
            for (Iterator<Server.Link> iterator = _links.iterator(); iterator.hasNext(); ) {
                Server.Link link = iterator.next();

                link.reload();
            }
        }
    }

    //~ METHODEN ==============================================================

    /** Terminiert alle Verbindungen des Protokolls und beendet anschlie�end das Protokoll. */
    public void shutdown() {
        _debug.fine("shutdown): " + this);

        synchronized (_protocolLock) {
            for (Iterator<Server.Link> iterator = _links.iterator(); iterator.hasNext(); ) {
                Server.Link link = iterator.next();

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
        _debug.fine("start(): " + this);

        synchronized (_protocolLock) {
            if (_protocolState != ProtocolState.CREATED) {
                throw new IllegalStateException("Protokoll kann nicht erneut gestartet werden: " + toString());
            }

            int localAddress = getLocalAddress();

            if ((localAddress < 1) || (localAddress > 254)) {
                throw new IllegalStateException("lokale OSI-2 Adresse muss zwischen 1 und 254 liegen, ist: " + localAddress);
            }

            _workThread.setName("wancom.Server.Worker(" + localAddress + ")");
            _workThread.start();
            _protocolState = ProtocolState.STARTING;
            _protocolLock.notifyAll();
        }
    }

    /** @return Gibt Informationen des Protokolls f�r Debugzwecke zur�ck */
    public String toString() {
        return "WAN-Com-Server(" + getLocalAddress() + ", " + _protocolState + ") ";
    }

    //~ INNERE KLASSEN ========================================================

    /** Definiert die Aktionscodes, die von den API-Methoden zur Steuerung des Protokoll-Threads versendet werden */
    public static class ActionType {

        /** Signalisiert dem Protokoll-Thread, das die Kommunikation auf einer Verbindung gestartet werden soll */
        public static final Server.ActionType CONNECT_CALLED = new Server.ActionType("CONNECT_CALLED");

        /** Signalisiert dem Protokoll-Thread, das die Kommunikation auf einer Verbindung geschlossen werden soll */
        public static final Server.ActionType SHUTDOWN_CALLED = new Server.ActionType("SHUTDOWN_CALLED");

        /** Signalisiert dem Protokoll-Thread, das ein Telegramm auf einer Verbindung versendet werden soll */
        public static final Server.ActionType SEND_CALLED = new Server.ActionType("SEND_CALLED");

        /** Signalisiert dem Protokoll-Thread, das ein erneuter Verbindungsversuch durchgef�hrt werden soll */
        public static final Server.ActionType RETRY_CONNECT = new Server.ActionType("RETRY_CONNECT");

        /** Signalisiert dem Protokoll-Thread, das die Kommunikation auf einer Verbindung mit Ber�cksichtigung von evtl. Parameter�nderungen neu aufgebaut werden soll */
        public static final Server.ActionType RELOAD_CALLED = new Server.ActionType("RELOAD_CALLED");

        /** Signalisiert dem Protokoll-Thread, das ein Keep-Alive-Telegramm gesendet werden soll und das der Empfang von Keep-Alive-Telegrammen gepr�ft werden soll */
        public static final Server.ActionType KEEPALIVE_TIMER = new Server.ActionType("KEEPALIVE_TIMER");

        /** Signalisiert dem Protokoll-Thread, das die Kommunikation auf einer Verbindung abgebrochen werden soll */
        public static final Server.ActionType ABORT_CALLED = new Server.ActionType("ABORT_CALLED");

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

        /** @return Name der Aktion f�r Debugzwecke */
        public String toString() {
            return _name;
        }
    }


    /** Signalisiert Fehler in empfangenen Telegrammen */
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

        /** Aktuell asynchron zu sendendes Telegramm */
        byte[] _packetOnTheAir = null;

        /** Enth�lt die online �nderbaren Parameter f�r diese Verbindung */
        private Properties _properties = null;

        /** Wartezeit in Sekunden, die nach einem fehlerbedingten Verbindungsabbruch gewartet wird, bevor die Verbindung neu aufgebaut wird. */
        private int _tlsoipCReconnectDelay = 60;

        /** Timerobjekt mit dem zuk�nftige Aktionen geplant und ausgef�hrt werden */
        private final Timer _timer = new Timer(true);

        /** Sendepuffer f�r versendete Telegramme */
        private final ByteBuffer _sendBuffer = ByteBuffer.allocateDirect(28 + getMaximumDataSize());

        /** Empfangspuffer f�r empfangene Telegramme */
        private final ByteBuffer _readBuffer = ByteBuffer.allocateDirect(2204);

        /** Lokale IP-Adresse, die in versendeten Telegrammen eingetragen wird. */
        private byte[] _wanComIp8 = new byte[8];

        /** Anzahl Intervall, in denen aktuell in Folge kein Keep-Alive-Telegramm empfangen wurde. */
        private int _keepAliveReceiveTimeoutCount;

        /** Zeitpunkt des letzten empfangenen Keep-Alive-Telegramms in Millisekunden */
        private long _lastKeepAliveReceive;

        /** Wrapper-Objekt zum bequemen Zugriff auf die online �nderbaren Parameter dieser Verbindung */
        private final PropertyConsultant _propertyConsultant;

        /** Priorisierte Queue mit den noch zu versendenden Telegrammen */
        private final PriorityChannel _sendChannel;

        /** Flag das signalisiert, dass ein Keep-Alive-Telegramm versendet werden soll */
        private boolean _sendKeepAlive;

        /**
         * Enth�lt w�hrend des Verbindungsaufbau das Kommunikationsobjekt mit internem Serversocket, �ber den die Verbindung des Clients entgegengenommen wird; sonst
         * <code>null</code>.
         */
        private ServerSocketChannel _serverSocketChannel;

        /**
         * Enth�lt w�hrend einer bestehenden Verbindung das Kommunikationsobjekt mit internem Server, �ber den der Datenaustausch mit dem Client abgewickelt wird;
         * sonst <code>null</code>.
         */
        private SocketChannel _socketChannel;

        /** Intervallzeit in Sekunden f�r den Versand und Empfang von Keep-Alive-Telegrammen */
        private int _wanComKeepAliveTimeSeconds;

        /** Anzahl Intervalle ohne Empfang eines Keep-Alive-Telegramms nach der eine bestehende Verbindung abgebrochen und neu aufgebaut wird. */
        private int _wanComKeepAliveTimeoutCount;

        /** WanCom-Typfeld ind Keep-Alive-Telegrammen */
        private int _wanComKeepAliveType;

        /** WanCom-Typ f�r versendete TLS-Telegramme */
        private int _wanComTlsType;

        /** WanCom-Typ f�r empfangene TLS-Telegramme */
        private int _wanComTlsTypeReceive;

        /** Versionsfeld der WanCom-Telegramme */
        private int _wanComVersion;

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
                throw new IllegalArgumentException("OSI-2 Adresse muss zwischen 1 und 254 liegen oder den speziellen Wert 255 (Broadcastaddresse) haben, versorgt ist: " + remoteAddress);
            }

            _sendChannel = new PriorityChannel(3, 2000);
            _readBuffer.order(ByteOrder.LITTLE_ENDIAN);
            _sendBuffer.order(ByteOrder.LITTLE_ENDIAN);
            _linkState = LinkState.DISCONNECTED;

            synchronized (_protocolLock) {
                for (Iterator<Server.Link> iterator = _links.iterator(); iterator.hasNext(); ) {
                    Server.Link link = iterator.next();

                    if (link.getRemoteAddress() == _remoteAddress) {
                        throw new IllegalStateException("Es gibt bereits ein Verbindung mit dieser Secondary-Adresse: " + _remoteAddress);
                    }
                }

                _links.add(this);
            }
        }

        //~ METHODEN ==========================================================

        /** Initiiert den sofortigen Abbruch der bestehenden Verbindung dieses Verbindungsobjekts */
        public void abort() {
            _debug.fine("abort " + this);

            synchronized (_linkLock) {
                if ((_linkState == LinkState.DISCONNECTED) || (_linkState == LinkState.DISCONNECTING)) {
                    return;
                }

                _linkState = LinkState.DISCONNECTING;
            }

            try {
                _sendChannel.put(new Server.PriorizedByteArray(null, 0));
            }
            catch (InterruptedException e) {
                e.printStackTrace();

                throw new RuntimeException(e);
            }

            notifyWorker(Server.ActionType.ABORT_CALLED);
        }

        /**
         * Schlie�t den Kommunikationskanal zum Client und plant den erneuten Aufbau der Kommunikationsverbindung nach der durch den Parameter
         * "wancom.connectRetryDelay" vorgebbaren Wartezeit ein.
         */
        private void closeChannel() {
            closeChannel(_tlsoipCReconnectDelay);
        }

        /**
         * Schlie�t den Kommunikationskanal zum Client und plant den erneuten Aufbau der Kommunikationsverbindung nach einer vorgebbaren Wartezeit ein.
         *
         * @param reconnectDelay Wartezeit nach der die Verbindung wieder aufgebaut werden soll.
         */
        private void closeChannel(int reconnectDelay) {
            synchronized (_linkLock) {
                if (_serverSocketChannel != null) {
                    try {
                        _serverSocketChannel.close();
                    }
                    catch (IOException e) {
                        _debug.warning("Fehler beim Schlie�en des ServerSocketChannels: " + e);
                    }
                    finally {
                        _serverSocketChannel = null;
                    }
                }

                if (_socketChannel != null) {
                    try {
                        _socketChannel.close();
                    }
                    catch (IOException e) {
                        _debug.warning("Fehler beim Schlie�en des SocketChannels: " + e);
                    }
                    finally {
                        _socketChannel = null;
                    }
                }

                if (_linkState == LinkState.DISCONNECTING) {
                    _linkState = LinkState.DISCONNECTED;
                    notifyEvent(DataLinkLayerEvent.Type.DISCONNECTED, null);
                } else if (_linkState == LinkState.CONNECTED) {
                    _linkState = LinkState.CONNECTING;
                    _debug.fine("N�chster Verbundungsversuch in " + reconnectDelay + " Sekunden; " + this);
                    scheduleActionTimer(Server.ActionType.RETRY_CONNECT, reconnectDelay);
                    notifyEvent(DataLinkLayerEvent.Type.DISCONNECTED, null);
                } else if (_linkState == LinkState.CONNECTING) {
                    _debug.fine("N�chster Verbundungsversuch in " + reconnectDelay + " Sekunden; " + this);
                    scheduleActionTimer(Server.ActionType.RETRY_CONNECT, reconnectDelay);
                } else {
                    _debug.error("closeChannel: Unm�glicher Zustand: Fehler ohne bestehende Verbindung; " + this);
                    _linkState = LinkState.DISCONNECTED;
                }
            }
        }

        /** Initiiert den Verbindungsaufbau mit dem Kommunikationspartner dieses Verbindungsobjekts */
        public void connect() {
            _debug.fine("connect " + this);

            synchronized (_protocolLock) {
                synchronized (_linkLock) {
                    if ((_linkState == LinkState.CONNECTED) || (_linkState == LinkState.CONNECTING)) {
                        return;
                    }

                    if (_linkState != LinkState.DISCONNECTED) {
                        throw new IllegalStateException("Verbindung kann in diesem Zustand nicht aufgebaut werden: " + _linkState);
                    }

                    _linkState = LinkState.CONNECTING;
                }

                _protocolLock.notifyAll();
            }

            notifyWorker(Server.ActionType.CONNECT_CALLED);
        }

        /**
         * Asynchroner Verbindungsaufbau. Zum Aufbau der Kommunikationsverbindung wird ein Kommunikationskanal mit einem Serversocket initialisiert, der auf dem
         * gew�nschten TCP-Port Verbindungen entgegennimmt. Nachdem Verbindungsaufbau wird der Serversocket wieder geschlossen und ein neuer Kommunikationskanal f�r
         * den Datenaustausch initialisiert.
         *
         * @param selector Selektor des Protokoll-Threads zum asynchronen Zugriff auf die Kommunikationskan�le.
         */
        private void connectSocketChannel(Selector selector) {
            synchronized (_linkLock) {
                if (_linkState == LinkState.CONNECTING) {
                    try {
                        if (_serverSocketChannel == null) {
                            _keepAliveReceiveTimeoutCount = 0;
                            _readBuffer.clear();
                            _sendBuffer.clear().flip();
                            _packetOnTheAir              = null;
                            _sendKeepAlive               = false;
                            _wanComVersion               = _propertyConsultant.getIntProperty("wancom.version");
                            _wanComKeepAliveTimeSeconds  = _propertyConsultant.getIntProperty("wancom.keepAliveTime");
                            _wanComKeepAliveTimeoutCount = _propertyConsultant.getIntProperty("wancom.keepAliveTimeoutCount");
                            _wanComKeepAliveType         = _propertyConsultant.getIntProperty("wancom.keepAliveType");
                            _wanComTlsType               = _propertyConsultant.getIntProperty("wancom.tlsType");

                            try {
                                _wanComTlsTypeReceive = _propertyConsultant.getIntProperty("wancom.tlsTypeReceive");
                            }
                            catch (Exception e) {
                                _wanComTlsTypeReceive = _wanComTlsType;
                            }

                            _tlsoipCReconnectDelay = _propertyConsultant.getIntProperty("tlsoip.C_ReconnectDelay");
                            _serverSocketChannel   = ServerSocketChannel.open();
                            _serverSocketChannel.configureBlocking(false);

                            final int localPort = _propertyConsultant.getIntProperty("wancom.port");

                            _serverSocketChannel.socket().bind(new InetSocketAddress(localPort));
                            _debug.info("Akzeptiere Verbindungen auf Port " + localPort + "; " + this);
                        }

                        if (_serverSocketChannel != null) {
                            _socketChannel = _serverSocketChannel.accept();

                            if (_socketChannel != null) {
                                final Socket socket = _socketChannel.socket();

                                _socketChannel.configureBlocking(false);
                                _serverSocketChannel.close();
                                _serverSocketChannel = null;

                                byte[]       ip           = _socketChannel.socket().getLocalAddress().getAddress();
                                final String localAddress = _propertyConsultant.getProperty("wancom.localAddress");

                                if ((localAddress != null) &&!localAddress.equals("")) {
                                    ip = InetAddress.getByName(localAddress).getAddress();
                                }

                                System.arraycopy(ip, 0, _wanComIp8, 0, Math.min(ip.length, _wanComIp8.length));
                                _lastKeepAliveReceive = System.currentTimeMillis();
                                _linkState            = LinkState.CONNECTED;
                                _debug.info("Verbindungsaufbau von " + socket.getInetAddress().getHostName() + " akzeptiert; " + this);
                                _socketChannel.register(selector, SelectionKey.OP_READ, this);
                                notifyEvent(DataLinkLayerEvent.Type.CONNECTED, null);
                                _sendKeepAlive = true;
                                scheduleActionTimer(Server.ActionType.KEEPALIVE_TIMER, _wanComKeepAliveTimeSeconds);
                            } else {
                                _debug.info("Verbindungsaufbau ist noch nicht abgeschlossen und wird asynchron durchgef�hrt; " + this);
                                _serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, this);
                            }
                        }
                    }
                    catch (Exception e) {
                        _debug.warning("Verbindungsversuch hat nicht funktioniert; " + this, e);
                        closeChannel();
                    }
                }
            }
        }

        //~ GET METHODEN ======================================================

        /** @return Das zu dieser Verbindung geh�rende Protokollmodul */
        public DataLinkLayer getDataLinkLayer() {
            return Server.this;
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

                return (value == null) ? Server.this.getProperty(name) : value;
            }
        }

        //~ METHODEN ==========================================================

        /**
         * F�hrt eine Aktion f�r dieses Verbindungsobjekt aus. Diese Methode wird vom Protokoll-Thread zur Verarbeitung einer Aktion aufgerufen.
         *
         * @param action   Auszuf�hrende Aktion
         * @param selector Selektor des Protokoll-Threads zum asynchronen Zugriff auf die Kommunikationskan�le.
         */
        public void handleAction(Server.ActionType action, Selector selector) {
            _debug.finer("handleAction(" + action + "): " + this);

            if ((action == Server.ActionType.CONNECT_CALLED) || (action == Server.ActionType.RETRY_CONNECT)) {
                _debug.fine("Verbindung aufbauen");
                connectSocketChannel(selector);
            } else if (action == Server.ActionType.KEEPALIVE_TIMER) {
                synchronized (_linkLock) {
                    if (_linkState == LinkState.CONNECTED) {
                        _sendKeepAlive = true;

                        if (_lastKeepAliveReceive + _wanComKeepAliveTimeSeconds * 1000L < System.currentTimeMillis()) {
                            ++_keepAliveReceiveTimeoutCount;
                            _debug.info("KeepAlive Timeout, Z�hler: " + _keepAliveReceiveTimeoutCount + "; " + this);

                            if (_keepAliveReceiveTimeoutCount >= _wanComKeepAliveTimeoutCount) {
                                _debug.warning("Verbindung wird neu initialisiert wegen fehlenden KeepAlive Telegrammen: " + this);
                                closeChannel();

                                return;
                            }
                        }

                        scheduleActionTimer(Server.ActionType.KEEPALIVE_TIMER, _wanComKeepAliveTimeSeconds);
                    }
                }
            } else if (action == Server.ActionType.SEND_CALLED) {

                // handleAsyncSend() wird auf jeden Fall aufgerufen (s.u.)
            } else if (action == Server.ActionType.RELOAD_CALLED) {
                closeChannel(2);
            } else if (action == Server.ActionType.ABORT_CALLED) {

                // nichts zu tun
            } else if (action == Server.ActionType.SHUTDOWN_CALLED) {

                // nichts zu tun
            } else {
                _debug.error("unbekannter ActionType: " + action);
            }

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
                        if (_sendKeepAlive) {
                            _debug.finer("Senden eines KeepAlive-Telegramms");
                            _debug.finest("eingetragene lokale IP: " + _wanComIp8[0] + "." + _wanComIp8[1] + "." + _wanComIp8[2] + "." + _wanComIp8[3] + "." + _wanComIp8[4] + "." + _wanComIp8[5] + "." + _wanComIp8[6] + "." + _wanComIp8[7]);
                            _sendKeepAlive = false;
                            _sendBuffer.clear();

                            int size = 43;

                            _sendBuffer.putInt(_wanComVersion);
                            _sendBuffer.putInt(size);
                            _sendBuffer.putInt(_wanComKeepAliveType);
                            _sendBuffer.putInt(0);
                            _sendBuffer.putInt(0);
                            _sendBuffer.put(_wanComIp8);
                            _sendBuffer.put((byte) 9);
                            _sendBuffer.put((byte) 255);
                            _sendBuffer.put((byte) 255);
                            _sendBuffer.put((byte) 0);
                            _sendBuffer.put((byte) 0);
                            _sendBuffer.put((byte) 0);
                            _sendBuffer.put((byte) 1);
                            _sendBuffer.put((byte) 7);
                            _sendBuffer.put((byte) 134);
                            _sendBuffer.put((byte) 2);
                            _sendBuffer.put((byte) 0);
                            _sendBuffer.put((byte) 1);
                            _sendBuffer.put((byte) 2);
                            _sendBuffer.put((byte) 255);
                            _sendBuffer.put((byte) 130);
                            _sendBuffer.flip();
                        } else {
                            Server.PriorizedByteArray priorizedByteArray = (Server.PriorizedByteArray) _sendChannel.poll(0);

                            if (priorizedByteArray != null) {
                                final byte[] bytes = priorizedByteArray.getBytes();

                                if (bytes == null) {
                                    closeChannel();
                                } else {
                                    _packetOnTheAir = bytes;
                                    _debug.finer("Senden eines TLS-Telegramms");
                                    _debug.finest("eingetragene lokale IP: " + _wanComIp8[0] + "." + _wanComIp8[1] + "." + _wanComIp8[2] + "." + _wanComIp8[3] + "." + _wanComIp8[4] + "." + _wanComIp8[5] + "." + _wanComIp8[6] + "." + _wanComIp8[7]);
                                    _sendBuffer.clear();

                                    int size = 28 + bytes.length;

                                    _sendBuffer.putInt(_wanComVersion);
                                    _sendBuffer.putInt(size);
                                    _sendBuffer.putInt(_wanComTlsType);
                                    _sendBuffer.putInt(0);
                                    _sendBuffer.putInt(0);
                                    _sendBuffer.put(_wanComIp8);
                                    _sendBuffer.put(bytes);
                                    _sendBuffer.flip();
                                }
                            }
                        }
                    }

                    if (_sendBuffer.hasRemaining()) {
                        _debug.finest("Sendeversuch f�r verbleibende " + _sendBuffer.remaining() + " Bytes");

                        int sent = _socketChannel.write(_sendBuffer);

                        _debug.finest("erfolgreich gesendete Bytes " + sent);
                    }

                    if (_sendBuffer.hasRemaining()) {
                        _debug.finer("Versand wird sobald m�glich fortgesetzt");
                        _socketChannel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, this);

                        break;
                    } else {
                        if (_packetOnTheAir != null) {
                            byte[] sentPacket = _packetOnTheAir;

                            _packetOnTheAir = null;
                            notifyEvent(DataLinkLayerEvent.Type.DATA_SENT, sentPacket);
                        }
                    }
                } while (!_sendChannel.isEmpty());
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                _debug.warning("unerwartete Exception: " + e);
            }
            catch (IOException e) {
                _debug.warning("Verbindung wird wegen Fehler beim Senden initialisiert: " + e);
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
            _debug.finer("handleSelection(" + selectionKey.readyOps() + "/" + selectionKey.interestOps() + "): " + this);

            if (selectionKey.isAcceptable()) {
                _debug.fine("Verbindungsaufbau akzeptieren");
                connectSocketChannel(selector);
            }

            if (!selectionKey.isValid()) {
                return;
            }

            if (selectionKey.isReadable()) {
                _debug.finest("Telegramm-Daten empfangen");

                try {
                    _debug.finest("_readBuffer vorm lesen: " + _readBuffer);

                    // HexDumper.dumpTo(System.out,_readBuffer.array(),0, _readBuffer.position());
                    int got = _socketChannel.read(_readBuffer);

                    if (got == -1) {
                        _debug.info("Verbindung wurde von der Gegenseite terminiert; " + this);
                        closeChannel();
                    } else {
                        _debug.finest("Anzahl gelesener Bytes: " + got);

                        // _debug.finer("_readBuffer", _readBuffer);
                        // HexDumper.dumpTo(System.out,_readBuffer.array(),0, _readBuffer.position());
                        _readBuffer.flip();

                        int remaining;

                        _debug.finest("_readBuffer: " + _readBuffer);

                        while ((remaining = _readBuffer.remaining()) >= 28) {
                            int telegramPosition = _readBuffer.position();
                            int telegramVersion  = _readBuffer.getInt(telegramPosition + 0);
                            int telegramSize     = _readBuffer.getInt(telegramPosition + 4);

                            _debug.finest("version: " + telegramVersion);
                            _debug.finest("size: " + telegramSize);

                            try {
                                if (telegramVersion != _wanComVersion) {
                                    throw new Server.IllegalTelegramException("Falsche WanCom Version: " + telegramVersion);
                                }

                                if (telegramSize < 28) {
                                    throw new Server.IllegalTelegramException("Empfangene WanCom-Telegrammgr��e ist zu klein: " + telegramSize);
                                }

                                if (telegramSize > 2204) {
                                    throw new Server.IllegalTelegramException("Empfangene WanCom-Telegrammgr��e ist zu gro�: " + telegramSize);
                                }

                                if (remaining >= telegramSize) {
                                    int telegramType               = _readBuffer.getInt(telegramPosition + 8);
                                    int telegramDestinationIpCount = _readBuffer.getInt(telegramPosition + 12);

                                    if ((telegramDestinationIpCount < 0) || (telegramDestinationIpCount > 16)) {
                                        throw new Server.IllegalTelegramException("Ung�ltige Anzahl IP-Adressen im WanCom im Telegramm: " + telegramDestinationIpCount);
                                    }

                                    int telegramDestinationIpPointer = _readBuffer.getInt(telegramPosition + 16);

                                    if ((telegramDestinationIpPointer < 0) || (telegramDestinationIpPointer > telegramDestinationIpCount)) {
                                        throw new Server.IllegalTelegramException("Ung�ltiger IP-Adress-Zeiger im WanCom im Telegramm: " + telegramDestinationIpCount);
                                    }

                                    int payloadOffset = 5 * 4 + 8 + telegramDestinationIpCount * 8;

                                    if (payloadOffset > telegramSize) {
                                        throw new Server.IllegalTelegramException("Berechneter Start der Nutzdaten liegt ausserhalb des Telegramms: " + payloadOffset);
                                    }

                                    int payloadSize = telegramSize - payloadOffset;

                                    if (telegramDestinationIpPointer != telegramDestinationIpCount) {
                                        _debug.warning("IP-Routing in Wan-Com Telegrammen wird nicht unterst�tzt");
                                    } else {
                                        if (telegramType == _wanComKeepAliveType) {
                                            _debug.finer("keepAlive Telegramm empfangen; " + this);
                                            _lastKeepAliveReceive         = System.currentTimeMillis();
                                            _keepAliveReceiveTimeoutCount = 0;
                                        } else if (telegramType == _wanComTlsTypeReceive) {

                                            // TLS Telegramm verarbeiten
                                            _debug.finer("TLS Telegramm empfangen; " + this);
                                            _readBuffer.position(telegramPosition + payloadOffset);

                                            byte[] payload = new byte[payloadSize];

                                            _readBuffer.get(payload);
                                            notifyEvent(DataLinkLayerEvent.Type.DATA_RECEIVED, payload);
                                        } else {
                                            throw new Server.IllegalTelegramException("Ung�ltiger WanCom Type im Telegramm: " + telegramType);
                                        }
                                    }

                                    _readBuffer.position(telegramPosition + telegramSize);
                                } else {

                                    // Nicht gen�gend Bytes im Puffer => Warten auf weitere Daten
                                    break;
                                }
                            }
                            catch (Server.IllegalTelegramException e) {
                                e.printStackTrace();
                                _debug.error(e.getLocalizedMessage());
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
        private void notifyWorker(Server.ActionType action) {
            _worker.notify(this, action);
        }

        /** Initiiert den Abbruch und erneuten Verbindungsaufbau einer bestehenden Verbindung mit evtl. ge�nderten Parametern */
        public void reload() {
            _debug.fine("reload " + this);

            synchronized (_protocolLock) {
                if ((_protocolState == ProtocolState.STARTED) || (_protocolState == ProtocolState.STARTING)) {
                    notifyWorker(Server.ActionType.RELOAD_CALLED);
                }
            }
        }

        /**
         * Plant eine Aktion mit Hilfe eines Timer-Objekts zur sp�teren Ausf�hrung ein.
         *
         * @param actionType   Auszuf�hrende Aktion
         * @param delaySeconds Verz�gerungszeit in Sekunden nach der die Aktion ausgef�hrt werden soll.
         */
        private void scheduleActionTimer(final Server.ActionType actionType, int delaySeconds) {
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
            _debug.finer("Telegramm soll gesendet werden, Priorit�t: " + priority);

            // _debug.finer("Daten: " + HexDumper.toString(bytes));
            synchronized (_linkLock) {
                if (_linkState != LinkState.CONNECTED) {
                    throw new IllegalStateException("Telegramm kann in diesem Verbindungszustand nicht versendet werden: " + _linkState);
                }
            }

            _debug.finest("Telegramm wird zum Versand gepuffert, Priorit�t: " + priority);
            _sendChannel.put(new Server.PriorizedByteArray(bytes, priority));
            notifyWorker(Server.ActionType.SEND_CALLED);
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
            _debug.fine("shutdown " + this);

            synchronized (_linkLock) {
                if ((_linkState == LinkState.DISCONNECTED) || (_linkState == LinkState.DISCONNECTING)) {
                    return;
                }

                _linkState = LinkState.DISCONNECTING;
            }

            try {
                _sendChannel.put(new Server.PriorizedByteArray(null, 2));
            }
            catch (InterruptedException e) {
                e.printStackTrace();

                throw new RuntimeException(e);
            }

            notifyWorker(Server.ActionType.SHUTDOWN_CALLED);
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
         * Selektor-Objekt, mit dessen Hilfe alle Kommunikationsoperationen (Verbindungsaufbau, Versand und Empfang von Daten) ohne zus�tzliche Threads asynchron
         * ausgef�hrt werden.
         */
        private final Selector _selector;

        /** Queue zur �bermittlung von Aktionen an den Protokoll-Thread */
        private final UnboundedQueue<WorkAction> _workQueue;

        //~ KONSTRUKTOREN  (und vom Konstruktor verwendete Methoden) ==========

        /** Konstruktor initialisiert den Selektor und die Queue zur �bermittlung von Aktionen */
        public Worker() throws IOException {
            _selector  = Selector.open();
            _workQueue = new UnboundedQueue<Server.Worker.WorkAction>();
        }

        //~ METHODEN ==========================================================

        /**
         * Kann von einem beliebigen Thread aufgerufen werden, um dem Protokoll-Thread zu signalisieren, dass eine bestimmte Aktion ausgef�hrt werden soll
         *
         * @param link   Verbindung, auf die sich die Aktion bezieht.
         * @param action Durchzuf�hrende Aktion
         */
        public void notify(Server.Link link, Server.ActionType action) {
            _workQueue.put(new Server.Worker.WorkAction(link, action));
            _debug.finer("Aufruf von _selector.wakeup()");
            _selector.wakeup();
        }

        /** Methode, die beim Start des Protokoll-Threads aufgerufen wird und die asynchrone Protokollsteuerung implementiert. */
        public void run() {
            synchronized (_protocolLock) {

                // Warten bis Protokoll gestartet wird
                while (_protocolState == ProtocolState.CREATED) {
                    _debug.fine("Warten auf den Start des Protokolls: " + toString());

                    try {
                        _protocolLock.wait();
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                        _debug.error("Fehler im " + toString() + ": " + e);
                    }
                }
            }

            // Hier wird 30 Sekunden gewartet, um der TLS-OSI-7 gen�gend Zeit zur Initialisierung zu geben
            _debug.fine("WanCom: 30 Sekunden warten: " + toString());

            try {
                Thread.sleep(30000);
            }
            catch (InterruptedException e) {}

            _debug.fine("WanCom: Beginn der Protokollabarbeitung: " + toString());

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
                        _debug.finest("Protokoll arbeitet: " + this);

                        Server.Worker.WorkAction action;

                        while (null != (action = _workQueue.poll(0))) {
                            action._link.handleAction(action._action, _selector);
                        }

                        try {
                            _debug.finest("Aufruf von select()");

                            int count = _selector.select();

                            _debug.finest("R�ckgabe von select(): " + count);

                            Set<SelectionKey> selectedKeys = _selector.selectedKeys();

                            for (Iterator<SelectionKey> iterator = selectedKeys.iterator(); iterator.hasNext(); ) {
                                SelectionKey selectionKey = iterator.next();

                                iterator.remove();

                                Server.Link selectedLink = (Server.Link) selectionKey.attachment();

                                selectedLink.handleSelection(selectionKey, _selector);
                            }
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (state == ProtocolState.STOPPED) {
                        _debug.fine("Protokoll wurde gestoppt: " + this);

                        break;
                    } else {
                        _debug.error("ung�ltiger Zustand: " + state + "; " + this);
                    }
                }
                catch (InterruptedException e) {
                    _debug.warning("InterruptedException: " + this, e);
                }
                catch (RuntimeException e) {
                    _debug.warning("Unerwarteter Fehler: " + e.getLocalizedMessage() + "; " + this, e);
                }
            }

            _debug.warning("Thread wird terminiert: " + this);
        }

        /** @return Informationen dieses Objekts f�r Debug-Zwecke */
        public String toString() {
            return "Worker f�r " + Server.this.toString();
        }

        //~ INNERE KLASSEN ====================================================

        /** Hilfsklasse, die zur Speicherung einer Aktion zusammen mit der Verbindung, auf die sich die Aktion bezieht, eingesetzt wird. */
        class WorkAction {

            /** Zur Speicherung der Aktion */
            public final Server.ActionType _action;

            /** Zur Speicherung der Verbindung */
            public final Server.Link _link;

            //~ KONSTRUKTOREN  (und vom Konstruktor verwendete Methoden) ======

            /**
             * Erzeugt ein neues Hilfsobjekt f�r eine Aktion, die an einer Verbindung auszuf�hren ist.
             *
             * @param link   Zu speichernde Verbindung
             * @param action Zu speichernde Aktion
             */
            public WorkAction(Server.Link link, Server.ActionType action) {
                _link   = link;
                _action = action;
            }

            //~ METHODEN ======================================================

            /** @return Informationen dieses Objekts f�r Debug-Zwecke */
            public String toString() {
                return "WorkAction(link: " + _link + ", action: " + _action;
            }
        }
    }
}



//~Formatiert mit 'inovat Kodierkonvention' am 04/05/10
