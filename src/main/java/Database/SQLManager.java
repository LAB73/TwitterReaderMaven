package Database;

import TwitterReader.Tweet;

import java.sql.*;


public class SQLManager {
    private Connection c = null;

    public SQLManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:./test.db");
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    public void createTableIfNotExists(String tableName){
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (\n"
                + "	tweetPath varchar(255),\n"
                + "	likes int,\n"
                + "	retweets int,\n"
                + "	replies int,\n"
                + "	date text,\n"
                + "	text varchar(255)\n"
                + ");";

        try {
            Statement stmt = c.createStatement();
            stmt.executeUpdate(sql);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean insert(Tweet tweet, String tableName){
        String sql = "INSERT INTO " + tableName + " values('" +
                tweet.getTweetPath() + "'," +
                tweet.getLikes() + "," +
                tweet.getRetweets() + "," +
                tweet.getReplies() + ",'" +
                tweet.getDate() + "','" +
                tweet.getText() + "')";
        try {
            Statement stmt = c.createStatement();
            stmt.executeUpdate(sql);
            return true;
        }catch (Exception e){
            createTableIfNotExists(tableName); //first tweet lost !!!!
            return false;
        }
    }

    private String showTable(String table) throws SQLException {
        String sql = "SELECT * FROM " + table;
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnsNumber = rsmd.getColumnCount();

        StringBuilder result = new StringBuilder();
        while(rs.next()){
            for (int i = 1; i <= columnsNumber; i++) {
                if (i > 1) result.append(",  ");
                String columnValue = rs.getString(i);
                result.append(columnValue).append(" ").append(rsmd.getColumnName(i));
            }
            result.append("\n");
        }
        return result.toString();
    }


}
