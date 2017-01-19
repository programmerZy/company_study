package com.oss.bucket;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.event.ProgressEvent;
import com.aliyun.oss.event.ProgressEventType;
import com.aliyun.oss.event.ProgressListener;
import com.aliyun.oss.model.AbortMultipartUploadRequest;
import com.aliyun.oss.model.AppendObjectRequest;
import com.aliyun.oss.model.AppendObjectResult;
import com.aliyun.oss.model.Bucket;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.CompleteMultipartUploadRequest;
import com.aliyun.oss.model.CreateBucketRequest;
import com.aliyun.oss.model.InitiateMultipartUploadRequest;
import com.aliyun.oss.model.InitiateMultipartUploadResult;
import com.aliyun.oss.model.ListBucketsRequest;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PartETag;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.UploadFileRequest;
import com.aliyun.oss.model.UploadPartRequest;
import com.aliyun.oss.model.UploadPartResult;
import com.oss.OssConfig;

public class BucketDemo {
	public static void main(String[] args) throws IOException {
		// --------------------------------------------- 管理bucket
		// ----------------------------
		/**
		 * 链接客户端
		 */
		OSSClient client = new OSSClient(OssConfig.endpoint, OssConfig.accessKeyId, OssConfig.accessKeySecret);

		String bucketName = "bjrqtest2";
		/**
		 * 创建默认权限
		 */
		client.createBucket(bucketName);
		/**
		 * 创建带权限的存储空间
		 */
		CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);
		createBucketRequest.setCannedACL(CannedAccessControlList.PublicRead);

		client.shutdown();

		/**
		 * 
		 * 查看 域 域名下的所有的存储空间
		 */

		List<Bucket> buckets = client.listBuckets();

		/**
		 * 查询列表的时候，可以利用
		 */

		ListBucketsRequest listBucketsRequest = new ListBucketsRequest();

		listBucketsRequest.setPrefix("bqjr");

		client.listBuckets(listBucketsRequest);

		// client.listBuckets(prefix, marker, maxKeys)

		/**
		 * 判断是否存在
		 */
		boolean exist = client.doesBucketExist(bucketName);

		client.getBucketAcl(bucketName); // 获取权限

		// --------------------------------------------- 管理bucket end
		// ---------------------------

		// 文件上传

		// 1 流的形式伤上传 ① 字符串

		client.putObject("bqjrtest2", "hello1", new ByteArrayInputStream("hell1".getBytes()));

		// 网络资源
		URL url = new URL("www.baidu.com");

		// 文件
		InputStream inputStream = url.openStream();
		client.putObject("bqjrtest2", "hell01", inputStream);

