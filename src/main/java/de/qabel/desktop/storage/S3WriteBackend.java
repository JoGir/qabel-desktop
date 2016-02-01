package de.qabel.desktop.storage;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import de.qabel.desktop.exceptions.QblStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class S3WriteBackend implements StorageWriteBackend {

	private static final Logger logger = LoggerFactory.getLogger(FolderNavigation.class.getSimpleName());

	public final AmazonS3Client s3Client;
	final String bucket;
	final String prefix;

	S3WriteBackend(AWSCredentials credentials, String bucket, String prefix) {
		s3Client = new AmazonS3Client(credentials);
		this.bucket = bucket;
		this.prefix = prefix;
		logger.info("S3WriteBackend running on prefix " + prefix);
	}

	@Override
	public long upload(String name, InputStream inputStream) throws QblStorageException {
		logger.info("Uploading to name " + name);
		try {
			String path = prefix + '/' + name;
			s3Client.putObject(bucket, path, inputStream, new ObjectMetadata());
			ObjectMetadata objectMetadata = s3Client.getObjectMetadata(bucket, path);
			return objectMetadata.getLastModified().getTime();
		} catch (RuntimeException e) {
			throw new QblStorageException(e);
		}
	}

	@Override
	public void delete(String name) throws QblStorageException {
		logger.info("Deleting name " + name);
		try {
			s3Client.deleteObject(bucket, prefix + '/' + name);
		} catch (RuntimeException e) {
			throw new QblStorageException(e);
		}
	}

}