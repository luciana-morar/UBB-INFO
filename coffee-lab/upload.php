<?php
// upload.php
session_start();
require_once 'config/database.php';
require_once 'includes/auth.php';
requireAuth();

$message = '';
$error = '';
$pdo = Database::getPDO();

// Preia datele utilizatorului
$stmt = $pdo->prepare("SELECT * FROM users WHERE id = ?");
$stmt->execute([$_SESSION['user_id']]);
$user = $stmt->fetch();

// Procesare upload fișier
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Upload poză de profil
    if (isset($_FILES['profile_image']) && $_FILES['profile_image']['error'] === UPLOAD_ERR_OK) {
        $uploadDir = __DIR__ . '/uploads/';
        
        // Creează folderul dacă nu există
        if (!is_dir($uploadDir)) {
            mkdir($uploadDir, 0777, true);
        }
        
        $extension = strtolower(pathinfo($_FILES['profile_image']['name'], PATHINFO_EXTENSION));
        $allowed = ['jpg', 'jpeg', 'png', 'gif'];
        
        if (in_array($extension, $allowed)) {
            // Verifică dimensiunea (max 2MB)
            if ($_FILES['profile_image']['size'] < 2 * 1024 * 1024) {
                $filename = 'user_' . $_SESSION['user_id'] . '_' . time() . '.' . $extension;
                $filepath = 'uploads/' . $filename;
                
                if (move_uploaded_file($_FILES['profile_image']['tmp_name'], $uploadDir . $filename)) {
                    // Șterge fișierul vechi dacă există
                    if (!empty($user['profile_image']) && file_exists(__DIR__ . '/../' . $user['profile_image'])) {
                        unlink(__DIR__ . '/../' . $user['profile_image']);
                    }
                    
                    // Actualizează baza de date
                    $updateStmt = $pdo->prepare("UPDATE users SET profile_image = ? WHERE id = ?");
                    $updateStmt->execute([$filepath, $_SESSION['user_id']]);
                    $message = " Poza de profil a fost încărcată cu succes!";
                    
                    // Reîncarcă datele utilizatorului
                    $stmt->execute([$_SESSION['user_id']]);
                    $user = $stmt->fetch();
                } else {
                    $error = "❌ Eroare la mutarea fișierului.";
                }
            } else {
                $error = "❌ Fișierul este prea mare (maxim 2MB).";
            }
        } else {
            $error = "❌ Format invalid. Formate acceptate: JPG, JPEG, PNG, GIF";
        }
    } 
    // Actualizare profil
    elseif (isset($_POST['update_profile'])) {
        $nume = $_POST['nume'] ?? '';
        $prenume = $_POST['prenume'] ?? '';
        $telefon = $_POST['telefon'] ?? '';
        $email = $_POST['email'] ?? '';
        
        $updateStmt = $pdo->prepare("UPDATE users SET nume = ?, prenume = ?, telefon = ?, email = ? WHERE id = ?");
        $updateStmt->execute([$nume, $prenume, $telefon, $email, $_SESSION['user_id']]);
        
        $_SESSION['nume'] = $nume;
        $_SESSION['prenume'] = $prenume;
        $_SESSION['email'] = $email;
        $_SESSION['telefon'] = $telefon;
        
        $message = "✅ Profil actualizat cu succes!";
        
        // Reîncarcă datele
        $stmt->execute([$_SESSION['user_id']]);
        $user = $stmt->fetch();
    } 
    // Ștergere fișier
    elseif (isset($_POST['delete_image'])) {
        if (!empty($user['profile_image']) && file_exists(__DIR__ . '/../' . $user['profile_image'])) {
            unlink(__DIR__ . '/../' . $user['profile_image']);
        }
        
        $updateStmt = $pdo->prepare("UPDATE users SET profile_image = NULL WHERE id = ?");
        $updateStmt->execute([$_SESSION['user_id']]);
        $message = " Poza de profil a fost ștearsă!";
        
        // Reîncarcă datele
        $stmt->execute([$_SESSION['user_id']]);
        $user = $stmt->fetch();
    }
}

