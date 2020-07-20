import DataReading.Worker;
import TwitterReader.TwitterReader;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    static Scanner reader = new Scanner(System.in);
    public static void main(String[] args) throws IOException {
        System.out.println("____ WELCOME TO TWITTER READER ____");
        showGuideThrough();
        showMenue();
    }

    private static void showGuideThrough() {
        System.out.println("Type in your search phrase (e.g. bitcoin)");
        String searchPhrase = reader.nextLine();
        System.out.println("Save results in database (d) or CSV (c)");
        String userInput =  reader.nextLine();
        boolean database = userInput.equals("d");
        if(database){
            Worker.saveToDatabase = true;
            System.out.println("Please type in your table name (e.g. tweets)");
            Worker.tableName = reader.nextLine();
        }
        startReader(searchPhrase);
    }

    private static void showMenue() throws IOException {
        while(1 == 1){

            String input = reader.next();
            if(input.contains("--phrase=")){
                System.out.println("Start: search");
                startReader(input.split("=")[1]);
            }else if(input.contains("--database=")){
                System.out.println("Start: database");
                boolean saveToDb = input.split("=")[1].equals("true");
                Worker.saveToDatabase = saveToDb;
                if(saveToDb){
                    System.out.println("Please type in your table name: ");
                    Worker.tableName =  reader.nextLine();
                }
            }else{
                System.out.println("no command found");
            }
        }
    }

    private static void startReader(String search){
        System.out.println("Enter which reader you would like to execute: \n 1 for LiveReader \n 2 for HistoriousReader");
        String readerInput = reader.nextLine();
        System.err.println("___ STARTING READER ___");
        TwitterReader tr = new TwitterReader();
        Worker w = new Worker();
        tr.addListener(w);

        switch (readerInput){
            case "1":
                tr.startLiveReader(search);
                break;
            case "2":
                System.out.println("Which date would you like to start with. (e.g YYYY-MM-DD)");
                String start = reader.nextLine();
                System.out.println("Which date would you like to end with. (e.g YYYY-MM-DD)");
                String end = reader.nextLine();
                tr.startHistoriousReading(
                        start,
                        end,
                        search,
                        ""
                );
                break;
            default:
                break;
        }
    }


}
