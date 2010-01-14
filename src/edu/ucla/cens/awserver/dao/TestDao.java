package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.datatransfer.AwRequest;

/**
 * DAO for running a simple test query against the database. Not for production use, just a simple test of accessing our 
 * database using a JDBC datasource and the Spring Framework's JDBC classes.
 * 
 * @author selsky
 */
public class TestDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(TestDao.class);
	private String _testSelect = "select legend_text, question_text from prompt;";
	
	/**
	 * 
	 */
	public TestDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Runs the test query and places the results in the passed-in Map.
	 */
	public void execute(AwRequest request) {
		_logger.info("executing test query");
		JdbcTemplate template = new JdbcTemplate(getDataSource());
		
		try {
			
			request.setAttribute("results", template.query(_testSelect, new QueryRowMapper()));
			
		} catch (org.springframework.dao.DataAccessException dae) {
			
			throw new DataAccessException(dae); // wrap the Spring exception and re-throw in order to avoid outside dependencies
			                                    // on the Spring Exception
		}
	}

	/**
	 * Maps a query ResultSet to a TestResult object. Used by JdbcTemplate. 
	 * 
	 * @author selsky
	 */
	public class QueryRowMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException { // The Spring classes will wrap this exception
			                                                                 // in a Spring DataAccessException
			
			String legendText = rs.getString(1);
			String questionText = rs.getString(2);
			
			TestResult tr = new TestResult();
			tr.setLegendText(legendText);
			tr.setQuestionText(questionText);
			tr.setRowNum(rowNum);
			
			return tr;
		}
	}
	
	/**
	 * Container used for query results.
	 * 
	 * @author selsky
	 */
	public class TestResult {
		private String _legendText;
		private String _questionText;
		private int _rowNum;
		
		public String getLegendText() {
			return _legendText;
		}
		public void setLegendText(String legendText) {
			_legendText = legendText;
		}
		public String getQuestionText() {
			return _questionText;
		}
		public void setQuestionText(String questionText) {
			_questionText = questionText;
		}
		public int getRowNum() {
			return _rowNum;
		}
		public void setRowNum(int rowNum) {
			_rowNum = rowNum;
		}
	}
}