		// 设置元信息
		// http header user Meta

	}

	public void objectMata() throws Throwable {
		// endpoint以杭州为例，其它region请按实际情况填写
		String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";
		// accessKey请登录https://ak-console.aliyun.com/#/查看
		String accessKeyId = "<yourAccessKeyId>";
		String accessKeySecret = "<yourAccessKeySecret>";
		String content = "Hello OSS";
		// 创建上传Object的Metadata
		ObjectMetadata meta = new ObjectMetadata();
		// 设置上传文件长度
		meta.setContentLength(content.length());
		// 设置上传MD5校验
		String md5 = BinaryUtil.toBase64String(BinaryUtil.calculateMd5(content.getBytes()));
		meta.setContentMD5(md5);
		// 设置上传内容类型
		meta.setContentType("text/plain");
		// 创建OSSClient实例
		OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
		// 上传文件
		ossClient.putObject("<yourBucketName>", "<yourKey>", new ByteArrayInputStream(content.getBytes()), meta);
		
		AppendObjectRequest appendObjectRequest = 
				new AppendObjectRequest("bjqrtest1", "key", new File(""));
		
		AppendObjectResult appendObjectResult = ossClient.appendObject(appendObjectRequest);
		
		
		appendObjectRequest.setPosition(appendObjectResult.getNextPosition());
		appendObjectResult = ossClient.appendObject(appendObjectRequest);
		
		appendObjectRequest.setPosition(appendObjectResult.getNextPosition());
		appendObjectResult = ossClient.appendObject(appendObjectRequest);
		
		
		
		
		UploadFileRequest uploadFileRequest = new UploadFileRequest("bqjrtest1", "key");
		
		uploadFileRequest.setUploadFile("file");
	    uploadFileRequest.setTaskNum(5);
	    uploadFileRequest.setEnableCheckpoint(true);
	    
	    ossClient.uploadFile(uploadFileRequest);
	    
		InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest("bucketname","key");
		InitiateMultipartUploadResult result = ossClient.initiateMultipartUpload(request);
		String uploadId = result.getUploadId();
		System.out.println(uploadId);
		
		UploadPartRequest uploadPartRequest = new UploadPartRequest();
		
		uploadPartRequest.setBucketName("bqjretst1");
		uploadPartRequest.setKey("part1");
		uploadPartRequest.setUploadId(uploadId);
		uploadPartRequest.setInputStream(new ByteArrayInputStream("111".getBytes()));
		// 分片上传的 每个分片的大小
		uploadPartRequest.setPartSize(100 * 1024);
		
		uploadPartRequest.setPartNumber(1);
		
		
		List<PartETag> partETags = new ArrayList<PartETag>();

		UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
		partETags.add(uploadPartResult.getPartETag());
		
		
		Collections.sort(partETags, new Comparator<PartETag>() {
		    public int compare(PartETag p1, PartETag p2) {
		        return p1.getPartNumber() - p2.getPartNumber();
		    }
		});
		
		CompleteMultipartUploadRequest completeMultipartUploadRequest = 
		        new CompleteMultipartUploadRequest("heandasf", "111", uploadId, partETags);
		ossClient.completeMultipartUpload(completeMultipartUploadRequest);
		/**
		 * 取消分区上传的情况
		 */
		AbortMultipartUploadRequest abortMultipartUploadRequest = 
		        new AbortMultipartUploadRequest("<yourBucketName>", "<yourKey>", "<uploadId>");
		ossClient.abortMultipartUpload(abortMultipartUploadRequest);
		// 关闭client
		ossClient.shutdown();
	}
	/**
	 * 文件分片上传
	 */
	public void multipartUpload(){
	}
	
	
	static class PutObjectProgressListener implements ProgressListener {
	    private long bytesWritten = 0;
	    private long totalBytes = -1;
	    private boolean succeed = false;
	    
	    public void progressChanged(ProgressEvent progressEvent) {
	        long bytes = progressEvent.getBytes();
	        ProgressEventType eventType = progressEvent.getEventType();
	        switch (eventType) {
	        case TRANSFER_STARTED_EVENT:
	            System.out.println("Start to upload......");
	            break;
	        case REQUEST_CONTENT_LENGTH_EVENT:
	            this.totalBytes = bytes;
	            System.out.println(this.totalBytes + " bytes in total will be uploaded to OSS");
	            break;
	        case REQUEST_BYTE_TRANSFER_EVENT:
	            this.bytesWritten += bytes;
	            if (this.totalBytes != -1) {
	                int percent = (int)(this.bytesWritten * 100.0 / this.totalBytes);
	                System.out.println(bytes + " bytes have been written at this time, upload progress: " + percent + "%(" + this.bytesWritten + "/" + this.totalBytes + ")");
	            } else {
	                System.out.println(bytes + " bytes have been written at this time, upload ratio: unknown" + "(" + this.bytesWritten + "/...)");
	            }
	            break;
	        case TRANSFER_COMPLETED_EVENT:
	            this.succeed = true;
	            System.out.println("Succeed to upload, " + this.bytesWritten + " bytes have been transferred in total");
	            break;
	        case TRANSFER_FAILED_EVENT:
	            System.out.println("Failed to upload, " + this.bytesWritten + " bytes have been transferred");
	            break;
	        default:
	            break;
	        }
	    }
	    public boolean isSucceed() {
	        return succeed;
	    }
	}
	
	public void jdt(){
		String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";
	    String accessKeyId = "<accessKeyId>";
	    String accessKeySecret = "<accessKeySecret>";
	    String bucketName = "<bucketName>";
	    String key = "object-get-progress-sample";
	    OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
	    try {            
	        // 带进度条的上传 
	    	// 添加进度条的代码计算
	        ossClient.putObject(new PutObjectRequest(bucketName, key, 
	        		new FileInputStream("<yourLocalFile>")).<PutObjectRequest>withProgressListener(new PutObjectProgressListener()));
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    ossClient.shutdown();
	}
	
}
