<?php
// includes/auth.php

function requireAuth() {
    if (!isset($_SESSION['user_id'])) {
        header('Location: php/login.php');
        exit();
    }
}

function requireRole($role) {
    requireAuth();
    if ($_SESSION['role'] !== $role && $_SESSION['role'] !== 'admin') {
        die('Acces interzis! Nu ai permisiunea necesară.');
    }
}

// Verifică dacă utilizatorul poate aproba comenzi (admin sau angajat)
function canApproveOrders() {
    return isset($_SESSION['role']) && ($_SESSION['role'] === 'admin' || $_SESSION['role'] === 'angajat');
}

// Verifică dacă utilizatorul este admin
function isAdmin() {
    return isset($_SESSION['role']) && $_SESSION['role'] === 'admin';
}

// Verifică dacă utilizatorul este angajat
function isAngajat() {
    return isset($_SESSION['role']) && $_SESSION['role'] === 'angajat';
}

function isLoggedIn() {
    return isset($_SESSION['user_id']);
}

function getCurrentUser() {
    if (!isLoggedIn()) return null;
    
    return [
        'id' => $_SESSION['user_id'],
        'username' => $_SESSION['username'],
        'role' => $_SESSION['role'],
        'nume' => $_SESSION['nume'] ?? '',
        'prenume' => $_SESSION['prenume'] ?? ''
    ];
}
?>