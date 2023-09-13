import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class Logger {

    //region Logger methods
    private String getLoggedInUser() throws FileNotFoundException {
        try{
            File sesObj = new File(CommonConfig.sysSessionFileName);
            Scanner sesReader = new Scanner(sesObj);
            String user = sesReader.nextLine();
            return user;
        }
        catch (FileNotFoundException e){
            return null;
        }
    }
    public void addLog(String action,String response,String user) throws IOException {
        String timeStamp = new SimpleDateFormat(CommonConfig.dateTimeFormat).format(new java.util.Date());
        String logData = user + CommonConfig.delimiter + action + CommonConfig.delimiter + response + CommonConfig.delimiter  + timeStamp + CommonConfig.delimiterBreak;
        FileWriter fw = new FileWriter(CommonConfig.sysLoggerFileName + CommonConfig.sysFileType,true);
        fw.write(logData);
        fw.close();
    }
    public void addLog(String action, String response) throws IOException {
        String user = getLoggedInUser();
        addLog(action, response, user);
    }
    //endregion

}
