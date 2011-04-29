package edu.ucla.cens.awserver.service;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.CampaignReadAwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * @author selsky
 */
public class RetrieveCampaignStringToListParameterConverterService implements Service {
	
	public void execute(AwRequest awRequest) {
		CampaignReadAwRequest req = (CampaignReadAwRequest) awRequest;
		
		req.setCampaignUrnList(StringUtils.splitCommaSeparatedString(req.getCampaignUrnListAsString()));
		req.setClassUrnList(StringUtils.splitCommaSeparatedString(req.getClassUrnListAsString()));
	}
}
