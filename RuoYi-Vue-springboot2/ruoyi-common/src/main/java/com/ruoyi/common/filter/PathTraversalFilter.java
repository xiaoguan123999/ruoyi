package com.ruoyi.common.filter;

import java.io.IOException;
import java.net.URLDecoder;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.ruoyi.common.utils.StringUtils;

/**
 * Block path traversal, including double-encoded dot-dot.
 */
public class PathTraversalFilter implements Filter
{
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        if (unsafe(req.getRequestURI()) || unsafe(req.getQueryString()))
        {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean unsafe(String raw)
    {
        if (StringUtils.isEmpty(raw))
        {
            return false;
        }
        String value = raw;
        for (int i = 0; i < 5; i++)
        {
            try
            {
                String decoded = URLDecoder.decode(value, "UTF-8");
                if (decoded.equals(value))
                {
                    break;
                }
                value = decoded;
            }
            catch (Exception e)
            {
                break;
            }
        }
        String normalized = value.replace('\\', '/').toLowerCase();
        return normalized.contains("..") || normalized.contains("%2e%2e");
    }
}
