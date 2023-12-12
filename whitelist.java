public final class WhitelistUrlFilter implements Filter {
 
    private String[] allowUrls = {};
    private String[] forwardExcludes = {};
    private String forwardTo = null;
 
    /**
     * Initialize "allowUrls" parameter from web.xml.
     *
     * @param filterConfig A filter configuration object used by a servlet container
     *                     to pass information to a filter during initialization.
     */
    public void init(final FilterConfig filterConfig) {
 
        final String allowParam = filterConfig.getInitParameter("allowUrls");
        if (StringUtils.isNotBlank(allowParam)) {
            this.allowUrls = allowParam.split(",");
        }
 
        final String forwardExcludesParam = filterConfig.getInitParameter("forwardExcludes");
        if (StringUtils.isNotBlank(forwardExcludesParam)) {
            this.forwardExcludes = forwardExcludesParam.split(",");
        }
 
        final String forwardToParam = filterConfig.getInitParameter("forwardTo");
        if (StringUtils.isNotBlank(forwardToParam)) {
            this.forwardTo = forwardToParam;
        }
 
    }
 
    /**
     * Check for allowed URLs being requested.
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
            boolean allowed = false;
            final String requestUrlExcludingContext = requestUri.substring(req.getContextPath().length());
            for (final String url: allowUrls) {
                if (requestUrlExcludingContext.equals("/")) {
                    if (url.trim().equals("/") || (url.trim().equals("/index.jsp")) || (url.trim().equals("/index.html"))) {
                        allowed = true;
                    }
                } else if (requestUrlExcludingContext.startsWith(url.trim())) {
                    allowed = true;
                }
            }
            if (!allowed) {
                if (forwardTo != null) {
                    for (final String url: allowUrls) {
                        if (forwardExcludes != null && Arrays.stream(forwardExcludes).anyMatch(url::equals)) {
                            break;
                        }
                        final int occurrence = requestUrlExcludingContext.indexOf(url);
                        if (occurrence > -1) {
                            final String queryString = (req.getQueryString() == null) ? "" : "?" + req.getQueryString();
                            final String resourceUrl = requestUrlExcludingContext.substring(occurrence) + queryString;
                            req.getRequestDispatcher(resourceUrl).forward(request, response);
                            return;
                        }
                    }
                    req.getRequestDispatcher(forwardTo).forward(request, response);
                } else {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
                return;
            }
        }
        chain.doFilter(request, response);
    }
 
    private boolean isExcludedForwardPath(String url) {
        return Arrays.stream(forwardExcludes).anyMatch(url::equals);
    }
 
 
    /**
     * {@inheritDoc}
     */
    public void destroy() {
    }
 
}
