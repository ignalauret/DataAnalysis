package ignalau.appauto;


import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;
import org.ejml.simple.SimpleMatrix;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Ignacio Lauret <ignalauret@gmail.com>
 *
 */

public class FileManager extends Activity {


    // Saves the matrix into a file in the external storage with the given name.
    public static void saveDataToFile(SimpleMatrix dataMatrix, String fileName, Activity activity){
        if(isExternalStorageWritable()){

            File filePath = new File(Environment.getExternalStorageDirectory(),fileName);

            try{
                dataMatrix.saveToFileCSV(filePath.toString());
                Toast.makeText(activity,"File Saved Succesfully.",Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else {
            Toast.makeText(activity,"Cannot Write.",Toast.LENGTH_SHORT).show();
        }
    }

    // If we don't have permission tu write the external storage, we ask the user to give it.
    public static boolean checkPermission(String permission,Activity activity){
        int check = ContextCompat.checkSelfPermission(activity, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    // Checks if the User gave us permission or not.
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 3 :
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Permission Granted.",Toast.LENGTH_SHORT).show();
                }
        }
    }


    // Checks if we can write in the internal storage (if we've got permission granted)
    private static boolean isExternalStorageWritable(){
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            Log.i("State","Yes, it is writable!");
            return true;
        }else{
            return false;
        }
    }

}
