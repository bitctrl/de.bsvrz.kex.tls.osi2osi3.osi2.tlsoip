/*
 * Copyright (c) 2009 by inovat, innovative systeme - verkehr - tunnel - technik, Dipl.-Ing. H. C. Kniß
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
 * inovat, Dipl.-Ing. H. C. Kniß
 * Kölner Straße 30
 * D-50859 Köln
 * +49 (0)2234 4301 800
 * info@invat.de
 * www.inovat.de
 */



package de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip;

//~ NICHT JDK IMPORTE =========================================================

import de.bsvrz.sys.funclib.debug.Debug;

//~ JDK IMPORTE ===============================================================

import java.nio.ByteBuffer;

//~ KLASSEN ===================================================================

/**
 * Telegrammrahmen für ein TLSoIP-Telegramm, welches aus Header und Daten besteht.
 * <p/>
 * Genaue Beschreibung des Aufbaus siehe TLS 2009 "Datenübertragung über TCP/IP (TLSoIP)".
 *
 * @author inovat, innovative systeme - verkehr - tunnel - technik
 * @author Dipl.-Ing. Hans Christian Kniß (HCK)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class TLSoIPFrame {

    /** DebugLogger für Debug-Ausgaben. */
    private static final Debug DEBUG               = Debug.getLogger();
    public static final int    FRAME_HEADER_LENGTH = 10;
    public static final byte   FRAME_HEADER_SYNC   = (byte) 0x68;
    public static final byte   TELTYPE_IB_V1       = (byte) 0x11;
    public static final byte   TELTYPE_KEEPALIVE   = (byte) 0x80;
    public static final byte   TELTYPE_QUITT       = (byte) 0x90;

    //~ FELDER ================================================================

    byte               _data[]   = null;
    private final byte _header[] = new byte[FRAME_HEADER_LENGTH];
    private long       _len;
    private int        _seqNum;
    private byte       _sync;
    private byte       _telType;

    //~ KONSTRUKTOREN  (und vom Konstruktor verwendete Methoden) ==============

    /**
     * Spezieller Konstruktor, der aus dem übergebenen ByteBuffer lediglich die HeaderDaten eines TLSoIP-Telegramms
     * ausliest.
     *
     * @param readBuffer Der Buffer mit den Headerdaten des Telegramms.
     */
    public TLSoIPFrame(ByteBuffer readBuffer) {
        byte sync    = readBuffer.get();
        byte telType = readBuffer.get();
        int  seqNum  = (readBuffer.get() & 0xff) + (readBuffer.get() & 0xff) * 0x100;

        readBuffer.get();  // 2-Byte überlesen -> Reservebytes im Header
        readBuffer.get();

        long len = (readBuffer.get() & 0xff) + (readBuffer.get() & 0xff) * 0x100 + (readBuffer.get() & 0x10000) + (readBuffer.get() & 0xff) * 0x1000000;

        constuctTLSoIPFrame(sync, seqNum, telType, null, len);
    }

    /**
     * Legt ein TLSoIP Telegramm an.
     *
     * @param seqNum  Sequenznummer des Telegramms.
     * @param telType Telegrammtyp (TELTYPE_IB_V1 , TELTYPE_KEEPALIVE , TELTYPE_QUITT )
     * @param data    Wenn telType = TELTYPE_IB_V1, die Nutzdaten, sonst <code>null</code>.
     */
    public TLSoIPFrame(int seqNum, byte telType, byte data[]) {
        constuctTLSoIPFrame(FRAME_HEADER_SYNC, seqNum, telType, data, -1L);
    }

    //~ METHODEN ==============================================================

    /**
     * Liest aus dem übergebenen Buffer die Bytes in den Datenteil des Telegramms. Die Anzahl der zu lesenden Bytes werden
     * aus der Längenangabe des Headers ermittelt. <p< Liegen nicht genügend Bytes im Buffer vor, wird der Datenteil auf
     * <code>null</code> gesetzt.
     *
     * @param readBuffer Der Buffer mit den Nutzdaten des Telegramms.
     */
    public void addData(ByteBuffer readBuffer) {
        if (_len > 0) {
            byte[] data = new byte[(int) _len];

            readBuffer.get(data, 0, (int) _len);
            _data = data.clone();
        }
    }

    /**
     * Privater "Konstruktor" zum Anlegen eines TLSoIP Telegramms.
     *
     * @param sync    Sync-Zeichen (normalerweise immer 0x68)
     * @param seqNum  Sequenznummer des Telegramms.
     * @param telType Telegrammtyp ({@link #TELTYPE_IB_V1} , {@link #TELTYPE_KEEPALIVE} , {@link #TELTYPE_QUITT} )
     * @param data    Wenn telType = TELTYPE_IB_V1, die Nutzdaten, sonst <code>null</code>.
     * @param len     Wenn <code>data != null</code>, dann wird Länge aus tatsächlicher Nutzdatenlänge ermittelt, ansonsten
     *                wird für len < 0 der Wert 0 und sonst der übergebene Wert eingestellt.
     */
    private void constuctTLSoIPFrame(byte sync, int seqNum, byte telType, byte data[], long len) {
        _sync    = sync;
        _seqNum  = seqNum;
        _telType = telType;

        if (data != null) {
            _data = data.clone();
            _len  = _data.length;
        } else {
            if (len < 0L) {
                _len = 0L;
            } else {
                _len = len;
            }
        }

        _header[0] = _sync;
        _header[1] = _telType;
        writeIntAsLowHighByte(_header, 2, _seqNum);
        writeIntAsLowHighByte(_header, 4, 0);
        writeIntAsLowHighByte(_header, 6, (int) (_len & 65535L));
        writeIntAsLowHighByte(_header, 8, 0);
    }

    //~ GET METHODEN ==========================================================

    /**
     * Liefert nur den Nutzdatenanteil des Gesamttelegramms.
     *
     * @return Nutzdatenanteil des Gesamttelegramms oder leeres Array, wenn keine Nutzdaten enthalten sind.
     */
    public byte[] getData() {
        if (_data != null) {
            byte data[] = new byte[_data.length];

            System.arraycopy(_data, 0, data, 0, _data.length);

            return data;
        } else {
            return new byte[0];
        }
    }

    /**
     * Liefert nur den Header des Gesamttelegramms.
     *
     * @return Header des Gesamttelegramms oder leeres Array, wenn kein Header enthalten ist.
     */
    public byte[] getHeader() {
        if (_header != null) {
            byte header[] = new byte[_header.length];

            System.arraycopy(_header, 0, header, 0, _header.length);

            return header;
        } else {
            return new byte[0];
        }
    }

    /**
     * Liefert die Länge der Nutzdaten in Anzahl Bytes.
     *
     * @return Länge des Datenblocks mit den Nutzdaten.
     */
    public long getLen() {
        return _len;
    }

    /**
     * Liefert die aktuelle Sequenznummer (0-65535)
     *
     * @return Aktuelle Sequenznummer (0-65535)
     */
    public int getSeqNum() {
        return _seqNum;
    }

    /**
     * Liefert das Startbyte des Headers eines TLSoIP-Telegramms. Muss eigentlich immer 0x68 sein.
     *
     * @return Startbyte des Headers eines TLSoIP-Telegramms. Muss eigentlich immer 0x68 sein.
     *
     * @see #isTLSoIPFrame()
     */
    public byte getSync() {
        return _sync;
    }

    /**
     * Liefert das komplette TLSoIP-Telegramm mit Header und Nutzdaten.
     *
     * @return TLSoIP-Telegramm mit Header und Nutzdaten.
     */
    public byte[] getTel() {
        if (_data != null) {
            byte tel[] = new byte[_header.length + _data.length];

            System.arraycopy(_header, 0, tel, 0, _header.length);
            System.arraycopy(_data, 0, tel, _header.length, _data.length);

            return tel;
        } else {
            return _header;
        }
    }

    /**
     * Liefert eine textuelle Beschreibung der unterstützten Telegrammtypen.
     *
     * @param telType Code des Telegrammtyps.
     *
     * @return Textuelle Beschreibung der unterstützten Telegrammtypen.
     */
    public static String getTelTypeInfo(int telType) {
        switch ((byte) (telType & 0xff)) {
        case TELTYPE_IB_V1 :
            return "0x11 [Telegramm mit TLS-Daten]";

        case TELTYPE_KEEPALIVE :
            return "0x80 [Kontroll-Telegramm Keep-Alive]";

        case TELTYPE_QUITT :
            return "0x90 [Kontroll-Telegramm Quittung]";
        }

        return "Unbekannter Telegrammtyp";
    }

    /**
     * Testet, ob es sich um ein Daten-Telegramm handelt.
     *
     * @return <code>true</code>, wenn Telegrammtyp == TELTYPE_IB_V1, sonst <code>false</code>.
     */
    public boolean isDataTel() {
        return _telType == TELTYPE_IB_V1;
    }

    /**
     * Testet, ob es sich um ein KeepAlive-Telegramm .
     *
     * @return <code>true</code>, wenn Telegrammtyp == TELTYPE_KEEPALIVE, sonst <code>false</code>.
     */
    public boolean isKeepAliveTel() {
        return _telType == TELTYPE_KEEPALIVE;
    }

    /**
     * Testet, ob es sich um ein Quittierungs-Telegramm .
     *
     * @return <code>true</code>, wenn Telegrammtyp == TELTYPE_QUITT, sonst <code>false</code>.
     */
    public boolean isQuittTel() {
        return _telType == TELTYPE_QUITT;
    }

    /**
     * Testet, ob es sich um ein gültiges TLSoIP-Telegramm handelt.
     *
     * @return <code>true</code>, wenn erstes Byte im Header == 0x68, sonst <code>false</code>.
     */
    public boolean isTLSoIPFrame() {
        return _sync == FRAME_HEADER_SYNC;
    }

    //~ METHODEN ==============================================================

    private void writeIntAsLowHighByte(byte array[], int index, int data) {
        array[index]     = (byte) (data & 255);
        array[index + 1] = (byte) ((data & 65280) >>> 8);
    }
}



//~Formatiert mit 'inovat Kodierkonvention' am 04/05/10
