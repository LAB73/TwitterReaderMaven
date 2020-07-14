package TwitterReader;

import java.util.List;

public interface TweetListener {
		void tweetUpdate(List<Tweet> list);
}
