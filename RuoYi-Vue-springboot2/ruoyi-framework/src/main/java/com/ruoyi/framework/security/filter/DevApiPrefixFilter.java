package com.ruoyi.framework.security.filter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 兼容 Swagger / App 直连 8080 时带上的 /dev-api 前缀。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DevApiPrefixFilter extends OncePerRequestFilter
{
    public static final String PREFIX = "/dev-api";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException
    {
        String contextPath = request.getContextPath() == null ? "" : request.getContextPath();
        String uri = request.getRequestURI();
        String path = uri.substring(contextPath.length());
        if (path.equals(PREFIX) || path.startsWith(PREFIX + "/"))
        {
            filterChain.doFilter(new DevApiRequestWrapper(request, contextPath, path), response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static class DevApiRequestWrapper extends HttpServletRequestWrapper
    {
        private final String servletPath;
        private final String requestUri;

        private DevApiRequestWrapper(HttpServletRequest request, String contextPath, String path)
        {
            super(request);
            String stripped = path.substring(PREFIX.length());
            if (stripped.isEmpty())
            {
                stripped = "/";
            }
            this.servletPath = stripped;
            this.requestUri = contextPath + stripped;
        }

        @Override
        public String getServletPath()
        {
            return servletPath;
        }

        @Override
        public String getRequestURI()
        {
            return requestUri;
        }

        @Override
        public StringBuffer getRequestURL()
        {
            String url = super.getRequestURL().toString();
            int idx = url.indexOf(PREFIX);
            if (idx >= 0)
            {
                url = url.substring(0, idx) + url.substring(idx + PREFIX.length());
            }
            return new StringBuffer(url);
        }
    }
}
