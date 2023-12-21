package util

import java.security.Permission

class NoExitSecurityManager : SecurityManager() {
    // Adapted from https://www.baeldung.com/junit-system-exit
    override fun checkPermission(perm: Permission?) {}

    override fun checkExit(status: Int) {
        super.checkExit(status)
        throw SystemExitException(status)
    }

    companion object {
        class SystemExitException(val status: Int)
            : RuntimeException("There was a System.exit() call with status $status")
    }
}