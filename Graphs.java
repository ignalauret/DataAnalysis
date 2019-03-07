package ignalau.appauto;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import javax.annotation.Nullable;

public class Graphs extends AppCompatActivity implements SensorEventListener {

    //Sensors
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    //Charts
    private LineChart xChart;
    private LineChart yChart;
    private LineChart zChart;

    //Threads
    private Thread thread;

    //Flags
    private boolean plotData = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphs);

        //Sensors Init
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //Layout Init
        xChart = (LineChart) findViewById(R.id.graph_x);
        yChart = (LineChart) findViewById(R.id.graph_y);
        zChart = (LineChart) findViewById(R.id.graph_z);

        //Chart Init
        LineData dataX = new LineData();
        LineData dataY = new LineData();
        LineData dataZ = new LineData();

        initializeChart(xChart,dataX);
        initializeChart(yChart,dataY);
        initializeChart(zChart,dataZ);

        //Start plotting thread.
        startPlot();

    }

    //Collects the accelerometer's data while the app is active.
    protected void onResume() {
        super.onResume();
        if(mAccelerometer != null)
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    //Stops the data collection while the app is inactive and stops the thread.
    protected void onPause() {
        super.onPause();
        if(thread != null){
            thread.interrupt();
        }
        mSensorManager.unregisterListener(this);
    }

    //UNUSED: Just needed for Sensor.
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    //When data is read, adds it to each plot. (Plot data flag for concurrency issues)
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(plotData){
            addEntry(event.values[0],xChart,"Eje X",Color.BLUE);
            addEntry(event.values[1],yChart,"Eje Y",Color.GREEN);
            addEntry(event.values[2] - 9.8f,zChart,"Eje Z",Color.RED);
            plotData = false;
        }
    }

    //Chart Init
    private void initializeChart(LineChart chart , LineData data){

        charConfig(chart);
        chart.setData(data);
    }

    //Chart configuration
    private void charConfig(LineChart chart){
        //No description.
        chart.getDescription().setEnabled(false);
        //No fancy features.
        chart.setTouchEnabled(false);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(true);
        chart.setPinchZoom(true);
        chart.setBackgroundColor(Color.WHITE);
        //No top labels.
        XAxis xl = chart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(false);
        //No right labels.
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
        //Just left labels
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMaximum(11f);
        leftAxis.setAxisMinimum(-11f);
        leftAxis.setDrawGridLines(true);

        chart.setDrawBorders(false);

    }

    //Plotting thread Init.
    private void startPlot(){

        if(thread != null){
            thread.interrupt();
        }

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    plotData = true;
                    try {
                        //Wait 10ms for each read.
                        Thread.sleep(10);

                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }

    //Add data $value to the plot $chart. Label and color just for initialization  ir set == null)
    private void addEntry(float value, LineChart chart, String label, int color){

        LineData data = chart.getData();

        if(data != null){
            ILineDataSet set = data.getDataSetByIndex(0);

            if(set == null){
                //If no set yet, create one.
                set = createSet(label,color);
                data.addDataSet(set);
            }
            //This addEntry es the lib function.
            data.addEntry(new Entry(set.getEntryCount(),value),0);
            //Refresh plot.
            data.notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.setVisibleXRangeMaximum(100);
            //Keeps view dynamically fixed on the last point.
            chart.moveViewTo(data.getEntryCount(), -10, YAxis.AxisDependency.LEFT);
        }
    }

    //Create the plot set if it doesn't exist yet.
    private LineDataSet createSet(String label, int color){

        LineDataSet set = new LineDataSet(null, label);
        //Set config
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(1.1f);
        set.setColor(color);
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.5f);

        return set;
    }

    //Returns to Main Activity.
    public void backActivity(View view){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
}
