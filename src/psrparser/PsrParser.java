/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package psrparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Manik
 */
public class PsrParser {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String mhtFile = "C:\\Users\\Manik\\Desktop\\Problem_20151125_1549.mht";
        List parts = getParts(mhtFile);
        String firstPart = parts.get(0).toString();
        String secondPart = parts.get(1).toString();
        String toinsertone = "<table border=\"1\">\n"
                + "	<tr>\n"
                + "	<td>\n"
                + "	<b>Data Model Chages: </b>\n"
                + "	</td>\n"
                + "	</tr>\n"
                + "	<tr>\n"
                + "	<td>\n"
                + "	<font color=\"red\">";
        String toinserttwo = "</font>\n"
                + "	</td>\n"
                + "	</tr>\n"
                + "	</table>\n";
        String finalStr = "";
        String strtofind = "<p ID=\"ProblemStepP\">";
        int ctr = 0;
        int i = firstPart.indexOf(strtofind);
        while (i >= 0) {
            ctr++;
            i = firstPart.indexOf(strtofind, i + 1);
        }
        ctr = ctr / 3;
        i = 0;
        int begin = 0;
        int end = 0;
        int loopctr = 0;
        i = firstPart.indexOf(strtofind);
        while (i >= 0) {
            end = i + strtofind.length();
            String substr = firstPart.substring(begin, end);
            String actionTime = getDate(firstPart.substring(i, i + 100));
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
            Date date;
            try {
                date = formatter.parse(actionTime);
                String capturedTime = formatter.format(date);
                finalStr = finalStr + substr + toinsertone + getMatchedData("localhost", "loggertest", "sa", "nepal@123", "1433", "orcl", capturedTime) + toinserttwo;
            } catch (ParseException ex) {
                Logger.getLogger(PsrParser.class.getName()).log(Level.SEVERE, null, ex);
            }

            i = firstPart.indexOf(strtofind, i + 1);
            begin = end;
            loopctr++;
            if (loopctr == ctr) {
                i = -1;
            }
        }
        finalStr = finalStr + firstPart.substring(begin, firstPart.length());
        String towrite = finalStr + secondPart;
        writeMht(towrite);
    }

    public static List getParts(String fname) {
        FileInputStream fis = null;
        List MainTables = new ArrayList();
        try {
            File file = new File(fname);
            fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            String str = new String(data, "UTF-8");
            int lastindex = str.indexOf("Content-Type: image/jpeg");
            String firstPart = str.substring(0, lastindex - 55);
            String lastPart = str.substring(lastindex - 54, str.length());
            MainTables.add(firstPart);
            MainTables.add(lastPart);
            return MainTables;
        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                return null;
            }
        }
    }

    public static void writeMht(String text) {
        try (PrintStream out = new PrintStream(new FileOutputStream("C:\\Users\\Manik\\Desktop\\HAHAHAHA.mht"))) {
            out.print(text);
        } catch (FileNotFoundException ex) {
            System.out.println("Error");
        }
    }

    public static String getDate(String text) {
        String re1 = ".*?";	// Non-greedy match on filler
        String re2 = "(\\(.*\\))";	// Round Braces 1

        Pattern p = Pattern.compile(re1 + re2, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(text);
        if (m.find()) {
            String rbraces1 = m.group(1);
            return rbraces1.toString().substring(1, rbraces1.toString().length() - 1);
        }
        return null;
    }

    public static String getMatchedData(String DB_URL, String DB_NAME, String USER, String PASS, String PORT, String SID, String caputuredDate) {
        try {
            String htmlDate = caputuredDate;
            String MSSQL_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            Connection conn = null;
            String CONSTRING = "";
            CONSTRING = "jdbc:sqlserver://" + DB_URL + ":" + PORT + ";databaseName=" + DB_NAME + ";user=" + USER + ";password=" + PASS;
            Class.forName(MSSQL_DRIVER);
            conn = DriverManager.getConnection(CONSTRING, USER, PASS);
            try {
                Statement stmt = null;
                stmt = conn.createStatement();
                String query = "select * from ZZZ_TMP_COL_UPDATE";
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    Date date;
                    try {
                        SimpleDateFormat formatter = new SimpleDateFormat("y-M-d HH:mm:ss");
                        date = formatter.parse(rs.getString(1));
                        SimpleDateFormat sdfDestination = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
                        String updatedTime = sdfDestination.format(date);
                        // System.out.println(caputuredDate + " " + updatedTime);
                        if (caputuredDate.equals(updatedTime)) {
                            caputuredDate = caputuredDate + " <br> " + rs.getString(2);
                        }
                    } catch (ParseException ex) {
                        System.out.println("Errir");
                        return caputuredDate;
                    }

                }
            } catch (SQLException ex) {
                System.out.println("SQL Error");
                return caputuredDate;
            }
            conn.close();
            String insertedtable = getMatchedDataInsert("localhost", "loggertest", "sa", "nepal@123", "1433", "orcl", htmlDate);
            return caputuredDate + insertedtable;
        } catch (ClassNotFoundException ex) {
            return caputuredDate;

        } catch (SQLException ex) {
            return caputuredDate;
        }

    }

    public static String getMatchedDataInsert(String DB_URL, String DB_NAME, String USER, String PASS, String PORT, String SID, String caputuredDate) {
        try {

            String updatedTable = "<br > Updated Tables: ";
            String MSSQL_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            Connection conn = null;
            String CONSTRING = "";
            CONSTRING = "jdbc:sqlserver://" + DB_URL + ":" + PORT + ";databaseName=" + DB_NAME + ";user=" + USER + ";password=" + PASS;
            Class.forName(MSSQL_DRIVER);
            conn = DriverManager.getConnection(CONSTRING, USER, PASS);
            Statement stmt = conn.createStatement();
            String query = "select table_name from information_schema.tables where table_name like 'ZZZ_TMP%'";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String tableName = rs.getString(1);
                Statement stmttblcnt = conn.createStatement();
                String queryTblCount = "select count(*) from " + tableName;
                System.out.println(queryTblCount);
                ResultSet rstblcnt = stmttblcnt.executeQuery(queryTblCount);
                while (rstblcnt.next()) {
                    if (!"0".equals(rstblcnt.getString(1))) {
                        Statement stmtDataLevel = conn.createStatement();
                        String queryDataLevel = "select * from " + tableName;
                        ResultSet rsDataLevel = stmtDataLevel.executeQuery(queryDataLevel);
                        while (rsDataLevel.next()) {
                            SimpleDateFormat formatter = new SimpleDateFormat("y-M-d HH:mm:ss");
                            Date date = formatter.parse(rsDataLevel.getString("TS"));
                            SimpleDateFormat sdfDestination = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
                            String updatedTime = sdfDestination.format(date);
                            if (caputuredDate.equals(updatedTime)) {
                                updatedTable = " " + updatedTable + "  <br> " + tableName;
                            }
                        }
                    }
                }

            }
            if (updatedTable.contentEquals("<br > Updated Tables: ")) {
                System.out.println("111");
                return "";
            } else {
                return updatedTable;

            }
        } catch (SQLException ex) {
            System.out.println("SQL Error");
            return "";
        } catch (ClassNotFoundException ex) {
            return "";
        } catch (ParseException ex) {
            return "";
        }

    }
}
