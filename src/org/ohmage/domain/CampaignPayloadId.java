package org.ohmage.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.request.UserRequest.TokenLocation;
import org.ohmage.request.observer.StreamReadRequest.ColumnNode;
import org.ohmage.request.survey.SurveyResponseReadRequest;
import org.ohmage.request.survey.SurveyResponseRequest;
import org.ohmage.util.StringUtils;

/**
 * This class represents a campaign-specific payload ID.
 *
 * @author John Jenkins
 */
public class CampaignPayloadId implements PayloadId {
	/**
	 * The possible types of the sub-ID. These are based on the sub-elements of
	 * {@link org.ohmage.domain.campaign.Campaign Campaigns}.
	 *
	 * @author John Jenkins
	 */
	public static enum Type {
		/**
		 * Indicates that the sub-ID will be a survey ID.
		 */
		SURVEY ("survey_id"),
		/**
		 * Indicates that the sub-ID will be a prompt ID.
		 */
		PROMPT ("prompt_id");
		
		/**
		 * A map of names to their respective Type.
		 */
		private static final Map<String, Type> NAMES = 
			new HashMap<String, Type>();
		/**
		 * Construct a mapping from names to the actual type for constant time
		 * lookups.
		 */
		static {
			Type[] types = Type.values();
			for(int i = 0; i < types.length; i++) {
				NAMES.put(types[i].name, types[i]);
			}
		}
		
		private final String name;
		
		/**
		 * Creates a Type with a name.
		 * 
		 * @param name The name of this type as it will appear in the payload
		 * 			   IDs.
		 */
		private Type(final String name) {
			if(StringUtils.isEmptyOrWhitespaceOnly(name)) {
				throw new IllegalArgumentException(
					"The name is null or only whitespace.");
			}
			
			this.name = name;
		}
		
		public static Type getType(final String name) {
			if(NAMES.containsKey(name)) {
				return NAMES.get(name);
			}
			else {
				throw new IllegalArgumentException(
					"No Type with the given name: " +
						name);
			}
		}
	};
	
	private final String campaignId;
	private final Type type;
	private final String subId;
	
	/**
	 * Defines a campaign payload ID that only contains a campaign ID. This 
	 * performs no validation on the campaign ID other than checking if it is
	 * null or whitespace only; that is the responsibility of the caller. 
	 * 
	 * @param campaignId The campaign ID.
	 * 
	 * @throws DomainException The campaign ID is null or whitespace only.
	 */
	public CampaignPayloadId(final String campaignId) throws DomainException {
		if(StringUtils.isEmptyOrWhitespaceOnly(campaignId)) {
			throw new DomainException(
				"The campaign ID is null or only whitespace.");
		}
		
		this.campaignId = campaignId;
		this.type = null;
		this.subId = null;
	}
	
	/**
	 * Defines a campaign payload ID that contains a campaign ID, a
	 * {@link Type type} and the ID for that type. No validation is performed
	 * on the parameters other than a null check and a whitespace check on the
	 * strings; that is the responsibility of the caller.
	 * 
	 * @param campaignId The campaign ID.
	 * 
	 * @param type The {@link Type type} of the sub-ID.
	 * 
	 * @param subId The sub-ID based on this {@link Type type}.
	 * 
	 * @throws DomainException Thrown if any of the parameters are invalid.
	 */
	public CampaignPayloadId(
			final String campaignId,
			final Type type,
			final String subId)
			throws DomainException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(campaignId)) {
			throw new DomainException(
				"The campaign ID is null or only whitespace.");
		}
		else if(type == null) {
			throw new DomainException("The type is null.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(subId)) {
			throw new DomainException(
				"The sub-ID is null or only whitespace.");
		}
		
		this.campaignId = campaignId;
		this.type = type;
		this.subId = subId;
	}

	/**
	 * Returns the campaign ID.
	 * 
	 * @return The campaign ID.
	 */
	@Override
	public String getId() {
		return campaignId;
	}
	
	/**
	 * Returns the {@link Type type} of the sub-ID.
	 * 
	 * @return The {@link Type type} of the sub-ID.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * If the {@link Type type} is not null, returns the ID of that type. 
	 * Otherwise, null is returned.
	 * 
	 * @return The survey ID or prompt ID depending on the {@link Type type} or
	 * 		   null if the type is unknown.
	 * 
	 * @see #getType()
	 */
	@Override
	public String getSubId() {
		return subId;
	}

	/**
	 * Creates a survey_response/read request.
	 * 
	 * @return A survey_response/read request.
	 */
	@Override
	public SurveyResponseReadRequest generateSubRequest(
			final HttpServletRequest httpRequest,
			final Map<String, String[]> parameters,
			final Boolean hashPassword,
			final TokenLocation tokenLocation,
			final long version,
			final DateTime startDate,
			final DateTime endDate,
			final ColumnNode<String> columns,
			final long numToSkip,
			final long numToReturn)
			throws DomainException {
		
		Collection<String> surveyIds = null;
		Collection<String> promptIds = null;
		if(CampaignPayloadId.Type.SURVEY.equals(type)) {
			surveyIds = new ArrayList<String>(1);
			surveyIds.add(subId);
		}
		else if(CampaignPayloadId.Type.PROMPT.equals(type)) {
			promptIds = new ArrayList<String>(1);
			promptIds.add(subId);
		}
		
		Collection<SurveyResponse.ColumnKey> translatedColumns = null;
		// TODO: Convert the columns list to something the request wants.
		
		try {
			return
				new SurveyResponseReadRequest(
					httpRequest,
					parameters,
					campaignId,
					SurveyResponseRequest.URN_SPECIAL_ALL_LIST,
					surveyIds,
					promptIds,
					null,
					startDate,
					endDate,
					null,
					null,
					translatedColumns,
					null,
					null,
					null,
					null,
					null,
					null,
					numToSkip,
					numToReturn);
		}
		catch(IOException e) {
			throw new DomainException(
				"There was an error reading the HTTP request.",
				e);
		}
		catch(InvalidRequestException e) {
			throw new DomainException(
				"Error parsing the parameters.",
				e);
		}
		catch(IllegalArgumentException e) {
			throw new DomainException(
				"One of the parameters was invalid.",
				e);
		}
	}
}
