# mdh-driftavbrott-ws-client

En Java-klient som kan kommunicera med mdh-driftavbrott-service. Den h�r
komponenten anv�nds av mdh-driftavbrott-filter, men �r intressant om du vill
integrera driftavbrott i n�got annat �n en webbapplikation.

## Anv�ndning

H�r f�ljer ett exempel p� lite Java-kod:

```
    DriftavbrottFacade facade = new DriftavbrottFacade();
    Driftavbrott driftavbrott = null;
    List<String> kanaler = new ArrayList<>();
    kanaler.add("ladok.backup");
    try {
      driftavbrott = facade.getPagaendeDriftavbrott(kanaler, "mitt-system");
      System.out.println("H�mtade detta driftavbrott:" + driftavbrott);
      // Hantera driftavbrott
    }
    catch (WebServiceException wse) {
      log.warn("Det gick inte att h�mta information om p�g�ende driftavbrott.", wse);
      // Felhantering
    }
```

## Konfigurering

Klassen `DriftavbrottFacade` beh�ver konfigurationsfilen
`se.mdh.driftavbrott.properties` som ska inneh�lla en URL till
mdh-driftavbrott-service. Till exempel s� h�r:

```
se.mdh.driftavbrott.service.url=http://localhost:3301/mdh-driftavbrott/v1
```
