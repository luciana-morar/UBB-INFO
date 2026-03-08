package org.example.lab6perfect.ui;

import org.example.lab6perfect.domain.Message;
import org.example.lab6perfect.domain.Persoana;
import org.example.lab6perfect.domain.User;
import org.example.lab6perfect.domain.duck.Duck;
import org.example.lab6perfect.domain.duck.FlyingDuck;
import org.example.lab6perfect.domain.duck.SwimmingDuck;
import org.example.lab6perfect.domain.event.RaceEvent;
import org.example.lab6perfect.repository.EventRepoDB;
import org.example.lab6perfect.repository.MessageRepoDB;
import org.example.lab6perfect.repository.UserRepoDB;
import org.example.lab6perfect.service.CardService;
import org.example.lab6perfect.service.FriendshipService;
import org.example.lab6perfect.service.NetworkService;
import org.example.lab6perfect.service.UserService;
import org.example.lab6perfect.validator.ValidationException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class ConsoleUI {
    private final UserService userService;
    private final FriendshipService friendshipService;
    private final NetworkService networkService;
    private final Scanner scanner = new Scanner(System.in);
    private final Menu menu = new Menu();
    private final CardService cardService;
    private final UserRepoDB  userRepoDB;
    private final EventRepoDB eventRepoDB;
    private final MessageRepoDB messageRepoDB;


    private List<RaceEvent> raceEvents = new ArrayList<>();
    private long nextRaceEventId = 1;

    public ConsoleUI(UserService userService, FriendshipService friendshipService, NetworkService networkService, CardService cardService, UserRepoDB userRepoDB, EventRepoDB eventRepoDB, MessageRepoDB messageRepoDB) {
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.networkService = networkService;
        this.cardService = cardService;
        this.userRepoDB = userRepoDB;
        this.eventRepoDB = eventRepoDB;
        this.messageRepoDB = messageRepoDB;

        this.raceEvents = eventRepoDB.getAllEvents();

    }

    public void start() throws Exception {
        boolean running = true;
        while (running) {
            Menu.printMainMenu();
            String option = scanner.nextLine();

            switch (option) {
                case "1" -> addPerson();
                case "2" -> deletePerson();
                case "3" -> userRepoDB.afiseazaToatePersoanele();
                case "4" -> addDuck();
                case "5" -> deleteDuck();
                case "6" -> userRepoDB.afiseazaToateRatele();
                case "7" -> handleFriendshipMenu();
                case "8" -> handleCommunityMenu();
                case "9" -> handleCardMenu();
                case "10" -> raceEventMenu();
                case "0" -> running = false;
                default -> System.out.println("Optiune invalida!");
            }
        }
    }

   private void handleFriendshipMenu() {
       boolean running = true;
       while (running) {
           Menu.meniuPrietenii();
           String subOption = scanner.nextLine();

           switch (subOption) {
               case "1" -> addFriendship();
               case "2" -> showFriendships();
               case "3" -> deleteFriendship();
               case "0" -> running = false;
               default -> System.out.println("Sub-opțiune invalidă!");
           }
       }
   }

   public void raceEventMenu() {
        boolean running=true;
        while (running) {
            Menu.meniuRaceEvent();
            String subOption = scanner.nextLine();
            switch (subOption) {
                case "1" -> creareRaceEvent();
                case "2" -> rulareRaceEvent();
                case "3" -> afisareResultRaceEvent();
                case "4" -> deleteRaceEvent();
                case "0" -> running = false;
                default -> System.out.println("Sub-opțiune invalidă!");
            }

        }
   }

    private void handleCommunityMenu() {
        boolean running = true;
        while (running) {
            Menu.meniuComunitati();
            String subOption = scanner.nextLine();

            List<User> allUsers = new ArrayList<>();
            allUsers.addAll(userService.listPersons());
            allUsers.addAll(userService.listDucks());


            switch (subOption) {
                case "1" -> showCommunities();
                case "2" -> printNumberOfCommunities();
                case "3" -> printBiggestCommunity();
                case "0" -> running = false;
                default -> System.out.println("Sub-opțiune invalidă!");
            }
        }
    }
    private void handleCardMenu() {
        boolean running = true;
        while (running) {
            Menu.meniuCarduri();
            String subOption = scanner.nextLine();

            switch (subOption) {
                case "1" -> createCard();
                case "2" -> showCards();
                case "3" -> addDuckToCard();
                case "4" -> showCardPerformance();
                case "0" -> running = false;
                default -> System.out.println("Sub-opțiune invalidă!");
            }
        }
    }

    //
    private void addPerson() {
        try {
            System.out.print("ID: ");
            Long id = Long.parseLong(scanner.nextLine());
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();
            System.out.print("Parola: ");
            String password = scanner.nextLine();
            System.out.print("Nume: ");
            String nume = scanner.nextLine();
            System.out.print("Prenume: ");
            String prenume = scanner.nextLine();
            System.out.print("Data nasterii (yyyy-mm-dd): ");
            LocalDate birth = LocalDate.parse(scanner.nextLine());
            System.out.print("Ocupatie: ");
            String ocupatie = scanner.nextLine();

            Persoana p = new Persoana(id, username, email, password, nume, prenume, ocupatie, birth, 5);
            userService.addUser(p);
            System.out.println("Persoana adaugata cu succes!");
        } catch (ValidationException e) {
            System.out.println("Eroare validare: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Format invalid.");
        }
    }

    private void addDuck() {
        try {
            System.out.print("ID: ");
            Long id = Long.parseLong(scanner.nextLine());
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();
            System.out.print("Parola: ");
            String password = scanner.nextLine();

            System.out.println("1. Swimming Duck");
            System.out.println("2. Flying Duck");
            System.out.print("Tip rata: ");
            int type = Integer.parseInt(scanner.nextLine());


            System.out.print("Viteza: ");
            double viteza = Double.parseDouble(scanner.nextLine());
            System.out.print("Rezistenta: ");
            double rezistenta = Double.parseDouble(scanner.nextLine());

            Duck duck;
            if (type == 1)
                duck = new SwimmingDuck(id, username, email, password, viteza, rezistenta);
            else
                duck = new FlyingDuck(id, username, email, password, viteza, rezistenta);

            try {
                userService.addUser(duck);
                System.out.println("Rata adaugata cu succes!");
            } catch (Exception e) {
                System.out.println("Eroare: " + e.getMessage());
            }
        } catch (ValidationException e) {
            System.out.println("Eroare validare: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Format numeric invalid.");
        } catch (IllegalArgumentException e) {
            System.out.println("Tip rata invalid! Trebuie să fie FLYING, SWIMMING sau FLYING_AND_SWIMMING.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void deletePerson() throws Exception {
        System.out.print("Username-ul persoanei de sters: ");
        String username = scanner.nextLine();

        Optional<User> userOpt = userService.findUserByUsername(username);

        if (userOpt.isPresent() && userOpt.get() instanceof Persoana) {
            userService.removeUser(userOpt.get());
            System.out.println("Persoana stearsa cu succes!");
        } else {
            System.out.println("Persoana inexistenta cu username-ul: " + username);
        }
    }

    private void deleteDuck() {
        System.out.print("Username-ul ratei de sters: ");
        String username = scanner.nextLine();

        Optional<User> userOpt = userService.findUserByUsername(username);

        if (userOpt.isPresent() && userOpt.get() instanceof Duck) {
            try {
                userService.removeUser(userOpt.get());
                System.out.println("Rata stearsa cu succes!");
            } catch (ValidationException e) {
                System.out.println("Eroare: " + e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Rata inexistenta!");
        }
    }

    private void addFriendship() {
        try {
            System.out.print("Username-ul primului utilizator: ");
            String u1 = scanner.nextLine();
            System.out.print("Username-ul celui de-al doilea utilizator: ");
            String u2 = scanner.nextLine();
            Optional<User> user1 = userService.findUserByUsername(u1);
            Optional<User> user2 = userService.findUserByUsername(u2);

            if (user1.isPresent() && user2.isPresent()) {
                friendshipService.addFriendship(user1.get(), user2.get());
                System.out.println("Prietenie adaugata cu succes!");
            } else {
                System.out.println("Cel puțin unul dintre utilizatori nu exista!");
            }
        } catch (ValidationException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    private void showFriendships() {
        friendshipService.listAll().forEach(f ->
                System.out.println(f.getUser1().getUsername() + " + " + f.getUser2().getUsername()));
    }

    private void deleteFriendship() {
        try {
            System.out.print("Username-ul primului utilizator: ");
            String u1 = scanner.nextLine();
            System.out.print("Username-ul celui de-al doilea utilizator: ");
            String u2 = scanner.nextLine();
            Optional<User> user1 = userService.findUserByUsername(u1);
            Optional<User> user2 = userService.findUserByUsername(u2);

            if (user1.isPresent() && user2.isPresent()) {
                friendshipService.removeFriendship(user1.get(), user2.get());
                System.out.println("Prietenie stearsa cu succes!");
            } else {
                System.out.println("Cel putin unul dintre utilizatori nu exista!");
            }
        } catch (ValidationException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    private void showCommunities() {
        List<User> allUsers = userService.listUsers();

        List<Set<User>> communities = networkService.getCommunities(allUsers);
        int index = 1;
        for (Set<User> community : communities) {
            System.out.print("Comunitatea " + index + ": ");
            for (User u : community) System.out.print(u.getUsername() + " ");
            System.out.println();
            index++;
        }
    }

    private void printNumberOfCommunities() {

        List<User> allUsers = userService.listUsers();
        int number = networkService.getNumberOfCommunities(allUsers);
        System.out.println("Numarul de comunitati: " + number);
    }

    private void printBiggestCommunity() {

        List<User> allUsers = userService.listUsers();
        Set<User> biggest = networkService.getBiggestCommunity(allUsers);
        System.out.print("Cea mai mare comunitate: ");
        for (User u : biggest) System.out.print(u.getUsername() + " ");
        System.out.println();
    }

    private void createCard() {
        System.out.print("Numele cardului: ");
        String name = scanner.nextLine();
        cardService.createCard(name);
    }

    private void addDuckToCard() {
        System.out.print("Numele cardului: ");
        String cardName = scanner.nextLine();

        System.out.print("Username rata: ");
        String username = scanner.nextLine();

        Optional<User> userOpt = userService.findUserByUsername(username);
        if (userOpt.isEmpty() || !(userOpt.get() instanceof Duck duck)) {
            System.out.println("Rata inexistenta cu username-ul: " + username);
            return;
        }

        cardService.addDuckToCard(cardName, duck);
    }

    private void showCardPerformance() {
        System.out.print("Numele cardului: ");
        String cardName = scanner.nextLine();
        cardService.printCardPerformance(cardName);
    }

    private void showCards() {
        cardService.listCards();
    }

    private void creareRaceEvent() {
        try {

            System.out.print("Numele evenimentului: ");
            String name = scanner.nextLine();

            System.out.print("Numar de participanti (M): ");
            int M = Integer.parseInt(scanner.nextLine());
            System.out.print("Numar de balize(>=M): ");
            int numBalize = Integer.parseInt(scanner.nextLine());

            if (numBalize < M) {
                System.out.println("Eroare: numarul de balize nu poate fi mai mare decat numarul de participanti!");
                return;
            }

            double[] distante = new double[numBalize];
            for (int i = 0; i < numBalize; i++) {
                System.out.print("Distanta balizei " + (i + 1) + ": ");
                distante[i] = Double.parseDouble(scanner.nextLine());
            }

            RaceEvent race = new RaceEvent(nextRaceEventId++, name, distante, M);

            // adaugă toți subscriberii care sunt inotatori
            List<SwimmingDuck> inotatoare = userService.listDucks().stream()
                    .filter(d -> d instanceof SwimmingDuck)
                    .map(d -> (SwimmingDuck) d)
                    .toList();

            for (SwimmingDuck d : inotatoare) {
                race.subscribe(d);
            }

            raceEvents.add(race);
            eventRepoDB.addEvent(race);
            System.out.println("RaceEvent creat: " + name + " cu " + inotatoare.size() + " participanti.");
        } catch (NumberFormatException e) {
            System.out.println("Format numeric invalid!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void rulareRaceEvent() {
        if (raceEvents.isEmpty()) {
            System.out.println("Nu exista RaceEvent-uri create.");
            return;
        }

        System.out.println("Selecteaza RaceEvent-ul de rulat:");
        for (int i = 0; i < raceEvents.size(); i++) {
            System.out.println((i + 1) + ". " + raceEvents.get(i).getNumeEveniment());
        }

        int opt;
        try {
            opt = Integer.parseInt(scanner.nextLine()) - 1;
            if (opt < 0 || opt >= raceEvents.size()) {
                System.out.println("Opțiune invalida!");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Format numeric invalid!");
            return;
        }

        RaceEvent race = raceEvents.get(opt);
        race.runRace();
        //salveazaMesajeCursa(race);
        eventRepoDB.saveRaceResults(race.getId(), race.getTimpCurent(), race.getParticipanti());

    }

    private void afisareResultRaceEvent() {
        if (raceEvents.isEmpty()) {
            System.out.println("Nu exista RaceEvent-uri create.");
            return;
        }

        System.out.println("Selecteaza RaceEvent-ul pentru afisare rezultate:");
        for (int i = 0; i < raceEvents.size(); i++) {
            System.out.println((i + 1) + ". " + raceEvents.get(i).getNumeEveniment());
        }

        int opt;
        try {
            opt = Integer.parseInt(scanner.nextLine()) - 1;
            if (opt < 0 || opt >= raceEvents.size()) {
                System.out.println("Opțiune invalida!");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Format numeric invalid!");
            return;
        }

        RaceEvent race = raceEvents.get(opt);

        if (race.getParticipanti() == null) {
            System.out.println("Cursa nu a fost rulata inca!");
            return;
        }

        System.out.println("Rezultatele cursei " + race.getNumeEveniment() + ":");
        Duck[] participanti = race.getParticipanti();
        for (int i = 0; i < participanti.length; i++) {
            Duck d = participanti[i];
            System.out.println((i + 1) + ". " + d.getUsername() + " (viteza: " + d.getViteza() + ", rezistența: " + d.getRezistenta() + ")");
        }
        System.out.println("Timp castigator: " + race.getTimpCurent());
    }

    private void deleteRaceEvent() {
        if (raceEvents.isEmpty()) {
            System.out.println("Nu exista RaceEvent-uri create.");
            return;
        }

        System.out.println("Selecteaza RaceEvent-ul de sters:");
        for (int i = 0; i < raceEvents.size(); i++) {
            System.out.println((i + 1) + ". " + raceEvents.get(i).getNumeEveniment() +
                    " (ID: " + raceEvents.get(i).getId() + ")");
        }

        int opt;
        try {
            opt = Integer.parseInt(scanner.nextLine()) - 1;
            if (opt < 0 || opt >= raceEvents.size()) {
                System.out.println("Opțiune invalida!");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Format numeric invalid!");
            return;
        }

        RaceEvent raceToDelete = raceEvents.get(opt);

        eventRepoDB.deleteEvent(raceToDelete.getId());
        raceEvents.remove(opt);
        System.out.println("Cursa '" + raceToDelete.getNumeEveniment() + "' a fost stearsa!");
    }

//    private void salveazaMesajeCursa(RaceEvent race) {
//        String mesaj = "Cursa " + race.getNumeEveniment() + " s-a terminat! Timp castigator: " + race.getTimpCurent();
//
//        for (User participant : race.getSubscribers()) {
//            messageRepoDB.addMessage(new Message(null, participant, mesaj));
//        }
//
//    }

}
