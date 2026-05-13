<?php
// comenzi.php
session_start();
require_once 'config/database.php';
require_once 'includes/auth.php';
requireAuth();

$pdo = Database::getPDO();

// Preia comenzile utilizatorului
$stmt = $pdo->prepare("SELECT * FROM comenzi WHERE user_id = ? ORDER BY created_at DESC");
$stmt->execute([$_SESSION['user_id']]);
$comenzileMele = $stmt->fetchAll();

// Preia datele utilizatorului
$stmtUser = $pdo->prepare("SELECT * FROM users WHERE id = ?");
$stmtUser->execute([$_SESSION['user_id']]);
$user = $stmtUser->fetch();
?>
<!DOCTYPE html>
<html lang="ro">
<head>
    <meta charset="UTF-8">
    <title>Comenzile mele - Coffee Lab</title>
    <link rel="stylesheet" href="style-responsive.css">
    <style>
        .status-pending { color: orange; font-weight: bold; }
        .status-approved { color: green; font-weight: bold; }
        .status-rejected { color: red; font-weight: bold; }
        table { width: 100%; background: white; border-collapse: collapse; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background: #3b82f6; color: white; }
        nav ul { list-style: none; display: flex; flex-wrap: wrap; justify-content: center; gap: 10px; }
        nav ul li a { display: block; padding: 12px 24px; color: white; text-decoration: none; font-weight: bold; background: #3b82f6; border-radius: 8px; }
        nav ul li a:hover { background: #60a5fa; }
    </style>
</head>
<body>
    <div class="container">
        <header>
            <h1>COFFEE LAB</h1>
            <p>Comenzile mele</p>
            <p>Bine ai venit, <?php echo htmlspecialchars($user['nume'] ?? $_SESSION['username']); ?>! 
            Rol: <?php echo htmlspecialchars($_SESSION['role']); ?>
            <a href="php/logout.php" style="color: white; margin-left: 15px;">Logout</a></p>
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

        <h2>Istoric comenzi</h2>
        <div class="table-responsive">
            <table border="1" cellpadding="10" cellspacing="0" style="width:100%; background:white; border-collapse:collapse;">
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
                            <td class="status-<?php echo $comanda['status']; ?>"><?php echo htmlspecialchars($comanda['status']); ?></td>
                            <td><?php echo htmlspecialchars($comanda['created_at']); ?></td>
                        </tr>
                    <?php endforeach; ?>
                <?php else: ?>
                    <tr><td colspan="5" style="padding:20px; text-align:center;">Nu ai plasat nicio comandă încă.</td></tr>
                <?php endif; ?>
            </table>
        </div>

        <footer>
            <p>Coffee Lab - Istoric comenzi</p>
        </footer>
    </div>
</body>
</html>