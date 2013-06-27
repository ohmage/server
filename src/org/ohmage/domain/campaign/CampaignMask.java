package org.ohmage.domain.campaign;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;
import org.ohmage.exception.DomainException;

/**
 * <p>
 * A campaign mask is a list of survey IDs for a specific campaign that are
 * visible to a user. An "assigner user" generates the list of survey IDs to be
 * applied to an "assignee user". 
 * </p>
 * 
 * <p>
 * The policy at this time is such that those without masks can see all surveys
 * in a campaign, but those with a mask can only see the surveys listed in the
 * mask. If a user has multiple masks for the same campaign, the most recent
 * mask is applied, regardless of the assigner. Masks have no association with
 * classes.
 * </p>
 *
 * @author John Jenkins
 */
public class CampaignMask implements Comparable<CampaignMask> {
	/**
	 * <p>
	 * The type of a campaign mask ID. This class allows us to change the
	 * underlying type without requiring code changes. This class is immutable.
	 * </p>
	 * 
	 * <p>
	 * Whenever the type is changed, a new, private constructor of that type
	 * should be added. Then, an extra hook should be added to the
	 * {@link #decodeString(String)} method that calls this new constructor.
	 * Finally, all of the old constructors will be modified to convert their
	 * type to this new type. This way, all previous versions of the underlying
	 * type can be read and decoded.
	 * </p>
	 * 
	 * @author John Jenkins
	 */
	public static final class MaskId
		implements Comparable<MaskId>, Serializable {

		/**
		 * The default serial version. This should be increased if the type is
		 * changed.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The internal mask ID object defined as its underlying type.
		 */
		final UUID maskId;

		/**
		 * Default constructor. Creates a random ID for the mask ID.
		 */
		public MaskId() {
			maskId = UUID.randomUUID();
		}

		/**
		 * Copy constructor.
		 * 
		 * @param maskId
		 *        The {@link MaskId} object to copy.
		 */
		public MaskId(final MaskId maskId) {
			this.maskId = maskId.maskId;
		}

		/**
		 * Creates a new {@link MaskId} from a UUID.
		 * 
		 * @param maskId
		 *        The UUID to use to create a {@link MaskId} object.
		 * 
		 * @throws DomainException
		 *         The UUID was null.
		 */
		private MaskId(final UUID maskId) throws DomainException {
			if(maskId == null) {
				throw new DomainException("The mask ID is null.");
			}

			this.maskId = maskId;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return maskId.toString();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result =
				prime * result + ((maskId == null) ? 0 : maskId.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj == null) {
				return false;
			}
			if(!(obj instanceof MaskId)) {
				return false;
			}
			MaskId other = (MaskId) obj;
			if(maskId == null) {
				if(other.maskId != null) {
					return false;
				}
			}
			else if(!maskId.equals(other.maskId)) {
				return false;
			}
			return true;
		}

		/**
		 * Compares another mask ID to this one. Their ordering is based on the
		 * ordering for strings.
		 * 
		 * @param other
		 *        The other mask ID.
		 * 
		 * @return Returns a negative number if this ID is lexicographically
		 *         less than the other ID, a positive number if this ID is
		 *         lexicographically greater than the other ID, or 0 if they
		 *         are the same.
		 */
		@Override
		public int compareTo(MaskId other) {
			return maskId.toString().compareTo(other.maskId.toString());
		}

		/**
		 * Converts the string representation of a mask ID to a concrete
		 * {@link MaskId} object.
		 * 
		 * @param maskId
		 *        The string representation of the mask ID.
		 * 
		 * @return The concrete {@link MaskId} object.
		 * 
		 * @throws DomainException
		 *         The given string could not be decoded into a {@link MaskId}
		 *         object.
		 */
		public static MaskId decodeString(final String maskId)
			throws DomainException {

			if(maskId == null) {
				throw new DomainException("The mask ID is null.");
			}

			try {
				return new MaskId(UUID.fromString(maskId));
			}
			catch(IllegalArgumentException e) {
				// It is not a UUID.
			}

			throw new DomainException("The mask ID is not properly formatted.");
		}
	}

	/**
	 * Builder class for {@link CampaignMask} objects.
	 * 
	 * @author John Jenkins
	 */
	public static class Builder {
		// The unique identifier for this mask.
		MaskId maskId = null;
		// The creation timestamp of this mask.
		DateTime creationTime = null;

		// The ID of the user assigning this mask.
		String assignerUserId = null;
		// The ID of the user to which this mask applies.
		String assigneeUserId = null;

		// The unique identifier for the corresponding campaign ID.
		String campaignId = null;
		// The map of survey-to-prompts in the campaign to which the mask
		// belongs.
		Map<String, Set<String>> promptIds = null;

