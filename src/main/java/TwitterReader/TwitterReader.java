package TwitterReader;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pmw.tinylog.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;


public class TwitterReader {

	private List<TweetListener> listeners = new ArrayList<>();
	
	private HashMap<String, Timer> liveTimerMap = new HashMap<>();
	private List<Thread> historicThreads = new ArrayList<>();
	
	private static Proxies proxies = Proxies.getInstance();
	
	public void startLiveReader(String query)
	{
	 if (liveTimerMap.containsKey(query)) return;
	 Timer temp = new Timer(query+"-Timer",true);
	 liveTimerMap.put(query, temp);
	 temp.schedule(new TimerTask() {
		@Override
		public void run() {
			String json ="";
			try {
			 json= TwitterReader.getTweets(null, null, query, "", proxies.getProxy());
			 List<Tweet> list = parseTweets(new JSONObject(json).getString("items_html"));
			 for (TweetListener listener : listeners) {
					listener.tweetUpdate(list);
				}
				System.out.println("Sending update");
			 Logger.debug("Sending update");
			}catch (Exception e) {
				Logger.error(e);
				Logger.error(json);
			}
		}
	}, 0, 20000);
	}
	public void addListener(TweetListener adder) {
		listeners.add(adder);
	}
	
	public void
	startHistoriousReading(String since, String until, String query, String minpos)
	{
		Thread temp =new Thread(new Runnable() {
			@Override
			public void run() {
			loadTweetsIntoDb(since, until, query, minpos);	
			}
		});
		temp.setName(query+"-TwitterThread");
		temp.setDaemon(true);
		temp.start();
		historicThreads.add(temp);
		Logger.debug("Starting historic Thread");
	}
	
	public void stopHistoriousReading(String name)
	{
		Thread temp = historicThreads.stream().filter(x-> x.getName().equals(name+"-TwitterThread")).findFirst().get();
		temp.stop();
		historicThreads.remove(temp);
	}
	public void stopHistorousThreads() {
		historicThreads.forEach(x-> x.stop());
	}
	public void stopTimers()
	{
		Logger.debug("Stopping Twitter Timers");
		liveTimerMap.forEach((x,y)-> y.cancel());
	}
	public void stopTimer(String query)
	{
		Logger.debug("Stopping Live Timer for query "+query);
		liveTimerMap.remove(query);
	}
	
	private void loadTweetsIntoDb(String since, String until, String query, String minposExtracted)// ca 20 tweets per stopcount
	{
		JSONObject root = null;
		String minpos;
		String json = null;
		List<Tweet> batchList = new ArrayList<>();
		if (minposExtracted == null)
			minpos = "";
		else
			minpos = minposExtracted;
		int count = 0;
		while (true) {
			try {

				boolean rateOk = false;
				String items = "";
//				String proxy = proxies.getProxy();
				String proxy = null;
				while(!rateOk) {
					
					json = TwitterReader.getTweets(since, until, query, minpos, proxy);
					
					if(json.equals("")) {
						Logger.debug("Jsonstring \"\" - shouldnt be");
//						proxies.reportTimedOut(proxy);
//						proxies.getNew();
						continue;
					}
					
					root = new JSONObject(json);
					minpos = root.getString("min_position");
					Logger.debug("Current min_pos: " + minpos);
					items = root.getString("items_html");
					if (items.length() < 10) {
//						Logger.error("Timeout by twitter retry - switching Proxy - " + proxy);
//						proxies.reportTimedOut(proxy);
//						proxy = proxies.getNew();
						continue;
					}else {
						rateOk = true;
					}
				}
				
				List<Tweet> tempList = parseTweets(items);
				if (tempList != null)
					batchList.addAll(tempList);
				else
					continue;
				if (batchList.size() >= 20) {
					for (TweetListener listener : listeners) {
						listener.tweetUpdate(batchList);
					}
					System.out.println("Sending update");

					Logger.debug("Sending update");				
					batchList.clear();
				}
				
			} catch (JSONException e) {
				Logger.error("Current min_pos: " + minpos);
				Logger.error("JSONerror");
				Logger.error(json);
				Logger.error(e);
				continue;
			}  catch (Exception e) {
				Logger.error(e);
				continue;
			}
		}

	}

