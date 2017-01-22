package com.oss;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.Properties;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.SendResult;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.Bucket;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.CreateBucketRequest;
import com.aliyun.oss.model.ListBucketsRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectAcl;
import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.mq.MqConfig;

public class OssDemo {
	private static String endpoint = "http://oss-cn-shanghai.aliyuncs.com";
    private static String accessKeyId = "LTAI5NkT3gXAD36M";
    private static String accessKeySecret = "8PxYIoKHaFNYdbRNyMqwFgy5P581Gh";
    private static String bucketName = "bqjrtest1";
    
    public static void main(String[] args) throws IOException {
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        try {
            if (!ossClient.doesBucketExist(bucketName)) {
                System.out.println("Creating bucket " + bucketName + "\n");
                ossClient.createBucket(bucketName);
                CreateBucketRequest createBucketRequest= new CreateBucketRequest(bucketName);
                createBucketRequest.setCannedACL(CannedAccessControlList.PublicRead);
                ossClient.createBucket(createBucketRequest);
            }
            System.out.println("Listing buckets");
            ListBucketsRequest listBucketsRequest = new ListBucketsRequest();
            listBucketsRequest.setMaxKeys(500);
            for (Bucket bucket : ossClient.listBuckets()) {
                System.out.println(" - " + bucket.getName());
            }
            System.out.println();
            System.out.println("Uploading a new object to OSS from a file\n");
            File file = createSampleFile();
            String key = "file:"+file.getName();
            
            PutObjectResult result =  ossClient.putObject(new PutObjectRequest(bucketName, key, file));
            System.out.println(JSONObject.toJSONString(result));
            /**
             * TODO judge result issuccess  
             * if(issccuess){
             * 	TODO success handler
             * }else{
             * 	TODO error handler
             * }
             */
            /**
             * Mq 
             */
            Properties producerProperties = new Properties();
            producerProperties.setProperty(PropertyKeyConst.ProducerId, MqConfig.PRODUCER_ID);
            producerProperties.setProperty(PropertyKeyConst.AccessKey, MqConfig.ACCESS_KEY);
            producerProperties.setProperty(PropertyKeyConst.SecretKey, MqConfig.SECRET_KEY);
            producerProperties.setProperty(PropertyKeyConst.ONSAddr, MqConfig.ONSADDR);
            Producer producer = ONSFactory.createProducer(producerProperties);
            producer.start();
            System.out.println("Producer Started");
            Message message = new Message(MqConfig.TOPIC, 
            		MqConfig.TAG,("already upload a file:" + file.getName()).getBytes());
            SendResult sendResult = producer.send(message);
            if (sendResult != null) {
                System.out.println(new Date() + " Send mq message success! Topic is:" + MqConfig.TOPIC + " msgId is: " + sendResult.getMessageId());
            }
            /*
             * Determine whether an object residents in your bucket
             */
            boolean exists = ossClient.doesObjectExist(bucketName, key);
            System.out.println("Does object " + bucketName + " exist? " + exists + "\n");
            
            ossClient.setObjectAcl(bucketName, key, CannedAccessControlList.PublicRead);
            ossClient.setObjectAcl(bucketName, key, CannedAccessControlList.Default);
            
            ObjectAcl objectAcl = ossClient.getObjectAcl(bucketName, key);
            System.out.println("ACL:" + objectAcl.getPermission().toString());
            
            /*
             * Download an object from your bucket
             */
            System.out.println("Downloading an object");
            OSSObject object = ossClient.getObject(bucketName, key);
            System.out.println("Content-Type: "  + object.getObjectMetadata().getContentType());
            displayTextInputStream(object.getObjectContent());

            /*
             * List objects in your bucket by prefix
             */
            System.out.println("Listing objects");
            ObjectListing objectListing = ossClient.listObjects(bucketName, "My");
            for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                System.out.println(" - " + objectSummary.getKey() + "  " +
                                   "(size = " + objectSummary.getSize() + ")");
            }
            System.out.println();
           // ossClient.deleteObject(bucketName, key);
            System.out.println("Deleting an object\n");
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message: " + oe.getErrorCode());
            System.out.println("Error Code:       " + oe.getErrorCode());
            System.out.println("Request ID:      " + oe.getRequestId());
            System.out.println("Host ID:           " + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ce.getMessage());
        } finally {
            ossClient.shutdown();
        }
    }
    
    private static File createSampleFile() throws IOException {
    	File file = File.createTempFile("oss-java-sdk-", ".txt");
		file.deleteOnExit();
        Writer writer = new OutputStreamWriter(new FileOutputStream(file));
        writer.write("abcdefghijklmnopqrstuvwxyz\n");
        writer.write("0123456789011234567890\n");
        writer.close();
        return file;
    }

    private static void displayTextInputStream(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        while (true) {
            String line = reader.readLine();
            if (line == null) break;

            System.out.println("    " + line);
        }
        System.out.println();
        
        reader.close();
    }

}