package org.example.lab6perfect.domain;

public class Friendship {
    //relație bidirecțională între doi User (om–om, om–rață, rață–rață)
    private User user1;
    private User user2;

    public Friendship(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
        user1.addFriend(user2); //relatia bidirectionala
    }

    public User getUser1() { return user1; }
    public User getUser2() { return user2; }

    public String toString(){
        return user1.getUsername()+ "+"+user2.getUsername();
    }

}
