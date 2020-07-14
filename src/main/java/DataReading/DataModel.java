package DataReading;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import java.util.Date;


@Entity("NormalizedData")
public class DataModel {

	@Property("id")
	@Id
	private ObjectId id;
	public DataModel(String q, String version,String originalData) {
		this.queryText = q;
		this.version = version;
		this.originalData = originalData;
	}
	
	
	
	public DataModel() {};
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	private String data;
	private double reachScore;
	private String originalData;
	
	
	public String getQueryText() {
		return queryText;
	}

	public void setQueryText(String queryText) {
		this.queryText = queryText;
	}
	@Embedded
	private Date date;
	private String queryText;
	private String version;
	
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getOriginalData()
	{
		return originalData;
	}
	
	public void setOriginalData(String originalData)
	{
		this.originalData = originalData;
	}
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public double getReachScore() {
		return reachScore;
	}
	public void setReachScore(double reachScore) {
		this.reachScore = reachScore;
	}
	
}
