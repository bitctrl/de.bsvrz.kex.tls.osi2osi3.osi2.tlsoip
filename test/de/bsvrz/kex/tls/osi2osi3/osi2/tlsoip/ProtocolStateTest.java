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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

//~ KLASSEN ===================================================================

/**
 * ProtocolState Testklasse.
 *
 * @author inovat, innovative systeme - verkehr - tunnel - technik
 * @author Dipl.-Ing. Hans Christian Kniß (HCK)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ProtocolStateTest {

    //~ SET METHODEN ==========================================================

    @Before
    public void setUp() throws Exception {
    }

    //~ METHODEN ==============================================================

    public static Test suite() {
        return new TestSuite(ProtocolStateTest.class);
    }

    @After
    public void tearDown() throws Exception {
    }

    @org.junit.Test
    public void testGetName() throws Exception {
        Assert.assertEquals("Erzeugt", ProtocolState.CREATED.toString());
        Assert.assertEquals("Wird gestartet", ProtocolState.STARTING.toString());
        Assert.assertEquals("Gestartet", ProtocolState.STARTED.toString());
        Assert.assertEquals("Wird gestoppt", ProtocolState.STOPPING.toString());
        Assert.assertEquals("Gestoppt", ProtocolState.STOPPED.toString());
    }
}


//~Formatiert mit 'inovat Kodierkonvention' am 18.12.09
