let sortCol = 'zi';      // coloana pentru sortare
let sortDir = 'asc';     // 'asc' sau 'desc'

const ordineZile = {
    'Luni': 1, 'Marti': 2, 'Miercuri': 3, 'Joi': 4, 'Vineri': 5
};

function afiseazaTabelClasic() {
    const tbody = document.getElementById('salesTableBody');
    if (!tbody) return;
    
    const dateSortate = [...vanariZilnice];
    dateSortate.sort(function(a, b) {
        if (sortCol === 'zi') {
            const idxA = ordineZile[a.zi];
            const idxB = ordineZile[b.zi];
            return sortDir === 'asc' ? idxA - idxB : idxB - idxA;
        }
        return sortDir === 'asc' ? a[sortCol] - b[sortCol] : b[sortCol] - a[sortCol];
    });
    
    tbody.innerHTML = '';
    for (let i = 0; i < dateSortate.length; i++) {
        const r = dateSortate[i];
        tbody.innerHTML += 
            '<tr>' +
            '<td>' + r.zi + '</td>' +
            '<td>' + r.cafea + '</td>' +
            '<td>' + r.gustari + '</td>' +
            '<td>' + r.deserturi + '</td>' +
            '<td>' + r.total + ' lei</td>' +
            '</tr>';
    }
    
    const celule = document.querySelectorAll('#salesTable th');
    for (let i = 0; i < celule.length; i++) {
        const th = celule[i];
        const col = th.getAttribute('data-column');
        th.innerHTML = th.innerHTML.replace(' ▲', '').replace(' ▼', '');
        if (col === sortCol) {
            th.innerHTML += sortDir === 'asc' ? ' ▲' : ' ▼';
        }
    }
}

let sortVerticalCol = 'Luni';
let sortVerticalDir = 'asc';
let sortVerticalCategorie = 'cafea';  // ← nou: categoria după care sortăm coloanele

function afiseazaTabelVertical() {
    const headerRow = document.getElementById('verticalHeader');
    const bodyRow = document.getElementById('verticalBody');
    if (!headerRow || !bodyRow) return;
    
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
        
        if (sortVerticalDir === 'asc') {
            return valA - valB;
        } else {
            return valB - valA;
        }
    });
    
    let headerHtml = '<table><th>Categorie / Zi</th>';
    for (let i = 0; i < zileSortate.length; i++) {
        const zi = zileSortate[i];
        headerHtml += '<th>' + zi + '</th>';
    }
    headerHtml += '</table>';
    headerRow.innerHTML = headerHtml;
    
    let bodyHtml = '';
    for (let i = 0; i < categorii.length; i++) {
        const cat = categorii[i];
        bodyHtml += '<tr>';
        
        bodyHtml += '<td style="cursor:pointer; font-weight:bold; background-color:#e3f2fd;" data-categorie="' + cat.cheie + '">' + cat.nume + '</td>';
        
        for (let j = 0; j < zileSortate.length; j++) {
            const zi = zileSortate[j];
            const indexZi = zile.indexOf(zi);
            let val = vanariZilnice[indexZi][cat.cheie];
            if (cat.cheie === 'total') val = val + ' lei';
            bodyHtml += '<td>' + val + '</td>';
        }
        bodyHtml += '</tr>';
    }
    bodyRow.innerHTML = bodyHtml;
    
    const randuriCategorii = document.querySelectorAll('#verticalBody tr td:first-child');
    for (let i = 0; i < randuriCategorii.length; i++) {
        const celula = randuriCategorii[i];
        const categorie = celula.getAttribute('data-categorie');
        
        celula.style.cursor = 'pointer';
        celula.style.backgroundColor = '#e3f2fd';
        celula.style.fontWeight = 'bold';
        
        celula.addEventListener('mouseenter', function() {
            this.style.backgroundColor = '#3b82f6';
            this.style.color = 'white';
        });
        
        celula.addEventListener('mouseleave', function() {
            this.style.backgroundColor = '#e3f2fd';
            this.style.color = 'black';
        });
        
        celula.addEventListener('click', function() {
            if (sortVerticalCategorie === categorie) {
                sortVerticalDir = (sortVerticalDir === 'asc') ? 'desc' : 'asc';
            } else {
                sortVerticalCategorie = categorie;
                sortVerticalDir = 'asc';
            }
            afiseazaTabelVertical();
        });
    }
}

function pornesteTabele() {
    const anteturi = document.querySelectorAll('#salesTable .sortable');
    for (let i = 0; i < anteturi.length; i++) {
        anteturi[i].addEventListener('click', function() {
            const col = this.getAttribute('data-column');
            if (sortCol === col) {
                sortDir = (sortDir === 'asc') ? 'desc' : 'asc';
            } else {
                sortCol = col;
                sortDir = 'asc';
            }
            afiseazaTabelClasic();
        });
    }
    afiseazaTabelClasic();
    afiseazaTabelVertical();
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', pornesteTabele);
} else {
    pornesteTabele();
}