const judeteSiLocalitati = {
    "București": ["Sector 1", "Sector 2", "Sector 3", "Sector 4", "Sector 5", "Sector 6"],
    "Cluj": ["Cluj-Napoca", "Florești", "Dej", "Turda"],
    "Timiș": ["Timișoara", "Lugoj", "Sânnicolau Mare", "Jimbolia"],
    "Brașov": ["Brașov", "Săcele", "Făgăraș", "Codlea", "Râșnov"]
};


const evenimentePerVirsta = {
    "sub18": [
        "Atelier de pictură",
        "Degustare deserturi (non-alcoolic)",
        "Concurs de board games",
        "Curs de barista pentru juniori"
    ],
    "peste18": [
        "Open Mic Night (cu consum de alcool)",
        "Degustare de vin",
        "Mixology Workshop",
        "Party cu DJ (18+)"
    ],
    "all": [
        "Concert acustic",
        "Atelier de creație",
        "Cafea și povești",
        "Proiecție film"
    ]
};

// carusel
const caruselSlideuri = [
    {
        imagine: "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=1200&h=450&fit=crop",
        titlu: "Cafea proaspăt prăjită",
        text: "Boabe de cafea de specialitate si retete noi în fiecare săptămână",
        link: "meniu.html#cafea"
    },
    {
        imagine: "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=1200&h=450&fit=crop",
        titlu: "Deserturi artizanale",
        text: "Cheesecake, tiramisu și brownie - făcute în fiecare dimineață",
        link: "meniu.html#deserturi"
    },
    {
        imagine: "https://images.unsplash.com/photo-1442512595331-e89e73853f31?w=1200&h=450&fit=crop",
        titlu: "Open Mic Night",
        text: "În fiecare vineri, de la 19:00. Vino să cânți sau să asculți!",
        link: "rezervari.html#evenimente"
    },
    {
        imagine: "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=1200&h=450&fit=crop",
        titlu: "Toast cu avocado -20%",
        text: "Ofertă specială în fiecare joi, între 15:00 și 18:00",
        link: "meniu.html#gustari"
    },
    {
        imagine: "https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=1200&h=450&fit=crop",
        titlu: "Atelier de creație",
        text: "Pictură, sculptură și cafea bună. Locurile sunt limitate!",
        link: "rezervari.html#evenimente"
    }
];


const vanariZilnice = [
    { zi: "Luni", cafea: 187, gustari: 92, deserturi: 78, total: 3240 },
    { zi: "Marti", cafea: 203, gustari: 105, deserturi: 82, total: 3580 },
    { zi: "Miercuri", cafea: 195, gustari: 98, deserturi: 85, total: 3420 },
    { zi: "Joi", cafea: 210, gustari: 110, deserturi: 91, total: 3780 },
    { zi: "Vineri", cafea: 245, gustari: 128, deserturi: 104, total: 4250 }
];