package ee.hm.dop.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@Setter
public class DopUserDetails implements UserDetails {

    private DopPrincipal dopPrincipal;

    public DopUserDetails(DopPrincipal dopPrincipal) {
        this.dopPrincipal = dopPrincipal;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = dopPrincipal.getAuthenticatedUser().getUser().getRole().getRole();
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return dopPrincipal.getAuthenticatedUser().getUser().getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
