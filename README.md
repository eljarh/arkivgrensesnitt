# Arkivgrensesnitt

Denne applikasjonen er en enkel proxy som tar i mot RDF-fragmenter og arkiverer disse i ePhorte.

## Funksjonelle krav

Her er en kjapp gjennomgang av de funskjonelle kravene til applikasjonen

### Dynamisk mapping

Tjenesten skal dynamisk mapping fra RDF-en den mottar til den interne modellen til ePhorte. Det vil si at om det opprettes et nytt felt "foo" i ePhorte skal det være nok å lage RDF-properties som ender på "/foo" for å skrive til det feltet. Det skal ikke være harde bindinger fra koden til felter og objekter i ePhorte.

### Flere dokumenter under en sak

Dokumenter må kunne referere til en sak i ePhorte og i så fall bli arkivert under denne.

### Oppdatering = Ny versjon

RDF-en som mottas skal inneholde ekstern ID på dokumentet (dette er URL-en som er subjekt i RDF-fragmentet). Dersom det allerede finnes et dokument i ePhorte med denne ID-en skal filen som mottas bli en ny versjon av dokumentet.

### Feilrapportering

Tjenesten skal, så lenge det i det hele tatt er mulig, sørge for at dokumentet faktisk kommer inn i ePhorte. Dersom det er problemer med dokumentet skal dette helst markeres på en eller annen måte i ePhorte slik at mennesker kan ta tak i det. Dersom tjenesten ikke klarer å få inn dokumentet skal den feile med HTTP 500-status og en mest mulig fornuftig feilmelding. Det er da opp til SDShare-klienten å logge feilen.

### Støtte for å referere til andre objekter

Vi oppretter ikke andre objekter. Vi kan ha referanser til kontakter og lignende, men vi lager ikke nye kontakter.

## Teknologivalg

Applikasjonen skal kjøre i en servlet container.  Den skal være enkel å vedlikeholde og feilsøke.  Dette tilsier at vi ønsker få avhengigheter som man må kjenne til for å kunne forstå løsningen.

### REST

For å passe inn i SESAM-arkitekturen implementeres proxyen som en REST-basert webtjeneste.

#### Jersey

Vi har erfaring med jersey til å skrive denne typen applikasjoner.  Dette er referanseimplementasjonen av JAX-RS standarden og dermed et modent og pålitelig valg for å lage REST-baserte webtjenester.

### RDF

Vi må kunne gjøre følgende:

-   Deserialisere RDF til en ePhorte DTO

RDF har mange forskjellige representasjoner, mellom annet RDF/XML, NTriples, Turtle, etc.  For å redusere kompleksiteten til løsningen har vi valgt å begrense oss til å støtte [RDF 1.1 NTriples](http://www.w3.org/TR/n-triples/).  Dette er et format som er designet for å være enkelt å parse, så vi velger derfor å skrive en enkel parser i stedet for å dra inn avhengigheter som Jena eller Sesame.

### SOAP

Integrasjon med ePhorte gjøres via SOAP-kall.  Vi bruker java 6 sin innebygde støtte for SOAP og
generering av stubs til dette.

## Ressurser

Denne webtjenesten eksponerer følgende ressurser:

-   **GET /:** Returnerer et skjema for å sende inn et fragment, fungerer som dokumentasjon for tjenesten i tillegg til at det gir oss en enkel måte å teste tjenesten på

-   **POST /fragment{?subject}:** Aksepterer [application/n-triples](http://www.w3.org/TR/n-triples/), subject spesifiserer hvilket subjekt i dokumentet som skal skrives og POST-body inneholder selve fragmentet

