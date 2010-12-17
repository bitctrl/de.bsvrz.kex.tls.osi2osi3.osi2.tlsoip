/*
 * Copyright (c) 2010-2011 by inovat, innovative systeme - verkehr - tunnel - technik, Dipl.-Ing. H. C. Kniss
 *
 * This file is part of de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.TLSoIPFrame
 *
 * de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.TLSoIPFrame is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.TLSoIPFrame is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.TLSoIPFrame; if not, write to the Free Software
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

import de.bsvrz.sys.funclib.debug.Debug;

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
    private static final Debug DEBUG = Debug.getLogger();

    /** Länge des Frame-Headers */
    public static final int FRAME_HEADER_LENGTH = 10;

    /** Sync-Byte (0x68) des Frame-Headers */
    public static final byte FRAME_HEADER_SYNC = (byte) 0x68;

    /** Telegrammtyp Datentelegramm Inselbus */
    public static final byte TELTYPE_IB_V1 = (byte) 0x11;

    /** Telegrammtyp Keep-Alive */
    public static final byte TELTYPE_KEEPALIVE = (byte) 0x80;

    /** Telegrammtyp Quittung */
    public static final byte TELTYPE_QUITT = (byte) 0x90;

    //~ FELDER ================================================================

    /** Längenangabe im Telegramm */
    private long _len;

    /** Sequenznummer im Telegramm */
    private int _seqNum;

    /** Sync-Byte im Telegramm */
    private byte _sync;

    /** Telegrammtyp-Byte im Telegramm */
    private byte _telType;

    /** Datenbytes */
    byte _data[] = null;

    /** Headerbytes */
    private final byte _header[] = new byte[FRAME_HEADER_LENGTH];

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
     * @param telType Telegrammtyp ({@link #TELTYPE_IB_V1} , {@link #TELTYPE_KEEPALIVE} , {@link #TELTYPE_QUITT} )
     * @param data    Wenn telType = {@link #TELTYPE_IB_V1}, die Nutzdaten, sonst <code>null</code>.
     */
    public TLSoIPFrame(int seqNum, byte telType, byte data[]) {
        constuctTLSoIPFrame(FRAME_HEADER_SYNC, seqNum, telType, data, -1L);
    }

    //~ GET METHODEN ==========================================================

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
     * @param data    Wenn telType = {@link #TELTYPE_IB_V1}, die Nutzdaten, sonst <code>null</code>.
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
     * Testet, ob es sich um ein Daten-Telegramm handelt.
     *
     * @return <code>true</code>, wenn Telegrammtyp == {@link #TELTYPE_IB_V1}, sonst <code>false</code>.
     */
    public boolean isDataTel() {
        return _telType == TELTYPE_IB_V1;
    }

    /**
     * Testet, ob es sich um ein KeepAlive-Telegramm handelt.
     *
     * @return <code>true</code>, wenn Telegrammtyp == {@link #TELTYPE_KEEPALIVE}, sonst <code>false</code>.
     */
    public boolean isKeepAliveTel() {
        return _telType == TELTYPE_KEEPALIVE;
    }

    /**
     * Testet, ob es sich um ein Quittierungs-Telegramm handelt.
     *
     * @return <code>true</code>, wenn Telegrammtyp == {@link #TELTYPE_QUITT}, sonst <code>false</code>.
     */
    public boolean isQuittTel() {
        return _telType == TELTYPE_QUITT;
    }

    /**
     * Testet, ob es sich um ein gültiges TLSoIP-Telegramm handelt.
     *
     * @return <code>true</code>, wenn erstes Byte im Header == {@link #FRAME_HEADER_SYNC}, sonst <code>false</code>.
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


//~Formatiert mit 'inovat Kodierkonvention' am 06.04.10
