package ignalau.appauto;

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

    static int debugCounter = 0;

    private static final double noiseValue = 2;

    //Checks if the data doesn't have much variations, taking into account the noise that could be.
    public static boolean checkConstantData(SimpleMatrix dataMatrix) {

        //Initialize the temp matrices for the calculations.
        SimpleMatrix maxMatrix = new SimpleMatrix(3,1, MatrixType.DDRM);
        SimpleMatrix minMatrix = new SimpleMatrix(3,1,MatrixType.DDRM);
        SimpleMatrix dataRangeMatrix = new SimpleMatrix(3,1,MatrixType.DDRM);;

        //Takes the max and the min of each row (movement on each axis) and then subtracts them to get
        //the range of variation in each axis.
        CommonOps_DDRM.maxRows(dataMatrix.getDDRM(),maxMatrix.getDDRM());
        CommonOps_DDRM.minRows(dataMatrix.getDDRM(),minMatrix.getDDRM());
        CommonOps_DDRM.subtract(maxMatrix.getDDRM(),minMatrix.getDDRM(),dataRangeMatrix.getDDRM());

        //Sums the absolute value of each axis variation range to get a number that describes the total variation.
        double dataRange = CommonOps_DDRM.elementSumAbs(dataRangeMatrix.getDDRM());

        return(dataRange < noiseValue);

    }


    //Modifies the data to reduce the noise in it, by making an average of a range of points near every point.
    public static SimpleMatrix maskData(SimpleMatrix matrix,int maskGrade){

        SimpleMatrix returnMatrix = matrix.copy();
        int cols = returnMatrix.numCols();
        int rows = returnMatrix.numRows();

        for(int i = 0;i<rows;i++){
            for(int j=0;j<cols-2*maskGrade-1;j++){

                returnMatrix.set(i,j+maskGrade,matrix.rows(i,i+1).cols(j,j+2*maskGrade+2).elementSum()/(2*maskGrade+1));

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
    public static String analyzeData(SimpleMatrix dataMatrix, Feedforward nNet,SimpleMatrix axisMatrix,SimpleMatrix debugMatrix,int debugWindow){

        //Takes the data collector's axis and rotates the data to the real world axis.
        centerData(dataMatrix,axisMatrix.extractVector(false,2).scale(9.8));
        SimpleMatrix orientedData = axisMatrix.mult(dataMatrix);
        double[] orientedDataArray = orientedData.getDDRM().data;

        //Adds gravity's acceleration to the Z-axis.
        int dataSize = dataMatrix.numCols();
        for(int i = 0;i<dataSize;i++){

            orientedDataArray[i+dataSize*2]+=9.8;

        }

        //########## Starts Debugging process ##########
        //TODO: Probar Concat para hacer esto mas eficiente.
        if(debugCounter == debugWindow) debugCounter = 0;

        for(int i=0;i<dataSize;i++){
            debugMatrix.setColumn(debugCounter,0,orientedData.cols(i,i+1).getDDRM().getData());
            debugCounter++;
        }
        //########## Finish Debugging process ##########

        //Feeds the data to the Neural Network.
        SimpleMatrix a = new SimpleMatrix(3*dataSize,1, true,orientedDataArray);
        SimpleMatrix output = Compite.eval(nNet.output(a).transpose());

        return checkResult(output);

    }

    //Returns the Tag of the given output from the NN.
    public static String checkResult(SimpleMatrix s){
        if(s.get(0)==1){
            return("Cambio de Marcha");
        } else if(s.get(1)==1){
            return("Giro Derecha");
        } else if(s.get(2)==1){
            return("Giro Izquierda");
        } else{
            return("Frenada");
        }
    }


}
