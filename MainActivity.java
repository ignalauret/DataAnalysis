package ignalau.appauto;

import android.Manifest;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.ejml.data.MatrixType;
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
    //Debug Window: Size of the chunk of data for storing in the Debug Matrix.
    private static final int debugWindow = 2000;

    //Layout Variables
    private TextView ejex;
    private TextView ejey;
    private TextView ejez;
    public EditText fileName;
    public Button saveButton;
    public Button startButton;
    public TextView resNN;

    //Sensor Variables
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    //Flags
    public boolean orientatedAxisFlag;
    public boolean zaxisCalculated;

    //Data Variables
    public int dataCounter;
    public SimpleMatrix dataMatrix = new SimpleMatrix(3,dataWindow,MatrixType.DDRM);
    public SimpleMatrix axisMatrix = new SimpleMatrix(3,3,MatrixType.DDRM);
    public SimpleMatrix ejeZauto = new SimpleMatrix(3,1,MatrixType.DDRM);
    public SimpleMatrix ejeXauto = new SimpleMatrix(3,1,MatrixType.DDRM);
    public SimpleMatrix ejeYauto = new SimpleMatrix(3,1,MatrixType.DDRM);
    public Feedforward redNeuronal;

    //Debug Variables
    public SimpleMatrix debugMatrix = new SimpleMatrix(3,debugWindow,MatrixType.DDRM);
    public int debugCounter = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Sensors Init
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //Layout Init
        ejex = findViewById(R.id.eje_x);
        ejey = findViewById(R.id.eje_y);
        ejez = findViewById(R.id.eje_z);
        fileName = findViewById(R.id.fileName);
        saveButton = findViewById(R.id.SaveButton);
        startButton = findViewById(R.id.startButton);
        resNN = findViewById(R.id.resultadoNN);

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
        ejex.setText("X:" + Float.toString(event.values[0]));
        ejey.setText("Y:" + Float.toString(event.values[1]));
        ejez.setText("Z:" + Float.toString(event.values[2]));

        //Upload data to Matrix
        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];
        saveDatatoMatrix(x,y,z);

    }


    //Save the data from the sensor into the dataMatrix and checks if it's full.
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void saveDatatoMatrix(Double x, Double y, Double z){

        if(dataCounter == dataWindow) dataFull();

        if(dataCounter < dataWindow) {
            dataMatrix.setColumn(dataCounter,0, x,y,z);
            dataCounter++;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    //Checks what to do with the filled Data Chunk.
    public void dataFull(){

        if(orientatedAxisFlag){

            //If the axis are orientated, it analyzes the data.
            resNN.setText(DataAnalizer.analyzeData(dataMatrix,redNeuronal,axisMatrix,debugMatrix,debugWindow));

        }else if(DataAnalizer.checkConstantData(dataMatrix)){
                //If we dont have the axis and the accelerations are constant, Calculates the Z-axis.
                ejeZauto = AxisOrientator.getAverageAxis(dataMatrix,dataWindow,"Z");
                zaxisCalculated = true;
                mensajeCalculando();

            } else if(zaxisCalculated) {

                    //If it's moving and has the Z-axis calculated, gets the other axis.
                    DataAnalizer.centerData(dataMatrix,ejeZauto.scale(9.8));
                    ejeXauto = AxisOrientator.calcularSVD(dataMatrix,"X");
                    ejeYauto = AxisOrientator.getThirdAxis(ejeXauto,ejeZauto,"Y");
                    axisMatrix = ejeXauto.concatColumns(ejeYauto,ejeZauto);

                    orientatedAxisFlag = true;
                    crearRed();

                } else {
                    //If I dont have the Z-axis and it's moving, can't do anything.
                    mensajeDejeQuieto();
                }
        dataCounter = 0;
    }


    //Si el celular no tiene orientado ningun eje y se esta moviendo.
    public void mensajeDejeQuieto(){
        resNN.setText("Deje quieto Porfavor");
    }

    //Eje Z calculado, esperando aceleracion para calcular X e Y.
    public void mensajeCalculando(){
        resNN.setText("Calculando Ejes...");
    }


    /*A tener en cuenta: Solo acepta archivo .xml (No CSV)
     y hay que ponerlos en la carpeta assets(crearla si no existe, dentro de src)
    */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void crearRed(){
        try {
            InputStream is = this.getAssets().open("nn.xml");
            redNeuronal = LoadNNAndroid.getNet(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //############# Debugging Process #############

    //Restart data collection.
    public void startSave(View view){

        debugCounter = 0;
    }

    //Saves data to file on external Storage.
    public void saveData(View view) {
        if (FileManager.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,this)) {
            FileManager.saveDataToFile(dataMatrix,"dataMatrix.csv",this);
            FileManager.saveDataToFile(axisMatrix,"axisMatrix.csv",this);
        } else {
            //Si no tengo permiso debo solicitarselo al usuario.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        }
    }
    //############# End Debugging Process #############

}
