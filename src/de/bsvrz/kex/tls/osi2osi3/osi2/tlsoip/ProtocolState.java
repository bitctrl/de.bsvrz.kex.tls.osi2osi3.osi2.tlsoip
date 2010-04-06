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

//~ KLASSEN ===================================================================

/**
 * Definiert die möglichen Zustände eines Protokolls.
 *
 * @author inovat, innovative systeme - verkehr - tunnel - technik
 * @author Dipl.-Ing. Hans Christian Kniß (HCK)
 * @version $Revision$ / $Date$ / ($Author$)
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



//~Formatiert mit 'inovat Kodierkonvention' am 04/05/10
