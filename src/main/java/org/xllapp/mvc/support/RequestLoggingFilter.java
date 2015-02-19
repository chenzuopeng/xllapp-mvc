package org.xllapp.mvc.support;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.xllapp.config.ApplicationConfigHolder;
import org.xllapp.config.BaseApplicationConfig;
import org.xllapp.mvc.utils.JSONHelper;
import org.xllapp.mvc.utils.RequestUtils;

/**
 * 用于将请求信息输出到日志中.
 *
 * @author dylan.chen Aug 20, 2014
 * 
 */
public class RequestLoggingFilter implements Filter {

	private final static Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

	private RequestDumper requestDumper = new RequestDumper();

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		putMDC((HttpServletRequest)request);
		try {
			if (isDumpRequest()) {

				long startTime = System.currentTimeMillis();

				ResettableStreamRequestWrapper requestWrapper = new ResettableStreamRequestWrapper((HttpServletRequest) request);
				this.requestDumper.dumpRequest(requestWrapper);
				requestWrapper.resetInputStream();
				chain.doFilter(requestWrapper, response);

				long elapsedTime = System.currentTimeMillis() - startTime;
				logger.info("request processing time: {} ms", elapsedTime);

			} else {
				chain.doFilter(request, response);
			}
		} finally {
			MDC.clear();
		}

	}

	private void putMDC(HttpServletRequest request) {
		
		MDC.put("IP", RequestUtils.getIp(request));
		
		String requestId = RequestUtils.generateRequestId(request);
		MDC.put("REQUEST_ID", requestId);
		RequestContextHolder.setRequestId(requestId);
		
		MDC.put("SESSION_ID", request.getSession(true).getId());
		
	}

	private boolean isDumpRequest() {
		return getApplicationConfig().isDumpRequest();
	}

	private BaseApplicationConfig getApplicationConfig() {
		return ApplicationConfigHolder.getApplicationConfig();
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	private static class RequestDumper {

		private final static Logger logger = LoggerFactory.getLogger(RequestDumper.class);

		public void dumpRequest(HttpServletRequest request) {
			try {
				if (logger.isInfoEnabled()) {
					logger.info("--------------------request dump  begin-------------------------");
					logger.info("request uri: {}", request.getRequestURI());
					logger.info("request type: {}", request.getClass().getName());
					logger.info("request method: {}", request.getMethod());
					logger.info("request characterEncoding: {}", request.getCharacterEncoding());
					dumpRequestHeaders(request);
					dumpCookis(request);
					dumpSession(request);
					dumpRequestParams(request);
					dumpRequestBody(request);
					logger.info("--------------------request dump  end---------------------------");
				}
			} catch (Throwable e) {
				logger.info("failure to dump request", e);
			}
		}

		@SuppressWarnings("unchecked")
		private void dumpRequestHeaders(HttpServletRequest request) {
			for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements();) {
				String headerName = headerNames.nextElement();
				String value = request.getHeader(headerName);
				logger.info("request header[{}]:{}", headerName, value);
			}
		}
		
		public void dumpCookis(HttpServletRequest request) {
			Cookie[] cookies = request.getCookies();
			if (ArrayUtils.isNotEmpty(cookies)) {
				for (Cookie cookie : cookies) {
					logger.debug("cookies: {}", JSONHelper.toJSONStringQuietly(cookie));
				}
			}
		}

		@SuppressWarnings("unchecked")
		public void dumpSession(HttpServletRequest request) {
			HttpSession session = request.getSession(false);
			if (null != session) {
				for (Enumeration<String> attributeNames = session.getAttributeNames(); attributeNames.hasMoreElements();) {
					String attributeName = attributeNames.nextElement();
					Object value = session.getAttribute(attributeName);
					logger.info("session[{}]:{}", attributeName, JSONHelper.toJSONStringQuietly(value));
				}
			}
		}

		@SuppressWarnings("unchecked")
		private void dumpRequestParams(HttpServletRequest request) {
			for (Enumeration<String> paramNames = request.getParameterNames(); paramNames.hasMoreElements();) {
				String paramName = paramNames.nextElement();
				String[] values = request.getParameterValues(paramName);
				logger.info("request param[{}]:{}", paramName, JSONHelper.toJSONStringQuietly(values));
			}
		}

		private void dumpRequestBody(HttpServletRequest request) {
			if (isPayloadInBody(request)) {
				String body = RequestUtils.resolveBody(request);
				logger.info("request body:{}", body);
			} else {
				logger.info("request body:[unsupported payload:[{},{}]]", request.getMethod(), request.getContentType());
			}
		}

		private boolean isPayloadInBody(HttpServletRequest request) {
			String method = request.getMethod();
			if (!("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method))) {
				return false;
			}
			String contentType = request.getContentType();
			
			return StringUtils.containsIgnoreCase(contentType, "application/json") 
					|| StringUtils.containsIgnoreCase(contentType, "text/plain") 
					|| StringUtils.containsIgnoreCase(contentType, "text/xml")
					|| StringUtils.containsIgnoreCase(contentType, "text/html");
		}

	}

	private static class ResettableStreamRequestWrapper extends HttpServletRequestWrapper {

		private static final String DEFAULT_CHARACTER_ENCODING = "UTF-8";

		private byte[] bodyData;
		private HttpServletRequest request;
		private ResettableServletInputStream resettableServletInputStream;

		public ResettableStreamRequestWrapper(HttpServletRequest request) {
			super(request);
			this.request = request;
			this.resettableServletInputStream = new ResettableServletInputStream();
		}

		public void resetInputStream() {
			if (this.bodyData != null) {
				this.resettableServletInputStream.is = new ByteArrayInputStream(this.bodyData);
			}
		}

		@Override
		public ServletInputStream getInputStream() throws IOException {
			if (this.bodyData == null) {
				resolveBodyData();
			}
			return this.resettableServletInputStream;
		}

		@Override
		public BufferedReader getReader() throws IOException {
			if (this.bodyData == null) {
				resolveBodyData();
			}
			return new BufferedReader(new InputStreamReader(this.resettableServletInputStream));
		}

		private void resolveBodyData() throws IOException {
			this.bodyData = IOUtils.toByteArray(this.request.getReader(), getCharacterEncoding());
			this.resettableServletInputStream.is = new ByteArrayInputStream(this.bodyData);
		}

		public String getCharacterEncoding() {
			return super.getCharacterEncoding() != null ? super.getCharacterEncoding() : DEFAULT_CHARACTER_ENCODING;
		}

		private class ResettableServletInputStream extends ServletInputStream {

			private InputStream is;

			@Override
			public int read() throws IOException {
				return this.is.read();
			}
		}
	}

}