package DataReading;

import Database.SQLManager;
import TwitterReader.Tweet;
import TwitterReader.TweetListener;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class Worker implements TweetListener {

    public static boolean saveToDatabase = false;
    public static String tableName = "tweets";
    private SQLManager manager;
    public Worker() {
        manager = new SQLManager();
    }

    @Override
    public void tweetUpdate(List<Tweet> list){
        if(saveToDatabase){
            saveToDatabase(list);
        }else{
            saveCSV(list);
        }
    }

    private void saveToDatabase(List<Tweet> list) {
        for (Tweet tweet: list) {
            manager.insert(tweet, tableName);
        }

    }

    public void saveCSV(List<Tweet> tweetList){
        FileWriter fileWriter = null; //Set true for ap
        try {
            fileWriter = new FileWriter("twitterResults.csv", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert fileWriter != null;
        try (PrintWriter pw = new PrintWriter(fileWriter)) {
            tweetList.stream()
                    .map(Tweet::toString)
                    .forEach(pw::println);
        }
    }
}
