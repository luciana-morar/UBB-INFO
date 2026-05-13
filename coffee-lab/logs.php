<?php
// logs.php - Demonstrează folosirea bazei de date SQLite
session_start();
require_once 'config/database.php';
require_once 'includes/auth.php';

// Doar admin și angajat pot vedea logurile (opțional)
if (!canApproveOrders()) {
    die('Acces interzis! Doar administratorii și angajații pot vedea logurile.');
}

$sqlite = Database::getSQLite();
$logs = [];

if ($sqlite) {
    try {
        $stmt = $sqlite->query("SELECT id, user_id, action, timestamp FROM logs ORDER BY timestamp DESC LIMIT 50");
        $logs = $stmt->fetchAll(PDO::FETCH_ASSOC);
    } catch (PDOException $e) {
        $error = "Eroare la citirea logurilor: " . $e->getMessage();
    }
}

// Obține numele utilizatorilor din MySQL pentru afișare
$pdo = Database::getPDO();
$users = [];
$stmt = $pdo->query("SELECT id, username FROM users");
while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
    $users[$row['id']] = $row['username'];
}
?>
<!DOCTYPE html>
<html lang="ro">
<head>
    <meta charset="UTF-8">
    <title>Loguri activitate - Coffee Lab</title>
    <link rel="stylesheet" href="style-responsive.css">
    <style>
        body { background: #f0f9ff; }
        .container { max-width: 1200px; margin: 0 auto; padding: 20px; }
        header, footer { background: #3b82f6; color: white; text-align: center; padding: 15px; border-radius: 8px; margin-bottom: 20px; }
        table { width: 100%; background: white; border-collapse: collapse; border-radius: 12px; overflow: hidden; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background: #3b82f6; color: white; }
        .info-box { background: #e3f2fd; padding: 15px; border-radius: 8px; margin-bottom: 20px; border-left: 5px solid #3b82f6; }
        .badge { background: #3b82f6; color: white; padding: 3px 8px; border-radius: 5px; font-size: 12px; }
        nav ul { list-style: none; display: flex; flex-wrap: wrap; justify-content: center; gap: 10px; margin-bottom: 20px; }
        nav ul li a { display: block; padding: 12px 24px; color: white; text-decoration: none; font-weight: bold; background: #3b82f6; border-radius: 8px; }
        nav ul li a:hover { background: #60a5fa; }
        .db-badge { display: inline-block; padding: 2px 8px; border-radius: 4px; font-size: 11px; margin-left: 8px; }
        .db-mysql { background: #00758f; color: white; }
        .db-sqlite { background: #003b57; color: white; }
    </style>
</head>
<body>
    <div class="container">
        <header>
            <h1>COFFEE LAB</h1>
            <p>Monitorizare activitate - Bază de date SQLite</p>
            <p>Bine ai venit, <?php echo htmlspecialchars($_SESSION['nume'] ?? $_SESSION['username']); ?>! 
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
                <li><a href="logs.php">Loguri</a></li>
            </ul>
        </nav>

        <div class="info-box">
            <h3>Baze de date utilizate</h3>
            <p>
                <strong>MySQL (PDO + MySQLi):</strong> Date principale (users, comenzi, rezervari)<br>
                <strong>SQLite:</strong> <span class="badge">Bază de date adițională</span> - Stochează loguri de activitate (login, logout, plasare comenzi)
            </p>
            <p style="margin-top: 10px; font-size: 13px;">
                📁 Fișier SQLite: <code><?php echo __DIR__ . '/coffee_lab.sqlite'; ?></code>
            </p>
        </div>

        <h2>📋 Loguri activitate <span class="db-badge db-sqlite">SQLite</span></h2>
        
        <?php if (isset($error)): ?>
            <div style="background: #f8d7da; color: #721c24; padding: 15px; border-radius: 8px;">
                <?php echo $error; ?>
            </div>
        <?php endif; ?>

        <div class="table-responsive">
            <table border="1" cellpadding="10" cellspacing="0">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>User ID</th>
                        <th>Acțiune</th>
                    </tr>
                </thead>
                <tbody>
                    <?php if (count($logs) > 0): ?>
                        <?php foreach ($logs as $log): ?>
                        <tr>
                            <td><?php echo $log['id']; ?></td>
                            <td><?php echo $log['user_id']; ?></td
                            <td><?php 
                                $username = $users[$log['user_id']] ?? 'Necunoscut';
                                echo htmlspecialchars($username);
                            ?></td>
                            <td>
                                <?php 
                                $action = $log['action'];
                                $icon = '';
                                if ($action == 'login') $icon = '🔐 ';
                                elseif ($action == 'logout') $icon = '🚪 ';
                                elseif ($action == 'placed_order') $icon = '📦 ';
                                elseif ($action == 'session_start') $icon = '🟢 ';
                                echo $icon . htmlspecialchars($action);
                                ?>
                            </td
                            <td><?php echo $log['timestamp']; ?></td
                        </tr>
                        <?php endforeach; ?>
                    <?php else: ?>
                        <tr>
                            <td colspan="5" style="text-align: center;">Nu există loguri încă. Autentifică-te sau plasează o comandă pentru a genera loguri.</td>
                        </tr>
                    <?php endif; ?>
                </tbody>
            </table>
        </div>

        

        <footer>
            <p>Coffee Lab - Sistem de loguri cu SQLite (bază de date adițională)</p>
        </footer>
    </div>
</body>
</html>