import java.io.IOException;
import java.util.Scanner;

public class Home {
    public void home() throws IOException {
        Scanner sc = new Scanner(System.in);
        UserManagement umObj = new UserManagement();
        while (true) {
            System.out.println("Query (1)\nLog out (2)");
            String option = sc.nextLine();
            switch (option){
                case "2":
                    umObj.Logout();
                    break;
                default:
                    System.out.println("Not a valid option!");
            }
        }
    }
}
