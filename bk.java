public final class BlacklistUrlFilter implements Filter {
 
    private String[] denyUrls = {};
    private String[] ignoreUrls = {};
 
    /**
     * Initialize "deny" parameter from web.xml.
     *
     * @param filterConfig A filter configuration object used by a servlet container
     *                     to pass information to a filter during initialization.
     */
    public void init(final FilterConfig filterConfig) {
 
        final String denyParam = filterConfig.getInitParameter("denyUrls");
        if (StringUtils.isNotBlank(denyParam)) {
            this.denyUrls = denyParam.split(",");
        }
 
        final String ignoreParam = filterConfig.getInitParameter("ignoreUrls");
        if (StringUtils.isNotBlank(ignoreParam)) {
            this.ignoreUrls = ignoreParam.split(",");
        }
 
    }
 
    /**
     * Check for denied or ignored URLs being requested.
     *
     * @param request The request object.
     * @param response The response object.
     * @param chain Refers to the {@code FilterChain} object to pass control to the next {@code Filter}.
     * @throws IOException a IOException
     * @throws ServletException a ServletException
     */
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
 
        final HttpServletRequest req = (HttpServletRequest) request;
        final HttpServletResponse res = (HttpServletResponse) response;
 
        final String requestUri = req.getRequestURI();
        if (requestUri != null) {
            for (final String url: denyUrls) {
                if (requestUri.startsWith(url.trim())) {
                    res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
            for (final String url: ignoreUrls) {
                if (requestUri.startsWith(url.trim())) {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }
 
 
    /**
     * {@inheritDoc}
     */
    public void destroy() {
    }
 
}
