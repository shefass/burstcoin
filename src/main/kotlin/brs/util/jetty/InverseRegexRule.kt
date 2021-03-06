package brs.util.jetty

import org.eclipse.jetty.rewrite.handler.Rule
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

abstract class InverseRegexRule protected constructor(private val regex: Regex) : Rule() {
    override fun matchAndApply(target: String, request: HttpServletRequest, response: HttpServletResponse): String? {
        return if (!regex.matches(target)) apply(target, request, response) else null
    }

    protected abstract fun apply(
        target: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): String?
}
