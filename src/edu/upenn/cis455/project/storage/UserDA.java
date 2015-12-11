package edu.upenn.cis455.project.storage;

import com.sleepycat.persist.PrimaryIndex;

import edu.upenn.cis455.project.bean.User;

// TODO: Auto-generated Javadoc
/**
 * Data Accessor class for the user entity class.
 *
 * @author cis455
 */
public class UserDA
{

	/**
	 * Gets the user.
	 *
	 * @param userName
	 *            the user name
	 * @return the user
	 */
	public static User getUser(String userName) // returns null if the entry
												// wasn't found
	{
		User user = null;
		if (DBWrapper.getStore() != null)
		{
			PrimaryIndex<String, User> userPrimaryIndex = DBWrapper.getStore()
					.getPrimaryIndex(String.class, User.class);
			if (userPrimaryIndex != null)
			{
				user = userPrimaryIndex.get(userName);
			}
		}
		return user;
	}

	/**
	 * Put user.
	 *
	 * @param user
	 *            the user
	 * @return the user
	 */
	public static User putUser(User user) // returns null is the user did not
											// exist in the DB
	{
		User insertedUser = null;
		if (DBWrapper.getStore() != null)
		{
			PrimaryIndex<String, User> userPrimaryIndex = DBWrapper.getStore()
					.getPrimaryIndex(String.class, User.class);
			if (userPrimaryIndex != null)
			{
				insertedUser = userPrimaryIndex.put(user);
			}
		}
		return insertedUser;
	}

	/**
	 * Removes the user.
	 *
	 * @param userName
	 *            the user name
	 * @return true, if successful
	 */
	public static boolean removeUser(String userName)
	{
		if (DBWrapper.getStore() != null)
		{
			PrimaryIndex<String, User> userPrimaryIndex = DBWrapper.getStore()
					.getPrimaryIndex(String.class, User.class);
			if (userPrimaryIndex != null)
			{
				return userPrimaryIndex.delete(userName);
			}
		}
		return false;
	}
}
