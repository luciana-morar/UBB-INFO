// jquery/validarijq.js - VERSIUNE CURĂȚATĂ
$(document).ready(function() {
    
    // ========== VALIDARE FORMULAR COMANDA ==========
    var $comandaForm = $('#comandaForm');
    
    if ($comandaForm.length) {
        $comandaForm.on('submit', function(event) {
            var isValid = true;
            var errorMessages = [];
            
            var $nume = $('#comandaNume');
            if ($nume.length && $nume.val().trim() === '') {
                $nume.css('border', '2px solid red');
                isValid = false;
                errorMessages.push('Numele este obligatoriu');
            } else if ($nume.length) {
                $nume.css('border', '1px solid #ddd');
            }
            
            var $prenume = $('#comandaPrenume');
            if ($prenume.length && $prenume.val().trim() === '') {
                $prenume.css('border', '2px solid red');
                isValid = false;
                errorMessages.push('Prenumele este obligatoriu');
            } else if ($prenume.length) {
                $prenume.css('border', '1px solid #ddd');
            }
            
            var $email = $('#comandaEmail');
            if ($email.length) {
                var emailValue = $email.val().trim();
                if (emailValue === '' || !emailValue.includes('@') || !emailValue.includes('.')) {
                    $email.css('border', '2px solid red');
                    isValid = false;
                    errorMessages.push('Email valid este obligatoriu');
                } else {
                    $email.css('border', '1px solid #ddd');
                }
            }
            
            var $telefon = $('#comandaTelefon');
            if ($telefon.length && $telefon.val().trim() !== '') {
                var telefonRegex = /^\d{10}$/;
                if (!telefonRegex.test($telefon.val().trim())) {
                    $telefon.css('border', '2px solid red');
                    isValid = false;
                    errorMessages.push('Telefonul trebuie să aibă 10 cifre');
                } else {
                    $telefon.css('border', '1px solid #ddd');
                }
            }
            
            // Înlocuiește vechea verificare a produselor cu asta:
            var produseSelectate = $('input[name^="prod"]:checked').length; 

            if (produseSelectate === 0) {
                isValid = false;
                errorMessages.push('Selectează cel puțin un produs și cantitatea acestuia');
            }
            
            var $errorDiv = $('#comandaErrors');
            if ($errorDiv.length) {
                if (isValid) {
                    // Permite submit-ul - ȘTERGE orice eroare
                    $errorDiv.html('').css('color', '').css('padding', '').css('background', '');
                    return true;
                } else {
                    $errorDiv.html(errorMessages.join(' • '))
                        .css('color', 'red')
                        .css('padding', '10px')
                        .css('borderRadius', '5px')
                        .css('background', '#f8d7da');
                    event.preventDefault();
                    return false;
                }
            }
            return true;
        });
        
        // Elimină chenarul roșu la tastare
        $('#comandaNume, #comandaPrenume, #comandaEmail, #comandaTelefon').on('input', function() {
            var $this = $(this);
            if ($this.attr('id') === 'comandaEmail') {
                if ($this.val().trim() !== '' && $this.val().includes('@')) {
                    $this.css('border', '1px solid #ddd');
                }
            } else if ($this.attr('id') === 'comandaTelefon') {
                var telefonRegex = /^\d{10}$/;
                if (telefonRegex.test($this.val().trim())) {
                    $this.css('border', '1px solid #ddd');
                }
            } else {
                if ($this.val().trim() !== '') $this.css('border', '1px solid #ddd');
            }
        });
    }
    
    // ========== VALIDARE FORMULAR FEEDBACK ==========
    var $feedbackForm = $('#feedbackForm');
    
    if ($feedbackForm.length) {
        $feedbackForm.on('submit', function(event) {
            event.preventDefault();
            
            var isValid = true;
            var errorMessages = [];
            
            var $nume = $('#fbName');
            if ($nume.val().trim() === '') {
                $nume.css('border', '2px solid red');
                isValid = false;
                errorMessages.push('Numele este obligatoriu');
            } else {
                $nume.css('border', '1px solid #ddd');
            }
            
            var $email = $('#fbEmail');
            var emailValue = $email.val().trim();
            if (emailValue === '' || !emailValue.includes('@') || !emailValue.includes('.')) {
                $email.css('border', '2px solid red');
                isValid = false;
                errorMessages.push('Email valid este obligatoriu');
            } else {
                $email.css('border', '1px solid #ddd');
            }
            
            var $rating = $('#fbRating');
            if ($rating.val() === '' || $rating.val() === 'Alege Rating') {
                $rating.css('border', '2px solid red');
                isValid = false;
                errorMessages.push('Selectează un rating');
            } else {
                $rating.css('border', '1px solid #ddd');
            }
            
            var $radioSelected = $('input[name="recomanda"]:checked');
            if (!$radioSelected.length) {
                $('input[name="recomanda"]').css('outline', '2px solid red');
                isValid = false;
                errorMessages.push('Alege o opțiune pentru recomandare');
            } else {
                $('input[name="recomanda"]').css('outline', 'none');
            }
            
            var $errorDiv = $('#feedbackErrors');
            if (isValid) {
                $errorDiv.html('Feedback trimis cu succes! Mulțumim!')
                    .css('color', 'green')
                    .css('padding', '10px')
                    .css('borderRadius', '5px')
                    .css('background', '#d4edda');
                // Aici poți trimite datele prin AJAX sau reset form
                $feedbackForm[0].reset();
            } else {
                $errorDiv.html(errorMessages.join(' • '))
                    .css('color', 'red')
                    .css('padding', '10px')
                    .css('borderRadius', '5px')
                    .css('background', '#f8d7da');
            }
        });
        
        $('#fbName').on('input', function() {
            if ($(this).val().trim() !== '') $(this).css('border', '1px solid #ddd');
        });
        
        $('#fbEmail').on('input', function() {
            if ($(this).val().trim() !== '' && $(this).val().includes('@')) {
                $(this).css('border', '1px solid #ddd');
            }
        });
        
        $('#fbRating').on('change', function() {
            if ($(this).val() !== '' && $(this).val() !== 'Alege Rating') {
                $(this).css('border', '1px solid #ddd');
            }
        });
    }
    
    // ========== VALIDARE FORMULAR REZERVARE ==========
    var $rezervareForm = $('#rezervareForm');
    
    if ($rezervareForm.length) {
        $rezervareForm.on('submit', function(event) {
            // Permite submit-ul - validarea se face pe server
            return true;
        });
    }
    
    // ========== JUDEȚ - LOCALITATE ==========
    var $judetSelect = $('#judetSelect');
    var $localitateSelect = $('#localitateSelect');
    
    if ($judetSelect.length && $localitateSelect.length && typeof judeteSiLocalitati !== 'undefined') {
        $judetSelect.on('change', function() {
            var judet = $(this).val();
            $localitateSelect.html('<option>-- Alege localitatea --</option>');
            
            if (judet && judeteSiLocalitati[judet]) {
                $.each(judeteSiLocalitati[judet], function(index, localitate) {
                    $localitateSelect.append($('<option>').val(localitate).text(localitate));
                });
            }
        });
    }
    
    // ========== VÂRSTA - EVENIMENTE ==========
    var $varstaInput = $('#varstaParticipant');
    var $evenimenteSelect = $('#evenimenteRecomandate');
    
    if ($varstaInput.length && $evenimenteSelect.length && typeof evenimentePerVirsta !== 'undefined') {
        $varstaInput.on('input', function() {
            var varsta = $(this).val().trim();
            $evenimenteSelect.empty();
            
            if (isNaN(varsta)) {
                $evenimenteSelect.html('<option>-- Alege vârsta mai întâi --</option>');
                return;
            }
            
            var evenimente = [];
            if (varsta < 18) {
                evenimente = evenimentePerVirsta.sub18 || [];
            } else {
                evenimente = evenimentePerVirsta.peste18 || [];
            }
            
            evenimente = evenimente.concat(evenimentePerVirsta.all || []);
            
            $.each(evenimente, function(index, eveniment) {
                $evenimenteSelect.append($('<option>').val(eveniment).text(eveniment));
            });
        });
    }
    
    // ========== CARUSEL ==========
    var $slideContainer = $('#carouselSlide');
    var $slideTitle = $('#slideTitle');
    var $slideText = $('#slideText');
    var $slideLink = $('#slideLink');
    var $prevBtn = $('#prevBtn');
    var $nextBtn = $('#nextBtn');
    
    var currentSlide = 0;
    var autoSlideInterval;
    
    if ($slideContainer.length && typeof caruselSlideuri !== 'undefined' && caruselSlideuri.length > 0) {
        
        function updateCarousel(index) {
            if (index < 0) index = caruselSlideuri.length - 1;
            if (index >= caruselSlideuri.length) index = 0;
            currentSlide = index;
            
            var slide = caruselSlideuri[currentSlide];
            $slideContainer.css('backgroundImage', 'url("' + slide.imagine + '")');
            $slideTitle.text(slide.titlu);
            $slideText.text(slide.text);
            $slideLink.attr('href', slide.link);
        }
        
        $prevBtn.on('click', function() {
            clearInterval(autoSlideInterval);
            updateCarousel(currentSlide - 1);
            autoSlideInterval = setInterval(function() { updateCarousel(currentSlide + 1); }, 3000);
        });
        
        $nextBtn.on('click', function() {
            clearInterval(autoSlideInterval);
            updateCarousel(currentSlide + 1);
            autoSlideInterval = setInterval(function() { updateCarousel(currentSlide + 1); }, 3000);
        });
        
        updateCarousel(0);
        autoSlideInterval = setInterval(function() { updateCarousel(currentSlide + 1); }, 3000);
    }
    
    // ========== LISTE IMBRICATE COLAPSABILE ==========
    function initNestedLists() {
        $('.nested-item').each(function() {
            var $item = $(this);
            var $toggle = $item.find('.nested-toggle');
            var $title = $item.find('.nested-title');
            var $sublist = $item.find('.nested-sublist');
            
            function toggleSublist() {
                if ($sublist.length) {
                    $sublist.toggleClass('show');
                    if ($sublist.hasClass('show')) {
                        $toggle.text('▼');
                    } else {
                        $toggle.text('▶');
                    }
                }
            }
            
            if ($toggle.length) $toggle.on('click', toggleSublist);
            if ($title.length) $title.on('click', toggleSublist);
        });
    }
    
    initNestedLists();
});