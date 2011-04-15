package edu.ucla.cens.awserver.service;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.RetrieveCampaignAwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * @author selsky
 */
public class RetrieveCampaignStringToListParameterConverterService implements Service {
	
	public void execute(AwRequest awRequest) {
		RetrieveCampaignAwRequest req = (RetrieveCampaignAwRequest) awRequest;
		
		req.setCampaignUrnList(StringUtils.splitCommaSeparatedString(req.getCampaignUrnListAsString()));
		req.setClassUrnList(StringUtils.splitCommaSeparatedString(req.getClassUrnListAsString()));
	}
}
