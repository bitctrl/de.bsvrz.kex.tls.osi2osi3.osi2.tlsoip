/*
 * Copyright (c) 2010-2011 by inovat, innovative systeme - verkehr - tunnel - technik, Dipl.-Ing. H. C. Kniss
 *
 * This file is part of de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.DefaultProperties
 *
 * de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.DefaultProperties is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.DefaultProperties is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.kex.tls.osi2osi3.osi2.tlsoip.DefaultProperties; if not, write to the Free Software
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

//~ JDK IMPORTE ===============================================================

import java.util.Properties;

//~ KLASSEN ===================================================================

/**
 * Legt die Standardeinstellungen der Kommunikationsparameter (Protokoll- und Verbindungsparameter)
 * f�r TLS over IP am Anschlusspunkt fest.
 *
 * Bei der Verwendung der unterschiedlichen Protokollmodule (Client, Server)
 * ist an dem jeweiligen Anschlu�punkt in der Konfiguration in der
 * Attributgruppe "atg.anschlussPunkt" im Attribut "ProtokollTyp"
 * der Wert "de.bsvrz.kex.tls.osi2osi3.tlsoip.Client" bzw.
 * der Wert "de.bsvrz.kex.tls.osi2osi3.tlsoip.Server" einzutragen.
 * <p>
 * Im Parameter "atg.protokollEinstellungenStandard" des Anschlu�punkts k�nnen
 * die Standardwerte f�r alle Verbindungen an diesem Anschlu�punkt eingestellt.
 * <p>
 * Im Parameter "atg.protokollEinstellungenPrimary" der dem Anschlu�punkt
 * zugeordneten Anschlu�PunktKommunikationsPartner k�nnen individuelle
 * Werte f�r die Verbindung zum jeweiligen Kommunikationspartner eingestellt werden.
 * <p>
 * Die Parameterdatens�tze k�nnen dabei mehrere Eintr�ge enthalten, die jeweils aus
 * einem Namen und einem Wert bestehen.
 * <p>
 * Folgende Eintr�ge werden unterst�tzt (siehe auch TLS 2009, Teil 2,
 * Daten�bertragung �ber TCP/IP (TLSoIP):
 * <p>
 * Verbindungsparameter f�r beide Verbindungspartner:
 * <p>
 * <table cellpadding="2" cellspacing="2" border="1">
 * <tr> <th> Name </th> <th> Defaultwert </th> <th> Beschreibung </th> </tr>
 * <tr> <td> tlsoip.C_HelloDelay </td> <td> 30 </td> <td> Zeit [s], nach der ein Keep-Alive-Telegramm an die Gegenstelle versendet werden muss (0=ausgeschaltet f�r Testzwecke, 1...3599). </td> </tr>
 * <tr> <td> tlsoip.C_HelloTimeout </td> <td> 60 </td> <td> Zeit [s], nach der sp�testens ein Keep-Alive-Telegramm der Gegenstelle erwartet wird ( > C_HelloDelay der Gegenstelle), (0=ausgeschaltet f�r Testzwecke, 1...3600). </td> </tr>
 * <tr> <td> tlsoip.C_ReceiptCount </td> <td> 10 </td> <td> Anzahl empfangener/gesendeter Telegramme, nach der sp�testens ein Quittungstelegramm versendet werden muss/erwartet wird (1..255). </td> </tr>
 * <tr> <td> tlsoip.C_ReceiptDelay </td> <td> 15 </td> <td> Zeit [s], nach der nach Erhalt eines Telegramms sp�tenstens ein Quittierungstelegramm an die Gegenstelle versendet werden muss (1..59). </td> </tr>
 * <tr> <td> tlsoip.C_ReceiptTimeout </td> <td> 30 </td> <td> Zeit [s], nach der sp�testens ein Quittungstelegramm von der Gegenstelle erwartet wird (> C_ReceiptDelay der Gegenstelle) (1..60). </td> </tr>
 * <tr> <td> tlsoip.C_SecureConnection </td> <td> nein </td> <td> WIRD AKTUELL NICHT UNTERST�TZT (immer nein): Verbindung wird ohne SSL betrieben (nein), Verbindung wird mit SSL betrieben (ja). </td> </tr>
 * </table>
 * <p/>
 * <p>
 * Verbindungsparameter f�r den Client:
 * <p>
 * <table cellpadding="2" cellspacing="2" border="1">
 * <tr> <th> Name </th> <th> Defaultwert </th> <th> Beschreibung </th> </tr>
 * <tr> <td> tlsoip.C_ServerAdrA </td> <td>  </td> <td> IP-Adresse des Servers. </td> </tr>
 * <tr> <td> tlsoip.C_AcceptPortA </td> <td>  </td>  <td> Portnummer des Servers. </td> </tr>
 * <tr> <td> tlsoip.C_ReconnectDelay </td> <td> 20 </td> <td> Zeit [s], nach der bei Nichtbestehen einer Verbindung sp�testens ein neuer Verbindungsaufbau initiiert werden muss (0=sofort, 1...3600). </td> </tr>
 * <tr> <td> tlsoip.C_ConnectDuration </td> <td> 0 </td> <td> AKTUELL WIRD NUR "0=immer" UNTERST�TZT!<br> Dauer [s], f�r die eine Verbindung vom Client aufrecht erhalten werden soll (0=immer, 1...3600). </td> </tr>
 * <tr> <td> tlsoip.C_ConnectDelay </td> <td> 00 05 00 </td> <td> WIRD AKTUELL NICHT UNTERST�TZT!<br> Zeit [hh mm ss], nach der zur Pr�fung der Erreichbarkeit des Servers ein Verbindungsaufbau stattfinden muss (optional, nur f�r tempor�re Verbindungen) (00 00 01...23 59 59). </td> </tr>
 * <tr> <td> tlsoip.C_ServerAdrB </td> <td>  </td><td> WIRD AKTUELL NICHT UNTERST�TZT!<br> IP-Adresse des Alternativ-Servers </td> </tr>
 * <tr> <td> tlsoip.C_AcceptPortB </td> <td>  </td><td> WIRD AKTUELL NICHT UNTERST�TZT!<br> Portnummer des Alternativ-Servers </td> </tr>
 * <tr> <td> tlsoip.waitForInitialReceive </td> <td> nein </td> <td> Wenn "ja", dann wartet das Protokoll nach dem Aufbau der TCP-Verbindung auf den Empfang eines initialen Telegramms, bevor eine Verbindung als "lebt" gemeldet wird. </td> </tr>
 * </table>
 * <p/>
 * <p>
 * Verbindungsparameter f�r den Server:
 * <p>
 * <table cellpadding="2" cellspacing="2" border="1">
 * <tr> <th> Name </th> <th> Defaultwert </th> <th> Beschreibung </th> </tr>
 * <tr> <td> tlsoip.C_AcceptPort </td> <td>  </td> <td> Dynamische und oder Private Ports (49152 bis 65535) gem�� IANA konfigurierbar, auf dem der Server anfragen entgegen nimmt. </td> </tr>
 * <tr> <td> tlsoip.C_PortMode </td> <td> aktiv </td> <td> AKTUELL WIRD NUR "AKTIV" UNTERST�TZT!<br> Portmodus (aktiv/passiv). Passiv nur f�r Mith�rschnittstellen. </td> </tr>
 * <tr> <td> tlsoip.C_ClientCount </td> <td> 1 </td> <td> AKTUELL WIRD NUR PortMode "AKTIV" mit ClientClount "1" UNTERST�TZT!<br> Anzahl der unterst�tzten Ports (1..255), bei aktiven Ports immer gleich 1. </td> </tr>
 * <tr> <td> tlsoip.C_ActivePort </td> <td>  </td> <td> WIRD AKTUELL NICHT UNTERST�TZT!<br> Bei passiven Ports: Zugeordneter aktiver Port. </td> </tr>
 * <tr> <td> tlsoip.C_XmitRequest </td> <td> nein </td> <td> WIRD AKTUELL NICHT UNTERST�TZT!<br> Bei passiven Ports: �bertragung der Telegramme in Abrufrichtung (ja/nein). </td> </tr>
 * <tr> <td> tlsoip.C_XmitAnswer </td> <td> nein </td> <td> WIRD AKTUELL NICHT UNTERST�TZT!<br> Bei passiven Ports: �bertragung der Telegramme in Antwortrichtung (ja/nein). </td> </tr>
 * <tr> <td> tlsoip.C_TelTypeList </td> <td>  </td> <td> WIRD AKTUELL NICHT UNTERST�TZT!<br> Bei passiven Ports: Liste der erlaubten TelType (OSI-2 TLSoIP). </td> </tr>
 * </table>
 * <p/>
 *
 * @author inovat, innovative systeme - verkehr - tunnel - technik
 * @author Dipl.-Ing. Hans Christian Kni� (HCK)
 * @version $Revision$ / $Date$ / ($Author$)
 */
