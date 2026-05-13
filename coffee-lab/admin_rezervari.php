<?php
// admin_rezervari.php
session_start();
require_once 'config/database.php';
require_once 'includes/auth.php';

// Doar admin și angajat pot accesa
if (!canApproveOrders()) {
    die('Acces interzis! Doar administratorii și angajații pot accesa această pagină.');
}

$pdo = Database::getPDO();
$message = '';

// Procesare aprobare/respingere rezervare
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (isset($_POST['confirm'])) {
        $rezervareId = $_POST['rezervare_id'];
        $stmt = $pdo->prepare("UPDATE rezervari SET status = 'confirmat' WHERE id = ?");
        $stmt->execute([$rezervareId]);
        $message = "Rezervarea #$rezervareId a fost confirmată!";
    } elseif (isset($_POST['cancel'])) {
        $rezervareId = $_POST['rezervare_id'];
        $stmt = $pdo->prepare("UPDATE rezervari SET status = 'anulat' WHERE id = ?");
        $stmt->execute([$rezervareId]);
        $message = "Rezervarea #$rezervareId a fost anulată!";
    }
}

$stmt = $pdo->prepare("SELECT r.*, u.username, u.role FROM rezervari r 
                       JOIN users u ON r.user_id = u.id 
                       ORDER BY CASE WHEN r.status = 'pending' THEN 0 ELSE 1 END, r.data DESC");
$stmt->execute();
$rezervari = $stmt->fetchAll();
?>
<!DOCTYPE html>
<html lang="ro">
<head>
    <meta charset="UTF-8">
    <title>Admin Rezervări - Coffee Lab</title>
    <link rel="stylesheet" href="style-responsive.css">
    <style>
        .status-pending { color: orange; font-weight: bold; }
        .status-confirmat { color: green; font-weight: bold; }
        .status-anulat { color: red; font-weight: bold; }
        .btn-confirm { background: #10b981c2; color: white; padding: 5px 15px; border: none; border-radius: 5px; cursor: pointer; }
        .btn-cancel { background: #ef4444c7; color: white; padding: 5px 15px; border: none; border-radius: 5px; cursor: pointer; }
        .rezervare-card { background: white; padding: 15px; border-radius: 12px; margin-bottom: 15px; border-left: 5px solid; }
        .border-pending { border-left-color: orange; }
        .border-confirmat { border-left-color: green; }
        .border-anulat { border-left-color: red; }
        .message { background: #d4edda; color: #155724; padding: 15px; border-radius: 8px; margin-bottom: 15px; }
        nav ul { list-style: none; display: flex; flex-wrap: wrap; justify-content: center; gap: 10px; }
        nav ul li a { display: block; padding: 12px 24px; color: white; text-decoration: none; font-weight: bold; background: #3b82f6; border-radius: 8px; }
        nav ul li a:hover { background: #60a5fa; }
    </style>
</head>
<body>
    <div class="container">
        <header>
            <h1>COFFEE LAB</h1>
            <p>Administrare Rezervări</p>
            <p>Bine ai venit, <?php echo htmlspecialchars($_SESSION['nume'] ?? $_SESSION['username']); ?>! 
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
        <?php if (canApproveOrders()): ?>
        <li><a href="admin_comenzi.php">Admin Comenzi</a></li>
        <li><a href="admin_rezervari.php">Admin Rezervări</a></li>
        <?php endif; ?>
        <li><a href="sprite-demo.html">Sprite</a></li>
    </ul>
</nav>

        <?php if ($message): ?>
            <div class="message"><?php echo htmlspecialchars($message); ?></div>
        <?php endif; ?>

        <h2>Gestionare Rezervări</h2>
        
        <?php foreach ($rezervari as $rez): ?>
            <div class="rezervare-card border-<?php echo $rez['status']; ?>">
                <p><strong>Rezervarea #<?php echo $rez['id']; ?></strong> 
                   - Client: <?php echo htmlspecialchars($rez['username']); ?> 
                   (<?php echo htmlspecialchars($rez['role']); ?>)</p>
                <p><strong>Data:</strong> <?php echo $rez['data']; ?> la ora <?php echo $rez['ora']; ?></p>
                <p><strong>Persoane:</strong> <?php echo $rez['persoane']; ?></p>
                <p><strong>Locație preferată:</strong> <?php echo htmlspecialchars($rez['locatie'] ?? '-'); ?></p>
                <p><strong>Cerințe speciale:</strong> <?php echo htmlspecialchars($rez['cerinte_speciale'] ?? '-'); ?></p>
                <p><strong>Status:</strong> <span class="status-<?php echo $rez['status']; ?>"><?php echo $rez['status']; ?></span></p>
                <p><strong>Data cererii:</strong> <?php echo $rez['created_at']; ?></p>
                
                <?php if ($rez['status'] === 'pending'): ?>
                    <form method="POST" style="display: inline-block;">
                        <input type="hidden" name="rezervare_id" value="<?php echo $rez['id']; ?>">
                        <button type="submit" name="confirm" class="btn-confirm"> Confirmă rezervarea</button>
                    </form>
                    <form method="POST" style="display: inline-block;">
                        <input type="hidden" name="rezervare_id" value="<?php echo $rez['id']; ?>">
                        <button type="submit" name="cancel" class="btn-cancel"> Anulează rezervarea</button>
                    </form>
                <?php endif; ?>
            </div>
        <?php endforeach; ?>
        
        <?php if (count($rezervari) == 0): ?>
            <p>Nu există rezervări în sistem.</p>
        <?php endif; ?>

        <footer>
            <p>Coffee Lab - Panou de administrare rezervări</p>
        </footer>
    </div>
</body>
</html>