package nextstep.member.domain;


import nextstep.auth.userdetails.UserDetails;

import java.util.List;

public class LoginMember implements UserDetails {
    private String email;
    private String password;
    private List<String> authorities;

    public static LoginMember of(Member member) {
        return new LoginMember(member.getEmail(), member.getPassword(), member.getRoles());
    }

    public static LoginMember of(String email, List<String> authorities) {
        return new LoginMember(email, null, authorities);
    }

    public static LoginMember guest() {
        return new LoginMember();
    }

    public LoginMember() {
    }

    public LoginMember(String email, String password, List<String> authorities) {
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getPrincipal() {
        return this.email;
    }

    @Override
    public List<String> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean invalidCredentials(String credentials) {
        return !checkPassword(credentials);
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

}
