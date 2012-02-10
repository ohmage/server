/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
