package org.example.lab6perfect.obs;

import org.example.lab6perfect.domain.FriendRequest;
import org.example.lab6perfect.domain.Message;

public interface Observer {
    void onNewMessage(Message message);
    void onNewFriendRequest(FriendRequest friendRequest);
}
