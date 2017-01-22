package com.oss.bucket;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.UploadFileRequest;

public class UploadDemo {
	public static void main(String[] args) throws Throwable {
		// endpoint以杭州为例，其它region请按实际情况填写
		String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";
		// accessKey请登录https://ak-console.aliyun.com/#/查看
		String accessKeyId = "<yourAccessKeyId>";
		String accessKeySecret = "<yourAccessKeySecret>";
		// 创建OSSClient实例
		OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
		// 设置断点续传请求
		UploadFileRequest uploadFileRequest = new UploadFileRequest("<yourBucketName>", "<yourKey>");
		// 指定上传的本地文件
		uploadFileRequest.setUploadFile("<yourLocalFile>");
		// 指定上传并发线程数
		uploadFileRequest.setTaskNum(5);
		// 指定上传的分片大小
		uploadFileRequest.setPartSize(1 * 1024 * 1024);
		// 开启断点续传
		uploadFileRequest.setEnableCheckpoint(true);
		// 断点续传上传
		ossClient.uploadFile(uploadFileRequest);
		// 关闭client
		ossClient.shutdown();
	}
}
