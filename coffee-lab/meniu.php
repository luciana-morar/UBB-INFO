<?php
// 1. PORNEȘTE SESIUNEA IMEDIAT
session_start();

// 2. CONFIGURARE ERORI
error_reporting(E_ALL);
ini_set('display_errors', 1);

// 3. INCLUDE RESURSELE
require_once 'config/database.php';
require_once 'includes/auth.php';
requireAuth();

$pdo = Database::getPDO();
$userId = $_SESSION['user_id'];

// 4. PRELUARE DATE UTILIZATOR (pentru header)
$stmtUser = $pdo->prepare("SELECT * FROM users WHERE id = ?");
$stmtUser->execute([$userId]);
$user = $stmtUser->fetch();

$message = "";
$error = "";

// 5. PROCESARE COMANDĂ
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['plaseaza_comanda'])) {
    
    // Colectăm datele din formular (atributul 'name' din input-uri)
    $nume = $_POST['nume'] ?? '';
    $prenume = $_POST['prenume'] ?? '';
    $email = $_POST['email'] ?? '';
    $telefon = $_POST['telefon'] ?? '';
    
    $produse_alese = [];
    $total = 0;
    
    // Prețuri (trebuie să coincidă cu cele din calculatorjq.js)
    $config_preturi = [
        1 => ['nume' => 'Espresso', 'pret' => 8],
        2 => ['nume' => 'Latte Macchiato', 'pret' => 12],
        3 => ['nume' => 'Sandwich club', 'pret' => 15],
        4 => ['nume' => 'Cheesecake', 'pret' => 14]
    ];

    for ($i = 1; $i <= 4; $i++) {
        // Verificăm dacă checkbox-ul este bifat (name="prod1", "prod2"...)
        if (isset($_POST["prod$i"])) {
            $cantitate = isset($_POST["cant$i"]) ? intval($_POST["cant$i"]) : 0;
            
            if ($cantitate > 0) {
                $nume_p = $config_preturi[$i]['nume'];
                $total += $config_preturi[$i]['pret'] * $cantitate;
                $produse_alese[] = "$nume_p (x$cantitate)";
            }
        }
    }

    if ($total > 0) {
        try {
            $produse_string = implode(", ", $produse_alese);
            $sql = "INSERT INTO comenzi (user_id, nume, prenume, email, telefon, produse, total, status) 
                    VALUES (?, ?, ?, ?, ?, ?, ?, 'pending')";
            $stmt = $pdo->prepare($sql);
            $stmt->execute([$userId, $nume, $prenume, $email, $telefon, $produse_string, $total]);
            
            // Redirect pentru a evita retrimiterea la Refresh
            header("Location: meniu.php?success=1");
            exit();
        } catch (PDOException $e) {
            $error = "Eroare bază de date: " . $e->getMessage();
        }
    } else {
        $error = "Eroare: Nu ai selectat niciun produs sau cantitatea este 0!";
    }
}

// Dacă am primit succes prin URL
if (isset($_GET['success'])) {
    $message = "Comanda a fost salvată cu succes!";
}

// 6. PRELUARE COMENZI PENTRU TABELUL DE JOS
$stmtComenzi = $pdo->prepare("SELECT * FROM comenzi WHERE user_id = ? ORDER BY created_at DESC");
$stmtComenzi->execute([$userId]);
$comenzileMele = $stmtComenzi->fetchAll();
?>



<!DOCTYPE html>
<html lang="ro">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Coffee Lab - Meniu</title>
    <link rel="stylesheet" href="style-horizontal.css">
    <link rel="stylesheet" href="style-responsive.css">
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
    <script src="javascript/date.js"></script>
    <!-- <script src="jquery/validarijq.js"></script> -->
    <script src="jquery/calculatorjq.js"></script>