		/**
		 * Creates an empty builder
		 */
		public Builder() {
			// Does nothing.
		}

		/**
		 * Returns maskId.
		 * 
		 * @return The maskId.
		 */
		public MaskId getMaskId() {
			return maskId;
		}

		/**
		 * Sets the maskId.
		 * 
		 * @param maskId
		 *        The maskId to set.
		 */
		public void setMaskId(final MaskId maskId) {
			this.maskId = maskId;
		}

		/**
		 * Returns creationTime.
		 * 
		 * @return The creationTime.
		 */
		public DateTime getCreationTime() {
			return creationTime;
		}

		/**
		 * Sets the creationTime.
		 * 
		 * @param creationTime
		 *        The creationTime to set.
		 */
		public void setCreationTime(final DateTime creationTime) {
			this.creationTime = creationTime;
		}

		/**
		 * Returns assignerUserId.
		 * 
		 * @return The assignerUserId.
		 */
		public String getAssignerUserId() {
			return assignerUserId;
		}

		/**
		 * Sets the assignerUserId.
		 * 
		 * @param assignerUserId
		 *        The assignerUserId to set.
		 */
		public void setAssignerUserId(final String assignerUserId) {
			this.assignerUserId = assignerUserId;
		}

		/**
		 * Returns assigneeUserId.
		 * 
		 * @return The assigneeUserId.
		 */
		public String getAssigneeUserId() {
			return assigneeUserId;
		}

		/**
		 * Sets the assigneeUserId.
		 * 
		 * @param assigneeUserId
		 *        The assigneeUserId to set.
		 */
		public void setAssigneeUserId(final String assigneeUserId) {
			this.assigneeUserId = assigneeUserId;
		}

		/**
		 * Returns campaignId.
		 * 
		 * @return The campaignId.
		 */
		public String getCampaignId() {
			return campaignId;
		}

		/**
		 * Sets the campaignId.
		 * 
		 * @param campaignId
		 *        The campaignId to set.
		 */
		public void setCampaignId(final String campaignId) {
			this.campaignId = campaignId;
		}

		/**
		 * Returns promptIds.
		 * 
		 * @return The promptIds.
		 */
		public Map<String, Set<String>> getPromptIds() {
			return promptIds;
		}

		/**
		 * Sets the promptIds.
		 * 
		 * @param promptIds
		 *        The promptIds to set.
		 */
		public void setPromptIds(final Map<String, Set<String>> promptIds) {
			this.promptIds = promptIds;
		}

		/**
		 * Adds a prompt ID to the list of prompt IDs for a survey ID.
		 * 
		 * @param surveyId
		 *        The survey ID to which the prompt belongs.
		 *        
		 * @param promptId
		 *        The prompt ID in the survey.
		 */
		public void addPromptId(final String surveyId, final String promptId) {
			if(promptIds == null) {
				promptIds = new HashMap<String, Set<String>>();
			}
			
			Set<String> prompts = promptIds.get(surveyId);
			if(prompts == null) {
				prompts = new HashSet<String>();
				promptIds.put(surveyId, prompts);
			}
			prompts.add(promptId);
		}

		/**
		 * Adds all of the prompt IDs to the list of survey IDs.
		 * 
		 * @param surveyId
		 *        The survey ID to which the prompts belong.
		 *
		 * @param promptIds
		 *        The prompt IDs to add to the list.
		 */
		public void addPromptIds(
			final String surveyId,
			final Collection<String> promptIds) {
			
			if(this.promptIds == null) {
				this.promptIds = new HashMap<String, Set<String>>();
			}

			Set<String> prompts = this.promptIds.get(surveyId);
			if(prompts == null) {
				prompts = new HashSet<String>();
				this.promptIds.put(surveyId, prompts);
			}
			prompts.addAll(promptIds);
		}

		/**
		 * Builds the {@link CampaignMask} object.
		 * 
		 * @return The {@link CampaignMask} object based on the current state
		 *		   of this builder.
		 *
		 * @throws DomainException One of the fields was invalid.
		 */
		public CampaignMask build() throws DomainException {
			return 
				new CampaignMask(
					maskId,
					creationTime,
					assignerUserId,
					assigneeUserId,
					campaignId,
					promptIds);
		}
	}

	/**
	 * The unique identifier for this mask.
	 */
	private final MaskId maskId;
	/**
	 * The creation timestamp of this mask.
	 */
	private final DateTime creationTime;

	/**
	 * The ID of the user assigning this mask.
	 */
	private final String assignerUserId;
	/**
	 * The ID of the user to which this mask applies.
	 */
	private final String assigneeUserId;

