package TwitterReader;

import java.util.Date;

public class Tweet {
	@Override
	public String toString() {
		return date.toString() + ";" + text;
	}

	String tweetPath;
	int likes;
	int retweets;
	int replies;
	Date date;
	String text;
	
	
	public Tweet(String tweetPath, int likes, int retweets, int replies, Date date, String text) {
		super();
		this.tweetPath = tweetPath;
		this.likes = likes;
		this.retweets = retweets;
		this.replies = replies;
		this.date = date;
		this.text = text;
	}
	public Tweet() {
		
	}

	public String getTweetPath() {
		return tweetPath;
	}
	public int getLikes() {
		return likes;
	}


	public int getRetweets() {
		return retweets;
	}

	public int getReplies() {
		return replies;
	}

	public Date getDate() {
		return date;
	}


	public String getText() {
		return text;
	}

		public void setTweetPath(String tweetPath) {
			this.tweetPath = tweetPath;
		}
		public void setLikes(int likes) {
			this.likes = likes;
		}
		public void setRetweets(int retweets) {
			this.retweets = retweets;
		}
		public void setReplies(int replies) {
			this.replies = replies;
		}
		public void setDate(Date date) {
			this.date = date;
		}
		public void setText(String text) {
			this.text = text;
		}
		
	
		
	
}
