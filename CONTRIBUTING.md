 Hinweise zur Bearbeitung / Beteiligung
======================================

Für Änderungen an dem Projekt ist  zuerst ein Eintrag (Issue) mit dem Tag "Feature-Wunsch" oder "Bug" 
anzulegen, damit die übernommen Änderungen auch einem Problem und einer potentiell erfolgten Diskussion
zugeordnet werden können. Solange das Fehler- und Änderungsmanagement noch über die bisherigen NERZ-Bugtracker 
abgewickelt wird, ist zudem eine Referenz auf den dortigen Eintrag vorzunehmen.

Vor der Bearbeitung sollte man den entsprechenden Eintrag persönlich übernehmen
und einen Bugfix- oder Feature-Branch anlegen.

Feature-Branches werden benannt als "feature/{name}", Bugfix-Branches als "hotfix/{name}".
Der Name sollte beschreiben, was innerhalb des Branches geändert wird und **nicht den Name des Bearbeiters**.

Feature-Wünsche werden auf Basis des "develop"-Branches umgesetzt und führen letztendlich zu einem Release einer
neuen Version auf dem zweiten Level, d.h. 0.1.0 wird mindestens 0.2.0 oder in der Hauptversion, d. h. aus z. B. 1.x.y wird 2.0.0, wenn **keine** Abwärtskompatibilität besteht.

Bugfixes sollten auf dem "master"-Branch beruhen und führen zu einer neuen Version auf dem dritten Level, d.h. aus 0.1.0 wird 0.1.1.
Die Änderungen werden dann natürlich auch in den "develop"-Branch gemerged. Damit können Bugfixes kurzfristig erfolgen
ohne den Zwang gleich alle Änderungen, die sich schon im Develop-Zweig befinden mit zu veröffentlichen.

Werden im Rahmen eines Auftrags mehrere Feature-Wünsche oder Bugfixes bearbeitet, kann auch einheitlich vom "develop"-Branch gemerged werden (konkrete Abstimmung im Zweifelsfall mit der NERZ-FTB).

Branches sollten nur für einen **einzelnen Eintrag** angelegt werden.
Die Branches können nach dem Merge in den Ursprungs-Branch entfernt werden (Das Löschen erfolgt automatisch, 
wenn der entsprechende Haken beim Anlegen des Merge-Request gesetzt wird).

Ein Merge-Request sollte folgende Punkte berücksichtigen:

- die Änderungen, die mit dem Request verbunden sind sollten in kurzer prägnanter Form in das CHANGELOG-File eingetragen werden. Der Eintrag erfolgt dabei im Abschnitt "Noch nicht veröffentlicht". Die Versionsnummer wird dort erst beim Release ergänzt (also letztlich bei der Übernahme durch die NERZ-FTB).
- wenn es notwendig ist, neue Features oder Änderungen zu beschreiben muss das README-File angepasst werden
- Änderungen am Code sollten keinen auskommentierten alten Code enthalten, für den Zugriff auf die Historie ist ja GIT vorgesehen
- die bearbeiteten Einträge sollten im Kommentar für den jeweiligen Commit oder für den Merge-Request mit "Fixes #{Eintragsnummer}" um die Zuordnung zu erhalten und das automatische Schließen zu ermöglichen

**Abgelehnte Merge-Request brauchen nicht gelöscht werden!** 
Angemerkte und diskutierte Probleme, die eine Übernahme verhindern, sollten stattdessen bearbeitet werden bis der Merge-Request übernommen werden kann. **Ein neuer Request ist nicht erforderlich!**