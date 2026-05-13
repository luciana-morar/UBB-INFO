<?php
// php/login.php - cu Remember Me functional
session_start();
require_once '../config/database.php';
require_once '../includes/auth.php';

// ========== VERIFICĂ COOKIE REMEMBER ME ==========
if (!isset($_SESSION['user_id']) && isset($_COOKIE['remember_token'])) {
    $token = $_COOKIE['remember_token'];
    $pdo = Database::getPDO();
    
    $stmt = $pdo->prepare("SELECT id, username, role, nume, prenume, email, telefon FROM users WHERE remember_token = ?");
    $stmt->execute([$token]);
    $user = $stmt->fetch();
    
    if ($user) {
        $_SESSION['user_id'] = $user['id'];
        $_SESSION['username'] = $user['username'];
        $_SESSION['role'] = $user['role'];
        $_SESSION['nume'] = $user['nume'];
        $_SESSION['prenume'] = $user['prenume'];
        $_SESSION['email'] = $user['email'];
        $_SESSION['telefon'] = $user['telefon'];
        
        header('Location: ../dashboard.php');
        exit();
    } else {
        // Token invalid, șterge cookie-ul
        setcookie('remember_token', '', time() - 3600, '/');
    }
}

if (isset($_SESSION['user_id'])) {
    header('Location: ../dashboard.php');
    exit();
}

// Inițializare CAPTCHA
if (!isset($_SESSION['captcha_num1'])) {
    $_SESSION['captcha_num1'] = rand(1, 20);
    $_SESSION['captcha_num2'] = rand(1, 20);
    $_SESSION['captcha_result'] = $_SESSION['captcha_num1'] + $_SESSION['captcha_num2'];
}

