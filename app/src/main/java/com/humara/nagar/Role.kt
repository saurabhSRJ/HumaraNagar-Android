package com.humara.nagar

enum class Role(val roleId: Int) {
    HumaraNagarTeam(1),
    Secretary(2),
    Parshad(3),
    Sahayak(4),
    Naagrik(5);

    companion object {
        fun canRaiseComplaint(roleId: Int): Boolean {
            return when (roleId) {
                HumaraNagarTeam.roleId, Naagrik.roleId -> true
                else -> false
            }
        }

        fun isAdmin(roleId: Int): Boolean {
            return when (roleId) {
                HumaraNagarTeam.roleId, Secretary.roleId, Parshad.roleId, Sahayak.roleId -> true
                else -> false
            }
        }

        fun isLocalAdmin(roleId: Int): Boolean {
            return when (roleId) {
                Secretary.roleId, Parshad.roleId, Sahayak.roleId -> true
                else -> false
            }
        }

        fun isFromHumaraNagarTeam(roleId: Int): Boolean {
            return when (roleId) {
                HumaraNagarTeam.roleId -> true
                else -> false
            }
        }

        fun isResident(roleId: Int): Boolean {
            return when(roleId) {
                Naagrik.roleId -> true
                else -> false
            }
        }

        fun shouldShowResidentsFromAllWards(roleId: Int): Boolean {
            return when(roleId) {
                HumaraNagarTeam.roleId, Secretary.roleId -> true
                else -> false
            }
        }

        fun shouldShowComplaintsFromAllWards(roleId: Int): Boolean {
            return when(roleId) {
                HumaraNagarTeam.roleId, Secretary.roleId -> true
                else -> false
            }
        }
    }
}