</head>
<body>
    <div class="container">
        <header>
            <h1>COFFEE LAB</h1>
            <p>Meniu</p>
            <p>Bine ai venit, <?php echo htmlspecialchars($user['nume'] ?? $user['username']); ?>! 
            <a href="php/logout.php" style="color: white;">Logout</a></p>
        </header>

        <nav>
            <ul>
                <li><a href="home.php">Acasa</a></li>
                <li><a href="meniu.php">Meniu</a></li>
                <li><a href="rezervari.php">Rezervari</a></li>
                <li><a href="comenzi.php">Comenzi</a></li>
                <li><a href="dashboard.php">Dashboard</a></li>
                <li><a href="upload.php">Profil</a></li>
                <?php if (function_exists('canApproveOrders') && canApproveOrders()): ?>
                <li><a href="admin_comenzi.php">Admin Comenzi</a></li>
                <li><a href="admin_rezervari.php">Admin Rezervări</a></li>
                <?php endif; ?>
                <li><a href="sprite-demo.html">Sprite</a></li>
            </ul>
        </nav>

        <script>
$(document).ready(function() {
    // Debug - vezi dacă formularul se trimite
    $('#comandaForm').on('submit', function(e) {
        console.log('Form submit triggered');
        // Nu face preventDefault aici
    });
});
</script>

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

        <h2>Everyday Hits</h2>

        <div class="menu-grid">
            <div class="menu-card">
                <h3>Coffee & Drinks</h3>
                <div class="table-responsive">
                    <table border="1" cellpadding="8" cellspacing="0" style="width:100%; border-collapse:collapse;">
                        <tr style="background:#60a5fa;"><th>Produs</th><th>Pret</th></tr>
                        <tr><td>Espresso</td><td>8 lei</td></tr>
                        <tr><td>Latte</td><td>12 lei</td></tr>
                        <tr><td>American</td><td>10 lei</td></tr>
                        <tr><td>Iced Latte</td><td>14 lei</td></tr>
                        <tr><td>Cappuccino</td><td>11 lei</td></tr>
                    </table>
                </div>
            </div>

            <div class="menu-card">
                <h3>Bites & Snacks</h3>
                <div class="table-responsive">
                    <table border="1" cellpadding="8" cellspacing="0" style="width:100%; border-collapse:collapse;">
                        <tr style="background:#60a5fa;"><th>Produs</th><th>Pret</th></tr>
                        <tr><td>Sandwich</td><td>15 lei</td></tr>
                        <tr><td>Toast cu avocado</td><td>18 lei</td></tr>
                        <tr><td>Cartofi wedges</td><td>12 lei</td></tr>
                        <tr><td>Wrap cu pui</td><td>16 lei</td></tr>
                    </table>
                </div>
            </div>

            <div class="menu-card">
                <h3>Desserts</h3>
                <div class="table-responsive">
                    <table border="1" cellpadding="8" cellspacing="0" style="width:100%; border-collapse:collapse;">
                        <tr style="background:#60a5fa;"><th>Produs</th><th>Pret</th></tr>
                        <tr><td>Cheesecake</td><td>14 lei</td></tr>
                        <tr><td>Brownie</td><td>10 lei</td></tr>
                        <tr><td>Tiramisu</td><td>16 lei</td></tr>
                        <tr><td>Blueberry Tart</td><td>12 lei</td></tr>
                    </table>
                </div>
            </div>
        </div>

        <h2>Setlist Specials</h2>
        <div class="table-responsive">
            <table border="1" cellpadding="8" cellspacing="0" style="width:100%; border-collapse:collapse;">
                <tr style="background:#60a5fa;">
                    <th rowspan="2">Oferta</th>
                    <th colspan="2">Descriere</th>
                    <th rowspan="2">Pret</th>
                </tr>
                <tr style="background:#60a5fa;">
                    <th>Include</th>
                    <th>Extra</th>
                </tr>
                <tr><td><strong>Solo Set</strong></td><td>1 bautura + 1 gustare</td><td>Apa plata</td><td>25 lei</td></tr>
                <tr><td><strong>Duet Deal</strong></td><td>2 bauturi + 2 gustari</td><td>2 ape plate</td><td>45 lei</td></tr>
                <tr><td><strong>Band Bundle</strong></td><td>4 bauturi + platou gustari</td><td>4 ape + 1 desert</td><td>80 lei</td></tr>
            </table>
        </div>

        <!-- FORMULAR COMANDA -->
        <form method="POST" action="" id="comandaForm">
            <fieldset>
                <legend>Formular comanda</legend>
                
                <div class="form-row">
                    <div class="form-group">
                        <label>Nume:</label>
                        <input type="text" name="nume" id="comandaNume" placeholder="Nume" value="<?php echo htmlspecialchars($user['nume'] ?? ''); ?>" required>
                    </div>
                    <div class="form-group">
                        <label>Prenume:</label>
                        <input type="text" name="prenume" id="comandaPrenume" placeholder="Prenume" value="<?php echo htmlspecialchars($user['prenume'] ?? ''); ?>" required>
                    </div>
                </div>
                
                <div class="form-row">
                    <div class="form-group">
                        <label>Email:</label>
                        <input type="email" name="email" id="comandaEmail" placeholder="email@exemplu.ro" value="<?php echo htmlspecialchars($user['email'] ?? ''); ?>" required>
                    </div>
                    <div class="form-group">
                        <label>Telefon:</label>
                        <input type="text" name="telefon" id="comandaTelefon" placeholder="Telefon (10 cifre)" value="<?php echo htmlspecialchars($user['telefon'] ?? ''); ?>">
                    </div>
                </div>
                
                <h4>Produse comandate</h4>
                <div class="table-responsive">
                    <table border="1" cellpadding="8" cellspacing="0" style="width:100%; border-collapse:collapse;">
                        <tr style="background:#60a5fa;">
                            <th>Selecteaza</th><th>Produs</th><th>Pret</th><th>Cantitate</th>
                        </tr>
                        <tr>
                            <td><input type="checkbox" name="prod1" checked></td>
                            <td>Espresso</td>
                            <td>8 lei</td>
                            <td><input type="number" name="cant1" value="1" min="0" max="10"></td>
                        </tr>
                        <tr>
                            <td><input type="checkbox" name="prod2"></td>
                            <td>Latte</td>
                            <td>12 lei</td>
                            <td><input type="number" name="cant2" value="0" min="0" max="10"></td>
                        </tr>
                        <tr>
                            <td><input type="checkbox" name="prod3"></td>
                            <td>Sandwich</td>
                            <td>15 lei</td>
                            <td><input type="number" name="cant3" value="0" min="0" max="5"></td>
                        </tr>
                        <tr>
                            <td><input type="checkbox" name="prod4"></td>
                            <td>Cheesecake</td>
                            <td>14 lei</td>
                            <td><input type="number" name="cant4" value="0" min="0" max="5"></td>
                        </tr>
                    </table>
                </div>
                
                <div class="form-row">
                    <div class="form-group">
                        <label>Tip comanda:</label><br>
                        <input type="radio" name="tip" value="Ridicare" checked> Ridicare 
                        <input type="radio" name="tip" value="Livrare"> Livrare
                    </div>
                    <div class="form-group">
                        <label>Metoda plata:</label>
                        <select name="plata">
                            <option value="Cash">Cash</option>
                            <option value="Card">Card</option>
                        </select>
                    </div>
                </div>
                
                <div class="form-group">
                    <label>Alergii:</label>
                    <select name="alergii[]" multiple size="3">
                        <option value="Fara alergii">Fara alergii</option>
                        <option value="Gluten">Gluten</option>
                        <option value="Lactate">Lactate</option>
                        <option value="Nuci">Nuci</option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label>Observatii:</label>
                    <textarea name="observatii" rows="3"></textarea>
                </div>
                
                <!-- Calculator bacsis -->
                <div class="calculator-bacsis">
                    <h4>Calculator bacșiș</h4>
                    <div class="form-row">
                        <div class="form-group">
                            <label>Total comandă (lei):</label>
                            <input type="number" id="totalComanda" name="total_comanda" readonly>
                        </div>
                        <div class="form-group">
                            <label>Alege procent bacșiș:</label>
                            <select id="procentBacsis" name="procent_bacsis">
                                <option value="0">0%</option>
                                <option value="10" selected>10%</option>
                                <option value="15">15%</option>
                                <option value="20">20%</option>
                            </select>
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label>Bacșiș (lei):</label>
                            <input type="text" id="bacsisLei" readonly>
                        </div>
                        <div class="form-group">
                            <label>Total de plată (lei):</label>
                            <input type="text" id="totalPlata" readonly>
                        </div>
                    </div>
                    <p class="bacsis-mesaj" id="bacsisMesaj"></p>
                </div>
                
                <div class="form-buttons">
                    <input type="submit" name="plaseaza_comanda" value="Plaseaza comanda"> 
                    <input type="reset" value="Reseteaza">
                </div>
            </fieldset>
        </form>
        <div id="comandaErrors"></div>

        <!-- ========== TABEL COMANZI ========== -->
        <h2>Comenzile mele</h2>
        <div class="table-responsive">
            <table border="1" cellpadding="10" cellspacing="0" style="width:100%; border-collapse:collapse; background:white;">
                <tr style="background:#3b82f6; color:white;">
                    <th>ID</th>
                    <th>Produse</th>
                    <th>Total</th>
                    <th>Status</th>
                    <th>Data</th>
                </tr>
                <?php if (count($comenzileMele) > 0): ?>
                    <?php foreach ($comenzileMele as $comanda): ?>
                        <tr style="border-bottom:1px solid #ddd;">
                            <td><?php echo htmlspecialchars($comanda['id']); ?></td>
                            <td><?php echo nl2br(htmlspecialchars(substr($comanda['produse'], 0, 100))); ?>...</td>
                            <td><?php echo htmlspecialchars($comanda['total']); ?> lei</td>
                            <td style="font-weight:bold; 
                                <?php if($comanda['status'] == 'pending') echo 'color:orange;';
                                elseif($comanda['status'] == 'approved') echo 'color:green;';
                                elseif($comanda['status'] == 'rejected') echo 'color:red;'; ?>">
                                <?php echo htmlspecialchars($comanda['status']); ?>
                            </td>
                            <td><?php echo htmlspecialchars($comanda['created_at']); ?></td>
                        </tr>
                    <?php endforeach; ?>
                <?php else: ?>
                    <tr><td colspan="5" style="padding:20px; text-align:center;">Nu ai plasat nicio comandă încă.</td></tr>
                <?php endif; ?>
            </table>
        </div>
        <!-- =================================== -->

        <h2>Statistici saptamanale</h2>
        <div class="table-responsive">
            <table border="1" cellpadding="8" cellspacing="0" style="width:100%; border-collapse:collapse;">
                <tr style="background:#60a5fa;">
                    <th rowspan="2">Categorie</th>
                    <th colspan="2">Comenzi saptamanale</th>
                    <th rowspan="2">Total</th>
                </tr>
                <tr style="background:#60a5fa;">
                    <th>Cantitate</th>
                    <th>Valoare</th>
                </tr>
                <tr><td>Cafea</td><td>245</td><td>2.450 lei</td><td rowspan="3">5.890 lei</td></tr>
                <tr><td>Gustari</td><td>180</td><td>2.160 lei</td></tr>
                <tr><td>Deserturi</td><td>160</td><td>1.280 lei</td></tr>
            </table>
        </div>

        <p><b>Nota:</b> Toate preturile sunt in lei si includ TVA.</p>

        <footer>
            <p>Coffee Lab - Meniu variat</p>
        </footer>
    </div>
</body>
</html>