class DefaultProperties extends Properties {

    /** Einziges Objekt dieser Klasse (Singleton). */
    private static DefaultProperties _defaultProperties = new DefaultProperties();

    //~ KONSTRUKTOREN  (und vom Konstruktor verwendete Methoden) ==============

    /* Nicht �ffentlicher Konstruktor zum Erzeugen der Dafaultparameter */
    private DefaultProperties() {

        // ===============================================================
        // Standard Verbindungsparameter f�r beide Verbindungspartner
        // ===============================================================
        // WIRD AKTUELL NICHT UNTERST�TZT:
        // Verbindung wird ohne SSL betrieben (nein), Verbindung wird mit SSL betrieben (ja)
        setProperty("tlsoip.C_SecureConnection", "nein");

        // Zeit [s], nach der ein Keep-Alive-Telegramm an die Gegenstelle versendet werden
        // muss (0=ausgeschaltet f�r Testzwecke, 1...3599)
        setProperty("tlsoip.C_HelloDelay", "30");

        // Zeit [s], nach der sp�testens ein Keep-Alive-Telegramm der Gegenstelle erwartet
        // wird ( > C_HelloDelay der Gegenstelle), (0=ausgeschaltet f�r Testzwecke, 1...3600)
        setProperty("tlsoip.C_HelloTimeout", "60");

        // Anzahl empfangener/gesendeter Telegramme, nach der sp�testens ein
        // Quittungstelegramm versendet werden muss/erwartet wird (1..255).
        setProperty("tlsoip.C_ReceiptCount", "10");

        // Zeit [s], nach der nach Erhalt eines Telegramms sp�tenstens ein Quittierungstelegramm
        // an die Gegenstelle versendet werden muss (1..59);
        setProperty("tlsoip.C_ReceiptDelay", "15");

        // Zeit [s], nach der sp�testens ein Quittungstelegramm von der Gegenstelle erwartet
        // wird (> C_ReceiptDelay der Gegenstelle) (1..60)
        setProperty("tlsoip.C_ReceiptTimeout", "30");

        // ===============================================================
        // Standard Verbindungsparameter f�r den Client
        // ===============================================================
        // IP-Adresse des Servers
        setProperty("tlsoip.C_ServerAdrA", "");

        // WIRD AKTUELL NICHT UNTERST�TZT:
        // IP-Adresse des Alternativ-Servers
        setProperty("tlsoip.C_ServerAdrB", "");

        // Portnummer des Servers
        setProperty("tlsoip.C_AcceptPortA", "");

        // WIRD AKTUELL NICHT UNTERST�TZT:
        // Portnummer des Alternativ-Servers
        setProperty("tlsoip.C_AcceptPortB", "");

        // Zeit [s], nach der bei Nichtbestehen einer Verbindung sp�testens ein neuer
        // Verbindungsaufbau initiiert werden muss (0=sofort, 1...3600)
        setProperty("tlsoip.C_ReconnectDelay", "20");

        // AKTUELL WIRD NUR "0=immer" UNTERST�TZT:
        // Dauer [s], f�r die eine Verbindung vom Client aufrecht erhalten werden soll
        // (0=immer, 1...3600)
        setProperty("tlsoip.C_ConnectDuration", "0");

        // WIRD AKTUELL NICHT UNTERST�TZT:
        // Zeit [hh mm ss], nach der zur Pr�fung der Erreichbarkeit des Servers ein
        // Verbindungsaufbau stattfinden muss (optional, nur f�r tempor�re Verbindungen) (00 00 01...23 59 59)
        setProperty("tlsoip.C_ConnectDelay", "00 05 00");

        // Wenn "ja", dann wartet das Protokoll nach dem Aufbau der TCP-Verbindung auf den Empfang eines initialen
        // Telegramms, bevor eine Verbindung als "lebt" gemeldet wird.
        setProperty("tlsoip.waitForInitialReceive", "nein");

        // ===============================================================
        // Standard Verbindungsparameter f�r den Server
        // ===============================================================
        // Dynamische und oder Private Ports (49152 bis 65535) gem�� IANA konfigurierbar
        setProperty("tlsoip.C_AcceptPort", "");

        // AKTUELL WIRD NUR "AKTIV" UNTERST�TZT:
        // Portmodus (aktiv/passiv). Passiv nur f�r Mith�rschnittstellen.
        setProperty("tlsoip.C_PortMode", "aktiv");

        // AKTUELL WIRD NUR PortMode "AKTIV" mit ClientClount "1" UNTERST�TZT:
        // Anzahl der unterst�tzten Ports (1..255), bei aktiven Ports immer gleich 1.
        setProperty("tlsoip.C_ClientCount", "1");

        // WIRD AKTUELL NICHT UNTERST�TZT:
        // Bei passiven Ports: Zugeordneter aktiver Port
        setProperty("tlsoip.C_ActivePort", "");

        // WIRD AKTUELL NICHT UNTERST�TZT:
        // Bei passiven Ports: �bertragung der Telegramme in Abrufrichtung (ja/nein)
        setProperty("tlsoip.C_XmitRequest", "nein");

        // WIRD AKTUELL NICHT UNTERST�TZT:
        // Bei passiven Ports: �bertragung der Telegramme in Antwortrichtung (ja/nein)
        setProperty("tlsoip.C_XmitAnswer", "nein");

        // WIRD AKTUELL NICHT UNTERST�TZT:
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


//~Formatiert mit 'inovat Kodierkonvention' am 06.04.10
