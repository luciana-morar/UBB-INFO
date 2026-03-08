package org.example.lab6perfect.ui;

public class Menu {

    public static void printMainMenu() {
        System.out.println("\n===== MENIU ==============");
        System.out.println("   1. Adauga persoana");
        System.out.println("   2. Sterge persoana");
        System.out.println("   3. Afiseaza persoane");
        System.out.println("   4. Adauga rata");
        System.out.println("   5. Sterge rata");
        System.out.println("   6. Afiseaza rate");
        System.out.println("   7. Meniu prietenii");
        System.out.println("   8. Meniu comunitati");
        System.out.println("   9. Meniu carduri");
        System.out.println("   10. Meniu RaceEvent");
        System.out.println("   0. Iesire...");
        System.out.println("===========================");
        System.out.print("Optiune: ");
    }

    public static void meniuPrietenii(){
        System.out.println("\n\n===== MENIU FRIENDSHIPS==============");
        System.out.println("   -> 1. Adauga prietenie");
        System.out.println("   -> 2. Afiseaza prietenii");
        System.out.println("   -> 3. Sterge prietenie");
        System.out.println("   -> 0. Iesire...");
        System.out.print("== SubOptiune: ");
    }
    public static void meniuComunitati(){
        System.out.println("\n\n===== MENU COMUNITATI==============");
        System.out.println("   -> 1. Afiseaza comunitati");
        System.out.println("   -> 2. Numarul de comunitati");
        System.out.println("   -> 3. Cea mai mare comunitate");
        System.out.println("   -> 0. Iesire...");
        System.out.print("== SubOptiune: ");
    }
    public static void meniuCarduri(){
        System.out.println("\n\n===== MENIU CARD=============");
        System.out.println("   -> 1. Creeaza card nou");
        System.out.println("   -> 2. Afiseaza carduri");
        System.out.println("   -> 3. Adauga o rata intr-un card");
        System.out.println("   -> 4. Afiseaza performanta unui card");
        System.out.println("   -> 0. Iesire...");
        System.out.print("== SubOptiune: ");
    }

    public static void meniuRaceEvent(){
        System.out.println("\n\n===== MENU RACE EVENT==============");
        System.out.println("   -> 1. Creeaza RaceEvent");
        System.out.println("   -> 2. Ruleaza RaceEvent");
        System.out.println("   -> 3. Afiseaza rezultate RaceEvent");
        System.out.println("   -> 4. Stergere RaceEvent");
        System.out.println("   -> 0. Iesire...");
        System.out.print("== SubOptiune: ");
    }

}
