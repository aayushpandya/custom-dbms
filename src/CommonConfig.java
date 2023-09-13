import java.nio.charset.StandardCharsets;

public class CommonConfig {
    //region Common configurations
    public static String delimiter = "::#::";
    public static String delimiterBreak = "##@^@##";
    public static String sysUserFileName = "sys/user";
    public static String sysLoggerFileName = "sys/logs";
    public static String sysFileType = ".ap";
    public static String sysMetadataFileType = ".metadata.ap";
    public static byte[] salt = "[B@5cb0d902".getBytes(StandardCharsets.UTF_8);
    public static String hashingAlgorithm = "SHA-512";
    public static String sysSessionFileName = "sys/.session";
    public static String primaryKeyDelimiter = "^^^PK^^^";
    public static String dateTimeFormat = "yyyy.MM.dd.HH.mm.ss";
    public static String sysFileName = "sys/.sys";
    public static String commonErrorMessage = "Something went wrong, please try again!";
    //endregion
}
