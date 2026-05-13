// jquery/calculatorjq.js - VERSIUNE CORECTĂ
$(document).ready(function() {
    
    function initCalculatorBacsis() {
        var $checkboxCafea = $('input[name="prod1"]');
        var $checkboxLatte = $('input[name="prod2"]');
        var $checkboxSandwich = $('input[name="prod3"]');
        var $checkboxCheesecake = $('input[name="prod4"]');
        
        var $inputCafea = $('input[name="cant1"]');
        var $inputLatte = $('input[name="cant2"]');
        var $inputSandwich = $('input[name="cant3"]');
        var $inputCheesecake = $('input[name="cant4"]');
        
        var $totalComanda = $('#totalComanda');
        var $procentSelect = $('#procentBacsis');
        var $bacsisLei = $('#bacsisLei');
        var $totalPlata = $('#totalPlata');
        var $mesajDiv = $('#bacsisMesaj');
        
        var preturi = {
            cafea: 8,
            latte: 12,
            sandwich: 15,
            cheesecake: 14
        };
        
        function calculeazaTotalComanda() {
            var cantCafea = $checkboxCafea.is(':checked') ? (parseInt($inputCafea.val()) || 0) : 0;
            var cantLatte = $checkboxLatte.is(':checked') ? (parseInt($inputLatte.val()) || 0) : 0;
            var cantSandwich = $checkboxSandwich.is(':checked') ? (parseInt($inputSandwich.val()) || 0) : 0;
            var cantCheesecake = $checkboxCheesecake.is(':checked') ? (parseInt($inputCheesecake.val()) || 0) : 0;
            
            var total = (cantCafea * preturi.cafea) +
                        (cantLatte * preturi.latte) +
                        (cantSandwich * preturi.sandwich) +
                        (cantCheesecake * preturi.cheesecake);
            
            $totalComanda.val(total);
            return total;
        }
        
        function calculeazaBacsis() {
            var total = parseInt($totalComanda.val()) || 0;
            var procent = parseInt($procentSelect.val());
            var bacsis = (total * procent) / 100;
            var totalPlataVal = total + bacsis;
            
            $bacsisLei.val(bacsis.toFixed(2) + ' lei');
            $totalPlata.val(totalPlataVal.toFixed(2) + ' lei');
        }
        
        function actualizeazaTot() {
            calculeazaTotalComanda();
            calculeazaBacsis();
        }
        
        $('input[name="prod1"], input[name="prod2"], input[name="prod3"], input[name="prod4"]').on('change', actualizeazaTot);
        $('input[name="cant1"], input[name="cant2"], input[name="cant3"], input[name="cant4"]').on('input', actualizeazaTot);
        $procentSelect.on('change', calculeazaBacsis);
        
        // Inițializează
        actualizeazaTot();
    }
    
    initCalculatorBacsis();
});