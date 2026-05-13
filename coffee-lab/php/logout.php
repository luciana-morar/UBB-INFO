<?php
// php/logout.php
session_start();
require_once '../config/database.php';

if (isset($_SESSION['user_id'])) {
    try {
        $pdo = Database::getPDO();
        // Șterge token-ul din baza de date
        $stmt = $pdo->prepare("UPDATE users SET remember_token = NULL WHERE id = ?");
        $stmt->execute([$_SESSION['user_id']]);
    } catch (Exception $e) {
        error_log("Logout error: " . $e->getMessage());
    }
}


if (isset($_COOKIE['remember_token'])) {
    setcookie('remember_token', '', time() - 3600, '/');
}

$sqlite = Database::getSQLite();
if ($sqlite && isset($_SESSION['user_id'])) {
    try {
        $stmtLog = $sqlite->prepare("INSERT INTO logs (user_id, action) VALUES (?, ?)");
        $stmtLog->execute([$_SESSION['user_id'], 'logout']);
    } catch (Exception $e) {
        error_log("SQLite log error: " . $e->getMessage());
    }
}
$_SESSION = array();
if (isset($_COOKIE[session_name()])) {
    setcookie(session_name(), '', time() - 3600, '/');
}
session_destroy();

header('Location: ../php/login.php');
exit();
?>