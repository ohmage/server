package edu.ucla.cens.awserver.domain;

/**
 * Immutable bean-style wrapper for configuration keys: campaign name-version pairs.
 * 
 * @author selsky
 */
public class CampaignNameVersion implements Comparable<CampaignNameVersion> {
	private String _campaignName;
	private String _campaignVersion;
	
	public CampaignNameVersion(String campaignName, String campaignVersion) {
		_campaignName = campaignName;
		_campaignVersion = campaignVersion;
	}

	public String getCampaignName() {
		return _campaignName;
	}

	public String getCampaignVersion() {
		return _campaignVersion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((_campaignName == null) ? 0 : _campaignName.hashCode());
		result = prime
				* result
				+ ((_campaignVersion == null) ? 0 : _campaignVersion.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CampaignNameVersion other = (CampaignNameVersion) obj;
		if (_campaignName == null) {
			if (other._campaignName != null)
				return false;
		} else if (!_campaignName.equals(other._campaignName))
			return false;
		if (_campaignVersion == null) {
			if (other._campaignVersion != null)
				return false;
		} else if (!_campaignVersion.equals(other._campaignVersion))
			return false;
		return true;
	}
	
	public int compareTo(CampaignNameVersion other) {
		if(null == other) {
			throw new NullPointerException("cannot compare against null");
		}
		
		if(this.equals(other)) {
			return 0;
		}
		
		int campaignCompareResult = this.getCampaignName().compareTo(other.getCampaignName()); 
		
		if(0 == campaignCompareResult) {
			return other.getCampaignVersion().compareTo(this.getCampaignVersion());
		}
		
		return campaignCompareResult;
	}
}
