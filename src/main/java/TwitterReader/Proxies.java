package TwitterReader;

import org.pmw.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Proxies {

    private static String[] proxies = new String[]{
    		"127.0.0.1:80",
        "159.65.120.163:80",
        "85.185.85.245:80",
        "68.183.133.23:80",
        "78.38.123.71:80",
        "106.0.37.10:80",
        "190.121.128.242:80",
//        "83.82.43.237:80",
        "186.179.97.210:80",
        "191.102.94.106:80"
//        "114.179.245.22:80",
//        "103.239.52.178:25",
//        "200.61.44.10:80",
////        "202.73.51.102:80",
////        "185.7.85.21:80",
//        "162.220.108.59:80",
////        "213.183.99.110:80",
////        "45.76.156.131:80",
//        "200.98.166.135:80",
////        "173.249.43.157:80",
//        "85.192.184.133:80",
//        "191.102.94.138:80",
////        "169.57.157.148:80",
//        "181.225.109.164:80",
//        "190.6.196.134:80"
    };
    
    private Map<String, Long> proxyTimeouts;
    
    String proxyFile = System.getProperty("user.dir") + "/proxies.conf";
    
    private Proxies() {
    	
    	File file = new File(proxyFile);
    	if(file.exists()) {
    		try {
				proxies = Files.readAllLines(file.toPath()).toArray(proxies);
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	
    	proxyTimeouts = Stream.of(proxies).collect(Collectors.toMap(item -> item, item -> 0L));
		
    	cursor = proxies[0];
    	
	}

    private String cursor;
    
    int faultCounter = 0;
    
    public void incFaultCounter(){
    	faultCounter++;
    }
    
    public void reportTimedOut(String proxy) {
    	
    	if(proxy != null) {
    		String key = proxyTimeouts.keySet().stream().filter(x -> x.contains(proxy)).findFirst().get();
    		
    		if(key == null) {
    			Logger.error("key null");
    		}
    		
	    	proxyTimeouts.put(key, System.currentTimeMillis());
	    }
    	
    	Logger.info(proxy + " reported Timedout");
    }
    
    long waitLimit = 1000 * 1000;
    long waitTime = 30 * 1000;

    public String getNew() throws InterruptedException {

        Entry<String, Long> proxy = proxyTimeouts.entrySet().stream()
        		.sorted((x, y) -> x.getKey().contains("127.0.0.1") ? 1 : y.getKey().contains("127.0.0.1") ? -1 : 0)
        		.sorted((x, y) -> Long.compare(x.getValue(), y.getValue()))
        		.findFirst().get();
        Logger.info("Next Proxy requested: " + proxy.getKey());
        if(proxy.getValue() != 0 && System.currentTimeMillis() - proxy.getValue() > waitLimit) {
        	Logger.info("Proxy timeout");
        	Thread.sleep(waitTime);
        }
        faultCounter = 0;
        cursor = proxy.getKey();
        reportTimedOut(cursor);
        return proxy.getKey();

    }
    
    public String getNewNoException() {
    	try {
    		return getNew();
    	}catch(InterruptedException e) {
    		Logger.error(e.getMessage());
    	}
    	return null;
    }
    
    public String getProxy() {
    	
    	return cursor;
    	
    }
    
    private static Proxies instance = null;
    
    public static Proxies getInstance(){
    	if(instance == null)
    		instance = new Proxies();
    	return instance;
    }

}
