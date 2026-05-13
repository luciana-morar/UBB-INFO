document.addEventListener('DOMContentLoaded', function() {
    
   const feedbackForm = document.getElementById('feedbackForm');
    
    if (feedbackForm) {
        feedbackForm.addEventListener('submit', function(event) {
            event.preventDefault(); 
            
            let isValid = true;
            
            const nume = document.getElementById('fbName');
            if (nume.value.trim() === '') {
                nume.style.border = '2px solid red';
                isValid = false;
            } 
            
            const email = document.getElementById('fbEmail');
            const emailValue = email.value.trim();
            if (emailValue === '' || !emailValue.includes('@') || !emailValue.includes('.')) {
                email.style.border = '2px solid red';
                isValid = false;
            } 
            
            const rating = document.getElementById('fbRating');
            if (rating.value === '' || rating.value === 'Alege Rating') {
                rating.style.border = '2px solid red';
                isValid = false;
            } 
            
            const radioSelected = document.querySelector('input[name="recomanda"]:checked');
            if (!radioSelected) {
                const allRadios = document.querySelectorAll('input[name="recomanda"]');
                allRadios.forEach(radio => {
                    radio.style.outline = '2px solid red';
                });
                isValid = false;
            } 
            
            const errorDiv = document.getElementById('feedbackErrors');
            if (isValid) {
                errorDiv.innerHTML = 'Feedback trimis cu succes!';
                errorDiv.style.color = 'green';
            } else {
                errorDiv.innerHTML = 'Te rugăm să completezi toate câmpurile obligatorii (marcate cu roșu).';
                errorDiv.style.color = 'red';
                
            }
        });
        
        // elimina chenarul roșu la tastare
        const numeInput = document.getElementById('fbName');
        if (numeInput) {
            numeInput.addEventListener('input', function() {
                if (this.value.trim() !== '') {
                    this.style.border = '1px solid #ddd';
                }
            });
        }
        
        const emailInput = document.getElementById('fbEmail');
        if (emailInput) {
            emailInput.addEventListener('input', function() {
                if (this.value.trim() !== '' && this.value.includes('@')) {
                    this.style.border = '1px solid #ddd';
                }
            });
        }
        
        const ratingSelect = document.getElementById('fbRating');
        if (ratingSelect) {
            ratingSelect.addEventListener('change', function() {
                if (this.value !== '' && this.value !== 'Alege Rating') {
                    this.style.border = '1px solid #ddd';
                }
            });
        }
    }
    
    // meniu.html
    const comandaForm = document.getElementById('comandaForm');
    
    if (comandaForm) {
        comandaForm.addEventListener('submit', function(event) {
            event.preventDefault();
            
            let isValid = true;
            
            const nume = document.getElementById('comandaNume');
            if (nume.value.trim() === '') {
                nume.style.border = '2px solid red';
                isValid = false;
            }
            
            const prenume = document.getElementById('comandaPrenume');
            if (prenume.value.trim() === '') {
                prenume.style.border = '2px solid red';
                isValid = false;
            }
            
            const email = document.getElementById('comandaEmail');
            const emailValue = email.value.trim();
            if (emailValue === '' || !emailValue.includes('@') || !emailValue.includes('.')) {
                email.style.border = '2px solid red';
                isValid = false;
            }

            const telefon = document.getElementById('comandaTelefon');
            const telefonValue = telefon.value.trim();  
            const telefonRegex = /^\d{10}$/;
            if (telefonValue === '' || !telefonRegex.test(telefonValue)) {
                telefon.style.border = '2px solid red';
                isValid = false;
            }            
           
            const errorDiv = document.getElementById('comandaErrors');
            if (isValid) {
                errorDiv.innerHTML = 'Comandă plasată cu succes! Vom contacta în curând.';
                errorDiv.style.color = 'green';
                
            } else {
                errorDiv.innerHTML = ' Te rugăm să completezi toate câmpurile obligatorii (marcate cu roșu).';
                errorDiv.style.color = 'red';
                errorDiv.style.padding = '15px';
                errorDiv.style.borderRadius = '10px';
                
            }
        });
        
    }

    // (rezervari.html) 
    const rezervareForm = document.getElementById('rezervareForm');
    
    if (rezervareForm) {
        rezervareForm.addEventListener('submit', function(event) {
            event.preventDefault();
            
            let isValid = true;
            
            const nume = document.getElementById('rezervareNume');
            if (nume.value.trim() === '') {
                nume.style.border = '2px solid red';
                isValid = false;
            }
            
            
            const prenume = document.getElementById('rezervarePrenume');
            if (prenume.value.trim() === '') {
                prenume.style.border = '2px solid red';
                isValid = false;
            } 
            
            const email = document.getElementById('rezervareEmail');
            const emailValue = email.value.trim();
            if (emailValue === '' || !emailValue.includes('@') || !emailValue.includes('.')) {
                email.style.border = '2px solid red';
                isValid = false;
            }

            const data = document.getElementById('rezervareData');
            if (data.value === '') {
                data.style.border = '2px solid red';
                isValid = false;
            } 
            
            const persoane = document.getElementById('rezervarePersoane');
            const nrPersoane = parseInt(persoane.value);
            if (isNaN(nrPersoane) || nrPersoane < 1 || nrPersoane > 20) {
                persoane.style.border = '2px solid red';
                isValid = false;
            } 
            const telefon = document.getElementById('rezervareTelefon');
            const telefonValue = telefon.value.trim();  
            const telefonRegex = /^\d{10}$/;
            if (telefonValue === '' || !telefonRegex.test(telefonValue)) {
                telefon.style.border = '2px solid red';
                isValid = false;
            }
            

            const errorDiv = document.getElementById('rezervareErrors');
            if (isValid) {
                errorDiv.innerHTML = 'Rezervare confirmată! Vă așteptăm cu drag!';
                errorDiv.style.color = 'green';
            
            } else {
                errorDiv.innerHTML = 'Te rugăm să completezi toate câmpurile obligatorii (marcate cu roșu).';
                errorDiv.style.color = 'red';
                errorDiv.style.padding = '15px';
                errorDiv.style.borderRadius = '10px';
                
            }
        });
        
       
    }

        // JUDEȚ - LOCALITATE
    const judetSelect = document.getElementById('judetSelect');
    const localitateSelect = document.getElementById('localitateSelect');
    
    if (judetSelect && localitateSelect) {
        judetSelect.addEventListener('change', function() {
            const judet = this.value;
            
            // sterge opțiunile existente
            localitateSelect.innerHTML = '<option>-- Alege localitatea --</option>';
            
            if (judet && judeteSiLocalitati[judet]) {
                judeteSiLocalitati[judet].forEach(localitate => {
                    const option = document.createElement('option');
                    option.value = localitate;
                    option.textContent = localitate;
                    localitateSelect.appendChild(option);
                });
            } else if (judet === '') {
                localitateSelect.innerHTML = '<option>-- Alege mai întâi județul --</option>';
            }
        });
    }
    
    // VRSTA - EVENIMENTE 
    const varstaInput = document.getElementById('varstaParticipant');
    const evenimenteSelect = document.getElementById('evenimenteRecomandate');
    
    if (varstaInput && evenimenteSelect) {
        varstaInput.addEventListener('input', function() {
            const varsta = this.value.trim();
            
            evenimenteSelect.innerHTML = '';
            
            if (isNaN(varsta)) {
                evenimenteSelect.innerHTML = '<option>-- Alege vârsta mai întâi --</option>';
                return;
            }
            
            let evenimente = [];
            
            if (varsta < 18) {
                evenimente = evenimentePerVirsta.sub18;
            } else {
                evenimente = evenimentePerVirsta.peste18;
            }
            
            evenimente = evenimente.concat(evenimentePerVirsta.all);
            
            evenimente.forEach(eveniment => {
                const option = document.createElement('option');
                option.value = eveniment;
                option.textContent = eveniment;
                evenimenteSelect.appendChild(option);
            });
        });
    }


    // carusel
const slideContainer = document.getElementById('carouselSlide');
const slideTitle = document.getElementById('slideTitle');
const slideText = document.getElementById('slideText');
const slideLink = document.getElementById('slideLink');
const prevBtn = document.getElementById('prevBtn');
const nextBtn = document.getElementById('nextBtn');

let currentSlide = 0;

if (slideContainer && typeof caruselSlideuri !== 'undefined') {
    
    function updateCarousel(index) {
        if (index < 0) index = caruselSlideuri.length - 1;
        if (index >= caruselSlideuri.length) index = 0;
        currentSlide = index;
        
        const slide = caruselSlideuri[currentSlide];
        slideContainer.style.backgroundImage = 'url("' + slide.imagine + '")';
        slideTitle.textContent = slide.titlu;
        slideText.textContent = slide.text;
        slideLink.href = slide.link;
    }
    
    prevBtn.onclick = () => updateCarousel(currentSlide - 1);
    nextBtn.onclick = () => updateCarousel(currentSlide + 1);
    
    updateCarousel(0);
    setInterval(() => updateCarousel(currentSlide + 1), 3000);
}


// liste imbiricate colapsabile
function initNestedLists() {
        const items = document.querySelectorAll('.nested-item');
        
        for (let i = 0; i < items.length; i++) {
            const item = items[i];
            const toggle = item.querySelector('.nested-toggle');
            const title = item.querySelector('.nested-title');
            const sublist = item.querySelector('.nested-sublist');
            
            function toggleSublist() {
                if (sublist) {
                    sublist.classList.toggle('show');
                    if (sublist.classList.contains('show')) {
                        toggle.textContent = '▼';
                    } else {
                        toggle.textContent = '▶';
                    }
                }
            }
            
            if (toggle) {
                toggle.addEventListener('click', toggleSublist);
            }
            
            if (title) {
                title.addEventListener('click', toggleSublist);
            }
        }
    }
    
    initNestedLists();

});