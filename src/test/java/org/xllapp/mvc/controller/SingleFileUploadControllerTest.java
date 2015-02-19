package org.xllapp.mvc.controller;


import org.junit.Assert;
import org.junit.Test;
import org.xllapp.mvc.controller.SingleFileUploadController.FileUploadStrategy;

/**
 *
 *
 * @Copyright: Copyright (c) 2014 FFCS All Rights Reserved 
 * @Company: 北京福富软件有限公司 
 * @author 陈作朋 Oct 23, 2014
 * @version 1.00.00
 * @history:
 * 
 */
public class SingleFileUploadControllerTest {

	/**
	 * 无父亲策略
	 */
	@Test
	public void test1(){
		
		 String[] exts=new String[]{"abc","efg"};

		 long maxSize = 123;

		 String baseStoragePath="baseStoragePath";

		 String storageDir="storageDir";

		 String baseFileUrl="baseFileUrl";
		 
		 FileUploadStrategy fileUploadStrategy=new FileUploadStrategy();
		 
		 fileUploadStrategy.setExts(exts);
		 
		 fileUploadStrategy.setMaxSize(maxSize);
		 
		 fileUploadStrategy.setBaseStoragePath(baseStoragePath);
		 
		 fileUploadStrategy.setStorageDir(storageDir);
		
		 fileUploadStrategy.setBaseFileUrl(baseFileUrl);
		 
		 Assert.assertArrayEquals(fileUploadStrategy.getExts(), exts);
		 
		 Assert.assertEquals(fileUploadStrategy.getMaxSize(), maxSize);
		 
		 Assert.assertEquals(fileUploadStrategy.getBaseStoragePath(), baseStoragePath);
		 Assert.assertEquals(fileUploadStrategy.getStorageDir(), storageDir);
		 Assert.assertEquals(fileUploadStrategy.getBaseFileUrl(), baseFileUrl);
		 
	}
	
	/**
	 * 所有属性全部继承自父策略
	 */
	@Test
	public void test2(){
		
		 String[] exts=new String[]{"abc","efg"};

		 long maxSize = 123;

		 String baseStoragePath="baseStoragePath";

		 String storageDir="storageDir";

		 String baseFileUrl="baseFileUrl";
		
		 FileUploadStrategy parent=new FileUploadStrategy();
		 
		 parent.setExts(exts);
		 
		 parent.setMaxSize(maxSize);
		 
		 parent.setBaseStoragePath(baseStoragePath);
		 
		 parent.setStorageDir(storageDir);
		
		 parent.setBaseFileUrl(baseFileUrl);
		 
		 FileUploadStrategy fileUploadStrategy=new FileUploadStrategy();
		 fileUploadStrategy.setParent(parent);
		 
		 Assert.assertArrayEquals(fileUploadStrategy.getExts(), exts);
		 
		 Assert.assertEquals(fileUploadStrategy.getMaxSize(), maxSize);
		 
		 Assert.assertEquals(fileUploadStrategy.getBaseStoragePath(), baseStoragePath);
		 Assert.assertEquals(fileUploadStrategy.getStorageDir(), storageDir);
		 Assert.assertEquals(fileUploadStrategy.getBaseFileUrl(), baseFileUrl);
	}
	
	/**
	 * 没有属性继承自父策略
	 */
	@Test
	public void test3(){
		
		 String[] exts=new String[]{"abc","efg"};

		 long maxSize = 123;

		 String baseStoragePath="baseStoragePath";

		 String storageDir="storageDir";

		 String baseFileUrl="baseFileUrl";
		 
		 FileUploadStrategy parent=new FileUploadStrategy();
		 
		 parent.setExts(new String[]{});
		 
		 parent.setMaxSize(maxSize+100);
		 
		 parent.setBaseStoragePath(baseStoragePath+"abc");
		 
		 parent.setStorageDir(storageDir+"123");
		
		 parent.setBaseFileUrl(baseFileUrl+"ppp");
		
		 FileUploadStrategy fileUploadStrategy=new FileUploadStrategy();
		 
		 fileUploadStrategy.setExts(exts);
		 
		 fileUploadStrategy.setMaxSize(maxSize);
		 
		 fileUploadStrategy.setBaseStoragePath(baseStoragePath);
		 
		 fileUploadStrategy.setStorageDir(storageDir);
		
		 fileUploadStrategy.setBaseFileUrl(baseFileUrl);
		 
		 fileUploadStrategy.setParent(parent);
		 
		 Assert.assertArrayEquals(fileUploadStrategy.getExts(), exts);
		 
		 Assert.assertEquals(fileUploadStrategy.getMaxSize(), maxSize);
		 
		 Assert.assertEquals(fileUploadStrategy.getBaseStoragePath(), baseStoragePath);
		 Assert.assertEquals(fileUploadStrategy.getStorageDir(), storageDir);
		 Assert.assertEquals(fileUploadStrategy.getBaseFileUrl(), baseFileUrl);
	}
	
	/**
	 * 部分属性继承自父策略
	 */
	@Test
	public void test4(){
		
		 String[] exts=new String[]{"abc","efg"};

		 long maxSize = 123;

		 String baseStoragePath="baseStoragePath";

		 String storageDir="storageDir";

		 String baseFileUrl="baseFileUrl";
		 
		 FileUploadStrategy parent=new FileUploadStrategy();
		 
		 parent.setExts(exts);
		 
		 parent.setMaxSize(maxSize);
		 
		 parent.setStorageDir(storageDir);
		
		 FileUploadStrategy fileUploadStrategy=new FileUploadStrategy();
		 
		 fileUploadStrategy.setBaseStoragePath(baseStoragePath);
		 
		 fileUploadStrategy.setBaseFileUrl(baseFileUrl);
		 
		 fileUploadStrategy.setParent(parent);
		 
		 Assert.assertArrayEquals(fileUploadStrategy.getExts(), exts);
		 Assert.assertEquals(fileUploadStrategy.getMaxSize(), maxSize);
		 Assert.assertEquals(fileUploadStrategy.getBaseStoragePath(), baseStoragePath);
		 Assert.assertEquals(fileUploadStrategy.getStorageDir(), storageDir);
		 Assert.assertEquals(fileUploadStrategy.getBaseFileUrl(), baseFileUrl);
	}
	
}
