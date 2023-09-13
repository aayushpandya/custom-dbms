import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import static java.lang.System.exit;

public class UserManagement {

    //region Class objects
    Logger loggerObj = new Logger();
    Home homeObj = new Home();
    //endregion

    //region Hashing Password
    public  String generateHashedPassword(String plainPassword, byte[] salt) {
        String hashedPassword = new String();
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(CommonConfig.hashingAlgorithm);
            messageDigest.update(salt);
            byte[] byteArray = messageDigest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < byteArray.length; i++) {
                stringBuilder.append(Integer.toString((byteArray[i] & 0xff) + 0x100, 16).substring(1));
            }
            hashedPassword = stringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        finally {
            return hashedPassword;
        }

    }
    //    public  byte[] generateNewSalt(){
//        SecureRandom randomString = new SecureRandom();
//        byte[] salt = new byte[16];
//        randomString.nextBytes(salt);
//        return salt;
//    }
    //endregion

    //region User Session
    public void createUserSession(String userName) throws IOException {
        FileWriter fw = new FileWriter(CommonConfig.sysSessionFileName);
        fw.write(userName);
        fw.close();
    }
    public void destroyUserSession() throws IOException {
        File fObj = new File(CommonConfig.sysSessionFileName);
        fObj.delete();
    }
    //endregion

    //region Login & Signup
    public void Login() {
        try{
            Scanner sc = new Scanner(System.in);
            System.out.printf("Login\nUsername:");
            String username = sc.nextLine();
            System.out.printf("Password:");
            String password = sc.nextLine();
            File myObj = new File(CommonConfig.sysUserFileName + CommonConfig.sysFileType);
            Scanner myReader = new Scanner(myObj);
            Boolean loggedIn = Boolean.FALSE;
            while (myReader.hasNextLine()) {
                String fileData = myReader.nextLine();
                String[] data = fileData.split(CommonConfig.delimiter);
                if(data[0].equals(username)){
//                    String hashedPassword = generateHashedPassword(password,data[4].getBytes(StandardCharsets.UTF_8));
                    String hashedPassword = generateHashedPassword(password, CommonConfig.salt);
                    if(data[1].equals(hashedPassword)) {
                        System.out.printf(data[2]+":");
                        String answer = sc.nextLine();
                        if (answer.equals(data[3])) {
                            loggedIn = Boolean.TRUE;
                        } else {
                            loggedIn = Boolean.FALSE;
                        }
                    }
                    else{
                        loggedIn = Boolean.FALSE;
                    }
                    break;
                }
            }
            myReader.close();
            if(loggedIn){
                createUserSession(username);
                loggerObj.addLog("Login", "success", username);
                homeObj.home();
            }
            else{
                loggerObj.addLog("Login", "failed", username);
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void SignUp(){
        try{
            Scanner sc = new Scanner(System.in);
            boolean check = true;
            String username = new String();
            System.out.println("Sign Up");
            while(check){
                System.out.println("Enter username:");
                username = sc.nextLine();
                check = CheckIfUserNameExists(username);
                if(check){
                    System.out.println("Username already exists!");
                }
            }



            System.out.println("Enter password:");
            String password = sc.nextLine();
            System.out.println("Enter security question:");
            String question = sc.nextLine();
            System.out.println("Enter answer:");
            String answer= sc.nextLine();

//            byte[] salt = generateNewSalt();
            String saltString = new String(CommonConfig.salt);
            String hashedPassword = generateHashedPassword(password, CommonConfig.salt);
            String newLine = username + CommonConfig.delimiter  + hashedPassword + CommonConfig.delimiter + question + CommonConfig.delimiter + answer +"\n";
            FileWriter fw = new FileWriter(CommonConfig.sysUserFileName + CommonConfig.sysFileType,true);
            fw.write(newLine);
            fw.close();
            System.out.println("User added!");
            loggerObj.addLog("Sign Up","success", username);
            Login();
        } catch (Exception e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
    public void Logout() throws IOException {
        loggerObj.addLog("Logout", "success");
        destroyUserSession();
        exit(0);
    }
    public static boolean CheckIfUserNameExists (String username)  {
        try{
            File myObj = new File(CommonConfig.sysUserFileName + CommonConfig.sysFileType);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String fileData = myReader.nextLine();
                String[] data = fileData.split(CommonConfig.delimiter);
                if(data[0].equals(username)){
                    return true;
                }
            }
            return false;
        } catch(FileNotFoundException e){
            System.out.println("An error occurred.");
            e.printStackTrace();
            return true;
        }
    }
    //endregion
}
