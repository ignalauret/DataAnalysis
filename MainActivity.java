package ignalau.appauto;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.ejml.data.MatrixType;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.simple.SimpleMatrix;
import org.gitia.froog.Feedforward;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Ignacio Lauret <ignalauret@gmail.com>
 *
 */


public class MainActivity extends Activity implements SensorEventListener {


    public MainActivity() {

    }

    //## Constants to Modify ##
    //Sensor Delay (ms): Time it takes between each data read of the sensor.
    private static final int sensorDelay = 10000;
    //Data Window: Size of the chunk of data to analyze.
    private static final int dataWindow = 200;
    //#### NOTE: DataSample duration (sec) = (dataWindow * sensorDelay)/1M ####

    //Layout Variables
    private TextView xAxis;
    private TextView yAxis;
    private TextView zAxis;
    public EditText fileName;
    public Button saveButton;
    public TextView statusText;

    //Sensor Variables
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    //Flags
    public boolean orientatedAxisFlag;
    public boolean zaxisCalculated;

    //########## Script Variables ###########
    //Data Collection
    public int dataCounter;
    public SimpleMatrix dataMatrix = new SimpleMatrix(3,dataWindow,MatrixType.DDRM);
    //Data Saving
    public int savingDataLength = 6000;
    public int savingDataCounter = 0;
    public SimpleMatrix dataSavingMatrix =  new SimpleMatrix(3,6000,MatrixType.DDRM);
    //Data Axis
    public SimpleMatrix axisMatrix = new SimpleMatrix(3,3,MatrixType.DDRM);
    public SimpleMatrix ejeZauto = new SimpleMatrix(3,1,MatrixType.DDRM);
    public SimpleMatrix ejeXauto = new SimpleMatrix(3,1,MatrixType.DDRM);
    public SimpleMatrix ejeYauto = new SimpleMatrix(3,1,MatrixType.DDRM);
    //N. Net
    public String nnResult;
    public Feedforward nNet;

