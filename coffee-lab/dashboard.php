<?php
// dashboard.php
session_start();
require_once 'config/database.php';
require_once 'includes/auth.php';
requireAuth();

$pdo = Database::getPDO();

// Statistici din baza de date
$stmt = $pdo->query("SELECT COUNT(*) as total FROM users");
$totalUsers = $stmt->fetch()['total'];

$stmt = $pdo->prepare("SELECT COUNT(*) as total FROM rezervari WHERE user_id = ?");
$stmt->execute([$_SESSION['user_id']]);
$myRezervari = $stmt->fetch()['total'];

$stmt = $pdo->prepare("SELECT * FROM rezervari WHERE user_id = ? ORDER BY data DESC LIMIT 5");
$stmt->execute([$_SESSION['user_id']]);
$rezervari = $stmt->fetchAll();
?>

<!DOCTYPE html>
<html lang="ro">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Coffee Lab - Dashboard</title>
    <link rel="stylesheet" href="style-responsive.css">
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
    <script src="javascript/date.js"></script>
    <script src="jquery/tabeleSortatejq.js"></script>
    <script src="jquery/validarijq.js"></script>    
</head>
<body>
    <div class="container">
        <header>
            <h1>COFFEE LAB</h1>
            <p>Dashboard</p>
            <p>Bine ai venit, <?php echo htmlspecialchars($_SESSION['nume'] ?? $_SESSION['username']); ?>! 
            <a href="php/logout.php" style="color: white;">Logout</a></p>
        </header>

<nav>
    <ul>
        <li><a href="home.php">Acasa</a></li>
        <li><a href="meniu.php">Meniu</a></li>
        <li><a href="rezervari.php">Rezervari</a></li>
        <li><a href="comenzi.php">Comenzi</a></li>  <!-- ← Adaugă asta -->
        <li><a href="dashboard.php">Dashboard</a></li>
        <li><a href="logs.php"> Loguri</a></li> 
        <li><a href="upload.php">Profil</a></li>
        <?php if (canApproveOrders()): ?>
        <li><a href="admin_comenzi.php">Admin Comenzi</a></li>
        <li><a href="admin_rezervari.php">Admin Rezervări</a></li>
        <?php endif; ?>
        <li><a href="sprite-demo.html">Sprite</a></li>
    </ul>
