package org.xllapp.mvc.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.xllapp.mvc.utils.RequestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.thoughtworks.xstream.XStream;

/**
 * 此类用于上传单个文件.支持通过上传策略进行上传限制，如支持的上传文件类型,上传文件大小限制等.
 *
 * @author dylan.chen Oct 17, 2014
 * 
 */
@RequestMapping(SingleFileUploadController.URI_PREFIX)
@Controller
public class SingleFileUploadController implements ServletContextAware, ApplicationContextAware, InitializingBean {

	private final static Logger logger = LoggerFactory.getLogger(SingleFileUploadController.class);
	
	private static ObjectMapper objectMapper;

	public final static String URI_PREFIX = "/upload/file/";

	private static final String LOCAL_FILE_STORAGE_BASE_DIR = "uploads";
	
	static {
		objectMapper = new ObjectMapper();
		objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
	}

	private ServletContext servletContext;

	private ApplicationContext applicationContext;

	private Map<String, CommonsMultipartResolver> multipartResolvers = new HashMap<String, CommonsMultipartResolver>();

	private Map<String, FileUploadStrategy> fileUploadStrategys = new HashMap<String, FileUploadStrategy>();

	private String configFile;

	@RequestMapping("demo")
	public String demo(HttpServletRequest request) {
		return "fileupload/upload-demo";
	}

	/**
	 * 展示文件上传对话框.
	 */
	@RequestMapping(value = "box")
	public String showBox(HttpServletRequest request) {
		return "fileupload/file-upload-box";
	}

	@RequestMapping(value = "fileurl")
	public void getFileUrl(@RequestParam("fileUploadStrategyId") String fileUploadStrategyId,@RequestParam("relativePath") String relativePath, HttpServletRequest request,HttpServletResponse response) throws IOException, Exception {
		FileUploadStrategy fileUploadStrategy = this.fileUploadStrategys.get(fileUploadStrategyId);
		String fullFileUrl = getFullFileUrl(request, fileUploadStrategy, relativePath);
		out(response, "text/plain", fullFileUrl);
	}

	/**
	 * 返回服务端相关信息
	 */
	@RequestMapping("js/variables")
	public void getJsVariables(HttpServletRequest request, HttpServletResponse response) throws IOException {
		out(response, "text/javascript", "var baseServerUrl='" + RequestUtils.getFullUrl(request, "/") + "';");
	}

	/**
	 * 客户端需要在在上传页面提供一个JS函数：fileUploadCallback(result,desc,absPath,relaPath,
	 * writeBackElementId)用于接收服务端返回的信息.
	 */
	@RequestMapping("js/{fileUploadStrategyId}/{writeBackElementId}")
	public void uploadJS(@PathVariable("fileUploadStrategyId") String fileUploadStrategyId, @PathVariable("writeBackElementId") String writeBackElementId, HttpServletRequest request, HttpServletResponse response) throws Exception {
		FileUploadResp fileUploadResp = upload(fileUploadStrategyId, request, response);
		String js = String.format("<script type='text/javascript'>window.parent.fileUploadCallback(%s,'%s','%s','%s','%s');</script>", fileUploadResp.getResultCode(), fileUploadResp.getResultDesc(), fileUploadResp.getAbsPath(), fileUploadResp.getRelaPath(), writeBackElementId);
		out(response, "text/html", js);
	}

	@RequestMapping("json/{fileUploadStrategyId}")
	public void uploadJSON(@PathVariable("fileUploadStrategyId") String fileUploadStrategyId, HttpServletRequest request, HttpServletResponse response) throws Exception {
		FileUploadResp fileUploadResp = upload(fileUploadStrategyId, request, response);
		String json = toJSON(fileUploadResp);
		out(response, "text/plain", json);
	}

