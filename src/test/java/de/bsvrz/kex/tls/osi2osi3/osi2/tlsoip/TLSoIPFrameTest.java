/*
 * Copyright (c) 2010-2011 by inovat, innovative systeme - verkehr - tunnel - technik, Dipl.-Ing. H. C. Kniss
 *
 * This file is part of de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.TLSoIPFrameTest
 *
 * de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.TLSoIPFrameTest is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.TLSoIPFrameTest is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.TLSoIPFrameTest; if not, write to the Free Software
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

import de.bsvrz.sys.funclib.hexdump.HexDumper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;

//~ KLASSEN ===================================================================

/**
 * TLSoIPFrame Testklasse.
 *
 * @author inovat, innovative systeme - verkehr - tunnel - technik
 * @author Dipl.-Ing. Hans Christian Kniß (HCK)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class TLSoIPFrameTest {
    @Before
    public void setUp() throws Exception {}

    //~ METHODEN ==============================================================

    @After
    public void tearDown() throws Exception {}

    @Test
    public void testGetSync() throws Exception {
        TLSoIPFrame tlSoIPFrame = new TLSoIPFrame(0x01, TLSoIPFrame.TELTYPE_KEEPALIVE, null);

        Assert.assertEquals(TLSoIPFrame.FRAME_HEADER_SYNC, tlSoIPFrame.getSync());
        System.out.printf("tlSoIPFrame.getSync() %5s %02x hex %5d dez%n", tlSoIPFrame.getSync(), tlSoIPFrame.getSync(), tlSoIPFrame.getSync());
    }

    @Test
    public void testGetTelTypeInfo() throws Exception {
        Assert.assertEquals("0x11 [Telegramm mit TLS-Daten]", TLSoIPFrame.getTelTypeInfo(0x11));
        Assert.assertEquals("0x80 [Kontroll-Telegramm Keep-Alive]", TLSoIPFrame.getTelTypeInfo(0x80));
        Assert.assertEquals("0x90 [Kontroll-Telegramm Quittung]", TLSoIPFrame.getTelTypeInfo(0x90));
        Assert.assertEquals("Unbekannter Telegrammtyp", TLSoIPFrame.getTelTypeInfo(0x12));
    }

    @Test
    public void testIsDataTel() throws Exception {
        TLSoIPFrame tlSoIPFrame = new TLSoIPFrame(0x01, TLSoIPFrame.TELTYPE_IB_V1, null);

        Assert.assertEquals(true, tlSoIPFrame.isDataTel());
    }

    @Test
    public void testIsKeepAliveTel() throws Exception {
        TLSoIPFrame tlSoIPFrame = new TLSoIPFrame(0x01, TLSoIPFrame.TELTYPE_KEEPALIVE, null);

        Assert.assertEquals(true, tlSoIPFrame.isKeepAliveTel());
    }

    @Test
    public void testIsQuittTel() throws Exception {
        TLSoIPFrame tlSoIPFrame = new TLSoIPFrame(0x01, TLSoIPFrame.TELTYPE_QUITT, null);

        Assert.assertEquals(true, tlSoIPFrame.isQuittTel());
    }

    @Test
    public void testIsTLSoIPFrame() throws Exception {
        TLSoIPFrame tlSoIPFrame = new TLSoIPFrame(0x01, TLSoIPFrame.TELTYPE_KEEPALIVE, null);

        Assert.assertEquals(true, tlSoIPFrame.isTLSoIPFrame());
    }

    @Test
    public void testKonstruktorMitByteBuffer() throws Exception {
        ByteBuffer readBuffer = ByteBuffer.allocate(25);

        readBuffer.put((byte) (0x68 & 0xff));  // Sync TLSoIP Telegramm
        readBuffer.put((byte) (0x11 & 0xff));  // TelTyp
        readBuffer.put((byte) (0xff & 0xff));  // SeqNum
        readBuffer.put((byte) (0x01 & 0xff));  // ..
        readBuffer.put((byte) (0x00));         //
        readBuffer.put((byte) (0x00));         //
        readBuffer.put((byte) (0x0a & 0xff));  // Len (LowByte) 10 Byte
        readBuffer.put((byte) (0x00));         // ..
        readBuffer.put((byte) (0x00));         // ..
        readBuffer.put((byte) (0x00));         // ..
        readBuffer.flip();

        TLSoIPFrame tlSoIPFrame = new TLSoIPFrame(readBuffer);
        byte[]      header      = tlSoIPFrame.getHeader();

        Assert.assertEquals(true, tlSoIPFrame.isTLSoIPFrame());
        Assert.assertEquals(true, tlSoIPFrame.isDataTel());
        Assert.assertEquals(511, tlSoIPFrame.getSeqNum());
        Assert.assertEquals(10, tlSoIPFrame.getLen());
        Assert.assertEquals(0, tlSoIPFrame.getData().length);

        // Testausgaben zur Kontrolle der Byteanordnung...
        System.out.printf("tlSoIPFrameMitBuffer: Bufferdaten %02x | %02x | %02x %02x | %02x %02x | %02x %02x %02x %02x%n", readBuffer.get(0), readBuffer.get(1), readBuffer.get(2), readBuffer.get(3), readBuffer.get(4), readBuffer.get(5), readBuffer.get(6), readBuffer.get(7), readBuffer.get(8), readBuffer.get(9));
        System.out.printf("tlSoIPFrameMitBuffer: Headerdaten %02x | %02x | %02x %02x | %02x %02x | %02x %02x %02x %02x%n", header[0], header[1], header[2], header[3], header[4], header[5], header[6], header[7], header[8], header[9]);
        System.out.printf("%s%n", HexDumper.toString(tlSoIPFrame.getData()));
    }

    @Test
    public void testKonstruktorMitDaten() throws Exception {
        TLSoIPFrame tlSoIPFrame;
        long        len;
        int         seqNum;
        byte[]      header;

        for (int i = 0; i < 0xffff; i = i + 100) {
            byte[] daten = new byte[i];

            for (int j = 0; j < i; j++) {
                daten[j] = (byte) (j & 0xff);
            }

            tlSoIPFrame = new TLSoIPFrame(i, TLSoIPFrame.TELTYPE_IB_V1, daten);
            len         = tlSoIPFrame.getLen();
            seqNum      = tlSoIPFrame.getSeqNum();
            header      = tlSoIPFrame.getHeader();
            Assert.assertEquals(i, len);
            Assert.assertEquals(i, seqNum);
            Assert.assertEquals(i, tlSoIPFrame.getData().length);

            // Testausgaben zur Kontrolle der Byte-Anordnung...
            System.out.printf("tlSoIPFrame.getLen(%5s) %04x hex %5d dez : Headerdaten %02x | %02x | %02x %02x | %02x %02x | %02x %02x %02x %02x%n", len, len, len, header[0], header[1], header[2], header[3], header[4], header[5], header[6], header[7], header[8], header[9]);

            if (i < 400) {
                System.out.printf("%s%n", HexDumper.toString(tlSoIPFrame.getData()));
            }
        }
    }

    @Test
    public void testaddDataUndGetData() throws Exception {
        ByteBuffer readBuffer = ByteBuffer.allocate(400);

        // Header
        readBuffer.put((byte) (0x68 & 0xff));  // Sync TLSoIP Telegramm
        readBuffer.put((byte) (0x11 & 0xff));  // TelTyp
        readBuffer.put((byte) (0xff & 0xff));  // SeqNum
        readBuffer.put((byte) (0x01 & 0xff));  // ..
        readBuffer.put((byte) (0x00));         //
        readBuffer.put((byte) (0x00));         //
        readBuffer.put((byte) (0x2c & 0xff));  // Len (LowByte) 300 Byte = 0x12c
        readBuffer.put((byte) (0x01));         // ..
        readBuffer.put((byte) (0x00));         // ..
        readBuffer.put((byte) (0x00));         // ..

        // Nutzdaten
        int nochFrei = readBuffer.remaining();

        for (int wert = 0; wert < nochFrei; wert++) {
            readBuffer.put((byte) (wert & 0xff));  // ..
        }

        // Zum Lesen zurücksetzen ...
        readBuffer.flip();

        // Zuerst Header aus Buffer erzeugen...
        TLSoIPFrame tlSoIPFrame = new TLSoIPFrame(readBuffer);
        byte[]      header      = tlSoIPFrame.getHeader();

        Assert.assertEquals(true, tlSoIPFrame.isTLSoIPFrame());
        Assert.assertEquals(true, tlSoIPFrame.isDataTel());
        Assert.assertEquals(511, tlSoIPFrame.getSeqNum());
        Assert.assertEquals(300, tlSoIPFrame.getLen());
        Assert.assertEquals(10, tlSoIPFrame.getHeader().length);
        Assert.assertEquals(0, tlSoIPFrame.getData().length);

        // ...dann weitere Daten als Nutzdaten aus Buffer anhängen...
        tlSoIPFrame.addData(readBuffer);

        // Ergebniss wie oben, aber Datenlänge über getData() muss jetzt 300 sein.
        Assert.assertEquals(true, tlSoIPFrame.isTLSoIPFrame());
        Assert.assertEquals(true, tlSoIPFrame.isDataTel());
        Assert.assertEquals(511, tlSoIPFrame.getSeqNum());
        Assert.assertEquals(10, tlSoIPFrame.getHeader().length);
        Assert.assertEquals(300, tlSoIPFrame.getLen());
        Assert.assertEquals(300, tlSoIPFrame.getData().length);

        // Testausgaben zur Kontrolle der Byteanordnung...
        System.out.printf("tlSoIPFrameMitBuffer: Headerdaten %02x | %02x | %02x %02x | %02x %02x | %02x %02x %02x %02x%n", header[0], header[1], header[2], header[3], header[4], header[5], header[6], header[7], header[8], header[9]);
        System.out.printf("%s%n", HexDumper.toString(tlSoIPFrame.getData()));

        int wert = 0;

        for (byte datum : tlSoIPFrame.getData()) {
            Assert.assertEquals((byte) (wert & 255), datum);
            wert++;
        }

        // Isolierter Test der getData() Rückgabe in eigener Datenstruktur (eigentlich überflüssig, da schon zuvor getestet.
        wert = 0;

        byte[] nutzdaten = tlSoIPFrame.getData();

        for (byte datum : nutzdaten) {
            Assert.assertEquals((byte) (wert & 255), datum);
            wert++;
        }
    }
}


//~Formatiert mit 'inovat Kodierkonvention' am 18.12.09
