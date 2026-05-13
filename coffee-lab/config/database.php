<?php
// config/database.php

class Database {
    private static $mysqli = null;
    private static $pdo = null;
    private static $sqlite = null;
    
    // Conexiune MySQLi
    public static function getMySQLi() {
        if (self::$mysqli === null) {
            self::$mysqli = new mysqli('localhost', 'root', '', 'coffee_lab', 3306);
            
            if (self::$mysqli->connect_error) {
                die("Conexiune MySQLi eșuată: " . self::$mysqli->connect_error);
            }
            
            self::$mysqli->set_charset("utf8");
        }
        return self::$mysqli;
    }
    
    // Conexiune PDO
    public static function getPDO() {
        if (self::$pdo === null) {
            $dsn = "mysql:host=localhost;port=3306;dbname=coffee_lab;charset=utf8";
            $options = [
                PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            ];
            
            try {
                self::$pdo = new PDO($dsn, 'root', '', $options);
            } catch (PDOException $e) {
                die("Conexiune PDO eșuată: " . $e->getMessage());
            }
        }
        return self::$pdo;
    }
    
    // Bază de date adițională - SQLite
    public static function getSQLite() {
        if (self::$sqlite === null) {
            $dbFile = __DIR__ . '/../coffee_lab.sqlite';
            $isNew = !file_exists($dbFile);
            
            try {
                self::$sqlite = new PDO("sqlite:$dbFile");
                self::$sqlite->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
                
                if ($isNew) {
                    self::createSQLiteTables(self::$sqlite);
                }
            } catch (PDOException $e) {
                // Nu oprim execuția dacă SQLite eșuează
                error_log("SQLite connection failed: " . $e->getMessage());
            }
        }
        return self::$sqlite;
    }
    
    private static function createSQLiteTables($pdo) {
        $pdo->exec("
            CREATE TABLE IF NOT EXISTS logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                action TEXT,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        ");
        
        // Log utilizator curent
        if (session_status() === PHP_SESSION_ACTIVE && isset($_SESSION['user_id'])) {
            $stmt = $pdo->prepare("INSERT INTO logs (user_id, action) VALUES (?, ?)");
            $stmt->execute([$_SESSION['user_id'], 'session_start']);
        }
    }
}
?>