    //Debug Variables
    public boolean bora = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Sensors Init
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //Layout Init
        xAxis = findViewById(R.id.eje_x);
        yAxis = findViewById(R.id.eje_y);
        zAxis = findViewById(R.id.eje_z);
        fileName = findViewById(R.id.fileName);
        saveButton = findViewById(R.id.SaveButton);
        statusText = findViewById(R.id.resultadoNN);
    }


    //Collects the accelerometer's data while the app is active.
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, sensorDelay);
    }

    //Stops the data collection while the app is inactive.
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    //No utility, needed for Sensor Manager.
    public void onAccuracyChanged(final Sensor sensor,int b){}


    @RequiresApi(api = Build.VERSION_CODES.N)
    //Each time the sensor gets a read it uploads the data into the matrix and displays it on screen.
    public void onSensorChanged(final SensorEvent event) {

        //Display data
        xAxis.setText("X:" + event.values[0]);
        yAxis.setText("Y:" + event.values[1]);
        zAxis.setText("Z:" + event.values[2]);

        //Upload data to Matrix
        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];
        saveDatatoMatrix(x,y,z);

    }


    //Save the data from the sensor into the dataMatrix and checks if it's full.
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void saveDatatoMatrix(Double x, Double y, Double z){

        if(dataCounter == dataWindow){
            //Refresh data collection and start analyzing thread.
            dataCounter = 0;
            new DataAnalisisThread().execute();

        } else {

            dataMatrix.setColumn(dataCounter,0, x,y,z);
            dataCounter++;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    //Checks what to do with the filled Data Chunk.
    public void dataFull(){

        if(orientatedAxisFlag){
            //If the axis are orientated, it analyzes the data.
            nnResult = DataAnalizer.analyzeData(dataMatrix,nNet,axisMatrix,dataSavingMatrix,savingDataCounter,bora);
            //Increase data counter because I wrote 200 columns
            savingDataCounter+=200;
            if(savingDataCounter == savingDataLength){
                //If I have filled the saving matrix, I save and start again TODO: Called on function crash().
                saveData(null);
            }
            //If its idle, I re-calculate the axis.
//            if(nnResult.equals("Idle")){
//                ejeZauto = AxisOrientator.getAverageAxis(dataMatrix,dataWindow,"Z");
//                orientatedAxisFlag = false;
//            }
        }else if(DataAnalizer.checkConstantData(dataMatrix)){
                //If we dont have the axis and the accelerations are constant, Calculates the Z-axis.
                ejeZauto = AxisOrientator.getAverageAxis(dataMatrix,"Z");
                zaxisCalculated = true;
                waitingAccelerationMsg();

            } else if(zaxisCalculated) {

                    //If it's moving and has the Z-axis calculated, gets the other axis.
                    DataAnalizer.centerData(dataMatrix,ejeZauto.scale(9.8));
                    ejeXauto = AxisOrientator.calcularSVD(dataMatrix,"X");

                    //Vect(Z,X) instead of Vect(Z,X) because I want Y-Axis pointing right.
                    ejeYauto = AxisOrientator.getThirdAxis(ejeZauto,ejeXauto,"Y");

                    //Saves the axis all in one matrix.
                    axisMatrix = ejeXauto.concatColumns(ejeYauto,ejeZauto);
                    axisMatrix = DataAnalizer.checkSVDOrientation(dataMatrix,axisMatrix);

                    orientatedAxisFlag = true;
                    createNet();

                } else {
                    //If I dont have the Z-axis and it's moving, can't do anything.
                    waitingIdleMsg();
                }

                //Data analyzing thread cant use UI, must use UIThread.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusText.setText(nnResult);
                    }
                });
        }


    //Waiting for idle...
    public void waitingIdleMsg(){
        nnResult = "Deje quieto porfavor";
    }

    //Waiting for an acceleration...
    public void waitingAccelerationMsg(){
        nnResult = "Calculando Ejes...";
    }

    //Sets the desired data saving length
    public void dataSavingLength(View view){
        switch(view.getId()){
            case R.id.seg10Btn :
                savingDataLength = 2000;
                dataSavingMatrix = new SimpleMatrix(3,2000,MatrixType.DDRM);
            break;
            case R.id.seg30Btn :
                savingDataLength = 6000;
                dataSavingMatrix = new SimpleMatrix(3,6000,MatrixType.DDRM);
                break;
            case R.id.min1Btn :
                savingDataLength = 12000;
                dataSavingMatrix = new SimpleMatrix(3,12000,MatrixType.DDRM);
                break;
        }
        //Restart data saving.
        savingDataCounter = 0;
    }


    /*Note: Just accepts .xml files. Put them on assets folder (Create it
    if there isn't, on src folder)
    */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void createNet(){
        try {
            InputStream is = this.getAssets().open("nn.xml");
            nNet = LoadNNAndroid.getNet(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*############# Threading #############
    Data analyzing thread, called to work when a chunk of data is filled.
    Note: Dont make it static for eventual features that will need it active
    while I'm in another activity.
    */
    class DataAnalisisThread extends AsyncTask<Void,Void,Void>{

        @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Void doInBackground(Void... voids) {
            //Just call function like in the previous version,
            // but this time it's called on another Thread than the Main one.
            dataFull();
            return null;
        }
    }

    //############# Debugging Process #############

    //Saves data to file on external Storage.
    public void saveData(View view) {

        FileManager.saveDataToFile(axisMatrix,fileName.getText().toString() + "AxisMatrix.csv",this);
        FileManager.saveDataToFile(dataSavingMatrix,fileName.getText().toString() + "DrivingData.csv",this);
        //Restart Saving
        savingDataCounter = 0;

    }

    //Changes to the graphs screen
    public void graphActivity(View view){
        Intent intent = new Intent(this,Graphs.class);
        startActivity(intent);
    }

    public void bora(View view){
        bora = true;
    }


}
