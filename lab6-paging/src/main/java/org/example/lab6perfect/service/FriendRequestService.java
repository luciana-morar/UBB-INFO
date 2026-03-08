package org.example.lab6perfect.service;

import org.example.lab6perfect.domain.FriendRequest;
import org.example.lab6perfect.domain.User;
import org.example.lab6perfect.obs.Observable;
import org.example.lab6perfect.repository.FriendRequestRepoDB;
import org.example.lab6perfect.validator.ValidationException;

import java.util.List;
import java.util.Optional;

public class FriendRequestService {
    private final FriendRequestRepoDB friendRequestRepo;
    private final FriendshipService friendshipService;

    public FriendRequestService(FriendRequestRepoDB friendRequestRepo, FriendshipService friendshipService) {
        this.friendRequestRepo = friendRequestRepo;
        this.friendshipService = friendshipService;
    }

    public FriendRequest sendFriendRequest(User sender, User receiver) throws ValidationException {

        if( areFriends(sender, receiver)){
            throw new ValidationException("Users are already friends");
        }
        Optional<FriendRequest> existingRequest = friendRequestRepo.findRequestBetweenUsers(sender.getId(), receiver.getId());
        if(existingRequest.isPresent()){
            FriendRequest request =existingRequest.get();
            if (request.getStatus() == FriendRequest.Status.PENDING) {
                throw new ValidationException("Exista deja o cerere de prietenie in asteptare");
            }
        }

        FriendRequest request = new FriendRequest(sender, receiver);
        Long requestId= friendRequestRepo.addFriendRequest(request);
        if(requestId != null){
            request.setId(requestId);
            Observable.getInstance().notifyObservers(request);
            return request;
        }
        throw new ValidationException("Nu s-a putut trimite cererea de prietenie");
    }

    public void acceptFriendRequest(Long requestId) throws ValidationException {

        FriendRequest request = getAllRequests().stream()
                .filter(r -> r.getId().equals(requestId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Cererea nu a fost gasita"));

        if (request.getStatus() != FriendRequest.Status.PENDING) {
            throw new ValidationException("Cererea nu este in asteptare");
        }

        request.setStatus(FriendRequest.Status.APPROVED);
        friendRequestRepo.updateFriendRequest(request);

        friendshipService.addFriendship(request.getSender(), request.getReceiver());
        Observable.getInstance().notifyObservers(request);
    }

    public void rejectFriendRequest(Long requestId) throws ValidationException {
        FriendRequest request = getAllRequests().stream()
                .filter(r -> r.getId().equals(requestId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Cererea nu a fost gasita"));

        if (request.getStatus() != FriendRequest.Status.PENDING) {
            throw new ValidationException("Cererea nu este in asteptare");
        }

        request.setStatus(FriendRequest.Status.REJECTED);
        friendRequestRepo.updateFriendRequest(request);

        Observable.getInstance().notifyObservers(request);
    }


    private boolean areFriends(User u1, User u2) {
        try {
            return friendshipService.listAll().stream()
                    .anyMatch(f ->
                            (f.getUser1().getId().equals(u1.getId()) && f.getUser2().getId().equals(u2.getId())) ||
                                    (f.getUser1().getId().equals(u2.getId()) && f.getUser2().getId().equals(u1.getId()))
                    );
        } catch (Exception e) {
            return false;
        }
    }



    public List<FriendRequest> getPendingRequestsForUser(User user) {
        return friendRequestRepo.getPendingRequestForUser(user.getId());
    }

    public List<FriendRequest> getSentRequestsByUser(User user) {
        return friendRequestRepo.getSentRequestsByUser(user.getId());
    }

    public List<FriendRequest> getAllRequests() {
        return friendRequestRepo.getAllRequests();
    }
}
