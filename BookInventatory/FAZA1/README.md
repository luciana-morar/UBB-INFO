# Sistem de inventar pentru o mică librărie 


### [ Vezi documentul UseCases pentru mai multe informatii ](Usecases_Library.pdf) </p>

## Descriere
Acest proiect reprezintă un sistem simplu de gestionare a inventarului pentru o mică librărie. 
Aplicația permite administrarea cărților disponibile în stoc, urmărirea cantităților și gestionarea informațiilor despre produse.

Scopul proiectului este de a facilita organizarea inventarului și de a simplifica procesul de adăugare, actualizare și căutare a cărților.

## Funcționalități
- Adăugarea unei cărți noi în inventar
- Ștergerea unei cărți din sistem
- Actualizarea informațiilor despre o carte
- Afișarea tuturor cărților disponibile
- Căutarea unei cărți după titlu sau autor
- Gestionarea stocului (cantitate disponibilă)

## Structura proiectului

Exemplu de organizare a fișierelor:

```
project/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
├── pom.xml
└── README.md
```


## Tehnologii utilizate

Acest proiect este o aplicație web de tip client-server construită folosind următoarele tehnologii:

- **Spring Boot** – framework utilizat pentru dezvoltarea backend-ului aplicației
- **Java** – limbajul principal de programare
- **PostgreSQL** – sistem de gestionare a bazei de date relaționale
- **HTML & CSS** – utilizate pentru realizarea interfeței utilizator
