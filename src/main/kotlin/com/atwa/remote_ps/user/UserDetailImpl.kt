package com.atwa.remote_ps.user

import com.atwa.remote_ps.user.model.User
import com.atwa.remote_ps.shop.Shop
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserDetailImpl(val user: User) : UserDetails {

    override fun getAuthorities(): MutableCollection<out SimpleGrantedAuthority> {
        return user.roles.map { SimpleGrantedAuthority(it.name) }.toMutableList()
    }

    fun getId(): Long? {
        return user.id
    }

    fun getShop(): Shop? {
        return user.shop
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
        return user.enabled
    }

}