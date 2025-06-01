package org.ganjp.blog.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.ganjp.blog.common.filter.RequestIdFilter;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Utility class for handling request-related operations.
 * Provides methods to retrieve the current request ID.
 */
public class RequestUtils {

    private RequestUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Retrieves the current request ID from the request context.
     * If no request ID is found or there is no active request context,
     * returns a default value of "no-request-id".
     *
     * @return the current request ID or "no-request-id" if unavailable
     */
    public static String getCurrentRequestId() {
        try {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if (attributes instanceof ServletRequestAttributes) {
                HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
                Object requestId = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
                if (requestId != null) {
                    return requestId.toString();
                }
            }
        } catch (Exception e) {
            // Silently handle any exceptions that might occur
        }
        return "no-request-id";
    }
}