	public FileUploadResp upload(String fileUploadStrategyId, HttpServletRequest request, HttpServletResponse response) throws Exception {

		MultipartResolver multipartResolver = null;
		FileUploadStrategy fileUploadStrategy = null;
		MultipartHttpServletRequest multipartHttpServletRequest = null;

		try {

			//确保到此处的请求没有被spring的MultipartResolver处理过
			if (request instanceof MultipartHttpServletRequest) {
				throw new Exception("need to use com.ffcs.icity.mvc.support.SingleFileUploadBeanPostProcessor");
			}

			multipartResolver = this.multipartResolvers.get(fileUploadStrategyId);

			if (null == multipartResolver) {
				return new FileUploadResp(FileUploadResp.RESULT_CODE_FAILURE, "无效的上传策略,请正确设置fileUploadStrategy属性的值");
			}

			if (!multipartResolver.isMultipart(request)) {
				return new FileUploadResp(FileUploadResp.RESULT_CODE_FAILURE, "无效请求");
			}

			//解析请求并验证文件大小是否超限
			fileUploadStrategy = this.fileUploadStrategys.get(fileUploadStrategyId);

			multipartHttpServletRequest = multipartResolver.resolveMultipart(request);
			MultipartFile file = multipartHttpServletRequest.getFile("file");

			//验证文件类型是否合法
			if (checkFileType(fileUploadStrategy, file)) {
				return new FileUploadResp(FileUploadResp.RESULT_CODE_FAILURE, "非法的上传文件类型,允许的上传文件类型为:" + Arrays.toString(fileUploadStrategy.getExts()));
			}

			//保存文件
			File storageDir = prepareStorageDir(request, fileUploadStrategy);
			String savedFileName = getSavedFileName(file.getOriginalFilename());
			file.transferTo(new File(storageDir, savedFileName));

			String relativePathSavedFile = RequestUtils.filePathToUri(fileUploadStrategy.getStorageDir() + "/" + savedFileName);

			return new FileUploadResp(relativePathSavedFile, getFullFileUrl(request, fileUploadStrategy, relativePathSavedFile));
		} catch (MaxUploadSizeExceededException maxUploadSizeExceededException) {
			return new FileUploadResp(FileUploadResp.RESULT_CODE_FAILURE, "上传文件的大小超过限制,允许的最大上传文件大小为: " + FileUtils.byteCountToDisplaySize(fileUploadStrategy.getMaxSize()) + "[" + fileUploadStrategy.getMaxSize() + "字节]");
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new FileUploadResp(FileUploadResp.RESULT_CODE_FAILURE, "服务端异常:" + e.getLocalizedMessage());
		} finally {
			if (null != multipartResolver && null != multipartHttpServletRequest) {
				multipartResolver.cleanupMultipart(multipartHttpServletRequest);
			}
		}

	}
	
	private String toJSON(Object object) throws Exception {
		return objectMapper.writeValueAsString(object);
	}

	private void out(HttpServletResponse response, String contentType, String content) throws IOException {
		response.setContentType(contentType);
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		try {
			out = response.getWriter();
			out.write(content);
		} catch (Exception e) {
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * 验证上传文件类型
	 */
	private boolean checkFileType(FileUploadStrategy fileUploadStrategy, MultipartFile file) {
		String ext = FilenameUtils.getExtension(file.getOriginalFilename());
		if (null != ext) {
			ext = ext.toLowerCase();
		}
		return ArrayUtils.isNotEmpty(fileUploadStrategy.getExts()) && !ArrayUtils.contains(fileUploadStrategy.getExts(), ext);
	}

	private String getFullFileUrl(HttpServletRequest request, FileUploadStrategy fileUploadStrategy, String relativePath) {

		String baseFileUrl = StringUtils.defaultIfBlank(fileUploadStrategy.getBaseFileUrl(), "");

		String separator = "/";
		if (StringUtils.endsWith(baseFileUrl, "/") || StringUtils.startsWith(relativePath, "/")) {
			separator = "";
		}

		String fullFileUrl = baseFileUrl + separator + relativePath;

		return RequestUtils.getFullPath(request, fullFileUrl);

	}

	/**
	 * 当文件保存目录不存在时,创建目录
	 */
	private File prepareStorageDir(HttpServletRequest request, FileUploadStrategy fileUploadStrategy) throws Exception {
		File dir = null;
		String storageDir = fileUploadStrategy.getStorageDir();
		if (StringUtils.isBlank(storageDir)) {
			storageDir = "/";
		}
		if (StringUtils.isNotBlank(fileUploadStrategy.getBaseStoragePath())) {
			dir = new File(fileUploadStrategy.getBaseStoragePath(), storageDir);
		} else {
			dir = new File(request.getSession().getServletContext().getRealPath(LOCAL_FILE_STORAGE_BASE_DIR), storageDir);
		}
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new Exception("创建保存目录失败");
			}
		}
		return dir;
	}

