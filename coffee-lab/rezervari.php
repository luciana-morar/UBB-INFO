<?php
// rezervari.php
session_start();
require_once 'config/database.php';
require_once 'includes/auth.php';
requireAuth();

$message = '';
$error = '';
$pdo = Database::getPDO();

// Preia datele utilizatorului pentru precompletare
$stmt = $pdo->prepare("SELECT * FROM users WHERE id = ?");
$stmt->execute([$_SESSION['user_id']]);
$user = $stmt->fetch();

// ========== IMPORTANT: Adaugă ACEST COD ==========
// Preia rezervările existente din baza de date
$stmt = $pdo->prepare("SELECT * FROM rezervari WHERE user_id = ? ORDER BY data DESC");
$stmt->execute([$_SESSION['user_id']]);
$rezervari = $stmt->fetchAll();  // ← ACEASTA LINIE LIPSEA!
// =================================================

// Procesare rezervare
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = $_POST['data'] ?? '';
    $ora = $_POST['ora'] ?? '';
    $persoane = $_POST['persoane'] ?? 2;
    $locatie = $_POST['locatie'] ?? '';
    $cerinte = $_POST['observatii'] ?? '';
    $termeni = isset($_POST['termeni']);
    
    if (!empty($data) && !empty($ora) && $termeni) {
        $insertStmt = $pdo->prepare("INSERT INTO rezervari (user_id, data, ora, persoane, locatie, cerinte_speciale) 
                                      VALUES (?, ?, ?, ?, ?, ?)");
        $insertStmt->execute([$_SESSION['user_id'], $data, $ora, $persoane, $locatie, $cerinte]);
        $message = "Rezervare confirmată! Vă așteptăm cu drag!";
        
        // Reîncarcă rezervările după inserare
        $stmt->execute([$_SESSION['user_id']]);
        $rezervari = $stmt->fetchAll();
    } else {
        $error = "Completează toate câmpurile obligatorii și acceptă termenii!";
    }
}
?>
<!DOCTYPE html>
<html lang="ro">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Coffee Lab - Rezervari</title>
    <link rel="stylesheet" href="style-horizontal.css">
    <link rel="stylesheet" href="style-responsive.css">
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
    <script src="javascript/date.js"></script>
    <script src="jquery/validarijq.js"></script>
    <script src="jquery/eveimentejq.js"></script>
</head>
<body>
    <div class="container">
        <header>
            <h1>COFFEE LAB</h1>
            <p>Rezervari</p>
            <p><a href="php/logout.php" style="color: white;">Logout</a></p>
        </header>

<nav>
    <ul>
        <li><a href="home.php">Acasa</a></li>
        <li><a href="meniu.php">Meniu</a></li>
        <li><a href="rezervari.php">Rezervari</a></li>
        <li><a href="comenzi.php">Comenzi</a></li>  <!-- ← Adaugă asta -->
        <li><a href="dashboard.php">Dashboard</a></li>
        <li><a href="upload.php">Profil</a></li>
        <?php if (canApproveOrders()): ?>
        <li><a href="admin_comenzi.php">Admin Comenzi</a></li>
        <li><a href="admin_rezervari.php">Admin Rezervări</a></li>
        <?php endif; ?>
        <li><a href="sprite-demo.html">Sprite</a></li>
    </ul>
