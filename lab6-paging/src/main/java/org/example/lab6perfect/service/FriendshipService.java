package org.example.lab6perfect.service;

import org.example.lab6perfect.domain.Friendship;
import org.example.lab6perfect.domain.User;
import org.example.lab6perfect.repository.FriendshipRepoDB;
import org.example.lab6perfect.validator.FriendshipValidator;
import org.example.lab6perfect.validator.ValidationException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FriendshipService {
    private final FriendshipRepoDB friendshipRepo;
    private final FriendshipValidator validator;

    public FriendshipService(FriendshipRepoDB friendshipRepo, FriendshipValidator validator) {
        this.friendshipRepo = friendshipRepo;
        this.validator = validator;
    }

    public void addFriendship(User u1, User u2) throws ValidationException {
        boolean exists = friendshipRepo.listAll().stream()
                .anyMatch(f ->
                        (f.getUser1().getUsername().equals(u1.getUsername()) &&
                                f.getUser2().getUsername().equals(u2.getUsername())) ||
                                (f.getUser1().getUsername().equals(u2.getUsername()) &&
                                        f.getUser2().getUsername().equals(u1.getUsername()))
                );

        if (exists)
            throw new ValidationException("Utilizatorii sunt deja prieteni.");

        // adăugăm prietenia
        friendshipRepo.addFriendship(new Friendship(u1, u2));

    }

    public void removeFriendship(User u1,User u2) throws ValidationException {
        boolean exists = friendshipRepo.listAll().stream()
                .anyMatch(f ->
                        (f.getUser1().getUsername().equals(u1.getUsername()) &&
                                f.getUser2().getUsername().equals(u2.getUsername())) ||
                                (f.getUser1().getUsername().equals(u2.getUsername()) &&
                                        f.getUser2().getUsername().equals(u1.getUsername()))
                );

        if (!exists)
            throw new ValidationException("Prietenia nu există.");

        friendshipRepo.removeFriendship(u1, u2);
    }
    public void removeAllFriendshipsOf(User user) {
        List<Friendship> toRemove = new ArrayList<>();

        for (Friendship f : friendshipRepo.listAll()) {
            if (f.getUser1().equals(user) || f.getUser2().equals(user)) {
                toRemove.add(f);
            }
        }

        for (Friendship f : toRemove) {
            friendshipRepo.removeFriendship(f.getUser1(), f.getUser2());
        }
    }


    public void addFriendshipByUsernames(String username1, String username2) throws ValidationException {
        User u1 = friendshipRepo.getUserRepo().findByUsername(username1)
                .orElseThrow(() -> new ValidationException("Utilizatorul " + username1 + " nu există."));
        User u2 = friendshipRepo.getUserRepo().findByUsername(username2)
                .orElseThrow(() -> new ValidationException("Utilizatorul " + username2 + " nu există."));

        addFriendship(u1, u2);
    }

    public void removeFriendshipByUsernames(String username1, String username2) throws ValidationException {
        User u1 = friendshipRepo.getUserRepo().findByUsername(username1)
                .orElseThrow(() -> new ValidationException("Utilizatorul " + username1 + " nu există."));
        User u2 = friendshipRepo.getUserRepo().findByUsername(username2)
                .orElseThrow(() -> new ValidationException("Utilizatorul " + username2 + " nu există."));

        removeFriendship(u1, u2);
    }



    public List<Friendship> listAll() {
        return friendshipRepo.listAll();
    }
    public int getNumberOfCommunities(List<User> users) {
        Set<User> visited = new HashSet<>();
        int count = 0;
        for (User u : users) {
            if (!visited.contains(u)) {
                dfs(u, visited);
                count++;
            }
        }
        return count;
    }

    private void dfs(User u, Set<User> visited) {
        visited.add(u);
        for (User friend : friendshipRepo.getFriendsOf(u)) {
            if (!visited.contains(friend)) {
                dfs(friend, visited);
            }
        }
    }

    public boolean areFriends(User u1, User u2) {
        return friendshipRepo.listAll().stream()
                .anyMatch(f ->
                        (f.getUser1().getId().equals(u1.getId()) && f.getUser2().getId().equals(u2.getId())) ||
                                (f.getUser1().getId().equals(u2.getId()) && f.getUser2().getId().equals(u1.getId()))
                );
    }


    public List<User> getFriendsOfUser(User user) {
        return friendshipRepo.getFriendsOf(user);
    }

}