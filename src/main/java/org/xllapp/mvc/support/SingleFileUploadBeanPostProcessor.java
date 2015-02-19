package org.xllapp.mvc.support;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.util.UrlPathHelper;
import org.xllapp.mvc.controller.SingleFileUploadController;

/**
 * 用于解决SingleFileUploadController与spring的MultipartResolver共存的问题.
 *
 * @author dylan.chen Nov 11, 2014
 * 
 */
public class SingleFileUploadBeanPostProcessor implements BeanPostProcessor {

	private final static Logger logger = LoggerFactory.getLogger(SingleFileUploadBeanPostProcessor.class);

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		
		if (DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME.equals(beanName) && bean instanceof MultipartResolver) {
			logger.debug("Bean [{},{}] proxied to com.ffcs.icity.mvc.support.SingleFileUploadMultipartResolver", beanName, bean.getClass());
			return new SingleFileUploadMultipartResolver((MultipartResolver) bean);
		}
		
		return bean;
	}

	private static class SingleFileUploadMultipartResolver implements MultipartResolver {

		private UrlPathHelper urlPathHelper = new UrlPathHelper();

		private MultipartResolver multipartResolver;

		private SingleFileUploadMultipartResolver(MultipartResolver multipartResolver) {
			this.multipartResolver = multipartResolver;
		}

		@Override
		public boolean isMultipart(HttpServletRequest request) {
			String requestUri = this.urlPathHelper.getPathWithinServletMapping(request);
			return StringUtils.startsWithIgnoreCase(requestUri, SingleFileUploadController.URI_PREFIX) ? false : this.multipartResolver.isMultipart(request);
		}

		@Override
		public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {
			return this.multipartResolver.resolveMultipart(request);
		}

		@Override
		public void cleanupMultipart(MultipartHttpServletRequest request) {
			this.multipartResolver.cleanupMultipart(request);
		}

	}

}
