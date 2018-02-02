/*
 * Copyright (c) 2010 - 2014 by inovat, innovative systeme - verkehr - tunnel - technik, Dipl.-Ing. H. C. Kniss
 *
 * This file is part of de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.TLSoIP
 *
 * de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.TLSoIP is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.TLSoIP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.TLSoIP; if not, write to the Free Software
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

import de.bsvrz.kex.tls.osi2osi3.osi2.api.AbstractDataLinkLayer;

//~ KLASSEN ===================================================================

/**
 * Abstrakte Klasse für den OSI2OSI3 {@link de.bsvrz.kex.tls.osi2osi3.osi2.api.AbstractDataLinkLayer}.
 *
 * @author inovat, innovative systeme - verkehr - tunnel - technik
 * @author Dipl.-Ing. Hans Christian Kniß (HCK)
 * @version $Revision$ / $Date$ / ($Author$)
 */
abstract class TLSoIP extends AbstractDataLinkLayer {

    /** Maximale Anzahl von Nutzdatenbytes in einem OSI-2 Paket (Telegramm) */
    final private int MAX_ANZAHL_NUTZDATENBYTES_PRO_OSI2_PAKET = 253;

    //~ KONSTRUKTOREN  (und vom Konstruktor verwendete Methoden) ==============

    /**
     * Konstruktor.
     */
    public TLSoIP() {}

    //~ GET METHODEN ==========================================================

    /**
     * Liefert den Standard-Wert für eine Eigenschaft an einem Anschlusspunkt bei TLS over IP.
     *
     * @param name der Eigenschaft.
     * @return Standard-Wert der abgefragten Eigenschaft.
     */
    protected String getDefaultProperty(String name) {
        return DefaultProperties.getInstance().getProperty(name);
    }

    /**
     * Bestimmt die maximale Anzahl von Nutzdatenbytes in einem OSI-2 Paket (Telegramm).
     *
     * @return Maximale Anzahl Nutzdatenbytes.
     */
    public int getMaximumDataSize() {
        return MAX_ANZAHL_NUTZDATENBYTES_PRO_OSI2_PAKET;
    }
}


//~Formatiert mit 'inovat Kodierkonvention' am 22.07.14
