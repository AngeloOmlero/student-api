package com.example.student_api.security

import com.example.student_api.model.Users
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails


class CustomUserDetails(private val user: Users) : UserDetails {

    val id: Long
        get() = user.id

    override fun getAuthorities(): Collection<GrantedAuthority> {
        // Ensure the authority is prefixed with "ROLE_" for standard Spring Security handling
        return listOf(SimpleGrantedAuthority("ROLE_" + user.role.name))
    }

    override fun getPassword(): String {
        return user.password
    }

    override fun getUsername(): String {
        return user.username
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
}