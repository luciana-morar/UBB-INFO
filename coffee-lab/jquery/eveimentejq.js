$(document).ready(function() {
    
    function initEvenimenteClick() {
        const $randuri = $('.evenimente-table tbody tr');
        const $mesajDiv = $('#evenimentMessage');
        
        if (!$randuri.length || !$mesajDiv.length) return;
        
        $randuri.each(function() {
            const $rand = $(this);
            
            $rand.on('click', function() {
                const $celule = $(this).find('td');
                
                if ($celule.length >= 3) {
                    const data = $celule.eq(0).text().trim();
                    const nume = $celule.eq(1).text().trim();
                    const descriere = $celule.eq(2).text().trim();
                    
                    $mesajDiv.html('<strong>' + data + '</strong> - <strong>' + nume + '</strong><br>' + descriere);
                    $mesajDiv.addClass('show');
                }
            });
        });
    }
    
    initEvenimenteClick();
    
});