package com.espimsystems.mespace.features.spaces.domain.model

enum class SpaceRole {

    OWNER,
    ADMIN,
    MEMBER,
}

fun SpaceRole.canManageSpace(): Boolean {
    return when (this) {
        SpaceRole.OWNER -> true
        SpaceRole.ADMIN -> true
        SpaceRole.MEMBER -> false
    }
}

fun SpaceRole.canManageMembers(): Boolean {
    return when (this) {
        SpaceRole.OWNER -> true
        SpaceRole.ADMIN -> true
        SpaceRole.MEMBER -> false
    }
}

fun SpaceRole.canDeleteSpace(): Boolean {
    return when (this) {
        SpaceRole.OWNER -> true
        SpaceRole.ADMIN -> false
        SpaceRole.MEMBER -> false
    }
}

fun SpaceRole.canManageTasks(): Boolean {
    return when (this) {
        SpaceRole.OWNER -> true
        SpaceRole.ADMIN -> true
        SpaceRole.MEMBER -> true
    }
}