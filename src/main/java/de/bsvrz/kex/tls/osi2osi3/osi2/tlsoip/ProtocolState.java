/*
 * Copyright (c) 2010 - 2014 by inovat, innovative systeme - verkehr - tunnel - technik, Dipl.-Ing. H. C. Kniss
 *
 * This file is part of de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.ProtocolState
 *
 * de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.ProtocolState is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.ProtocolState is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.ProtocolState; if not, write to the Free Software
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

//~ KLASSEN ===================================================================

/**
 * Definiert die möglichen Zustände eines Protokolls.
 *
 * @author inovat, innovative systeme - verkehr - tunnel - technik
 * @author Dipl.-Ing. Hans Christian Kniß (HCK)
 * @version $Revision: 624 $ / $Date: 2011-10-27 10:26:35 +0200 (Do, 27 Okt 2011) $ / ($Author: HCK $)
 */
public final class ProtocolState {

    /** Stabiler Zustand für ein noch nicht gestartetes Protokoll. */
    public static final ProtocolState CREATED = new ProtocolState("Erzeugt");

    /** Übergangszustand für ein startendes Protokoll. */
    public static final ProtocolState STARTING = new ProtocolState("Wird gestartet");

    /** Stabiler Zustand für ein gestartetes Protokoll. */
    public static final ProtocolState STARTED = new ProtocolState("Gestartet");

    /** Übergangszustand für ein zu stoppendes Protokoll. */
    public static final ProtocolState STOPPING = new ProtocolState("Wird gestoppt");

    /** Stabiler Zustand für ein gestopptes Protokoll. */
    public static final ProtocolState STOPPED = new ProtocolState("Gestoppt");

    //~ FELDER ================================================================

    /** Name des Zustands */
    private final String _name;

    //~ KONSTRUKTOREN  (und vom Konstruktor verwendete Methoden) ==============

    /**
     * Nicht öffentlicher Konstruktor der zum Erzeugen der vordefinierten Zustände benutzt wird.
     *
     * @param name Name des Zustandes.
     */
    private ProtocolState(String name) {
        _name = name;
    }

    //~ METHODEN ==============================================================

    /**
     * Liefert eine textuelle Beschreibung dieses Zustands zurück. Das genaue Format ist nicht festgelegt und kann sich ändern.
     *
     * @return Beschreibung dieses Zustands.
     */
    public String toString() {
        return _name;
    }
}


//~Formatiert mit 'inovat Kodierkonvention' am 22.07.14
