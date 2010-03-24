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

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.kex.tls.osi2osi3.osi2.api.DataLinkLayer;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

//~ KLASSEN ===================================================================

/**
 * TLSoIP Testklasse.
 *
 * @author inovat, innovative systeme - verkehr - tunnel - technik
 * @author Dipl.-Ing. Hans Christian Kniß (HCK)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class TLSoIPTest {
    TLSoIP _tlSoIP;

    //~ SET METHODEN ==========================================================

    @Before
    public void setUp() throws Exception {
	    _tlSoIP = new TLSoIP() {
            public void setDavConnection(ClientDavInterface connection) {}
            public void start() {}
            public void shutdown() {}
            public void abort() {}
            public boolean isStarted() {
                return false;
            }
            public DataLinkLayer.Link createLink(int remoteAddress) {
                return null;
            }
        };
    }

    //~ METHODEN ==============================================================

    public static Test suite() {
        return new TestSuite(TLSoIPTest.class);
    }

    @After
    public void tearDown() throws Exception {
    }

    @org.junit.Test
    public void testGetDefaultProperty() throws Exception {

        // Standard Verbindungsparameter für beide Verbindungspartner
        Assert.assertEquals("nein", _tlSoIP.getDefaultProperty("tlsoip.C_SecureConnection"));
        Assert.assertEquals("30", _tlSoIP.getDefaultProperty("tlsoip.C_HelloDelay"));
        Assert.assertEquals("60", _tlSoIP.getDefaultProperty("tlsoip.C_HelloTimeout"));
        Assert.assertEquals("10", _tlSoIP.getDefaultProperty("tlsoip.C_ReceiptCount"));
        Assert.assertEquals("15", _tlSoIP.getDefaultProperty("tlsoip.C_ReceiptDelay"));
        Assert.assertEquals("30", _tlSoIP.getDefaultProperty("tlsoip.C_ReceiptTimeout"));

        // Standard Verbindungsparameter für den Client
        Assert.assertEquals("", _tlSoIP.getDefaultProperty("tlsoip.C_ServerAdrA"));
        Assert.assertEquals("", _tlSoIP.getDefaultProperty("tlsoip.C_ServerAdrB"));
        Assert.assertEquals("", _tlSoIP.getDefaultProperty("tlsoip.C_AcceptPortA"));
        Assert.assertEquals("", _tlSoIP.getDefaultProperty("tlsoip.C_AcceptPortB"));
        Assert.assertEquals("20", _tlSoIP.getDefaultProperty("tlsoip.C_ReconnectDelay"));
        Assert.assertEquals("0", _tlSoIP.getDefaultProperty("tlsoip.C_ConnectDuration"));
        Assert.assertEquals("00 05 00", _tlSoIP.getDefaultProperty("tlsoip.C_ConnectDelay"));
        Assert.assertEquals("nein", _tlSoIP.getDefaultProperty("tlsoip.waitForInitialReceive"));

        // Standard Verbindungsparameter für den Server
        Assert.assertEquals("", _tlSoIP.getDefaultProperty("tlsoip.C_AcceptPort"));
        Assert.assertEquals("aktiv", _tlSoIP.getDefaultProperty("tlsoip.C_PortMode"));
        Assert.assertEquals("1", _tlSoIP.getDefaultProperty("tlsoip.C_ClientCount"));
        Assert.assertEquals("", _tlSoIP.getDefaultProperty("tlsoip.C_ActivePort"));
        Assert.assertEquals("nein", _tlSoIP.getDefaultProperty("tlsoip.C_XmitRequest"));
        Assert.assertEquals("nein", _tlSoIP.getDefaultProperty("tlsoip.C_XmitAnswer"));
        Assert.assertEquals("", _tlSoIP.getDefaultProperty("tlsoip.C_TelTypeList"));
    }

    @org.junit.Test
    public void testGetLocalAddress() throws Exception {
        Assert.assertEquals(0, _tlSoIP.getLocalAddress());
    }

    @org.junit.Test
    public void testGetMaximumDataSize() throws Exception {
        Assert.assertEquals(253, _tlSoIP.getMaximumDataSize());
    }
}


//~Formatiert mit 'inovat Kodierkonvention' am 18.12.09
