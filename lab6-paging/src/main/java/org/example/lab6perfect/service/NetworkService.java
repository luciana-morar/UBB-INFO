package org.example.lab6perfect.service;

import org.example.lab6perfect.domain.Friendship;
import org.example.lab6perfect.domain.User;
import org.example.lab6perfect.service.FriendshipService;

import java.util.*;

/**
 * NetworkService se ocupă de partea de grafic social:
 * - comunități (componente conexe)
 * - cea mai mare comunitate
 */
public class NetworkService {
    private final FriendshipService friendshipService;

    public NetworkService(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    public List<Set<User>> getCommunities(List<User> allUsers) {
        Map<Long, User> idToUserMap = new HashMap<>();
        for (User user : allUsers) {
            idToUserMap.put(user.getId(), user);
        }

        Map<User, Set<User>> graph = new HashMap<>();
        for (User u : allUsers) {
            graph.put(u, new HashSet<>());
        }

        for (Friendship f : friendshipService.listAll()) {
            Long u1Id = f.getUser1().getId();
            Long u2Id = f.getUser2().getId();

            User user1FromList = idToUserMap.get(u1Id);
            User user2FromList = idToUserMap.get(u2Id);

            if (user1FromList != null && user2FromList != null) {
                graph.get(user1FromList).add(user2FromList);
                graph.get(user2FromList).add(user1FromList);
            }
        }

        //componentele conexe folosind DFS
        Set<User> visited = new HashSet<>();
        List<Set<User>> communities = new ArrayList<>();
        for (User u : allUsers) {
            if (!visited.contains(u)) {
                Set<User> component = new HashSet<>();
                dfs(u, graph, visited, component);
                communities.add(component);
            }
        }

        return communities;
    }

    private void dfs(User u, Map<User, Set<User>> graph, Set<User> visited, Set<User> component) {
        visited.add(u);
        component.add(u);
        for (User neighbor : graph.get(u)) {
            if (!visited.contains(neighbor)) {
                dfs(neighbor, graph, visited, component);
            }
        }
    }

    /**
     * Returnează comunitatea cea mai mare.
     */
    public Set<User> getBiggestCommunity(List<User> allUsers) {
        List<Set<User>> communities = getCommunities(allUsers);
        Set<User> biggest = new HashSet<>();
        for (Set<User> c : communities) {
            if (c.size() > biggest.size()) {
                biggest = c;
            }
        }
        return biggest;
    }

    /**
     * Returnează numărul de comunități
     */
    public int getNumberOfCommunities(List<User> allUsers) {
        return getCommunities(allUsers).size();
    }
}