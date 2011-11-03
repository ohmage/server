package org.ohmage.query;

import org.ohmage.exception.DataAccessException;

public interface IUserCampaignClassQueries {

	/**
	 * Retrieves the number of classes through which the user is associated 
	 * with a campaign. Another way of phrasing it is, this determines how many
	 * classes to which the user is associated and then returns the number of
	 * those classes which are associated with the campaign.<br />
	 * <br />
	 * Note: This may be misleading in our current implementation. Our current
	 * method of handling this information grants all of the users in a class
	 * direct associations with the campaign when the class is associated. If a
	 * user were to then have those roles revoked, there would be no way to
	 * detect this meaning that this call would still return that class'
	 * association despite having those roles removed.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @return The number of classes that are associated with the campaign and
	 * 		   of which the user is a member.
	 */
	int getNumberOfClassesThroughWhichUserIsAssociatedWithCampaign(
			String username, String campaignId) throws DataAccessException;

}