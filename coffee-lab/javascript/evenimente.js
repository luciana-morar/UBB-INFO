function initEvenimenteClick() {
    const randuri = document.querySelectorAll('.evenimente-table tbody tr');
    const mesajDiv = document.getElementById('evenimentMessage');
    
    for (let i = 0; i < randuri.length; i++) {
        const rand = randuri[i];
       
        rand.addEventListener('click', function() {
            const celule = this.querySelectorAll('td');
            
            if (celule.length >= 3) {
                const data = celule[0].textContent.trim();
                const nume = celule[1].textContent.trim();
                const descriere = celule[2].textContent.trim();
                
                mesajDiv.innerHTML = '<strong>' + data + '</strong> - ' +
                                    '<strong>' + nume + '</strong><br>' +
                                    '' + descriere + '<br>'
                
                mesajDiv.classList.add('show');
                
            }
        });
    }
}
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function() {
        initEvenimenteClick();
        
    });
} 