	private String getSavedFileName(String origFileName) {
		return DateFormatUtils.format(new Date(), "yyyyMMddHHmmss") + "-" + RandomStringUtils.randomNumeric(5) + "." + FilenameUtils.getExtension(origFileName);
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Value("${uploadfile.config.file:fileupload.xml}")
	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void afterPropertiesSet() throws Exception {

		//载入上传策略定义文件
		Resource resource = this.applicationContext.getResource("classpath:" + this.configFile);

		if (!resource.exists()) {
			return;
		}

		//解析上传策略定义文件
		XStream xstream = new XStream();
		xstream.alias("fileUploadStrategy", FileUploadStrategy.class);
		xstream.alias("fileUploadStrategys", List.class);
		xstream.alias("ext", String.class);
		List<FileUploadStrategy> list = (List<FileUploadStrategy>) xstream.fromXML(resource.getFile());

		logger.debug("Loaded :{} from {}", list, resource.getFile().getAbsolutePath());

		for (FileUploadStrategy fileUploadStrategy : list) {
			if (StringUtils.isBlank(fileUploadStrategy.getId())) {
				continue;
			}
			if (!this.multipartResolvers.containsKey(fileUploadStrategy.getId())) {
				CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(this.servletContext);
				if (fileUploadStrategy.getMaxSize() > 0) {
					multipartResolver.setMaxUploadSize(fileUploadStrategy.getMaxSize());
				}
				this.multipartResolvers.put(fileUploadStrategy.getId(), multipartResolver);
			}
			if (!this.fileUploadStrategys.containsKey(fileUploadStrategy.getId())) {
				this.fileUploadStrategys.put(fileUploadStrategy.getId(), fileUploadStrategy);
			}
		}

		Set<String> fileUploadStrategyKeys = this.fileUploadStrategys.keySet();
		for (String fileUploadStrategyKey : fileUploadStrategyKeys) {
			FileUploadStrategy fileUploadStrategy = this.fileUploadStrategys.get(fileUploadStrategyKey);
			String parentId = fileUploadStrategy.getParentId();
			if (StringUtils.isNotBlank(parentId)) {
				fileUploadStrategy.setParent(this.fileUploadStrategys.get(parentId));
			}
		}

	}

	public static class FileUploadStrategy {

		private String id;

		private String parentId;

		private String[] exts;

		private long maxSize = -1;

		private String baseStoragePath;

		private String storageDir;

		private String baseFileUrl;

		private FileUploadStrategy parent;

		public FileUploadStrategy getParent() {
			return this.parent;
		}

		public void setParent(FileUploadStrategy parent) {
			this.parent = parent;
		}

		public String getId() {
			return this.id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getParentId() {
			return this.parentId;
		}

		public void setParentId(String parentId) {
			this.parentId = parentId;
		}

		public String[] getExts() {
			if (null == this.exts && null != this.parent) {
				return this.parent.getExts();
			}
			return this.exts;
		}

		public void setExts(String[] exts) {
			this.exts = exts;
		}

		public long getMaxSize() {
			if (this.maxSize == -1 && null != this.parent) {
				return this.parent.getMaxSize();
			}
			return this.maxSize;
		}

		public void setMaxSize(long maxSize) {
			this.maxSize = maxSize;
		}

		public String getBaseStoragePath() {
			if (StringUtils.isBlank(this.baseStoragePath) && null != this.parent) {
				return this.parent.getBaseStoragePath();
			}
			return this.baseStoragePath;
		}

		public void setBaseStoragePath(String baseStoragePath) {
			this.baseStoragePath = baseStoragePath;
		}

		public String getStorageDir() {
			if (StringUtils.isBlank(this.storageDir) && null != this.parent) {
				return this.parent.getStorageDir();
			}
			return this.storageDir;
		}

		public void setStorageDir(String storageDir) {
			this.storageDir = storageDir;
		}

		public String getBaseFileUrl() {
			if (StringUtils.isBlank(this.baseFileUrl) && null != this.parent) {
				return this.parent.getBaseFileUrl();
			}
			return this.baseFileUrl;
		}

		public void setBaseFileUrl(String baseFileUrl) {
			this.baseFileUrl = baseFileUrl;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}

	}

	public static class FileUploadResp {

		public final static int RESULT_CODE_SUCCESS = 0;

		public final static int RESULT_CODE_FAILURE = 1;

		/**
		 * 结果：0 表示成功；1 表示失败
		 */
		private int resultCode;

		/**
		 * 结果说明
		 */
		private String resultDesc;

		/**
		 * 相对路径(用于入库保存使用)
		 */
		private String relaPath;

		/**
		 * 绝对路径(用于进行页面展示使用)
		 */
		private String absPath;

		public FileUploadResp() {
		}

		public FileUploadResp(int resultCode, String resultDesc) {
			super();
			this.resultCode = resultCode;
			this.resultDesc = resultDesc;
		}

		public FileUploadResp(String relaPath, String absPath) {
			this.resultCode = RESULT_CODE_SUCCESS;
			this.resultDesc = "上传成功";
			this.relaPath = relaPath;
			this.absPath = absPath;
		}

		public int getResultCode() {
			return this.resultCode;
		}

		public void setResultCode(int resultCode) {
			this.resultCode = resultCode;
		}

		public String getResultDesc() {
			return this.resultDesc;
		}

		public void setResultDesc(String resultDesc) {
			this.resultDesc = resultDesc;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}

		public String getRelaPath() {
			return this.relaPath;
		}

		public void setRelaPath(String relaPath) {
			this.relaPath = relaPath;
		}

		public String getAbsPath() {
			return this.absPath;
		}

		public void setAbsPath(String absPath) {
			this.absPath = absPath;
		}

	}

}
