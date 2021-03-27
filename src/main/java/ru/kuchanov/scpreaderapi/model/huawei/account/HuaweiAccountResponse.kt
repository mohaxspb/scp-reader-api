package ru.kuchanov.scpreaderapi.model.huawei.account

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class HuaweiAccountResponse(
        val openID: String,
        val displayName: String,
        val headPictureURL: String?,
        val mobileNumber: String?,
        val srvNationalCode: String?,
        val nationalCode: String?,
        val birthDate: String?,
        /**
         * -1: unknown (The date of birth is not set.)
         * 0: adult
         * 1: juvenile
         * 2: child
         */
        val ageGroupFlag: Int?,
        val email: String?,
)