<?php
// admin_comenzi.php
session_start();
require_once 'config/database.php';
require_once 'includes/auth.php';

if (!canApproveOrders()) {
    die('Acces interzis! Doar administratorii și angajații pot accesa această pagină.');
}

$pdo = Database::getPDO();
$message = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (isset($_POST['approve'])) {
        $comandaId = $_POST['comanda_id'];
        $stmt = $pdo->prepare("UPDATE comenzi SET status = 'approved' WHERE id = ?");
        $stmt->execute([$comandaId]);
        $message = "Comanda #$comandaId a fost aprobată!";
    } elseif (isset($_POST['reject'])) {
        $comandaId = $_POST['comanda_id'];
        $stmt = $pdo->prepare("UPDATE comenzi SET status = 'rejected' WHERE id = ?");
        $stmt->execute([$comandaId]);
        $message = "Comanda #$comandaId a fost respinsă!";
    }
}

$stmt = $pdo->prepare("SELECT c.*, u.username, u.role FROM comenzi c JOIN users u ON c.user_id = u.id ORDER BY 
CASE WHEN c.status = 'pending' THEN 0 ELSE 1 END, c.created_at DESC");
$stmt->execute();
$comenzi = $stmt->fetchAll();
?>
<!DOCTYPE html>
<html lang="ro">
<head>
    <meta charset="UTF-8">
    <title>Admin Comenzi - Coffee Lab</title>
    <link rel="stylesheet" href="style-responsive.css">
    <style>
        .status-approved { color: green; font-weight: bold; }
        .status-rejected { color: red; font-weight: bold; }
        .btn-approve { background: #0e9b6cb6; color: white; padding: 5px 15px; border: none; border-radius: 5px; cursor: pointer; }
        .btn-reject { background: #ef4444ac; color: white; padding: 5px 15px; border: none; border-radius: 5px; cursor: pointer; }
        .comanda-card { background: white; padding: 15px; border-radius: 12px; margin-bottom: 15px; border-left: 5px solid; }
        .border-pending { border-left-color: orange; }
        .border-approved { border-left-color: green; }
        .border-rejected { border-left-color: red; }
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
            <p>Administrare Comenzi</p>
            <p>Bine ai venit, <?php echo htmlspecialchars($_SESSION['nume'] ?? $_SESSION['username']); ?>! 
            Rol: <?php echo htmlspecialchars($_SESSION['role']); ?>
            <a href="php/logout.php" style="color: white; margin-left: 15px;">Logout</a></p>
        </header>

        <nav>
            <ul>
                <li><a href="home.php">Acasa</a></li>
                <li><a href="meniu.php">Meniu</a></li>
                <li><a href="rezervari.php">Rezervari</a></li>
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

        <h2>Gestionare Comenzi</h2>
        
        <?php foreach ($comenzi as $comanda): ?>
            <div class="comanda-card border-<?php echo $comanda['status']; ?>">
                <p><strong>Comanda #<?php echo $comanda['id']; ?></strong> 
                   - Client: <?php echo htmlspecialchars($comanda['username']); ?> 
                   (<?php echo htmlspecialchars($comanda['role']); ?>)</p>
                <p><strong>Nume:</strong> <?php echo htmlspecialchars($comanda['nume'] . ' ' . $comanda['prenume']); ?></p>
                <p><strong>Email:</strong> <?php echo htmlspecialchars($comanda['email']); ?></p>
                <p><strong>Telefon:</strong> <?php echo htmlspecialchars($comanda['telefon'] ?? '-'); ?></p>
                <p><strong>Produse:</strong><br><?php echo nl2br(htmlspecialchars($comanda['produse'])); ?></p>
                <p><strong>Total:</strong> <?php echo $comanda['total']; ?> lei</p>
                <p><strong>Status:</strong> <span class="status-<?php echo $comanda['status']; ?>"><?php echo $comanda['status']; ?></span></p>
                <p><strong>Data plasării:</strong> <?php echo $comanda['created_at']; ?></p>
                
                <?php if ($comanda['status'] === 'pending'): ?>
                    <form method="POST" style="display: inline-block;">
                        <input type="hidden" name="comanda_id" value="<?php echo $comanda['id']; ?>">
                        <button type="submit" name="approve" class="btn-approve"> Aprobă comanda</button>
                    </form>
                    <form method="POST" style="display: inline-block;">
                        <input type="hidden" name="comanda_id" value="<?php echo $comanda['id']; ?>">
                        <button type="submit" name="reject" class="btn-reject"> Respinge comanda</button>
                    </form>
                <?php endif; ?>
            </div>
        <?php endforeach; ?>
        
        <?php if (count($comenzi) == 0): ?>
            <p>Nu există comenzi în sistem.</p>
        <?php endif; ?>

        <footer>
            <p>Coffee Lab - Panou de administrare comenzi</p>
        </footer>
    </div>
</body>
</html>