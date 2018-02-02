/*
 * Copyright (c) 2010-2011 by inovat, innovative systeme - verkehr - tunnel - technik, Dipl.-Ing. H. C. Kniss
 *
 * This file is part of de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.ProtocolStateTest
 *
 * de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.ProtocolStateTest is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.ProtocolStateTest is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.ProtocolStateTest; if not, write to the Free Software
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
