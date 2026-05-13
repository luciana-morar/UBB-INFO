$(document).ready(function() {
    
    let sortCol = 'zi';
    let sortDir = 'asc';
    
    const ordineZile = {
        'Luni': 1, 'Marti': 2, 'Miercuri': 3, 'Joi': 4, 'Vineri': 5
    };
    
    function afiseazaTabelClasic() {
        const $tbody = $('#salesTableBody');
        if (!$tbody.length) return;
        
        const dateSortate = [...vanariZilnice];
        dateSortate.sort(function(a, b) {
            if (sortCol === 'zi') {
                const idxA = ordineZile[a.zi];
                const idxB = ordineZile[b.zi];
                return sortDir === 'asc' ? idxA - idxB : idxB - idxA;
            }
            return sortDir === 'asc' ? a[sortCol] - b[sortCol] : b[sortCol] - a[sortCol];
        });
        
        $tbody.empty();
        $.each(dateSortate, function(i, r) {
            $tbody.append(
                '<tr>' +
                '<td>' + r.zi + '</td>' +
                '<td>' + r.cafea + '</td>' +
                '<td>' + r.gustari + '</td>' +
                '<td>' + r.deserturi + '</td>' +
                '<td>' + r.total + ' lei</td>' +
                '</tr>'
            );
        });
        
    }
    
    let sortVerticalCategorie = 'cafea';
    let sortVerticalDir = 'asc';
    
    function afiseazaTabelVertical() {
        const $headerRow = $('#verticalHeader');
        const $bodyRow = $('#verticalBody');
        if (!$headerRow.length || !$bodyRow.length) return;
        
        const categorii = [
            { cheie: 'cafea', nume: 'Cafea' },
            { cheie: 'gustari', nume: 'Gustări' },
            { cheie: 'deserturi', nume: 'Deserturi' },
            { cheie: 'total', nume: 'Total (lei)' }
        ];
        const zile = ['Luni', 'Marti', 'Miercuri', 'Joi', 'Vineri'];
        
        const zileSortate = [...zile];
        zileSortate.sort(function(a, b) {
            const indexA = zile.indexOf(a);
            const indexB = zile.indexOf(b);
            let valA = vanariZilnice[indexA][sortVerticalCategorie];
            let valB = vanariZilnice[indexB][sortVerticalCategorie];
            return sortVerticalDir === 'asc' ? valA - valB : valB - valA;
        });
        
        let headerHtml = '<tr><th>Categorie / Zi</th>';
        $.each(zileSortate, function(i, zi) {
            headerHtml += '<th>' + zi + '</th>';
        });
        headerHtml += '</tr>';
        $headerRow.html(headerHtml);
        
        let bodyHtml = '';
        $.each(categorii, function(i, cat) {
            bodyHtml += '<tr>';
            bodyHtml += '<td class="categorie-click" data-categorie="' + cat.cheie + '" style="cursor:pointer; font-weight:bold; background-color:#e3f2fd;">' + cat.nume + '</td>';
            $.each(zileSortate, function(j, zi) {
                const indexZi = zile.indexOf(zi);
                let val = vanariZilnice[indexZi][cat.cheie];
                if (cat.cheie === 'total') val = val + ' lei';
                bodyHtml += '<td>' + val + '</td>';
            });
            bodyHtml += '</tr>';
        });
        $bodyRow.html(bodyHtml);
        
        $('.categorie-click').off('click mouseenter mouseleave').on({
            mouseenter: function() { $(this).css({ backgroundColor: '#3b82f6', color: 'white' }); },
            mouseleave: function() { $(this).css({ backgroundColor: '#e3f2fd', color: 'black' }); },
            click: function() {
                const categorie = $(this).data('categorie');
                if (sortVerticalCategorie === categorie) {
                    sortVerticalDir = (sortVerticalDir === 'asc') ? 'desc' : 'asc';
                } else {
                    sortVerticalCategorie = categorie;
                    sortVerticalDir = 'asc';
                }
                afiseazaTabelVertical();
            }
        });
    }

    //oferta
 
    let reducereAplicata = false;
    let totaluriSalvate = [];

    function salveazaTotaluri() {
        totaluriSalvate = [];
        for (let i = 0; i < vanariZilnice.length; i++) {
            totaluriSalvate.push(vanariZilnice[i].total);
        }
    }

    function aplicaReducere() {
        for (let i = 0; i < vanariZilnice.length; i++) {
            if (vanariZilnice[i].zi === 'Luni') {
                vanariZilnice[i].total = totaluriSalvate[i] * 0.8; // 20% reducere
                return true;
            }
        }
        return false;
    }

    function reseteazaReducere() {
        for (let i = 0; i < vanariZilnice.length; i++) {
            vanariZilnice[i].total = totaluriSalvate[i];
        }
    }

    function esteInOferta() {

        // Luni, ora 10:00 
        const acum = new Date();


        acum.setDate(acum.getDate() + (1 - acum.getDay()))
        acum.setHours(10, 0, 0);


        const ora = acum.getHours();
        const minut = acum.getMinutes();
        const timpCurent = ora * 60 + minut;
        
        // Luni  12:00 - 15:00
        const esteLuni = (acum.getDay() === 1); // 1 = Luni
        const esteInInterval = (timpCurent >= 720 && timpCurent <= 900); // 12:00=720, 15:00=900
        
        return (esteLuni && esteInInterval);
    }
    

    function arataStatus() {
        const status = $('#ofertaStatus');
        const btn = $('#aplicaOfertaBtn');
        
        if (!status.length) return;
        
        if (reducereAplicata) {
            status.html('Reducere aplicată!').css('color', 'green');
            
        } else if (esteInOferta()) {
            status.html(' Ofertă activă! Apasă butonul pentru 20% reducere').css('color', 'red');
            
        } else {
            status.html('Ofertă Luni 12:00-15:00').css('color', '#666');
            
        }
    }

    const afisareOriginala = afiseazaTabelClasic;
    
    afiseazaTabelClasic = function() {
        const $tbody = $('#salesTableBody');
        if (!$tbody.length) return;
        
        const dateSortate = [...vanariZilnice];
        dateSortate.sort(function(a, b) {
            if (sortCol === 'zi') {
                return sortDir === 'asc' ? ordineZile[a.zi] - ordineZile[b.zi] : ordineZile[b.zi] - ordineZile[a.zi];
            }
            return sortDir === 'asc' ? a[sortCol] - b[sortCol] : b[sortCol] - a[sortCol];
        });
        
        $tbody.empty();
        
        for (let i = 0; i < dateSortate.length; i++) {
            const r = dateSortate[i];
            let totalHtml = r.total + ' lei';
            
            if (reducereAplicata && r.zi === 'Luni') {
                totalHtml = '<span style="text-decoration:line-through;color:#999">' + totaluriSalvate[i] + ' lei</span> <span style="color:red;font-weight:bold">' + r.total.toFixed(2) + ' lei</span>';
            }
            
            $tbody.append(
                '<tr>' +
                '<td>' + r.zi + '</td>' +
                '<td>' + r.cafea + '</td>' +
                '<td>' + r.gustari + '</td>' +
                '<td>' + r.deserturi + '</td>' +
                '<td>' + totalHtml + '</td>' +
                '</tr>'
            );
        }
        
    };

    // Pornește oferta
    salveazaTotaluri();
    arataStatus();
    
    $('#aplicaOfertaBtn').on('click', function() {
        if (!reducereAplicata && !esteInOferta()) return;
        
        if (reducereAplicata) {
            reseteazaReducere();
            reducereAplicata = false;
            $(this).text('Aplică reducerea 20%');
        } else {
            aplicaReducere();
            reducereAplicata = true;
            $(this).text('Elimină reducerea');
        }
        afiseazaTabelClasic();
        arataStatus();
    });



//pornire
    function pornesteTabele() {
        $('#salesTable .sortable').each(function() {
            const $th = $(this);
            $th.on('click', function() {
                const col = $(this).data('column');
                if (sortCol === col) {
                    sortDir = (sortDir === 'asc') ? 'desc' : 'asc';
                } else {
                    sortCol = col;
                    sortDir = 'asc';
                }
                afiseazaTabelClasic();
            });
        });
        afiseazaTabelClasic();
        afiseazaTabelVertical();
    }
    
    pornesteTabele();


    
});