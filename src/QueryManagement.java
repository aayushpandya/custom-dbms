import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.security.cert.PolicyQualifierInfo;
import java.sql.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class QueryManagement {

    //region Class objects
    Logger loggerObj = new Logger();
    //endregion

    //region Generic query parser
    public String[][] whereConditionParser(String[] con){
        String[][] conditions = new String[con.length][3];
       for(int i=0; i< con.length; i++){
            String operator = new String();
            if(con[i].contains(">="))
                operator =">=";
            else if(con[i].contains("<="))
                operator ="<=";
            else if(con[i].contains("!=") || con[i].contains("<>"))
                operator ="!=";
            else if(con[i].contains("="))
                operator ="=";
            else if(con[i].contains(">"))
                operator =">";
            else if(con[i].contains("<"))
                operator ="<";
            conditions[i][0] = con[i].split(operator)[0].trim();
            conditions[i][1] = operator;
            conditions[i][2] = con[i].split(operator)[1].trim();
        }
        return conditions;
    }
    public void parseQuery(String query){
        try {
            query = query.replace(";","");
            String[] data = query.trim().split(" ");
            String queryType = data[0].toLowerCase();
            HashMap<String, Object> queryData = new HashMap<String, Object>();
            CustomResult<String, Boolean> response = new CustomResult<String, Boolean>(null, null);
            String message = "";
            if (queryType.equals("select")) {
                queryData = selectQueryParser(query, data);
                executeSelectQuery(queryData);
            } else if ((data[0].trim() + " " + data[1].trim()).toLowerCase().equals("create database")) {
                message = executeCreateDatabaseQuery(data[2].trim());
            } else if ((data[0].trim() + " " + data[1].trim()).toLowerCase().equals("drop database")) {
                message = executeDropDatabaseQuery(data[2].trim());
            } else if ((data[0].trim() + " " + data[1].trim()).toLowerCase().equals("create table")) {
                message = createTableQueryParser(query);
            } else if ((data[0].trim() + " " + data[1].trim()).toLowerCase().equals("drop table")) {
                response = executeDropTableQuery(data[2].trim());
            } else if ((data[0].trim() + " " + data[1].trim()).toLowerCase().equals("truncate table")) {
                response = executeTruncateTableQuery(data[2].trim());
            } else if ((data[0].trim() + " " + data[1].trim()).toLowerCase().equals("insert into")) {
                response = insertQueryParser(query);
            } else if ((data[0].trim() + " " + data[1].trim()).toLowerCase().equals("delete from")) {
                response = deleteQueryParser(query);
            } else if (data[0].trim().toLowerCase().equals("update")) {
                response = updateQueryParser(query);
            }
            System.out.println(message);
            System.out.println(response.getFirst());


        }
        catch(Exception e){
            System.out.println(CommonConfig.commonErrorMessage);
        }

//

    }
    //endregion

    //region Select Query
    public void executeSelectQuery(HashMap<String, Object> queryData) {

        HashMap<String, Object> tableMetadata = new HashMap<String, Object>();
        ArrayList<String[]> tableData = null;
        try{
            String database = getDatabaseName();
            tableMetadata = getTableMetadata(database, (String) queryData.get("table"));
            tableData = getTableData(database, (String) queryData.get("table"));
            ArrayList<ArrayList<String>> rowData = new ArrayList<ArrayList<String>>();

            if(queryData != null){
                String[] fields = (String[]) queryData.get("fields");
                if(fields != null && fields.length > 0){
                    for (String[] row: tableData) {
                        rowData.add(new ArrayList<String>(Arrays.asList(row)));
                    }
                }

            }
            ArrayList<ArrayList<String>> finalOutputData = new ArrayList<ArrayList<String>>();
            ArrayList<String> allColList = (ArrayList<String>) tableMetadata.get("col");
            ArrayList<ArrayList<String>> newData = new ArrayList<ArrayList<String>>();

            if(rowData!=null && rowData.size() > 0){
                String[][] conditions = (String[][]) queryData.get("conditions");
                if(conditions != null && conditions.length > 0) {
                    ArrayList<ArrayList<ArrayList<String>>> dataArr = new ArrayList<ArrayList<ArrayList<String>>>();
                    for (String[] con : conditions) {
                        ArrayList<ArrayList<String>> newDataTemp = new ArrayList<ArrayList<String>>();
                        int colPos = allColList.indexOf(con[0]);
                        for (int i = 0; i < rowData.size(); i++) {
                            for (int j = 0; j < rowData.get(i).size(); j++) {
                                if (colPos == j) {
                                    String data = rowData.get(i).get(j);
                                    if (con[1].equals("=")) {
                                        if (con[2].contains("\"")) {
                                            con[2] = con[2].replace("\"", "").toLowerCase();
                                        }
                                        if (rowData.get(i).get(j).toLowerCase().equals(con[2])) {
                                            newDataTemp.add(rowData.get(i));
                                        }
                                    } else if (con[1].contains(">")) {
                                        if (Integer.parseInt(rowData.get(i).get(j)) > Integer.parseInt(con[2])) {
                                            newDataTemp.add(rowData.get(i));
                                        }
                                    } else if (con[1].contains("<")) {
                                        if (Integer.parseInt(rowData.get(i).get(j)) < Integer.parseInt(con[2])) {
                                            newDataTemp.add(rowData.get(i));
                                        }
                                    } else if (con[1].contains("<=")) {
                                        if (Integer.parseInt(rowData.get(i).get(j)) < Integer.parseInt(con[2])) {
                                            newDataTemp.add(rowData.get(i));
                                        }
                                    } else if (con[1].contains(">=")) {
                                        if (Integer.parseInt(rowData.get(i).get(j)) < Integer.parseInt(con[2])) {
                                            newDataTemp.add(rowData.get(i));
                                        }
                                    }

                                }

                            }
                        }
                        dataArr.add(newDataTemp);
                    }
                    if(dataArr.size() == 1)
                        newData = dataArr.get(0);
                    else if(dataArr.size() == 2) {
                        if(((String)queryData.get("conditionType")).equals("and")){
                            HashSet<ArrayList<String>> tempData = new HashSet<ArrayList<String>>(dataArr.get(0));
                            tempData.retainAll(dataArr.get(1));
                            newData = new ArrayList<ArrayList<String>>(tempData);
                        }
                        if(((String)queryData.get("conditionType")).equals("or")){
                            HashSet<ArrayList<String>> tempData = new HashSet<ArrayList<String>>();
                            tempData.addAll(dataArr.get(0));
                            tempData.addAll(dataArr.get(1));
                            newData = new ArrayList<ArrayList<String>>(tempData);
                        }
                    }
                }
                else
                    newData = rowData;
            }
            String[] fields = (String[]) queryData.get("fields");
            ArrayList<ArrayList<String>> newData2 = new ArrayList<ArrayList<String>>();
            for (ArrayList<String> row: newData) {
                if(fields[0].equals("*")){
                    newData2.add(row);
                }
                else {
                    ArrayList<String> newRow = new ArrayList<String>();

                    for (String col: fields) {
                        int pos = ((ArrayList<String>) tableMetadata.get("col")).indexOf(col);
                        newRow.add(row.get(pos));
                    }
                    newData2.add(newRow);
                }
            }

            //Adding Column name's row to the output
            if(newData2!=null && newData2.size() > 0){
                if(fields[0].equals("*"))
                    finalOutputData.add(allColList);
                else
                    finalOutputData.add(new ArrayList<String>(Arrays.asList(fields)));
                int limit = (int)queryData.get("limit");

                for(int i = 0; i < newData2.size(); i++){
                    if(limit != 0 && i >= limit)
                        break;
                    finalOutputData.add(newData2.get(i));

                }
            }
            System.out.println("Output:");
            displayDataTable(finalOutputData);
            System.out.printf("\nResponse: %s row(s) returned", newData.size());
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
//            String database = getDatabaseName();
//            String[][] tableData = null;
//
    }
    public HashMap<String, Object> selectQueryParser(String query, String[] data){
        String[] queryFields = query.split("from")[0].replace("select","").replaceAll(" ","").split(",");
        String table = query.split(" from ")[1].split(" ")[0].replace(";","");
        String[][] conditions = new String[0][0];
        String conditionType = null;
        int limit = 0;
        if(query.contains("where")){
            String[] con = query.split("where")[1].split("limit")[0].trim().replace(";","").split(" and | or ");
            conditionType = query.contains(" and ")?"and":query.contains(" or ")?"or":"";
            conditions = whereConditionParser(con);
        }
        //limit pending
        if(query.contains("limit"))
            limit = Integer.parseInt(query.split("limit ")[1].replace(";",""));
        HashMap<String, Object> queryData= new HashMap<String, Object>();
        queryData.put("type", "select");
        queryData.put("fields", queryFields);
        queryData.put("table", table);
        queryData.put("conditions", conditions);
        queryData.put("limit", limit);
        queryData.put("conditionType", conditionType);
        return queryData;

    }
    //endregion

    //region Create, Drop & Truncate table
    public String createTableQueryParser(String query){
        try{
            query = query.replace(";","").trim();
            String tableName = query.split(" ")[2];
            ArrayList<String> tableNames = getAllTableNames();
            if(tableNames.contains(tableName.trim())){
                return "Table \""+ tableName +"\" already exists!";
            }
            String cols = query.toLowerCase().split(tableName)[1].trim();
            String[] colArray = cols.substring(1,cols.length()-1).split(",");
            ArrayList<String> colName = new ArrayList<String>();
            ArrayList<String> colDataType = new ArrayList<String>();
            ArrayList<Integer> colNotNull = new ArrayList<Integer>();
            String primaryKey = null;
            if(!colArray[colArray.length-1].contains("primary")  && !colArray[colArray.length-1].contains("key"))
                return "Primary Key missing!";
            for(String row: colArray){
                row = row.trim();
                if(row.contains("primary key")){
                    primaryKey = row.replace("primary","").replace("key", "").trim();
                    primaryKey = primaryKey.substring(1,primaryKey.length()-1);
                }
                else{
                    if(row.contains("not null")) {
                        colNotNull.add(1);
                        row = row.replace("not null", "").trim();
                    }
                    else
                        colNotNull.add(0);
                    colName.add(row.split(" ")[0]);
                    colDataType.add(row.split(" ")[1]);

                }


            }
            int primaryKeyIndex = colName.indexOf(primaryKey);
            if(primaryKeyIndex == -1)
                return "Invalid primary key!";
            colName.set(primaryKeyIndex,primaryKey + CommonConfig.primaryKeyDelimiter);
            String[] writeObj = new String[3];
            writeObj[0] = colName.toString().replace("[", "").replace("]", "").replace(", ", CommonConfig.delimiter);
            writeObj[1] = colDataType.toString().replace("[", "").replace("]", "").replace(", ", CommonConfig.delimiter);
            writeObj[2] = colNotNull.toString().replace("[", "").replace("]", "").replace(", ", CommonConfig.delimiter);
            String message = createNewTable(tableName, writeObj);
            return message;

        }
        catch(Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            return CommonConfig.commonErrorMessage;
        }
    }
    public String createNewTable(String tableName, String[] rawMetaData){
        try{
            String database = getDatabaseName();
            ArrayList<String> tables = getAllTableNames();
            if(tables.contains(tableName))
                return "Table \"" + tableName + "\" already exists";
            File newFileMetadataObj = new File(database + "/" + tableName + CommonConfig.sysMetadataFileType);
            if(newFileMetadataObj.createNewFile()){
                FileWriter metadataFileWriter = new FileWriter(database + "/" + tableName + CommonConfig.sysMetadataFileType);
                for(String row: rawMetaData){
                    metadataFileWriter.write(row + "\n");
                }
                metadataFileWriter.close();
                File newFileObj = new File(database + "/" + tableName + CommonConfig.sysFileType);
                newFileObj.createNewFile();
                return "Table \"" + tableName + "\" created";
            }
            else{
                return CommonConfig.commonErrorMessage;
            }
        } catch (Exception e){
            return CommonConfig.commonErrorMessage;
        }
    }
    public CustomResult<String, Boolean> executeDropTableQuery(String tableName){
        try{
            String databaseName = getDatabaseName();
            ArrayList<String> tables = getAllTableNames();
            if(!tables.contains(tableName))
                return new CustomResult<String, Boolean>("Table \"" + tableName + "\" does not exists",false);
            File[] fileList = new File(System.getProperty("user.dir") + "/" + databaseName).listFiles();
            for(File file: fileList){
                if(file.getName().equals(tableName+CommonConfig.sysFileType) || file.getName().equals(tableName+CommonConfig.sysMetadataFileType))
                    file.delete();
            }
            return new CustomResult<String, Boolean>("Table \"" +  tableName + "\" deleted.", true);
        }
        catch (Exception e){
            System.out.println(CommonConfig.commonErrorMessage);
            return null;
        }
    }
    public CustomResult<String, Boolean> executeTruncateTableQuery(String tableName){
        try{
            String databaseName = getDatabaseName();
            ArrayList<String> tables = getAllTableNames();
            if(!tables.contains(tableName))
                return new CustomResult<String, Boolean>("Table \"" + tableName + "\" does not exists",false);
            File[] fileList = new File(System.getProperty("user.dir") + "/" + databaseName).listFiles();
            for(File file: fileList){
                if(file.getName().equals(tableName+CommonConfig.sysFileType)){
                    PrintWriter pw = new PrintWriter(System.getProperty("user.dir") + "/" + databaseName + "/"+ tableName+CommonConfig.sysFileType);
                    pw.close();
                }

            }
            return new CustomResult<String, Boolean>("Table \"" +  tableName + "\" truncated.", true);
        }
        catch (Exception e){
            System.out.println(CommonConfig.commonErrorMessage);
            return null;
        }
    }
    //endregion

    //region Create & Drop Database
    public String executeCreateDatabaseQuery(String databaseName){
        try{
            boolean databaseExists = getDatabaseName() != null;
            if(databaseExists)
                return "Only 1 database allowed per user!";
            FileWriter dbFile = new FileWriter(CommonConfig.sysFileName);
            dbFile.write("database" + CommonConfig.delimiter + databaseName);
            dbFile.close();
            File dbDir = new File(System.getProperty("user.dir") + "/" + databaseName);
            if (!dbDir.exists()){
                dbDir.mkdirs();
            }
            return "Database \"" +  databaseName + "\" created.";
        }
        catch (Exception e){
            System.out.println(CommonConfig.commonErrorMessage);
            return null;
        }
    }
    public String executeDropDatabaseQuery(String databaseName){
        try{
            String storedDatabaseName = getDatabaseName();
            if(!storedDatabaseName.equals(databaseName))
                return "Database \"" + databaseName + "\" does not exists!";
            File fObj = new File(CommonConfig.sysFileName);
            fObj.delete();
            File dirObj = new File(System.getProperty("user.dir") + "/" + databaseName);

            File[] fileList = dirObj.listFiles();
            for(File file: fileList){
                file.delete();
            }

            dirObj.delete();
            return "Database \"" +  databaseName + "\" deleted.";
        }
        catch (Exception e){
            System.out.println(CommonConfig.commonErrorMessage);
            return null;
        }
    }
    //endregion

    //region Insert Records
    public CustomResult<String, Boolean> insertQueryParser(String query){
        try{
            String queryOgCase = query;
            String databaseName = getDatabaseName();
            ArrayList<String> tables = getAllTableNames();
            String tableName = query.trim().split(" ")[2];
            if(!tables.contains(tableName))
                return new CustomResult<String, Boolean>("Table \"" + tableName + "\" does not exists",false);

            String[] tempData = query.split("values");
            ArrayList<String> cols = new ArrayList<String>();
            ArrayList<String> val = new ArrayList<String>();
            if (tempData[0].contains("(") && tempData[0].contains(")")){
                cols.addAll(Arrays.asList(tempData[0].substring(tempData[0].indexOf("(")+1, tempData[0].indexOf(")")).split(","))) ;
                cols.replaceAll(String::trim);
            }
            if (tempData[1].contains("(") && tempData[1].contains(")")){
                val.addAll(Arrays.asList(tempData[1].substring(tempData[1].indexOf("(")+1, tempData[1].indexOf(")")).split(","))) ;
                val.replaceAll(String::trim);
            }
            if(val.isEmpty())
                return new CustomResult<String, Boolean>("Incorrect query, values are not specified", false);

            HashMap<String, Object> tableMetadata = getTableMetadata(databaseName, tableName);
            ArrayList<String> ogCols = (ArrayList<String>) tableMetadata.get("col");
            String[] colType = (String[]) tableMetadata.get("col-type");
            String[] colNotNull = (String[]) tableMetadata.get("col-not-null");
            String primaryKey = (String) tableMetadata.get("pk");
            if(cols.isEmpty())
                cols = ogCols;
            else{
                for(String col: cols){
                    if(!ogCols.contains(col))
                        return new CustomResult<String, Boolean>("Column '" + col +"' doesn't exist in table '" + tableName +"'", false);
                }
            }
            if(cols.size() != val.size())
                return new CustomResult<String, Boolean>("Column count doesn't match value count!", false);
            // check for primary key
            if(cols.indexOf(primaryKey) == -1)
                return new CustomResult<String, Boolean>("Primary key value missing!", false);
            // check for duplicate value on primary key column
            if(!checkPrimaryKeyValueforDuplicate(databaseName, tableName,cols.indexOf(primaryKey),val.get(cols.indexOf(primaryKey))))
                return new CustomResult<String, Boolean>("Duplicate entry '"+val.get(cols.indexOf(primaryKey))+"' for key '"+ tableName+".PRIMARY'", false);
            // check datatype & constraints of values
            String[] row = new String[ogCols.size()];
            for (String col: cols) {
                int pos = ogCols.indexOf(col);
                if(colType[pos].equals("int")){
                    if(!isInteger(val.get(pos)))
                        return new CustomResult<String, Boolean>("Incorrect integer value: "+val.get(pos)+" for column '" +col +"'", false);
                }
                if(colType[pos].contains("varchar(")){
                    String value = colType[pos].substring(colType[pos].indexOf("(")+1, colType[pos].indexOf(")"));
                    int maxLength = isInteger(value)? Integer.parseInt(value): Integer.MAX_VALUE;
                    if(val.get(pos).length() > maxLength)
                        return new CustomResult<String, Boolean>("Data too long for column '" +col +"'", false);
                }
                row[pos] = val.get(pos).replace("\"","");
            }

            for (int i = 0; i < row.length; i++){
                if((row[i] == null || row[i].contains("null")) && colNotNull[i].equals("1")){
                    return new CustomResult<String, Boolean>("Field '" + ogCols.get(i) +"' doesn't have a default value", false);
                }
            }
            Boolean flag = executeInsertQuery(tableName, row);
            if(flag)
                return new CustomResult<String, Boolean>("1 row(s) affected", flag);
            else return new CustomResult<String, Boolean>(CommonConfig.commonErrorMessage, flag);
        }
        catch (Exception e){
            return new CustomResult<String, Boolean>(CommonConfig.commonErrorMessage, false);
        }
    }

    public Boolean executeInsertQuery(String tableName, String[] rows){
        try {
            String databaseName = getDatabaseName();
            String data = String.join(CommonConfig.delimiter, rows) + "\n";
            FileWriter fw = new FileWriter( databaseName +"/"+ tableName + CommonConfig.sysFileType,true);
            fw.write(data);
            fw.close();
            return true;

        } catch(Exception e){
            return false;
        }
    }
    //endregion

    //region Delete Records
    public CustomResult<String, Boolean> deleteQueryParser(String query){
        try{
            String databaseName = getDatabaseName();
            ArrayList<String> tables = getAllTableNames();
            String tableName = query.trim().split(" ")[2];
            CustomResult<String, Boolean> response = new CustomResult<String, Boolean>(null,null);
            if(!tables.contains(tableName))
                return new CustomResult<String, Boolean>("Table \"" + tableName + "\" does not exists",false);
            if(!query.contains(" where ")){
                return executeTruncateTableQuery(tableName);
            }
            HashMap<String, Object> tableMetadata = getTableMetadata(databaseName, tableName);
            ArrayList<String> ogCols = (ArrayList<String>) tableMetadata.get("col");

            String[] con = query.split("where")[1].trim().replace(";","").split(" and | or ");
            String conditionType = query.contains(" and ")?"and":query.contains(" or ")?"or":"";
            String[][] conditions = whereConditionParser(con);
            for(String[] col: conditions){
                if(!ogCols.contains(col[0]))
                    return new CustomResult<String, Boolean>("Column '" + col[0] +"' doesn't exist in table '" + tableName +"'", false);
            }
            response = executeDeleteQuery(databaseName, tableName, conditions, conditionType);
            return response;
        }
        catch (Exception e){
            return new CustomResult<String, Boolean>(CommonConfig.commonErrorMessage,false);
        }
    }
    public CustomResult<String, Boolean> executeDeleteQuery (String database, String tableName, String[][] conditions, String operator){
        try {
            ArrayList<String[]> dataTemp = getTableData(database, tableName);
            ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
            ArrayList<ArrayList<String>> filteredData = getFilteredData(database, tableName, conditions, operator);

            for(String[] row: dataTemp){
                data.add(new ArrayList<String>(Arrays.asList(row)));
            }
            if(data.isEmpty())
                return new CustomResult<String, Boolean>(CommonConfig.commonErrorMessage, false);
            if(filteredData.isEmpty())
                return new CustomResult<String, Boolean>("0 row(s) affected", false);
            data.removeAll(filteredData);

            String writeData = data.stream().map(col -> col.stream().collect(Collectors.joining(CommonConfig.delimiter))).collect(Collectors.joining("\n"));
            FileWriter fw = new FileWriter( database +"/"+ tableName + CommonConfig.sysFileType);
            fw.write(writeData + "\n");
            fw.close();

            return new CustomResult<String, Boolean>(filteredData.size() + " row(s) affected", true);
        }catch (Exception e){
            return new CustomResult<String, Boolean>(CommonConfig.commonErrorMessage, false);

        }
    }

    //endregion

    //region Update Records
    public CustomResult<String, Boolean> updateQueryParser(String query){
        try{
            CustomResult<String, Boolean> response = new CustomResult<String, Boolean>(null,null);
            String databaseName = getDatabaseName();
            ArrayList<String> tables = getAllTableNames();
            String tableName = query.split(" ")[1].trim();
            HashMap<String, Object> tableMetadata = getTableMetadata(databaseName, tableName);
            ArrayList<String> ogCols = (ArrayList<String>) tableMetadata.get("col");
            if(!tables.contains(tableName))
                return new CustomResult<String, Boolean>("Table \"" + tableName + "\" does not exists",false);
            if(!query.contains(" where ")){
                return new CustomResult<String, Boolean>("Invalid query, 'where' clause expected!",false);
            }
            System.out.println();
            String[] whereCon = query.substring(query.indexOf(" where ")+1).replace("where","").trim().replace(";","").split(" and | or ");
            String whereConditionType = query.contains(" and ")?"and":query.contains(" or ")?"or":"";
            String[][] whereConditions = whereConditionParser(whereCon);
            for(String[] col: whereConditions){
                if(!ogCols.contains(col[0]))
                    return new CustomResult<String, Boolean>("Column '" + col[0] +"' doesn't exist in table '" + tableName +"'", false);
            }
            String[] setCon = query.substring(query.indexOf(" set ")+1, query.indexOf(" where ")).replace("set","").trim().replace(";","").split(",");
            String[][] setConditions = whereConditionParser(setCon);
            for(String[] col: setConditions){
                if(!ogCols.contains(col[0]))
                    return new CustomResult<String, Boolean>("Column '" + col[0] +"' doesn't exist in table '" + tableName +"'", false);
            }
            response = executeUpdateQuery(databaseName, tableName, whereConditions, setConditions, whereConditionType);
            return response;
        }
        catch (Exception e){
            return new CustomResult<String, Boolean>(CommonConfig.commonErrorMessage, false);
        }
    }

    public CustomResult<String, Boolean> executeUpdateQuery(String database, String tableName, String[][] whereConditions, String[][] setConditions, String operator){
        try{
            CustomResult<String, Boolean> response = new CustomResult<String, Boolean>(null,null);
            ArrayList<String[]> dataTemp = getTableData(database, tableName);
            ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
            HashMap<String, Object> tableMetadata = getTableMetadata(database, tableName);
            ArrayList<String> allColList = (ArrayList<String>) tableMetadata.get("col");
            ArrayList<ArrayList<String>> filteredData = getFilteredData(database, tableName, whereConditions, operator);
            for(String[] row: dataTemp){
                data.add(new ArrayList<String>(Arrays.asList(row)));
            }
            if(data.isEmpty())
                return new CustomResult<String, Boolean>(CommonConfig.commonErrorMessage, false);
            if(filteredData.isEmpty())
                return new CustomResult<String, Boolean>("0 row(s) affected", true);
            for(int i=0; i < data.size(); i++){
                if(filteredData.contains(data.get(i))){
                    for(String[] con: setConditions){
                        int pos = allColList.indexOf(con[0]);
                        data.get(i).set(pos,con[2].replace("\"","").replace("\'",""));
                    }

                }
            }
            String writeData = data.stream().map(col -> col.stream().collect(Collectors.joining(CommonConfig.delimiter))).collect(Collectors.joining("\n"));
            FileWriter fw = new FileWriter( database +"/"+ tableName + CommonConfig.sysFileType);
            fw.write(writeData + "\n");
            fw.close();
            return new CustomResult<String, Boolean>(filteredData.size() + " row(s) affected", true);
        } catch(Exception e){
            return new CustomResult<String, Boolean>(CommonConfig.commonErrorMessage, false);
        }
    }
    //endregion Records

    //region Display Data Table
    public void displayDataTable (ArrayList<ArrayList<String>> data){
        //System.out.println(Arrays.deepToString(data));
        if(data != null && data.size() > 0){
            int[] maxColWidth = new int[data.get(0).size()];
            for(int i = 0;i<data.size();i++){
                for(int j = 0;j<data.get(i).size();j++){
                    maxColWidth[j] = Math.max(maxColWidth[j], data.get(i).get(j).length());
                }
            }
            System.out.println("_".repeat(Arrays.stream(maxColWidth).sum() + ((maxColWidth.length * 2) + 4 )));

            for(int i = 0;i<data.size();i++){
                System.out.print("| ");
                for(int j = 0;j<data.get(i).size();j++){
                    System.out.print(data.get(i).get(j) + " ".repeat(maxColWidth[j]-data.get(i).get(j).length())  + " | ");
                }
                System.out.print("\n");
                if(i == 0)
                    System.out.println("-".repeat(Arrays.stream(maxColWidth).sum() + ((maxColWidth.length * 2) + 4 )));
            }
            System.out.println("-".repeat(Arrays.stream(maxColWidth).sum() + ((maxColWidth.length * 2) + 4 )));
        }
    }
    //endregion

    //region Random private methods
    private ArrayList<String> getAllTableNames() {
        String database = getDatabaseName();
        ArrayList<String> tableNames = new ArrayList<String>();
        File[] tablefiles = new File(System.getProperty("user.dir") + "/" + database).listFiles();
        for (File file : tablefiles) {
            if (file.isFile()) {
                String fileName = file.getName();
                if(!fileName.contains(CommonConfig.sysMetadataFileType))
                    tableNames.add(fileName.replace(CommonConfig.sysFileType,"").trim());
            }
        }
        return tableNames;
    }
    private String getDatabaseName(){
        try{
            File myObj = new File(CommonConfig.sysFileName);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String fileData = myReader.nextLine();
                String[] dataArray = fileData.split(CommonConfig.delimiter);
                if(dataArray[0].equals("database"))
                    return dataArray[1];
            }
            return null;
        }
        catch (Exception e){
            //e.printStackTrace();
            //System.out.println(e.getMessage());
            return null;
        }
    }
    private ArrayList<String[]> getTableData(String database, String table){
        ArrayList<String[]> tableData = new ArrayList<String[]>();

        try {
            File tableObj = new File(database + "/" + table + CommonConfig.sysFileType);
            Scanner myReader = new Scanner(tableObj);

            while (myReader.hasNextLine()) {
                String rowData = myReader.nextLine();
                tableData.add(rowData.split(CommonConfig.delimiter));
            }
            return tableData;
        }
        catch (Exception e){
            return tableData;
        }
    }
    private HashMap<String, Object> getTableMetadata(String database, String table) {
        HashMap<String, Object> tableMetadata = new HashMap<String, Object>();
        try {
            File tableObj = new File(database + "/" + table + ".metadata" + CommonConfig.sysFileType);
            Scanner myReader = new Scanner(tableObj);
            int count = 0;
            while (myReader.hasNextLine()) {
                String rowData = myReader.nextLine();
                //find all columns and primary key
                if(count == 0){
                    ArrayList<String> colData = new ArrayList<String>(Arrays.asList(rowData.split(CommonConfig.delimiter)));
                    for (int i = 0; i<colData.size();i++){
                        if(colData.get(i).contains(CommonConfig.primaryKeyDelimiter)){
                            colData.set(i, colData.get(i).replace(CommonConfig.primaryKeyDelimiter, ""));
                            tableMetadata.put("pk", colData.get(i));
                            break;
                        }
                    }
                    tableMetadata.put("col",colData);
                }
                //find column types and not null
                else {
                    tableMetadata.put(count == 1 ? "col-type" : "col-not-null",rowData.split(CommonConfig.delimiter));
                }
                count++;
            }
            return tableMetadata;
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
            System.out.println(e.getMessage());
            return null;
        }

    }
    public Boolean isInteger(String value) {
        try {
            int num = Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public Boolean checkPrimaryKeyValueforDuplicate(String database, String tableName, int pos, String value){
        try{
            ArrayList<String[]> data = getTableData(database, tableName);
            for(int i = 0; i < data.size(); i++){
                if (data.get(i)[pos].equals(value))
                    return false;
            }
            return true;
        } catch (Exception e){
            return false;
        }
    }
    private ArrayList<ArrayList<String>> getFilteredData(String database, String tableName, String[][] conditions, String operator){
        ArrayList<ArrayList<String>> newData = new ArrayList<ArrayList<String>>();
        try{
            ArrayList<String[]> tableData = getTableData(database, tableName);
            ArrayList<ArrayList<String>> rowData = new ArrayList<ArrayList<String>>();

            for(String[] row: tableData){
                rowData.add(new ArrayList<String>(Arrays.asList(row)));
            }
            HashMap<String, Object> tableMetadata = getTableMetadata(database,tableName);
            ArrayList<String> allColList = (ArrayList<String>) tableMetadata.get("col");
            if(rowData!=null && rowData.size() > 0){

                if(conditions != null && conditions.length > 0) {
                    ArrayList<ArrayList<ArrayList<String>>> dataArr = new ArrayList<ArrayList<ArrayList<String>>>();
                    for (String[] con : conditions) {
                        ArrayList<ArrayList<String>> newDataTemp = new ArrayList<ArrayList<String>>();
                        int colPos = allColList.indexOf(con[0]);
                        for (int i = 0; i < rowData.size(); i++) {
                            for (int j = 0; j < rowData.get(i).size(); j++) {
                                if (colPos == j) {
                                    String data = rowData.get(i).get(j);
                                    if (con[1].equals("=")) {
                                        if (con[2].contains("\"")) {
                                            con[2] = con[2].replace("\"", "").toLowerCase();
                                        }
                                        if (rowData.get(i).get(j).toLowerCase().equals(con[2])) {
                                            newDataTemp.add(rowData.get(i));
                                        }
                                    } else if (con[1].contains(">")) {
                                        if (Integer.parseInt(rowData.get(i).get(j)) > Integer.parseInt(con[2])) {
                                            newDataTemp.add(rowData.get(i));
                                        }
                                    } else if (con[1].contains("<")) {
                                        if (Integer.parseInt(rowData.get(i).get(j)) < Integer.parseInt(con[2])) {
                                            newDataTemp.add(rowData.get(i));
                                        }
                                    } else if (con[1].contains("<=")) {
                                        if (Integer.parseInt(rowData.get(i).get(j)) < Integer.parseInt(con[2])) {
                                            newDataTemp.add(rowData.get(i));
                                        }
                                    } else if (con[1].contains(">=")) {
                                        if (Integer.parseInt(rowData.get(i).get(j)) < Integer.parseInt(con[2])) {
                                            newDataTemp.add(rowData.get(i));
                                        }
                                    }

                                }

                            }
                        }
                        dataArr.add(newDataTemp);
                    }
                    if(dataArr.size() == 1)
                        newData = dataArr.get(0);
                    else if(dataArr.size() == 2) {
                        if(operator.equals("and")){
                            HashSet<ArrayList<String>> tempData = new HashSet<ArrayList<String>>(dataArr.get(0));
                            tempData.retainAll(dataArr.get(1));
                            newData = new ArrayList<ArrayList<String>>(tempData);
                        }
                        if(operator.equals("or")){
                            HashSet<ArrayList<String>> tempData = new HashSet<ArrayList<String>>();
                            tempData.addAll(dataArr.get(0));
                            tempData.addAll(dataArr.get(1));
                            newData = new ArrayList<ArrayList<String>>(tempData);
                        }
                    }
                }
                else
                    newData = rowData;
            }
            return newData;
        }
        catch (Exception e){
            return newData;
        }
    }
    //endregion
}
