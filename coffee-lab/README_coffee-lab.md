# ☕ Coffee Lab

O aplicație web pentru o cafenea de specialitate, dezvoltată ca proiect academic la UBB Informatică. Permite clienților să facă rezervări, să plaseze comenzi și să vadă meniul, iar angajaților și administratorilor să gestioneze cererile din panoul de administrare.

---

## 📋 Descriere

Coffee Lab simulează sistemul informatic al unei cafenele reale, cu autentificare bazată pe roluri, management de rezervări și comenzi, profil utilizator cu upload de imagine, jurnal de activitate și elemente interactive în interfață.

---

## 🛠️ Tehnologii folosite

| Tehnologie | Utilizare |
|---|---|
| **PHP** | Backend, logică server-side, sesiuni |
| **MySQL (PDO)** | Bază de date principală (utilizatori, comenzi, rezervări) |
| **SQLite** | Bază de date adițională pentru loguri de activitate |
| **HTML5 / CSS3** | Structură și stilizare pagini |
| **JavaScript** | Interactivitate client-side (carusel, liste colapsibile, calculator bacșiș) |
| **jQuery** | Validări formulare, sortare tabele, evenimente |

---

## 📁 Structură proiect

```
coffee-lab/
│
├── index.php                  # Redirect către login sau dashboard
├── home.php                   # Pagina principală (despre, program, contact)
├── meniu.php                  # Meniu cafenea (băuturi, deserturi, gustări)
├── rezervari.php              # Formular rezervare + istoricul rezervărilor
├── comenzi.php                # Istoricul comenzilor utilizatorului
├── dashboard.php              # Statistici, carusel promoții, feedback, vânzări
├── upload.php                 # Profil utilizator + upload poză
├── logs.php                   # Jurnal activitate (doar admin/angajat)
├── admin_comenzi.php          # Panou administrare comenzi
├── admin_rezervari.php        # Panou administrare rezervări
├── sprite-demo.html           # Demonstrație CSS sprites
│
├── config/
│   └── database.php           # Conexiune MySQL + SQLite
│
├── includes/
│   └── auth.php               # Autentificare și verificare roluri
│
├── php/
│   ├── login.php              # Pagina de autentificare
│   └── logout.php             # Deconectare sesiune
│
├── javascript/
│   └── date.js                # Funcții JS pentru dată/oră și oferte speciale
│
├── jquery/
│   ├── validarijq.js          # Validări formulare cu jQuery
│   ├── tabeleSortatejq.js     # Sortare tabele cu jQuery
│   └── eveimentejq.js         # Evenimente interactive (județ → localitate, vârstă → evenimente)
│
├── images/                    # Imagini statice (logo, interior)
├── uploads/                   # Poze de profil încărcate de utilizatori
├── coffee_lab.sqlite          # Baza de date SQLite pentru loguri
│
├── style-horizontal.css       # Stil cu navigație orizontală + submeniu dropdown
├── style-responsive.css       # Stil responsive (mobile-first, media queries)
├── style-vertical.css         # Stil alternativ cu navigație verticală
└── style-sprite.css           # Stil pentru demonstrația CSS sprites
```

---

## 👥 Sistem de roluri

Aplicația are trei tipuri de utilizatori:

- **Client** — poate face rezervări, plasa comenzi, vedea meniul și edita profilul
- **Angajat** — are acces suplimentar la panourile de administrare comenzi și rezervări
- **Admin** — acces complet, inclusiv la jurnalul de activitate (loguri)

---

## ✨ Funcționalități principale

- **Autentificare** cu sesiuni PHP și verificare rol
- **Rezervări** cu formular complet (dată, oră, număr persoane, locație, cerințe speciale, județ/localitate)
- **Comenzi** cu istoric și statusuri (pending / approved / rejected)
- **Panou admin** pentru aprobare/respingere rezervări și comenzi
- **Profil utilizator** cu upload poză (JPG, PNG, GIF, max 2MB)
- **Dashboard** cu carusel promoții, meniu interactiv colapsibil, formular feedback, tabele cu sortare și calculator bacșiș
- **Loguri SQLite** — înregistrează login, logout și plasare comenzi
- **Navigație responsivă** — trei variante de meniu (orizontal cu submeniu, vertical, cu sprites)
- **Validări jQuery** pe formulare (rezervări, feedback)

---

## 🚀 Instalare și rulare

### Cerințe
- PHP >= 7.4
- MySQL / MariaDB
- SQLite3 (extensie PHP activată)
- Server local: XAMPP, WAMP, Laragon sau similar

### Pași

1. Clonează repository-ul:
   ```bash
   git clone https://github.com/luciana-morar/UBB-INFO.git
   cd UBB-INFO/coffee-lab
   ```

2. Importă schema bazei de date MySQL (creează tabelele `users`, `rezervari`, `comenzi`):
   ```bash
   mysql -u root -p < config/schema.sql
   ```

3. Configurează conexiunea în `config/database.php` cu datele tale (host, user, parolă, nume DB).

4. Asigură-te că folderul `uploads/` are permisiuni de scriere:
   ```bash
   chmod 777 uploads/
   ```

5. Pornește serverul local și accesează `http://localhost/coffee-lab/`.

---

## 📸 Pagini principale

| Pagină | Descriere |
|---|---|
| `/home.php` | Prezentare cafenea, program, contact |
| `/meniu.php` | Meniu cu categorii |
| `/rezervari.php` | Rezervare masă + calendar evenimente |
| `/dashboard.php` | Statistici personale, promoții, feedback |
| `/admin_comenzi.php` | Gestionare comenzi (admin/angajat) |
| `/admin_rezervari.php` | Gestionare rezervări (admin/angajat) |

---

## 📚 Context academic

Proiect realizat în cadrul cursului de **Tehnologii Web** la Universitatea Babeș-Bolyai, Facultatea de Matematică și Informatică.
