package org.example.lab6perfect.service;

import org.example.lab6perfect.domain.Persoana;
import org.example.lab6perfect.domain.User;
import org.example.lab6perfect.domain.duck.Duck;
import org.example.lab6perfect.domain.duck.FlyingDuck;
import org.example.lab6perfect.domain.duck.SwimmingDuck;
import org.example.lab6perfect.repository.UserRepoDB;
import org.example.lab6perfect.validator.UserValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


public class UserService {
    private final UserRepoDB userRepo;
    private final UserValidator validator;
    private final FriendshipService friendshipService;
    private final NetworkService networkService;
    private final FriendRequestService friendRequestService;

    public UserService(UserRepoDB userRepo, FriendshipService friendshipService, UserValidator validator, FriendRequestService friendRequestService) {
        this.userRepo = userRepo;
        this.friendshipService = friendshipService;
        this.validator = validator;
        this.networkService = new NetworkService(friendshipService);
        this.friendRequestService = friendRequestService;
    }
    public FriendRequestService getFriendRequestService() {
        return friendRequestService;
    }


    public int getNumberOfCommunities() {
        return networkService.getNumberOfCommunities(listUsers());
    }

    public Set<User> getBiggestCommunity() {
        return networkService.getBiggestCommunity(listUsers());
    }
    public void addUser(User user) throws Exception{
        validator.validate(user);
        userRepo.addUser(user);
    }
    public void removeUser(User user) throws Exception{
        validator.validate(user);
        friendshipService.removeAllFriendshipsOf(user);
        userRepo.removeUser(user);
    }

    public UserRepoDB getUserRepo() {
        return userRepo;
    }
    public List<User> listUsers(){
        return userRepo.listAllUsers();
    }
    public Optional<User> findUserByUsername(String username){
        return  userRepo.findByUsername(username);
    }
    public Optional<User> findUserByID(Long id){
        return  userRepo.findById(id);
    }

    public List<Persoana> listPersons() {
        return userRepo.listAllUsers().stream()
                .filter(u -> u instanceof Persoana)
                .map(u -> (Persoana) u)
                .toList();
    }


    public List<Duck> listDucks() {
        return userRepo.listAllUsers().stream()
                .filter(user -> user instanceof Duck)
                .map(user -> (Duck) user)
                .collect(Collectors.toList());
    }


    public List<Duck> getDucksByType(String type) {
        return listDucks().stream()
                .filter(duck -> {
                    if ("FLYING".equals(type)) {
                        return duck instanceof FlyingDuck;
                    }
                    if ("SWIMMING".equals(type)) {
                        return duck instanceof SwimmingDuck;
                    }
                    return false;
                })
                .toList();
    }

    public FriendshipService getFriendshipService() {
        return friendshipService;
    }


    public List<String> getAllDuckTypes() {
        return List.of("FLYING", "SWIMMING"); // "FLYING" și "SWIMMING" în loc de "FLYING_DUCK"
    }
    public List<Duck> getDucksPage(int page, int pageSize) {
        List<Duck> allDucks = listDucks();
        int start = page * pageSize;
        int end = Math.min(start + pageSize, allDucks.size());

        if (start < allDucks.size()) {
            return allDucks.subList(start, end);
        }
        return new ArrayList<>();
    }

    public List<Duck> getDucksByTypePage(String type, int page, int pageSize) {
        List<Duck> filtered = getDucksByType(type);
        int start = page * pageSize;
        int end = Math.min(start + pageSize, filtered.size());

        if (start < filtered.size()) {
            return filtered.subList(start, end);
        }
        return new ArrayList<>();
    }

    public int getTotalDucksCount() {
        return listDucks().size();
    }
    public Optional<User> findUserById(Long id) {
        return userRepo.findById(id);
    }
    public int getTotalDucksByTypeCount(String type) {
        return getDucksByType(type).size();
    }

}