</nav>

        <!-- Afișează mesaje de succes/eroare -->
        <?php if ($message): ?>
            <div style="background: #d4edda; color: #155724; padding: 15px; border-radius: 8px; margin: 15px 0;">
                <?php echo htmlspecialchars($message); ?>
            </div>
        <?php endif; ?>
        <?php if ($error): ?>
            <div style="background: #f8d7da; color: #721c24; padding: 15px; border-radius: 8px; margin: 15px 0;">
                <?php echo htmlspecialchars($error); ?>
            </div>
        <?php endif; ?>

        <!-- Evenimente -->
        <h2>Evenimente Noiembrie 2026</h2>
        <div class="table-responsive">
            <table class="evenimente-table">
                <thead>
                    <tr><th>Data</th><th>Eveniment</th><th>Descriere</th></tr>
                </thead>
                <tbody>
                    <tr><td>7 Noiembrie</td><td><strong>Degustare deserturi</strong></td><td>CandyBar deschis cu cele mai inedite deserturi</td></tr>
                    <tr><td>11 Noiembrie</td><td><strong>Open Mic</strong></td><td>Canta, recita sau doar asculta</td></tr>
                    <tr><td>14 Noiembrie</td><td><strong>Atelier de creatie</strong></td><td>Pictura, sculptura si arte plastice</td></tr>
                    <tr><td>17 Noiembrie</td><td><strong>DJ Night</strong></td><td>House beats si groove-uri de seara</td></tr>
                    <tr><td>25 Noiembrie</td><td><strong>Acoustic Session</strong></td><td>Voci calde si magie acustica</td></tr>
                </tbody>
            </table>
        </div>

        <div id="evenimentMessage" class="eveniment-message"></div>

        <!-- Formular rezervare -->
        <form method="POST">
            <fieldset>
                <legend>Formular rezervare</legend>
                
                <div class="form-row">
                    <div class="form-group">
                        <label>Nume:</label>
                        <input type="text" name="nume" value="<?php echo htmlspecialchars($user['nume'] ?? ''); ?>" required>
                    </div>
                    <div class="form-group">
                        <label>Prenume:</label>
                        <input type="text" name="prenume" value="<?php echo htmlspecialchars($user['prenume'] ?? ''); ?>" required>
                    </div>
                </div>
                
                <div class="form-row">
                    <div class="form-group">
                        <label>Email:</label>
                        <input type="email" name="email" value="<?php echo htmlspecialchars($user['email'] ?? ''); ?>" required>
                    </div>
                    <div class="form-group">
                        <label>Telefon:</label>
                        <input type="tel" name="telefon" value="<?php echo htmlspecialchars($user['telefon'] ?? ''); ?>">
                    </div>
                </div>
                
                <h4>Detalii rezervare</h4>
                <div class="form-row">
                    <div class="form-group">
                        <label>Data:</label>
                        <input type="date" name="data" value="<?php echo date('Y-m-d'); ?>" required>
                    </div>
                    <div class="form-group">
                        <label>Ora:</label>
                        <select name="ora">
                            <option>18:00</option>
                            <option>19:00</option>
                            <option>20:00</option>
                            <option>21:00</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Numar persoane:</label>
                        <input type="number" name="persoane" value="2" min="1" max="20" required>
                    </div>
                </div>
                
                <div class="form-group">
                    <label>Locatie preferata:</label>
                    <select name="locatie">
                        <option>Indiferent</option>
                        <option>La fereastra</option>
                        <option>Langă scena</option>
                        <option>Terasa</option>
                    </select>
                </div>
                
                <div class="checkbox-group">
                    <strong>Preferinte suplimentare:</strong><br>
                    <input type="checkbox" name="scaunCopil"> Scaun copil<br>
                    <input type="checkbox" name="accesDizabilitati"> Acces dizabilitati<br>
                    <input type="checkbox" name="meniuVegetarian"> Meniu vegetarian
                </div>
                
                <div class="form-group">
                    <label>Alergii alimentare:</label>
                    <select name="alergii" multiple size="3">
                        <option>Fara alergii</option>
                        <option>Gluten</option>
                        <option>Lactate</option>
                        <option>Nuci</option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label>Eveniment:</label>
                    <select name="eveniment">
                        <option>Fara eveniment</option>
                        <option>Degustare deserturi</option>
                        <option>Open Mic</option>
                        <option>Atelier de creatie</option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label>Cerinte speciale:</label>
                    <textarea name="observatii" rows="4"></textarea>
                </div>
                
                <div class="checkbox-group">
                    <input type="checkbox" name="noutati" checked> Doresc noutati despre evenimente<br>
                </div>
                
                <span id="termeniContainer">
                    <input type="checkbox" name="termeni" id="rezervareTermeni" required> Accept termenii si conditiile *
                </span>

                <!-- JUDEȚ → LOCALITATE -->
                <h4>Locație eveniment (dacă este cazul)</h4>
                <div class="form-row">
                    <div class="form-group">
                        <label>Județ:</label>
                        <select id="judetSelect">
                            <option value="">-- Alege județul --</option>
                            <option value="București">București</option>
                            <option value="Cluj">Cluj</option>
                            <option value="Timiș">Timiș</option>
                            <option value="Brașov">Brașov</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Localitate:</label>
                        <select id="localitateSelect">
                            <option>-- Alege mai întâi județul --</option>
                        </select>
                    </div>
                </div>

                <!-- VARSTA → EVENIMENTE -->
                <h4>Evenimente recomandate în funcție de vârstă</h4>
                <div class="form-row">
                    <div class="form-group">
                        <label>Vârsta ta:</label>
                        <input type="number" id="varstaParticipant" placeholder="Ex: 25" min="1" max="120">
                        <small>Completează pentru a vedea evenimente recomandate</small>
                    </div>
                    <div class="form-group">
                        <label>Evenimente recomandate:</label>
                        <select id="evenimenteRecomandate" size="4">
                            <option>-- Alege vârsta mai întâi --</option>
                        </select>
                    </div>
                </div>
                
                <div class="form-buttons">
                    <input type="submit" value="Confirma rezervarea">
                    <input type="reset" value="Reseteaza">
                </div>
                
                <p style="font-size: 12px; color: red; margin-top: 10px;">* Campuri obligatorii</p>
            </fieldset>
        </form>

        <!-- În rezervari.php, la tabelul "Rezervările mele" -->
