package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq")
    @SequenceGenerator(name = "users_seq", sequenceName = "users_id_seq", allocationSize = 50)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false,  length = 255)
    private String password;

    @Column(name = "role", nullable = false, length = 50)
    @Enumerated(EnumType.STRING) // для конвертации в строку
    private Role role; // user or admin

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "balance", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "held_balance", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal heldBalance = BigDecimal.ZERO;

    // для UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name())); // чтобы получалось типа ROLE_USER
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // аккаунты бессрочны
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive; // не заблокирован ли пользователь
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // пароли бессрочны
    }

    @Override
    public boolean isEnabled() {
        return isActive; // активен ли аккаунт
    }
}
