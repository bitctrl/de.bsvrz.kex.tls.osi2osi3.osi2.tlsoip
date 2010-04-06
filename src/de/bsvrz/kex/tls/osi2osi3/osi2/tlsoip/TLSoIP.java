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

import de.bsvrz.kex.tls.osi2osi3.osi2.api.AbstractDataLinkLayer;

//~ KLASSEN ===================================================================

/**
 * Abstrakte Klasse für den OSI2OSI3 {@link AbstractDataLinkLayer}.
 *
 * @author inovat, innovative systeme - verkehr - tunnel - technik
 * @author Dipl.-Ing. Hans Christian Kniß (HCK)
 * @version $Revision$ / $Date$ / ($Author$)
 */
abstract class TLSoIP extends AbstractDataLinkLayer {

    /** Mamimale Anzahl von Nutzdatenbytes in einem OSI-2 Paket (Telegramm) */
    final private int MAX_ANZAHL_NUTZDATENBYTES_PRO_OIS2_PAKET = 253;

    //~ KONSTRUKTOREN  (und vom Konstruktor verwendete Methoden) ==============

    /**
     * Konsturktor.
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
        return MAX_ANZAHL_NUTZDATENBYTES_PRO_OIS2_PAKET;
    }
}



//~Formatiert mit 'inovat Kodierkonvention' am 04/05/10