<h2>Rezervările mele</h2>
<div class="table-responsive">
    <table>
        <thead>
            <tr>
                <th>Data</th>
                <th>Ora</th>
                <th>Persoane</th>
                <th>Status</th>
            </tr>
        </thead>
        <tbody>
            <?php foreach ($rezervari as $rez): ?>
            <tr>
                <td><?php echo htmlspecialchars($rez['data']); ?></td>
                <td><?php echo htmlspecialchars($rez['ora']); ?></td>
                <td><?php echo htmlspecialchars($rez['persoane']); ?></td>
                <td class="status-<?php echo $rez['status']; ?>"><?php echo htmlspecialchars($rez['status']); ?></td>
            </tr>
            <?php endforeach; ?>
        </tbody>
    </table>
</div>
        <!-- Mese disponibile -->
        <h2>Mese disponibile</h2>
        <div class="table-responsive">
            <table>
                <thead><tr><th>Data</th><th>Masa</th><th>Locatie</th></tr></thead>
                <tbody>
                    <tr><td rowspan="3">7 Noiembrie</td><td>Masa 1 (2 persoane)</td><td>Langă scena</td></tr>
                    <tr><td>Masa 2 (4 persoane)</td><td>Fereastra</td></tr>
                    <tr><td>Masa 3 (6 persoane)</td><td>Centru</td></tr>
                    <tr><td rowspan="2">14 Noiembrie</td><td>Masa 4 (2 persoane)</td><td>Terasa</td></tr>
                    <tr><td>Masa 5 (4 persoane)</td><td>Langă scena</td></tr>
                </tbody>
            </table>
        </div>

        <div class="policies">
            <h4>Politici de rezervare</h4>
            <ol>
                <li><strong>Confirmare</strong> - cu 24h inainte</li>
                <li><strong>Anulare</strong> - gratuita pana la 12h inainte</li>
                <li><strong>Intarziere</strong> - masa se pastreaza 15 minute</li>
            </ol>
        </div>

        <img src="images/cafe.jpeg" alt="Interior Coffee Lab" class="interior-img">

        <footer>
            <p>Coffee Lab - Rezerva-ti locul cat mai curand!</p>
        </footer>
    </div>
</body>
</html>