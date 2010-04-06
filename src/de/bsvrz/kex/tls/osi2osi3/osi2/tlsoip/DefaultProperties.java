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

//~ JDK IMPORTE ===============================================================

import java.util.Properties;

//~ KLASSEN ===================================================================

/**
 * Legt die Standardeinstellungen der Kommunikationsparameter (Protokoll- und Verbindungsparameter)
 * für TLS over IP am Anschlusspunkt fest.
 *
 * Bei der Verwendung der unterschiedlichen Protokollmodule (Client, Server)
 * ist an dem jeweiligen Anschlußpunkt in der Konfiguration in der
 * Attributgruppe "atg.anschlussPunkt" im Attribut "ProtokollTyp"
 * der Wert "de.bsvrz.kex.tls.osi2osi3.tlsoip.Client" bzw.
 * der Wert "de.bsvrz.kex.tls.osi2osi3.tlsoip.Server" einzutragen.
 * <p>
 * Im Parameter "atg.protokollEinstellungenStandard" des Anschlußpunkts können
 * die Standardwerte für alle Verbindungen an diesem Anschlußpunkt eingestellt.
 * <p>
 * Im Parameter "atg.protokollEinstellungenPrimary" der dem Anschlußpunkt
 * zugeordneten AnschlußPunktKommunikationsPartner können individuelle
 * Werte für die Verbindung zum jeweiligen Kommunikationspartner eingestellt werden.
 * <p>
 * Die Parameterdatensätze können dabei mehrere Einträge enthalten, die jeweils aus
 * einem Namen und einem Wert bestehen.
 * <p>
 * Folgende Einträge werden unterstützt (siehe auch TLS 2009, Teil 2,
 * Datenübertragung über TCP/IP (TLSoIP):
 * <p>
 * Verbindungsparameter für beide Verbindungspartner:
 *
 *
 * <p>
 * Verbindungsparameter für den Client:
 *
 *
 * <p>
 * Verbindungsparameter für den Server:
 *
 *
 * @author inovat, innovative systeme - verkehr - tunnel - technik
 * @author Dipl.-Ing. Hans Christian Kniß (HCK)
 * @version $Revision$ / $Date$ / ($Author$)
 */
class DefaultProperties extends Properties {

    /** Einziges Objekt dieser Klasse (Singleton). */
    private static DefaultProperties _defaultProperties = new DefaultProperties();

    //~ KONSTRUKTOREN  (und vom Konstruktor verwendete Methoden) ==============

