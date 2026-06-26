package com.backend.common;

public class UserContext {

    private static final ThreadLocal<UserInfo> userInfoThreadLocal = new ThreadLocal<>();

    public static void setUserInfo(UserInfo userInfo) {
        userInfoThreadLocal.set(userInfo);
    }

    public static UserInfo getUserInfo() {
        return userInfoThreadLocal.get();
    }

    public static Integer getUserId() {
        UserInfo userInfo = userInfoThreadLocal.get();
        return userInfo != null ? userInfo.getUserId() : null;
    }

    public static String getUsername() {
        UserInfo userInfo = userInfoThreadLocal.get();
        return userInfo != null ? userInfo.getUsername() : null;
    }

    public static String getRole() {
        UserInfo userInfo = userInfoThreadLocal.get();
        return userInfo != null ? userInfo.getRole() : null;
    }

    public static void clear() {
        userInfoThreadLocal.remove();
    }

    public static class UserInfo {
        private Integer userId;
        private String username;
        private String role;

        public UserInfo() {}

        public UserInfo(Integer userId, String username, String role) {
            this.userId = userId;
            this.username = username;
            this.role = role;
        }

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}