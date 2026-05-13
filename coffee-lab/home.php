
<?php
// home.php
session_start();
require_once 'config/database.php';
require_once 'includes/auth.php';
requireAuth();
$user = getCurrentUser();
?>

<!DOCTYPE html>
<html lang="ro">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Coffee Lab - Acasa</title>
    <link rel="stylesheet" href="style-horizontal.css" id="main-style"> 
    <link rel="stylesheet" href="style-responsive.css" id="main-style">
</head>

<body>
    <div class="container">
        <header>
            <h1>COFFEE LAB</h1>
            <p>Your next favourite place</p>
        </header>


        <nav>
    <ul>
        <li><a href="home.php">Acasa</a></li>
        <li><a href="meniu.php">Meniu</a>
            <ul class="submenu">
                <li><a href="meniu.php#cafea">Cafea</a></li>
                <li><a href="meniu.php#gustari">Gustari</a></li>
                <li><a href="meniu.php#deserturi">Deserturi</a></li>
            </ul>
        </li>
        <li><a href="rezervari.php">Rezervari</a>
            <ul class="submenu">
                <li><a href="rezervari.php#evenimente">Evenimente</a></li>
                <li><a href="rezervari.php#mese">Mese disponibile</a></li>
            </ul>
        </li>
        <li><a href="dashboard.php">Dashboard</a></li>
        <li><a href="sprite-demo.html">Sprite</a></li>
    </ul>
</nav>

        <div class="about">
            <div class="about-text">
                <h2>Despre Coffee Lab</h2>
                <p>Din dorinta de a aduce un strop de incantare in fiecare gura de cafea, am creat CoffeeLab - un spatiu in care pasiunea pentru cafeaua de specialitate se imbina armonios cu o atmosfera calda si prietenoasa.</p>
                <p>Aici, fiecare vizitator poate descoperi arome noi, dezvoltate in propriul nostru laborator. Muzica buna nu lipseste nicio clipa, iar evenimentele saptamanale aduc comunitatea mai aproape.</p>
                
                <ul>
                    <li><strong>Cafea artizanala</strong>
                        <ul><li>Cafea de specialitate</li><li>Try your own blend</li><li>Bauturi speciale</li></ul>
                    </li>
                    <li><strong>Snacks & Deserts</strong>
                        <ol type="a"><li>Healthy Snacks</li><li>Deserturi speciale</li><li>Gustari</li></ol>
                    </li>
                    <li><strong>Activitati</strong>
                        <ol><li>Concerte vineri</li><li>Laboratoare saptamanale</li><li>Ateliere de creatie</li></ol>
                    </li>
                </ul>
                <a href="rezervari.html" class="btn">Rezerva o masa</a>
            </div>
            <div class="about-image">
                <img src="images/Lab.png" alt="Cafea proaspata">
            </div>
        </div>

        <h2>Program</h2>
        <div class="table-responsive">
            <table>
                <thead><tr><th colspan="2">Program</th></tr></thead>
                <tbody>
                    <tr><td><strong>Luni - Vineri</strong></td><td>08:00 - 20:00</td></tr>
                    <tr><td><strong>Sambata</strong></td><td>10:00 - 19:00</td></tr>
                    <tr><td><strong>Duminica</strong></td><td>10:00 - 21:00</td></tr>
                </tbody>
            </table>
        </div>

        <p><b>Contact:</b> 123 Anywhere St. | (123) 456 7890 | contact@coffeelab.ro</p>

        <footer>
            <p>Coffee Lab - Specialty Coffee Since 2024</p>
        </footer>
    </div>
</body>
</html>