</nav>

        <h2>Promotiile saptamanii</h2>
        <div class="carousel-container">
            <button class="carousel-btn prev" id="prevBtn">❮</button>
            <div class="carousel-slide" id="carouselSlide">
                <div class="slide-content">
                    <h3 id="slideTitle"></h3>
                    <p id="slideText"></p>
                    <a href="#" id="slideLink" class="btn">Vezi oferta</a>
                </div>
            </div>
            <button class="carousel-btn next" id="nextBtn">❯</button>
        </div>
        <div class="carousel-dots" id="carouselDots"></div>
        
        <div class="dashboard-header">
            <h2>Statistici</h2>
            <div class="action-buttons">
                <a href="rezervari.php" class="btn">Rezerva acum</a>
            </div>
        </div>

        <!-- Statistici DINAMICE din baza de date -->
        <div class="stats-grid">
            <div class="stat-card"><h3><?php echo $myRezervari; ?></h3><p>Rezervările mele</p></div>
            <div class="stat-card"><h3><?php echo $_SESSION['role']; ?></h3><p>Rolul tău</p></div>
            <div class="stat-card"><h3><?php echo $totalUsers; ?></h3><p>Utilizatori</p></div>
            <div class="stat-card"><h3>4.8</h3><p>Rating mediu</p></div>
        </div>

        <!-- Rezervările mele - DINAMICE din baza de date -->
        <h2>Rezervările mele recente</h2>
        <div class="table-responsive">
            <table>
                <thead><tr><th>Data</th><th>Ora</th><th>Persoane</th><th>Status</th></tr></thead>
                <tbody>
                    <?php foreach ($rezervari as $rez): ?>
                    <tr>
                        <td><?php echo htmlspecialchars($rez['data']); ?></td>
                        <td><?php echo htmlspecialchars($rez['ora']); ?></td>
                        <td><?php echo htmlspecialchars($rez['persoane']); ?></td>
                        <td><?php echo htmlspecialchars($rez['status']); ?></td>
                    </tr>
                    <?php endforeach; ?>
                    <?php if (count($rezervari) == 0): ?>
                    <tr><td colspan="4">Nu ai nicio rezervare încă.</td></tr>
                    <?php endif; ?>
                </tbody>
            </table>
        </div>

        <!-- RESTUL conținutului tău original din dashboard.html -->
        <!-- Liste imbricate colapsibile -->
        <h2>Meniu interactiv</h2>
        <div class="nested-list-container">
            <ul id="meniuNested" class="nested-list">
                <li class="nested-item">
                    <span class="nested-toggle">▶</span>
                    <span class="nested-title">Băuturi calde</span>
                    <ul class="nested-sublist">
                        <li>Espresso - 8 lei</li>
                        <li>Latte - 12 lei</li>
                        <li>Cappuccino - 11 lei</li>
                        <li>Americano - 10 lei</li>
                    </ul>
                </li>
                <li class="nested-item">
                    <span class="nested-toggle">▶</span>
                    <span class="nested-title">Băuturi reci</span>
                    <ul class="nested-sublist">
                        <li>Iced Latte - 14 lei</li>
                        <li>Frappe - 15 lei</li>
                        <li>Limonadă - 10 lei</li>
                        <li>Smoothie cu fructe - 16 lei</li>
                    </ul>
                </li>
                <li class="nested-item">
                    <span class="nested-toggle">▶</span>
                    <span class="nested-title">Deserturi</span>
                    <ul class="nested-sublist">
                        <li>Cheesecake - 14 lei</li>
                        <li>Brownie - 10 lei</li>
                        <li>Tiramisu - 16 lei</li>
                        <li>Tartă cu afine - 12 lei</li>
                    </ul>
                </li>
                <li class="nested-item">
                    <span class="nested-toggle">▶</span>
                    <span class="nested-title">Gustări sărate</span>
                    <ul class="nested-sublist">
                        <li>Sandwich - 15 lei</li>
                        <li>Toast cu avocado - 18 lei</li>
                        <li>Wrap cu pui - 16 lei</li>
                        <li>Cartofi wedges - 12 lei</li>
                    </ul>
                </li>
            </ul>
        </div>

        <h3>Feedback</h3>
        <form id="feedbackForm">
            <fieldset>
                <legend>Spune-ne parerea ta!</legend>
                <!-- formularul tău original -->
                <div class="form-group">
                    <label>Full name: </label>
                    <input type="text" id="fbName" placeholder="ex: Popescu Mihai">
                </div>
                <div>
                    <label>Email:</label>
                    <input type="email" id="fbEmail" placeholder="ex:mihaip@exemplu.ro">
                </div>
                <div>
                    <label>Rating(1-5)</label>
                    <select id="fbRating">
                        <option>Alege Rating</option>
                        <option value="5">5x⭐ - Excelent</option>
                        <option value="4">4x⭐ - Foarte bine</option>
                        <option value="3">3x⭐ - Bine</option>
                        <option value="2">2x⭐ - Slab</option>
                        <option value="1">1x⭐ - Nesatisfăcător</option>
                    </select>
                </div>
                <div>
                    <label>Recomanzi Coffee Lab?</label><br>
                    <label for="radioDa">Da</label>
                    <input type="radio" name="recomanda" value="da" id="radioDa">
                    <label for="radioNu">Nu</label>
                    <input type="radio" name="recomanda" value="nu" id="radioNu">
                    <label for="radioPoate">Poate</label>
                    <input type="radio" name="recomanda" value="poate" id="radioPoate">
                </div>
                <div>
                    <label>Vrem sa iti stim parerea!</label>
                    <textarea id="fbMesaj" rows="3" placeholder="type your message here..."></textarea>
                </div>
                <div class="form-buttons">
                    <button type="submit">Trimite feedback</button>
                    <button type="reset">Reseteaza</button>
                </div>
            </fieldset>
        </form>
        <div id="feedbackErrors"></div>
        
        <div class="widget">
            <div class="widget-header">Recenzii recente</div>
            <div class="widget-body">
                <div><p><strong>Maria I.</strong> - Cafea excelenta</p></div>
                <div><p><strong>Andrei P.</strong> - Deserturi delicioase</p></div>
                <div><p><strong>Elena C.</strong> - Personal amabil</p></div>
            </div>
        </div>

        <div class="widget">
            <div class="widget-header">Program & Contact</div>
            <div class="widget-body">
                <p><strong>Luni - Vineri:</strong> 08:00 - 22:00</p>
                <p><strong>Sambata - Duminica:</strong> 10:00 - 00:00</p>
                <hr>
                <p> (123) 456 7890</p>
                <p> contact@coffeelab.ro</p>
            </div>
        </div>

        <div class="widget">
            <div class="widget-header">Oferta speciala</div>
            <div class="widget-body">
                <h3>-20%</h3>
                <p>La orice comanda online</p>
                <p id="ofertaInterval">Luni 12:00 - 15:00</p>
                <p id="ofertaStatus"></p>
                <button id="aplicaOfertaBtn" class="btn">Aplică oferta pe tabel</button>
            </div>
        </div>

        <!-- Tabele cu sortare (funcționează cu JS-ul tău) -->
        <h2>Vanzari zilnice</h2>
        <div class="table-responsive">
            <table id="salesTable">
                <thead>
                    <tr>
                        <th data-column="zi" class="sortable">Zi</th>
                        <th data-column="cafea" class="sortable">Cafea</th>
                        <th data-column="gustari" class="sortable">Gustări</th>
                        <th data-column="deserturi" class="sortable">Deserturi</th>
                        <th data-column="total" class="sortable">Total (lei)</th>
                    </tr>
                </thead>
                <tbody id="salesTableBody"></tbody>
            </table>
        </div>

        <div class="table-responsive">
            <table id="verticalTable">
                <thead id="verticalHeader"></thead>
                <tbody id="verticalBody"></tbody>
            </table>
        </div>

        <footer>
            <p>Coffee Lab - Dashboard</p>
        </footer>
    </div>
</body>
</html>