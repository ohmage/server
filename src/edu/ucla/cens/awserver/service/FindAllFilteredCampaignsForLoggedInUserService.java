//package edu.ucla.cens.awserver.service;
//
//import java.util.List;
//
//import edu.ucla.cens.awserver.dao.Dao;
//import edu.ucla.cens.awserver.dao.DataAccessException;
//import edu.ucla.cens.awserver.request.AwRequest;
//import edu.ucla.cens.awserver.request.RetrieveCampaignAwRequest;
//
///**
// * @author selsky
// */
//public class FindAllFilteredCampaignsForLoggedInUserService extends AbstractDaoService {
//
//    public FindAllFilteredCampaignsForLoggedInUserService(Dao dao) {
//    	super(dao);
//    }
//	
//	public void execute(AwRequest awRequest) {
//		RetrieveCampaignAwRequest req = (RetrieveCampaignAwRequest) awRequest;
//		try {
//			getDao().execute(req);
//			
////			List<?> results = req.getResultList();
////			List<LoginIdUserRole> 
////			int size = results.size();
////			for(int i = 0; i < size; i++) {
////				
////				
////			}
//			
//			
//			
//		} catch (DataAccessException dae) {
//			
//			throw new ServiceException(dae);
//		}
//	}
//}
