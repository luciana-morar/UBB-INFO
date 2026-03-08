package org.example.lab6perfect.service;

import org.example.lab6perfect.domain.User;
import org.example.lab6perfect.repository.UserRepoDBPaged;

import java.util.List;

public class UserPagingService {
    private final UserRepoDBPaged userRepoPaged;
    private int currentPage = 0;
    private final int pageSize = 10;
    public UserPagingService(UserRepoDBPaged userRepoPaged) {
        this.userRepoPaged = userRepoPaged;
    }

    public List<User> getCurrentPage() {
        return userRepoPaged.getUsersPage(currentPage, pageSize);
    }

    public List<User> getNextPage() {
        if (hasNextPage()) {
            currentPage++;
        }
        return getCurrentPage();
    }

    public List<User> getPreviousPage() {
        if (hasPreviousPage()) {
            currentPage--;
        }
        return getCurrentPage();
    }

    public boolean hasNextPage() {
        int totalPages = userRepoPaged.getTotalPages(pageSize);
        return currentPage < totalPages - 1;
    }

    public boolean hasPreviousPage() {
        return currentPage > 0;
    }

    public int getCurrentPageNumber() {
        return currentPage + 1; }

    public int getTotalPages() {
        return userRepoPaged.getTotalPages(pageSize);
    }

    public String getPageInfo() {
        int totalUsers = userRepoPaged.getTotalUsers();
        return String.format("Pagina %d/%d ",
                getCurrentPageNumber(), getTotalPages());
    }

    public void resetToFirstPage() {
        currentPage = 0;
    }

}