$error = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $username = $_POST['username'] ?? '';
    $password = $_POST['password'] ?? '';
    $remember = isset($_POST['remember']);
    $captchaInput = intval($_POST['captcha'] ?? 0);
    
    if ($captchaInput !== $_SESSION['captcha_result']) {
        $error = ' Cod CAPTCHA incorect! Încearcă din nou.';
        $_SESSION['captcha_num1'] = rand(1, 20);
        $_SESSION['captcha_num2'] = rand(1, 20);
        $_SESSION['captcha_result'] = $_SESSION['captcha_num1'] + $_SESSION['captcha_num2'];
    } elseif (!empty($username) && !empty($password)) {
        $pdo = Database::getPDO();
        
        $stmt = $pdo->prepare("SELECT id, username, password, role, nume, prenume, email, telefon FROM users WHERE username = ?");
        $stmt->execute([$username]);
        $user = $stmt->fetch();
        
        if ($user && password_verify($password, $user['password'])) {
            $_SESSION['user_id'] = $user['id'];
            $_SESSION['username'] = $user['username'];
            $_SESSION['role'] = $user['role'];
            $_SESSION['nume'] = $user['nume'];
            $_SESSION['prenume'] = $user['prenume'];
            $_SESSION['email'] = $user['email'];
            $_SESSION['telefon'] = $user['telefon'];
            
            // ========== SETEAZĂ COOKIE PENTRU REMEMBER ME ==========
            if ($remember) {
                $token = bin2hex(random_bytes(32));
                
                // Salvează token în baza de date
                $updateStmt = $pdo->prepare("UPDATE users SET remember_token = ? WHERE id = ?");
                $updateStmt->execute([$token, $user['id']]);
                
                // Setează cookie-ul - parametri corecți!
                setcookie(
                    'remember_token',           // nume
                    $token,                     // valoare
                    time() + (86400 * 30),      // expirează după 30 zile
                    '/',                        // cale - disponibil pe tot domeniul
                    '',                         // domain
                    false,                      // secure (false pentru HTTP)
                    true                        // httponly (accesibil doar server)
                );
                
                // Debug - verifică dacă s-a setat
                error_log("Remember me token set for user: " . $user['username']);
            }
            
            // Log în SQLite
            $sqlite = Database::getSQLite();
            if ($sqlite) {
                $stmtLog = $sqlite->prepare("INSERT INTO logs (user_id, action) VALUES (?, ?)");
                $stmtLog->execute([$user['id'], 'login']);
            }
            
            header('Location: ../dashboard.php');
            exit();
        } else {
            $error = ' Username sau parolă incorecte!';
            $_SESSION['captcha_num1'] = rand(1, 20);
            $_SESSION['captcha_num2'] = rand(1, 20);
            $_SESSION['captcha_result'] = $_SESSION['captcha_num1'] + $_SESSION['captcha_num2'];
        }
    } else {
        $error = ' Completează toate câmpurile!';
        $_SESSION['captcha_num1'] = rand(1, 20);
        $_SESSION['captcha_num2'] = rand(1, 20);
        $_SESSION['captcha_result'] = $_SESSION['captcha_num1'] + $_SESSION['captcha_num2'];
    }
}
?>
<!DOCTYPE html>
<html lang="ro">
<head>
    <meta charset="UTF-8">
    <title>Login - Coffee Lab</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Arial, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #823bf3 100%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
        }
        .login-container {
            background: white;
            padding: 40px;
            border-radius: 20px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            width: 400px;
        }
        h1 { 
            color: #3b82f6; 
            text-align: center; 
            margin-bottom: 10px;
            font-size: 28px;
        }
        .subtitle { 
            text-align: center; 
            color: #666; 
            margin-bottom: 30px;
            font-size: 14px;
        }
        input {
            width: 100%;
            padding: 12px;
            margin: 10px 0;
            border: 1px solid #ddd;
            border-radius: 8px;
            box-sizing: border-box;
            font-size: 14px;
        }
        input:focus {
            outline: none;
            border-color: #3b82f6;
        }
        .captcha-container {
            display: flex;
            gap: 10px;
            align-items: center;
            margin: 15px 0;
        }
        .captcha-question {
            background: #f3f4f6;
            padding: 12px;
            border-radius: 8px;
            font-size: 18px;
            font-weight: bold;
            text-align: center;
            flex: 1;
            color: #3b82f6;
        }
        .captcha-input {
            flex: 1;
        }
        .captcha-input input {
            margin: 0;
        }
        .checkbox { 
            display: flex; 
            align-items: center; 
            gap: 10px; 
            margin: 15px 0; 
        }
        .checkbox input { 
            width: auto; 
            margin: 0;
        }
        button[type="submit"] {
            width: 100%;
            padding: 12px;
            background: #3b82f6;
            color: white;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            font-size: 16px;
            margin-top: 10px;
            font-weight: bold;
        }
        button[type="submit"]:hover {
            background: #2563eb;
        }
        .error {
            background: #fee2e2;
            color: #dc2626;
            padding: 12px;
            border-radius: 8px;
            margin-bottom: 20px;
            text-align: center;
            font-size: 14px;
        }
        .test-account {
            margin-top: 20px;
            padding: 15px;
            background: #f3f4f6;
            border-radius: 8px;
            text-align: center;
            font-size: 13px;
        }
        hr {
            margin: 20px 0;
            border: none;
            border-top: 1px solid #ddd;
        }
        .tips {
            margin-top: 15px;
            font-size: 12px;
            color: #666;
            text-align: center;
        }
    </style>
</head>
<body>
    <div class="login-container">
        <h1>Coffee Lab</h1>
        <div class="subtitle">Autentificare în platformă</div>
        
        <?php if ($error): ?>
            <div class="error"><?php echo htmlspecialchars($error); ?></div>
        <?php endif; ?>
        
        <form method="POST" action="">
            <input type="text" name="username" placeholder="Username" required autofocus>
            <input type="password" name="password" placeholder="Parolă" required>
            
            <div class="captcha-container">
                <div class="captcha-question">
                    <?php echo $_SESSION['captcha_num1']; ?> + <?php echo $_SESSION['captcha_num2']; ?> = ?
                </div>
                <div class="captcha-input">
                    <input type="text" name="captcha" placeholder="Rezultat" required>
                </div>
            </div>
            
            <div class="checkbox">
                <input type="checkbox" name="remember" id="remember">
                <label for="remember">Ține-mă minte (30 zile)</label>
            </div>
            
            <button type="submit"> Autentificare</button>
        </form>
        
        <hr>
        
        <div class="test-account">
            <strong>Conturi de test</strong><br><br>
            <strong>Admin:</strong> admin / password<br>
            <strong>Angajat:</strong> angajat / password<br>
            <strong>Client:</strong> client / password
        </div>
        
        <div class="tips">
             <strong>CAPTCHA:</strong> Calculează suma numerelor afișate
        </div>
    </div>
</body>
</html>