    /* Nicht öffentlicher Konstruktor zum Erzeugen der Dafaultparameter */
    private DefaultProperties() {

        // ===============================================================
        // Standard Verbindungsparameter für beide Verbindungspartner
        // ===============================================================
        // WIRD AKTUELL NICHT UNTERSTÜTZT:
        // Verbindung wird ohne SSL betrieben (nein), Verbindung wird mit SSL betrieben (ja)
        setProperty("tlsoip.C_SecureConnection", "nein");

        // Zeit [s], nach der ein Keep-Alive-Telegramm an die Gegenstelle versendet werden
        // muss (0=ausgeschaltet für Testzwecke, 1...3599)
        setProperty("tlsoip.C_HelloDelay", "30");

        // Zeit [s], nach der spätestens ein Keep-Alive-Telegramm der Gegenstelle erwartet
        // wird ( > C_HelloDelay der Gegenstelle), (0=ausgeschaltet für Testzwecke, 1...3600)
        setProperty("tlsoip.C_HelloTimeout", "60");

        // Anzahl empfangener/gesendeter Telegramme, nach der spätestens ein
        // Quittungstelegramm versendet werden muss/erwartet wird (1..255).
        setProperty("tlsoip.C_ReceiptCount", "10");

        // Zeit [s], nach der nach Erhalt eines Telegramms spätenstens ein Quittierungstelegramm
        // an die Gegenstelle versendet werden muss (1..59);
        setProperty("tlsoip.C_ReceiptDelay", "15");

        // Zeit [s], nach der spätestens ein Quittungstelegramm von der Gegenstelle erwartet
        // wird (> C_ReceiptDelay der Gegenstelle) (1..60)
        setProperty("tlsoip.C_ReceiptTimeout", "30");

        // ===============================================================
        // Standard Verbindungsparameter für den Client
        // ===============================================================
        // IP-Adresse des Servers
        setProperty("tlsoip.C_ServerAdrA", "");

        // WIRD AKTUELL NICHT UNTERSTÜTZT:
        // IP-Adresse des Alternativ-Servers
        setProperty("tlsoip.C_ServerAdrB", "");

        // Portnummer des Servers
        setProperty("tlsoip.C_AcceptPortA", "");

        // WIRD AKTUELL NICHT UNTERSTÜTZT:
        // Portnummer des Alternativ-Servers
        setProperty("tlsoip.C_AcceptPortB", "");

        // Zeit [s], nach der bei Nichtbestehen einer Verbindung spätestens ein neuer
        // Verbindungsaufbau initiiert werden muss (0=sofort, 1...3600)
        setProperty("tlsoip.C_ReconnectDelay", "20");

        // AKTUELL WIRD NUR "0=immer" UNTERSTÜTZT:
        // Dauer [s], für die eine Verbindung vom Client aufrecht erhalten werden soll
        // (0=immer, 1...3600)
        setProperty("tlsoip.C_ConnectDuration", "0");

        // WIRD AKTUELL NICHT UNTERSTÜTZT:
        // Zeit [hh mm ss], nach der zur Prüfung der Erreichbarkeit des Servers ein
        // Verbindungsaufbau stattfinden muss (optional, nur für temporäre Verbindungen) (00 00 01...23 59 59)
        setProperty("tlsoip.C_ConnectDelay", "00 05 00");

        // Wenn "ja", dann wartet das Protokoll nach dem Aufbau der TCP-Verbindung auf den Empfang eines initialen
        // Telegramms, bevor eine Verbindung als "lebt" gemeldet wird.
        setProperty("tlsoip.waitForInitialReceive", "nein");

        // ===============================================================
        // Standard Verbindungsparameter für den Server
        // ===============================================================
        // Dynamische und oder Private Ports (49152 bis 65535) gemäß IANA konfigurierbar
        setProperty("tlsoip.C_AcceptPort", "");

        // AKTUELL WIRD NUR "AKTIV" UNTERSTÜTZT:
        // Portmodus (aktiv/passiv). Passiv nur für Mithörschnittstellen.
        setProperty("tlsoip.C_PortMode", "aktiv");

        // AKTUELL WIRD NUR PortMode "AKTIV" mit ClientClount "1" UNTERSTÜTZT:
        // Anzahl der unterstützten Ports (1..255), bei aktiven Ports immer gleich 1.
        setProperty("tlsoip.C_ClientCount", "1");

        // WIRD AKTUELL NICHT UNTERSTÜTZT:
        // Bei passiven Ports: Zugeordneter aktiver Port
        setProperty("tlsoip.C_ActivePort", "");

        // WIRD AKTUELL NICHT UNTERSTÜTZT:
        // Bei passiven Ports: Übertragung der Telegramme in Abrufrichtung (ja/nein)
        setProperty("tlsoip.C_XmitRequest", "nein");

        // WIRD AKTUELL NICHT UNTERSTÜTZT:
        // Bei passiven Ports: Übertragung der Telegramme in Antwortrichtung (ja/nein)
        setProperty("tlsoip.C_XmitAnswer", "nein");

        // WIRD AKTUELL NICHT UNTERSTÜTZT:
        // Bei passiven Ports: Liste der erlaubten TelType (OSI-2 TLSoIP)
        setProperty("tlsoip.C_TelTypeList", "");
    }

    //~ GET METHODEN ==========================================================

    /**
     * Bestimmt das einziges Objekt dieser Klasse (Singleton).
     *
     * @return Einziges Objekt dieser Klasse (Singleton).
     */
    public static DefaultProperties getInstance() {
        return _defaultProperties;
    }
}



//~Formatiert mit 'inovat Kodierkonvention' am 04/05/10
