package com.bigbasket.mobileapp.util;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.util.Log;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class LogUtil {

    @SuppressLint("SimpleDateFormat")
    public static void fileCreation(String text, String classname) {

        File logFile = new File("sdcard/Bigbasket/Manual_log.txt");
        final String DATE_FORMAT_NOW = "dd-MM-yyyy HH:mm:ss";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append(String.valueOf(sdf.format(cal.getTime())) + "/" + classname + " " + text);
                buf.newLine();
                buf.close();
            } catch (IOException e) {

                e.printStackTrace();
            }
        } else {
            BufferedWriter buf;
            try {
                buf = new BufferedWriter(new FileWriter(logFile, true));
                InputStream inputStream = new FileInputStream(logFile);
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                while ((receiveString = bufferedReader.readLine()) != null) {
                    List<String> list = new ArrayList<String>(Arrays.asList(receiveString.split("/")));
                    for (int i = 0; i < list.size(); i++) {
                        //list.get(i).replace("[","");
                        if (i == 0) {
                            System.out.println("date sepration-->" + list.get(i).trim());
                            DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                            Date convertedDate;
                            try {
                                convertedDate = (Date) formatter.parse(list.get(i).trim());
                                Date dNow = new Date();
                                long diff = dNow.getTime() - convertedDate.getTime();
                                long diffDays = diff / (24 * 60 * 60 * 1000);
                                System.out.println("differnce between days-->" + diffDays);
                                if (diffDays > 4) {
                                    System.out.println("differnce getting for if condition");
                                    removeLineFromFile(logFile.toString(), receiveString);

                                }

                            } catch (ParseException e) {

                                e.printStackTrace();
                            }


                        }

                    }

                }
                bufferedReader.close();
                inputStream.close();
                buf.append(String.valueOf(sdf.format(cal.getTime())) + "/" + classname + " " + text);
                buf.newLine();
                buf.close();

            } catch (IOException e1) {

                e1.printStackTrace();
            }
        }
    }

    private static void removeLineFromFile(String file, String lineToRemove) {

        try {

            File inFile = new File(file);

            if (!inFile.isFile()) {
                System.out.println("Parameter is not an existing file");
                return;
            }
            //Construct the new file that will later be renamed to the original filename.
            File tempFile = new File(inFile.getAbsolutePath() + ".tmp");
            BufferedReader br = new BufferedReader(new FileReader(file));
            PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
            String line = null;
            //Read from the original file and write to the new
            //unless content matches data to be removed.
            while ((line = br.readLine()) != null) {

                if (!line.trim().equals(lineToRemove)) {
                    pw.println(line);
                    pw.flush();
                }
            }
            pw.close();
            br.close();

            //Delete the original file
            if (!inFile.delete()) {
                System.out.println("Could not delete file");
                return;
            }

            //Rename the new file to the filename the original file had.
            if (!tempFile.renameTo(inFile))
                System.out.println("Could not rename file");

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void fileHandling(String exp_Message, String classname) {

        File folder = new File(Environment.getExternalStorageDirectory() + "/Bigbasket");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
            fileCreation(exp_Message, classname);
        } else {
            fileCreation(exp_Message, classname);
        }
    }


    public static void WriteFile(String tag, String message) {
        //fileHandling(message, tag);
        Log.i(tag, message);

    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }
}
