package ru.kuchanov.scpreaderapi.configuration.security

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler
import org.springframework.stereotype.Component
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.service.push.UserToPushTokensService
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class ScpReaderLogoutHandler @Autowired constructor(
        private val userToPushTokensService: UserToPushTokensService,
        private val log: Logger
) : SecurityContextLogoutHandler() {

    override fun logout(
            request: HttpServletRequest,
            response: HttpServletResponse,
            authentication: Authentication?
    ) {
        log.debug("logout!")
//        log.error("addLogoutHandler request.userPrincipal: ${request.userPrincipal}")
//        log.error("addLogoutHandler request.authType: ${request.authType}")
//        request.headerNames.toList().forEach {
//            log.error("HEADER: $it: ${request.getHeader(it)}")
//        }
//        log.error("addLogoutHandler authentication: $authentication")
//        log.error("addLogoutHandler authentication.principal: ${authentication?.principal as? User}")

        val user = authentication?.principal as? User
        if (user != null) {
            checkNotNull(user.id)
            val token = request.getParameter(ScpReaderConstants.Api.PARAM_PUSH_TOKEN)
            checkNotNull(token)
            val tokenToDelete = userToPushTokensService.findByUserIdAndPushTokenValue(user.id, token)
            if (tokenToDelete != null) {
                userToPushTokensService.deleteById(tokenToDelete.id!!)
            }
        }

        super.logout(request, response, authentication)
    }
}