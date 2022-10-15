package research.sg.edu.edapp.data.model;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.dropbox.core.DbxException;
import com.dropbox.core.util.IOUtil;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.CreateFolderResult;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.WriteMode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import research.sg.edu.edapp.MainActivity;

public class UploadTask extends AsyncTask {

    private DbxClientV2 dbxClient;
    private String fileName;
    private Context context;
    private Uri uri;
    File sdCardRoot = Environment.getExternalStorageDirectory();
    static Boolean status=false;
    String folder_name;
    private ProgressDialog progress;

//    private String path;

//    public UploadTask(DbxClientV2 dbxClient, File file, Context context) {
//        this.dbxClient = dbxClient;
//        this.file = file;
//        this.context = context;
//    }


    public UploadTask(DbxClientV2 dbxClient, Uri uri, Context context, String name, String email) {
        this.dbxClient = dbxClient;
        this.uri = uri;
        this.context = context;
        this.fileName = name;
        this.status=false;
        // salma added the below line for creating user wise folder in dropbox
        this.folder_name=email;
    }



    @Override
    protected Object doInBackground(Object[] params) {
        File file=new File(sdCardRoot,fileName);
        long file_size=file.length();
        try{
            System.out.println("folder name will be:"+folder_name);
            CreateFolderResult folder=dbxClient.files().createFolderV2("/"+folder_name);

        }catch (CreateFolderErrorException e) {
            if(e.errorValue.isPath() && e.errorValue.getPathValue().isConflict()){
                System.out.println("Folder already exist");
                WriteFile("Folder already exist");
            }
            else {
                e.printStackTrace();
            }
        } catch (DbxException e) {
            System.out.println("some other exception occured");
            WriteFile("some other exception occured");
            e.printStackTrace();
        }



            try {
                IOUtil.ProgressListener progressListener = new IOUtil.ProgressListener() {
                    @Override
                    public void onProgress(long l) {
                        System.out.println("I am within progress with status=" + status);
                        WriteFile("I am within progress with status="+ status);

                        if (l == file_size) {
                            status = true;
                        }
                    }
                };


                // Upload to Dropbox
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                FileMetadata metadata = dbxClient.files().uploadBuilder("/" + folder_name+"/"+fileName) //Path in the user's Dropbox to save the file.
                        .withMode(WriteMode.ADD) //always overwrite existing file
                        .uploadAndFinish(inputStream, progressListener);
                Log.d("Upload Status", "success");
                System.out.println(metadata.toStringMultiline());
                System.out.println("Upload status after upload function=" + status);
                WriteFile("Upload status after upload function=" + status);
                inputStream.close();
            } catch (DbxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        return null;
    }




    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        Toast.makeText(context, "Data uploaded successfully", Toast.LENGTH_SHORT).show();

        // Salma uncomment this below line
        //File file = new File(String.valueOf(pathDir)+"/"+fileName);
        File file=new File(sdCardRoot,fileName);
        boolean result = false;
        if(status==true) {
            if (file.isFile()) {
                Log.d("File", "File Exists");
                result = file.delete();
            }
            Log.d("Deleted - " + fileName + ": ", String.valueOf(result));
            deleteStorage();
            status=false;
        }
        //progress.dismiss();
    }

    private void deleteStorage() {
        File directory=new File(sdCardRoot,"/AffectSense/");
        deleteFolder(directory);
    }

    private void deleteFolder(File directory) {
        File[] files=directory.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFromDirectory(f);
                }
            }
        }
    }

    private void deleteFromDirectory(File f) {
        File[] files=f.listFiles();
        if(files!=null) {
            for(File fin: files) {
                fin.delete();
            }
        }
    }
    public void WriteFile(String text) {
        File sdCardRoot = Environment.getExternalStorageDirectory();
        File logFile = new File(sdCardRoot, "/AffectSense/logfile.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