// Determină calea corectă pentru imagine
$profileImagePath = 'uploads/default-avatar.png'; // cale implicită
if (!empty($user['profile_image']) && file_exists(__DIR__ . '/../' . $user['profile_image'])) {
    $profileImagePath = $user['profile_image'];
} elseif (!empty($user['profile_image']) && file_exists($user['profile_image'])) {
    $profileImagePath = $user['profile_image'];
}
?>
<!DOCTYPE html>
<html lang="ro">
<head>
    <meta charset="UTF-8">
    <title>Profil - Coffee Lab</title>
    <link rel="stylesheet" href="style-responsive.css">
    <style>
        .profile-container { display: flex; gap: 30px; flex-wrap: wrap; }
        .profile-image { flex: 1; text-align: center; background: white; padding: 20px; border-radius: 12px; }
        .profile-image img { width: 200px; height: 200px; object-fit: cover; border-radius: 50%; margin-bottom: 15px; border: 3px solid #3b82f6; }
        .profile-form { flex: 2; background: white; padding: 20px; border-radius: 12px; }
        .message { padding: 10px; border-radius: 8px; margin-bottom: 15px; }
        .success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
        .error { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
        .btn { background: #3b82f6; color: white; padding: 8px 20px; border: none; border-radius: 5px; cursor: pointer; }
        .btn-danger { background: #ef4444; }
        .btn:hover { opacity: 0.9; }
        nav ul { list-style: none; display: flex; flex-wrap: wrap; justify-content: center; gap: 10px; }
        nav ul li a { display: block; padding: 12px 24px; color: white; text-decoration: none; font-weight: bold; background: #3b82f6; border-radius: 8px; }
        nav ul li a:hover { background: #60a5fa; }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 5px; font-weight: bold; }
        .form-group input { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; }
        .preview-img { 
            width: 200px; 
            height: 200px; 
            object-fit: cover; 
            border-radius: 50%; 
            margin-bottom: 15px;
            border: 3px solid #3b82f6;
        }
    </style>
</head>
<body>
    <div class="container">
        <header>
            <h1>COFFEE LAB</h1>
            <p>Profilul tău</p>
            <p>Bine ai venit, <?php echo htmlspecialchars($user['nume'] ?? $user['username']); ?>! 
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
        
        <?php if ($message): ?>
            <div class="message success"><?php echo htmlspecialchars($message); ?></div>
        <?php endif; ?>
        <?php if ($error): ?>
            <div class="message error"><?php echo htmlspecialchars($error); ?></div>
        <?php endif; ?>
        
        <div class="profile-container">
            <div class="profile-image">
                <?php if (!empty($user['profile_image']) && file_exists(__DIR__ . '/../' . $user['profile_image'])): ?>
                    <img src="<?php echo $user['profile_image']; ?>" alt="Poza profil" class="preview-img">
                <?php elseif (!empty($user['profile_image']) && file_exists($user['profile_image'])): ?>
                    <img src="<?php echo $user['profile_image']; ?>" alt="Poza profil" class="preview-img">
                <?php else: ?>
                    <img src="https://ui-avatars.com/api/?name=<?php echo urlencode($user['username']); ?>&background=3b82f6&color=fff&size=200" alt="Avatar" class="preview-img">
                <?php endif; ?>
                
                <form method="POST" enctype="multipart/form-data">
                    <input type="file" name="profile_image" accept="image/jpeg,image/png,image/gif" style="margin: 10px 0;">
                    <button type="submit" class="btn">Încarcă poză</button>
                </form>
                
                <?php if (!empty($user['profile_image'])): ?>
                    <form method="POST" style="margin-top: 10px;">
                        <button type="submit" name="delete_image" class="btn btn-danger">Șterge poza</button>
                    </form>
                <?php endif; ?>
                <p style="font-size: 12px; color: #666; margin-top: 10px;">Formate acceptate: JPG, PNG, GIF (max 2MB)</p>
            </div>
            
            <div class="profile-form">
                <h3>Date personale</h3>
                <form method="POST">
                    <div class="form-group">
                        <label>Username:</label>
                        <input type="text" value="<?php echo htmlspecialchars($user['username']); ?>" disabled style="background:#f3f4f6;">
                    </div>
                    <div class="form-group">
                        <label>Nume:</label>
                        <input type="text" name="nume" value="<?php echo htmlspecialchars($user['nume'] ?? ''); ?>">
                    </div>
                    <div class="form-group">
                        <label>Prenume:</label>
                        <input type="text" name="prenume" value="<?php echo htmlspecialchars($user['prenume'] ?? ''); ?>">
                    </div>
                    <div class="form-group">
                        <label>Email:</label>
                        <input type="email" name="email" value="<?php echo htmlspecialchars($user['email'] ?? ''); ?>">
                    </div>
                    <div class="form-group">
                        <label>Telefon:</label>
                        <input type="tel" name="telefon" value="<?php echo htmlspecialchars($user['telefon'] ?? ''); ?>">
                    </div>
                    <button type="submit" name="update_profile" class="btn">Salvează modificările</button>
                </form>
            </div>
        </div>
        
        <footer>
            <p>Coffee Lab - Profil utilizator</p>
        </footer>
    </div>
</body>
</html>