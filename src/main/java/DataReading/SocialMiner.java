/*
package Main.DataReading;
*/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import Main.TwitterReader.TwitterReader.TwitterReader.Tweet;
import Main.TwitterReader.TwitterReader.TwitterReader.TweetListener;
import Main.TwitterReader.TwitterReader.TwitterReader.TwitterReader;

*/
/**
 *
 * @author Torsten Welsch
 *
 *//*

public class SocialMiner implements TwitterReader.TweetListener {

    public enum Source {
        TWITTER, STOCKTWITS, REDDIT
    }

    private final List<Source> sources;
    private String phrase;
    private LocalDateTime start;
    private LocalDateTime end;
    private File configFile;
    private boolean normalize;
    private TwitterReader.TwitterReader twitterReader;
    private List<SocialMinerAdapter> adapters;
    private File normalizeConfigFile;

    public static class Builder {
        private final List<Source> sources;
        private List<SocialMinerAdapter> adapters;

        private String phrase;
        private LocalDateTime start;
        private LocalDateTime end;
        private File configFile;
        private boolean normalize;
        private File normalizeConfigFile;

        public Builder() {
            this.sources = new LinkedList<>();
            this.adapters = new LinkedList<>();
        }

        */
/**
         * Adds a social-media source for mining.
         *
         * @param source
         *            Social-media source (Twitter, StockTwits, Reddit, ...)
         * @return Builder
         *//*

        public Builder searchSource(Source source) {
            sources.add(source);
            return this;
        }

        */
/**
         * Sets the phrase that is used as search term for the mining.
         *
         * @param phrase
         *            Search phrase
         * @return Builder
         *//*

        public Builder forPhrase(String phrase) {
            this.phrase = phrase;
            return this;
        }

        */
/**
         * Adds an Adapter through which the social data can be read
         *
         * @param Adapter
         *            which implements the SocialMinerAdapter
         * @return Builder
         *//*

        public Builder addAdapter(SocialMinerAdapter adapter) {
            adapters.add(adapter);
            return this;
        }

        */
/**
         * Sets the time interval from which the mined data should come from.
         *
         * @param start
         *            Start date (and time).
         * @param end
         *            End date (and time). Null is allowed.
         * @return Builder
         *//*

        public Builder inInterval(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
            return this;
        }

        */
/**
         * Sets the path to the configuration file.
         *
         * This method could be replaced by methods for configuration parameters later.
         *
         * @param configFile
         *            Configuration File
         * @return Builder
         *//*

        public Builder withConfigFile(File configFile) {
            this.configFile = configFile;
            return this;
        }

        */
/**
         * The mined data is normalized to match the universal output format.
         *
         * @param configFile
         *            for normalization
         * @return Builder
         *//*

        public Builder normalizeData(File configFile) {
            this.normalize = true;
            this.normalizeConfigFile = configFile;
            return this;
        }

        public SocialMiner build() {
            SocialMiner miner = new SocialMiner();
            if (sources.isEmpty())
                throw new IllegalArgumentException("no source selected");
            miner.sources.addAll(sources);
            if (phrase == null)
                throw new IllegalArgumentException("no phrase given");
            miner.phrase = this.phrase;
            if (start == null || end == null)
                throw new IllegalArgumentException("no start/end date specified");
            miner.start = this.start;
            miner.end = this.end;
            if (configFile == null)
                throw new IllegalArgumentException("no config file given");
            miner.configFile = this.configFile;
            if (adapters.isEmpty())
                throw new IllegalArgumentException("no output specified");
            if (normalize && normalizeConfigFile == null)
                throw new IllegalArgumentException("No config file for normalization given");
            miner.normalizeConfigFile = normalizeConfigFile;
            miner.adapters = this.adapters;
            miner.normalize = this.normalize;
            return miner;
        }
    }

    private SocialMiner() {
        this.sources = new LinkedList<>();
    }

    public void run() {

        // load config file
        HashMap<String, String> config = loadConf();

        // set config parameters
        if (config.containsKey("phrase"))
            phrase = config.get("phrase");
        if (config.containsKey("normalize"))
            normalize = Boolean.parseBoolean(config.get("normalize"));

        // start source crawlers, using the given search phrase and time interal
        String startDateString = start.format(DateTimeFormatter.BASIC_ISO_DATE);
        String endDateString = end.format(DateTimeFormatter.BASIC_ISO_DATE);

        if (sources.contains(Source.TWITTER)) {
            twitterReader = new TwitterReader.TwitterReader();
            twitterReader.addListener(this);
            twitterReader.startHistoriousReading(startDateString, endDateString, phrase, null);

        }

        // normalize the data if necessary

        // show a progress bar or some fancy console output :-)

    }

    private HashMap<String, String> loadConf() {
        try {
            List<String> lines = Files.readAllLines(configFile.toPath());
            HashMap<String, String> conf = new HashMap<>();
            lines.forEach(x -> {
                if (!x.startsWith("#") && x.contains("=")) {
                    String[] temp = x.split("=");
                    conf.put(temp[0], temp[1]);
                }
            });
            return conf;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void tweetUpdate(List<TwitterReader.Tweet> list) {
        List<DataModel> normalizedData = null;
        if (normalize) {
            normalizedData = normalize(list);
        }
        for (SocialMinerAdapter adapter : adapters) {
            adapter.twitterUpdate(list);
            if (normalize)
                adapter.normalizedUpdate(normalizedData);

        }
    }

    private <T> List<DataModel> normalize(List<T> list) {
        List<DataModel> normalizedData = new ArrayList<>();

        for (Object raw : list) {
            DataModel temp = new DataModel();
            PreProcessing.fillDataModel(temp, raw, configFile);
            normalizedData.add(temp);
        }
        return normalizedData;
    }

    //PRE-Processing
    public static void fillDataModel(DataModel d, Object o,File configFile) {
        if(configFile!=null)
        {
            Map<String, Object> sndConfig = readConfigFile(configFile);
            config = sndConfig.keySet().size()==0?config:sndConfig;
        }
        if (o instanceof TwitterReader.Tweet) {
            fillModel((TwitterReader.Tweet) o, d);
        }

        d.setOriginalData(d.getData());
    }

    private static void fillModel(TwitterReader.Tweet o, DataModel d) {
        d.setData(o.getText());
        if ((String) config.get("calcTwitterScore") != null) {
            String calcString = (String) config.get("calcTwitterScore");
            Function calcTwitterScore = new Function(calcString);
            ArrayList<Argument> argumentList = new ArrayList<Argument>();
            Expression ex;
            if (calcString.contains("Retweets")) {
                argumentList.add(new Argument("Retweets=" + o.getRetweets()));
            }
            if (calcString.contains("Likes")) {
                argumentList.add(new Argument("Likes=" + o.getLikes()));
            }
            if (calcString.contains("Replies")) {
                argumentList.add(new Argument("Replies=" + o.getReplies()));
            }
            if(argumentList.size()>0)
            {
                calculateReachScore(d, calcTwitterScore, argumentList);
            }else
            {
                d.setReachScore(o.getRetweets());
            }
        } else {
            d.setReachScore(o.getRetweets());
        }
        d.setDate(o.getDate());
    }

}
*/
