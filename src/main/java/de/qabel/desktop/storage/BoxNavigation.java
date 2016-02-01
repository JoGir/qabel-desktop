package de.qabel.desktop.storage;

import de.qabel.desktop.exceptions.QblStorageException;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public interface BoxNavigation extends ReadOnlyBoxNavigation {

	DirectoryMetadata reloadMetadata() throws QblStorageException;

	void setMetadata(DirectoryMetadata dm);

	/**
	 * Bumps the version and uploads the metadata file
	 * <p/>
	 * All actions are not guaranteed to be finished before the commit
	 * method returned.
	 *
	 * @throws QblStorageException
	 */
	void commit() throws QblStorageException;

	/**
	 * Upload a new file to the current folder
	 *
	 * @param name name of the file, must be unique
	 * @param file file object that must be readable
	 * @param listener
	 * @return the resulting BoxFile object
	 * @throws QblStorageException if the upload failed or the name is not unique
	 */
	BoxFile upload(String name, File file, ProgressListener listener) throws QblStorageException;

	/**
	 * Upload a new file to the current folder
	 *
	 * @param name name of the file, must be unique
	 * @param file file object that must be readable
	 * @return the resulting BoxFile object
	 * @throws QblStorageException if the upload failed or the name is not unique
	 */
	BoxFile upload(String name, File file) throws QblStorageException;

	/**
	 * Overwrite a file in the current folder
	 *
	 * @param name name of the file which must already exist
	 * @param file file object that must be readable
	 * @param listener
	 * @return the updated BoxFile object
	 * @throws QblStorageException if he upload failed or the name does not exist
	 */
	BoxFile overwrite(String name, File file, ProgressListener listener) throws QblStorageException;

	/**
	 * Overwrite a file in the current folder
	 *
	 * @param name name of the file which must already exist
	 * @param file file object that must be readable
	 * @return the updated BoxFile object
	 * @throws QblStorageException if he upload failed or the name does not exist
	 */
	BoxFile overwrite(String name, File file) throws QblStorageException;

	/**
	 * Create an {@link InputStream} for a {@link BoxFile} in the current folder
	 *
	 * @param file file in the current folder
	 * @param listener
	 * @return Decrypted stream
	 * @throws QblStorageException if the download or decryption failed
	 */
	InputStream download(BoxFile file, ProgressListener listener) throws QblStorageException;

	/**
	 * Create an {@link InputStream} for a {@link BoxFile} in the current folder
	 *
	 * @param file file in the current folder
	 * @return Decrypted stream
	 * @throws QblStorageException if the download or decryption failed
	 */
	InputStream download(BoxFile file) throws QblStorageException;

	/**
	 * Create a subfolder in the current folder. You should commit
	 * after creating a new subfolder to minimize conflict potential.
	 *
	 * @param name name of the folder, must be unique
	 * @return new folder object
	 * @throws QblStorageException
	 */
	BoxFolder createFolder(String name) throws QblStorageException;

	/**
	 * Delete a file in the current folder. The block will be deleted when committing
	 *
	 * @param file
	 * @throws QblStorageException if the file does not exist or the deletion failed
	 */
	void delete(BoxFile file) throws QblStorageException;

	/**
	 * Delete a subfolder recursively.
	 *
	 * @param folder
	 * @throws QblStorageException
	 */
	void delete(BoxFolder folder) throws QblStorageException;

	/**
	 * Remove a share mount from the current folder
	 *
	 * @param external
	 * @throws QblStorageException
	 */
	void delete(BoxExternal external) throws QblStorageException;

	/**
	 * Enable or disable autocommits. Implicitly commits after each committable action (defaults to true)
	 */
	void setAutocommit(boolean autocommit);

	DirectoryMetadata getMetadata();

}
