package DataReading;

import TwitterReader.Tweet;

import java.util.List;

public interface SocialMinerAdapter {
	public void twitterUpdate(List<Tweet> list);
	public void normalizedUpdate(List<DataModel> list);
}