	private static String getTweets(String since, String until, String query, String maxpos, String proxy) {
		
		String json = "";
		String urlString = "https://twitter.com/i/search/timeline?f=tweets&l=en&q=";
		boolean writeToDisk = true;
		String getData = ' ' + query;
		if(since!=null)getData+= " since:" + since;
		if(until!=null)getData+= " until:" + until;
		URL url;
		HttpsURLConnection.setFollowRedirects(false);
		try {

			url = new URL(urlString + URLEncoder.encode(getData, "UTF-8") + "&src=typd&max_position=" + maxpos);
			Logger.debug("Fetching tweets with URL: " + url.toString());
			HttpsURLConnection connection;
			
//			if(proxy == null) {
//				System.out.println("Proxy null - wtf");
//				return "";
//			}
//			
//			if(!proxy.startsWith("127.0.0.1")) {
//				
//				String[] proxySplit = proxy.split(":");
//				Proxy p = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxySplit[0], Integer.parseInt(proxySplit[1])));
//				connection = (HttpsURLConnection) url.openConnection(p);
//				 
//			}else {
//				
//				connection = (HttpsURLConnection) url.openConnection();
//				
//			}
			connection = (HttpsURLConnection) url.openConnection();
			
			connection.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:61.0) Gecko/20100101 Firefox/61.0");
			connection.setRequestProperty("Referer", url.toString());
			connection.setInstanceFollowRedirects(false);
			connection.setConnectTimeout(20000);
			connection.setReadTimeout(60000);
			connection.connect();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = "";
			line = reader.readLine();
			while (line != null) {
				line = line.replaceAll("\\\\n", "");
				json += line;
				line = reader.readLine();
			}

			if (writeToDisk) {
				BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));
				writer.write(json);
				writer.flush();
				writer.close();
			}
			if (!json.startsWith("{")) // falls twitter bl�dsinn zur�ck schickt
			{
				Logger.error("got no json: retry");
				return getTweets(since, until, query, maxpos, proxy);
			}
			return json;
		} catch (MalformedURLException e) {

			Logger.error("MalformedURLException");
			Logger.error(e);
			

		}catch (IOException e) {
			
			//Retry with other Proxy
			Logger.debug("Connectexeption - getting new Proxy " + e.getMessage());
			proxies.reportTimedOut(proxy); //Evtl, weil Connectexception ganz ausschlie�en? Kommt ja h�chstwahrscheinlich immer wieder bei dem Eintrag...
			getTweets(since, until, query, maxpos, proxies.getNewNoException());
			
//		} catch (IOException e) {
//			Logger.error("IOException");
//			Logger.error(e);
//			
		} catch (Exception e) {
			Logger.error(e);
		}
		return "";
	}

	private static List<Tweet> parseTweets(String input) {
		Logger.debug("Parsing tweets");
		input = input.replaceAll("\\\\u003c", "<");
		input = input.replaceAll("\\\\u003e", ">");
		input = input.replaceAll("\\\\", "");
		String s = "<html><head><title>First parse</title></head><body><p>Parsed HTML into a doc.</p></body></html>";
		Document doc = Jsoup.parse(input);
		Elements tweetContainer = doc.getElementsByClass("js-stream-item"); // doc.getElementsByClass(".js-stream-item");

		if (tweetContainer.size() == 0) {
			Logger.error("No tweets");
		} else {
			Logger.debug("Tweets Found: " + tweetContainer.size());
		}
		List<Tweet> tweetList = new ArrayList<>();
		for (Element child : tweetContainer) {
			try {
				Element tweetHeader = child.child(0);
				String permalink = tweetHeader.attr("data-permalink-path");

				Element tweetText = tweetHeader.getElementsByClass("js-tweet-text-container").first();

				Element dateElement = tweetHeader.getElementsByClass("js-short-timestamp").first();
				Date date = null;
				if (dateElement != null && dateElement.attr("data-time-ms") != null)
					date = new Date(Long.parseLong(dateElement.attr("data-time-ms")));

				Elements tweetstats = tweetHeader.getElementsByClass("ProfileTweet-actionCount");
				int replies = getCountFromTweetElement(tweetstats.get(0));// replies
				int retweets = getCountFromTweetElement(tweetstats.get(1));// retweets
				int likes = getCountFromTweetElement(tweetstats.get(2));// likes

				Tweet tweet = new Tweet(permalink, likes, retweets, replies, date, tweetText.text());
				tweetList.add(tweet);
			} catch (Exception e) {
				continue;
			}
			// System.out.println(date+" Likes: "+likes+" Retweets: "+retweets+" Replies:
			// "+replies);
		}
		return tweetList;
	}

	private static int getCountFromTweetElement(Element source) {

		return Integer.parseInt(source.attr("data-tweet-stat-count"));
	}
	
		
		
	
}
