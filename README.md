# mdh-driftavbrott-facade

En Java-klient som kan kommunicera med mdh-driftavbrott-service. Den här
komponenten används av mdh-driftavbrott-filter, men är intressant om du vill
integrera driftavbrott i något annat än en webbapplikation.

## Användning

Här följer ett exempel på lite Java-kod:

```
    DriftavbrottFacade facade = new DriftavbrottFacade();
    Driftavbrott driftavbrott = null;
    List<String> kanaler = new ArrayList<>();
    kanaler.add("ladok.backup");
    try {
      driftavbrott = facade.getPagaendeDriftavbrott(kanaler, "mitt-system");
      System.out.println("Hämtade detta driftavbrott:" + driftavbrott);
      // Hantera driftavbrott
    }
    catch (WebServiceException wse) {
      log.warn("Det gick inte att hämta information om pågående driftavbrott.", wse);
      // Felhantering
    }
```

## Konfigurering

Klassen `DriftavbrottFacade` behöver konfigurationsfilen
`se.mdh.driftavbrott.properties` som ska innehålla en URL till
mdh-driftavbrott-service. Till exempel så här:

```
se.mdh.driftavbrott.service.url=http://localhost:3301/mdh-driftavbrott/v1
```

## Loggning

Klienten använder Commons Logging som loggnings-API.

Klienten använder Apache CXF för att göra REST-anrop. Loggning från CXF styrs
till SLF4J, så om konsumerande applikation vill ha loggning från CXF behöver den
dirigera om loggning från SLF4J till önskad loggningsimplementation.
