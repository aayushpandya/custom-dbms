import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.nio.charset.StandardCharsets;
import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner; // Import the Scanner class to read text files



public class Main {
    public static void main(String[] args) {
        try {
//            Scanner sc = new Scanner(System.in);
//            UserManagement umObj = new UserManagement();
//
//            while (true) {
//                System.out.println("Choose an option:\nLogin (1)\nSign Up (2)");
//                String option = sc.nextLine();
//                switch (option){
//                    case "1":
//                        umObj.Login();
//                        break;
//                    case "2":
//                        umObj.SignUp();
//                        break;
//                    default:
//                        System.out.println("Not a valid option!");
//                }
//            }
            QueryManagement qb = new QueryManagement();
           // String q = "select * from people where id > 0 and lastname = \"Pandya\" limit 1";
//            String q = "select * from people where firstname = \"Aayush\";";
//            String q = "select * from people where lastname = \"Pandya\" or id = 3;";
            //String q = "select * from people where id = 3;";
            //String q = "select id, first, last from users where id = 1 and id = 2 limit 2;";

            //String q = "CREATE TABLE person (id int NOT NULL, firstname varchar(10), lastname varchar(5), PRIMARY KEY (id));";

           // String q = "INSERT INTO person (id, firstname, lastname) VALUES (1, \"Aayush\", \"Pandya\");";
            //String q = "INSERT INTO person (id, firstname, lastname) VALUES (1, \"Aayush\", \"Pandya\";";
           // String q = "INSERT INTO person (id) VALUES (\"avc\");";
//           String q = "INSERT INTO person VALUES (7,\"Aayush\",\"abcd\");";
//            String q = "INSERT INTO person (id,firstname) VALUES (5,\"root\");";
            //String q = "INSERT INTO person  VALUES (10,null,null);";
           // String q = "drop table person;";
            //String q = "create database test";
            //String q = "drop database test";
            //String q = "truncate table person";
//            String q = "DELETE FROM person WHERE firstnameaw = \"aayush\" and lastname = \"abcd\";";
           // String q = "update person set lastname = \"pandya\", firstname = \"aayush\" where id = 1 and firstname = \"abcd1\"";

            qb.parseQuery(q.toLowerCase());
//            String x = "INSERT INTO person VALUES (\"aaa\",\"Aayush\",\"Pandya\");";
//            qb.parseQuery(x.toLowerCase());
           // qb.executeDropDatabaseQuery("testdb");
//            QueryManagement qb = new QueryManagement();
//            String[][] data = { {"id", "firstName", "lastName"}, {"1", "Aayush", "Pandya"}, {"2", "root123513", "rootd14d14d1d41414414"} };
//            qb.displayDataTable(data);
           // System.out.println(qb.getAllTableNames("testdb").toString());


        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}

