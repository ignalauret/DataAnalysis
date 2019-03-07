package ignalau.appauto;


import android.Manifest;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.ejml.data.MatrixType;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.simple.SimpleMatrix;
import org.gitia.froog.Feedforward;
import org.gitia.froog.statistics.Compite;

/**
 *
 * @author Ignacio Lauret <ignalauret@gmail.com>
 *
 */

public class DataAnalizer {

    //Noise value for Idle status precision.
    private static final double noiseValue = 3.5;
    //Value of acceleration precision.
    private static final double minAccelerationValue = 0.8;

    public static SimpleMatrix orientedFinalData = new SimpleMatrix(3,200,MatrixType.DDRM);

    //Checks if the data doesn't have much variations, taking into account the noise that could be.
    public static boolean checkConstantData(SimpleMatrix dataMatrix) {

        //Initialize the temp matrices for the calculations.
        SimpleMatrix maxMatrix = new SimpleMatrix(3,1, MatrixType.DDRM);
        SimpleMatrix minMatrix = new SimpleMatrix(3,1,MatrixType.DDRM);
        SimpleMatrix dataRangeMatrix = new SimpleMatrix(3,1,MatrixType.DDRM);

        //Takes the max and the min of each row (movement on each axis) and then subtracts them to get
        //the range of variation in each axis.
        CommonOps_DDRM.maxRows(dataMatrix.getDDRM(),maxMatrix.getDDRM());
        CommonOps_DDRM.minRows(dataMatrix.getDDRM(),minMatrix.getDDRM());
        CommonOps_DDRM.subtract(maxMatrix.getDDRM(),minMatrix.getDDRM(),dataRangeMatrix.getDDRM());

        //Sums the absolute value of each axis variation range to get a number that describes the total variation.
        double dataRange = CommonOps_DDRM.elementSumAbs(dataRangeMatrix.getDDRM());

        return(dataRange < noiseValue);

    }


    //Modifies the data to reduce the noise in it, by making an average of a range of points near every point (Unused yet)
    public static SimpleMatrix maskData(SimpleMatrix matrix,int maskGrade){

        SimpleMatrix returnMatrix = matrix.copy();
        int cols = returnMatrix.numCols();
        int rows = returnMatrix.numRows();

        for(int i = 0;i<rows;i++) {
            for (int j = 0; j < cols - 2 * maskGrade - 1; j++) {

                returnMatrix.set(i, j + maskGrade, matrix.rows(i, i + 1).cols(j, j + 2 * maskGrade + 2).elementSum() / (2 * maskGrade + 1));

            }
        }
        return returnMatrix;

    }

    //Subtracts a given vector on each column to center the data around it.
    public static void centerData(SimpleMatrix dataMatrix,SimpleMatrix centerVector){

        int dataSize = dataMatrix.numCols();

        for(int i=0;i<dataSize;i++){

            dataMatrix.setColumn(i,0, (dataMatrix.cols(i,i+1).minus(centerVector)).getDDRM().getData());

        }
    }

    //Makes the final pre-processing modifications to the data and feeds it into the Neural Network.
    public static String analyzeData(SimpleMatrix dataMatrix, Feedforward nNet,SimpleMatrix axisMatrix,SimpleMatrix dataSaveMatrix, int dataSaveCounter,boolean bora){


        centerData(dataMatrix, axisMatrix.extractVector(false, 2).scale(9.8));

        if(!bora) {
            //Takes the data collector's axis and rotates the data to the real world axis.
            orientedFinalData = axisMatrix.transpose().mult(dataMatrix);

        } else {
            orientedFinalData.set(dataMatrix);
        }

        //Saving data...
        CommonOps_DDRM.insert(orientedFinalData.getDDRM(),dataSaveMatrix.getDDRM(),0,dataSaveCounter);

        //Checks if there is constant, then assumes its idle.
        if(checkConstantData(dataMatrix)){
            return("Idle");
        }
        //Checks if there is a high acceleration on de X axis.
        double average_X = average(orientedFinalData).get(0);
        if (average_X > minAccelerationValue) {
            return ("Acelerando");
        }

        double[] orientedDataArray = orientedFinalData.getDDRM().data;

        //Adds gravity's acceleration to the Z-axis.
        int dataSize = dataMatrix.numCols();
        for(int i = 0;i<dataSize;i++){

            orientedDataArray[i+dataSize*2]+=9.8;

        }

        //Feeds the data to the Neural Network.
        SimpleMatrix a = new SimpleMatrix(3*dataSize,1, true,orientedDataArray);
        SimpleMatrix output = Compite.eval(nNet.output(a).transpose());

        return checkResult(output);
    }

    //Checks if the axis is correctly orientated because SVD has two possible reults: correct axis and (correct axis) * -1
    //The dataMatrix is already centered.
    public static SimpleMatrix checkSVDOrientation(SimpleMatrix dataMatrix, SimpleMatrix axisMatrix){

        SimpleMatrix orientatedData = axisMatrix.mult(dataMatrix);

        double average_X = average(orientatedData).get(0);
        if(average_X > 0) return axisMatrix;
        axisMatrix.cols(0,1).scale(-1);
        return axisMatrix;
    }

    //Returns the Tag of the given output from the NN.
    public static String checkResult(SimpleMatrix s){
        if(s.get(0) == 1){
            return("Cambio de Marcha");
        } else if(s.get(1)==1){
            return("Giro Derecha");
        } else if(s.get(2)==1){
            return("Giro Izquierda");
        } else{
            return("Frenada");
        }
    }

    //Returns the matrix = [averageX;averageY;averageZ]
    public static SimpleMatrix average(SimpleMatrix matrix){

        int cols = matrix.numCols();
        int rows = matrix.numRows();
        SimpleMatrix averageMatrix = new SimpleMatrix(rows,1,MatrixType.DDRM);

        for(int i = 0;i<rows;i++) {
            averageMatrix.set(i, CommonOps_DDRM.elementSum(matrix.rows(i, i + 1).getDDRM())/cols);
        }
        return averageMatrix;
    }

}
