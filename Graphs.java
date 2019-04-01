package ignalau.appauto;


import android.graphics.Color;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;


class Graphs {


    /* Chart configuration */
    private static void charConfig(LineChart chart){

        /* No description. */
        chart.getDescription().setEnabled(false);

        /* No fancy features. */
        chart.setTouchEnabled(false);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(true);
        chart.setPinchZoom(true);

        /* No top labels. */
        XAxis xl = chart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(false);

        /* No right labels. */
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        /* Just left labels */
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMaximum(5f);
        leftAxis.setAxisMinimum(-5f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawZeroLine(true);
        leftAxis.setZeroLineWidth(2);

        chart.setDrawBorders(false);
    }

    /* Create the plot set if it doesn't exist yet. */
    private static LineDataSet createSet(String label, int color){

        LineDataSet set = new LineDataSet(null, label);
        /* Set config */
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

    /* Chart Init */
    static void initializeChart(LineChart chart , LineData data){

        Graphs.charConfig(chart);
        chart.setData(data);
    }

    /* Add data $value to the plot $chart. Label and color just for initialization  ir set == null) */
    static void addEntry(float value, LineChart chart, String label, int color){

        LineData data = chart.getData();

        if(data != null){
            ILineDataSet set = data.getDataSetByIndex(0);

            if(set == null){
                /* If no set yet, create one. */
                set = Graphs.createSet(label,color);
                data.addDataSet(set);
            }
            /* This addEntry es the lib function. */
            data.addEntry(new Entry(set.getEntryCount(),value),0);
            /* Refresh plot. */
            data.notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.setVisibleXRangeMaximum(100);
            /* Keeps view dynamically fixed on the last point. */
            chart.moveViewTo(data.getEntryCount(), -10, YAxis.AxisDependency.LEFT);
        }
    }

    /* Highlights the most influential axis */
    static void highLight(String label, LineChart xChart, LineChart yChart, LineChart zChart){
        erraseHighlights(xChart,yChart,zChart);
        switch (label){
            case "Idle" :
            break;

            case "Giro Izquierda":
                setLine(yChart,2f);
            break;

            case "Giro Derecha":
                setLine(yChart,-2f);
            break;

            case "Frenada":
                setLine(xChart,-2f);
            break;

            case "Acelerando":
                setLine(xChart,0.8f);
            break;
        }
    }

    /* Cleans all highlights */
    private static void erraseHighlights(LineChart xChart, LineChart yChart, LineChart zChart){

        YAxis leftAxisX = xChart.getAxisLeft();
        leftAxisX.removeAllLimitLines();
        leftAxisX.setGridColor(Color.BLACK);

        YAxis leftAxisY = yChart.getAxisLeft();
        leftAxisY.removeAllLimitLines();
        leftAxisY.setGridColor(Color.BLACK);

        YAxis leftAxisZ = zChart.getAxisLeft();
        leftAxisZ.removeAllLimitLines();
        leftAxisZ.setGridColor(Color.BLACK);
    }

    /* Creates black line on $value in the $chart axis */
    private static void setLine(LineChart chart, float value){
        LimitLine line = new LimitLine(value);
        line.setLineColor(Color.BLACK);
        line.setLineWidth(1.5f);
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.addLimitLine(line);
        leftAxis.setGridColor(Color.RED);
    }
}