	/**
	 * The unique identifier for the corresponding campaign ID.
	 */
	private final String campaignId;
	/**
	 * The map of survey IDs to their prompt IDs that are part of the mask.
	 */
	private final Map<String, Set<String>> promptIds;

	/**
	 * Creates a new campaign mask.
	 * 
	 * @param maskId
	 *        A unique identifier for this mask. If null, a random identifier
	 *        will be generated.
	 * 
	 * @param creationTime
	 *        The creation time-stamp for this mask. If null, the current
	 *        time-stamp will be used.
	 * 
	 * @param assignerUserId
	 *        The user ID of the user that is assigning this mask to another
	 *        user.
	 * 
	 * @param assigneeUserId
	 *        The user ID of the user to which this mask applies.
	 * 
	 * @param campaignId
	 *        The unique identifier of the campaign being masked.
	 * 
	 * @param surveyIds
	 *        A map of survey IDs to prompt IDs that will be visible to the
	 *        user. Every set associated with a survey must be non-null,
	 *        non-empty as the user must be able to see at least one prompt.
	 * 
	 * @throws DomainException
	 *         One of the parameters was invalid.
	 */
	public CampaignMask(
		final MaskId maskId,
		final DateTime creationTime,
		final String assignerUserId,
		final String assigneeUserId,
		final String campaignId,
		final Map<String, Set<String>> promptIds)
		throws DomainException {

		// Validate and assign the assigner's user ID.
		if(assignerUserId == null) {
			throw new DomainException("The assigner's user ID is null.");
		}
		else {
			this.assignerUserId = assignerUserId;
		}

		// Validate and assign the assignee's user ID.
		if(assigneeUserId == null) {
			throw new DomainException("The assignee's user ID is null.");
		}
		else {
			this.assigneeUserId = assigneeUserId;
		}

		// Validate and assign the campaign ID.
		if(campaignId == null) {
			throw new DomainException("The campaign ID is null.");
		}
		else {
			this.campaignId = campaignId;
		}

		// Every set must be non-empty.
		this.promptIds = new HashMap<String, Set<String>>();
		for(String surveyId : promptIds.keySet()) {
			Set<String> promptIdsSet = promptIds.get(surveyId); 
			if(promptIdsSet.isEmpty()) {
				throw
					new DomainException(
						"The prompt IDs for a survey are empty.");
			}
			
			promptIds.put(surveyId, Collections.unmodifiableSet(promptIdsSet));
		}

		// If the mask ID is null, create a random one. Otherwise, use the
		// given one.
		if(maskId == null) {
			this.maskId = new MaskId();
		}
		else {
			this.maskId = maskId;
		}

		// If the creation time-stamp is null, user the current time-stamp.
		// Otherwise, use the given one.
		if(creationTime == null) {
			this.creationTime = new DateTime();
		}
		else {
			this.creationTime = creationTime;
		}
	}
	
	/**
	 * Returns the ID.
	 *
	 * @return The ID.
	 */
	public MaskId getId() {
		return maskId;
	}

	/**
	 * Returns the creation time.
	 *
	 * @return The creation time.
	 */
	public DateTime getCreationTime() {
		return creationTime;
	}

	/**
	 * Returns the assigner's username.
	 *
	 * @return The assigner's username.
	 */
	public String getAssignerUserId() {
		return assignerUserId;
	}

	/**
	 * Returns the assignee's username.
	 *
	 * @return The assignee's username.
	 */
	public String getAssigneeUserId() {
		return assigneeUserId;
	}

	/**
	 * Returns the campaign's ID.
	 *
	 * @return The campaign's ID.
	 */
	public String getCampaignId() {
		return campaignId;
	}

	/**
	 * Returns an unmodifiable map of survey IDs to their prompt IDs.
	 * 
	 * @return An unmodifiable map of survey IDs to their prompt IDs.
	 */
	public Map<String, Set<String>> getSurveyPromptMap() {
		return Collections.unmodifiableMap(promptIds);
	}

	/**
	 * Defines the sort-order for {@link CampaignMask} objects. They are first
	 * sorted by time and, if two masks were created at the same time, they are
	 * sorted by their ID.
	 * 
	 * @param other The {@link CampaignMask} object to compare to this
	 * 				{@link CampaignMask}.
	 * 
	 * @return Returns a negative number if this mask was created before the
	 *         other mask, a positive number if this mask was create after the
	 *         other mask, or, if the two masks were created at the same time,
	 *         the IDs are used for comparison.
	 */
	@Override
	public int compareTo(CampaignMask other) {
		int timeComparison =
			new Long(creationTime.getMillis()).compareTo(other.creationTime
				.getMillis());

		if(timeComparison == 0) {
			return maskId.compareTo(other.maskId);
		}
		else {
			return timeComparison;
		}
	}
}