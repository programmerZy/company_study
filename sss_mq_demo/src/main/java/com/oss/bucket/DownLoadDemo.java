package com.oss.bucket;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.DownloadFileRequest;
import com.aliyun.oss.model.DownloadFileResult;
import com.aliyun.oss.model.OSSObject;

public class DownLoadDemo {
	/**
	 *  
	 * @param args
	 * @throws Throwable 
	 */
	public static void main(String[] args) throws Throwable {
		String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";
		// accessKey请登录https://ak-console.aliyun.com/#/查看
		String accessKeyId = "<yourAccessKeyId>";
		String accessKeySecret = "<yourAccessKeySecret>";
		String bucketName = "<yourBucketName>";
		OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
		OSSObject ossObject = ossClient.getObject(bucketName, "yourKey");
		BufferedReader reader = new BufferedReader(new InputStreamReader(ossObject.getObjectContent()));
		while (true) {
		    String line = reader.readLine();
		    if (line == null) break;
		    System.out.println("\n" + line);
		}
		
		
		byte[] buf = new byte[1024];
		InputStream in = ossObject.getObjectContent();
		for (int n = 0; n != -1; ) {
		  n = in.read(buf, 0, buf.length);
		}
		in.close();
		/**
		 * 上传完成后调用回调函数
		 */
		DownloadFileRequest downloadFileRequest = new DownloadFileRequest("bucketName", "key");
		downloadFileRequest.setDownloadFile("downloadFile");
		downloadFileRequest.setTaskNum(10);
		downloadFileRequest.setEnableCheckpoint(true);
		
		// 断点续传
		
		DownloadFileResult downloadRes = ossClient.downloadFile(downloadFileRequest);
		// 下载成功时，会返回文件的元信息
		downloadRes.getObjectMetadata();
		
		
	}
	
	
}
