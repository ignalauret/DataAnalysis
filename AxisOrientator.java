package ignalau.appauto;


import android.util.Log;

import org.ejml.data.MatrixType;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.SingularOps_DDRM;
import org.ejml.simple.SimpleMatrix;

/**
 *
 * @author Ignacio Lauret <ignalauret@gmail.com>
 *
 */

public class AxisOrientator {

    /*
    *I receive a data Matrix (already centered) and return the direction vector that points to the
    * most increasing direction of the Data.
    * This relies on the use of my first column in the U matrix from my SV Decomposition of the data Matrix.
    */
    public static SimpleMatrix calcularSVD(SimpleMatrix dataMatrix, String axis) {

        SimpleMatrix returnAxis = new SimpleMatrix(3, 1, MatrixType.DDRM);

        int range = SingularOps_DDRM.rank(dataMatrix.getDDRM());
        double condition = dataMatrix.conditionP2();

        //I check if my matrix's Range and Condition satisfy the SVD conditions.
        if (range > 1 && condition < 100) {

            //I get my U matrix from my SV Decomposition and return the first column.
            SimpleMatrix svdMatrix_U = dataMatrix.svd(false).getU();
            CommonOps_DDRM.extractColumn(svdMatrix_U.getDDRM(), 0, returnAxis.getDDRM());

            printAxis(returnAxis, axis + "_axis");
            return returnAxis;

        } else {
            //If I don't satisfy SVD conditions, I return an error.
            //TODO: Por implementar...
            Log.d("Error", "SVD conditions unsatisfied");
            Log.d("Error", "Range: "+ Integer.toString(range));
            Log.d("Error", "Condition: " + Double.toString(condition));

            return null;
        }
    }


    //If I already got two axis, I can calculate the third one by doing the cross product of the first two,
    //relying on the fact that they are orthogonal vectors.
    public static SimpleMatrix getThirdAxis(SimpleMatrix axis1, SimpleMatrix axis2,String tag) {

        SimpleMatrix returnAxis;

        returnAxis = crossProduct(axis1, axis2);

        normalize(returnAxis);
        printAxis(returnAxis,tag + "_axis");
        return returnAxis;

    }

    //If I've got constant data it gets the vector where that data is pointing to.
    public static SimpleMatrix getAverageAxis(SimpleMatrix dataMatrix,int dataSize,String tag){

        SimpleMatrix returnAxis;

        //It uses the average of each axis to get an estimation of the coordinates of the direction vector.
        double average_x = dataMatrix.rows(0,1).elementSum()/dataSize;
        double average_y = dataMatrix.rows(1,2).elementSum()/dataSize;
        double average_z = dataMatrix.rows(2,3).elementSum()/dataSize;

        returnAxis = new SimpleMatrix(3,1,true,new double[]{average_x,average_y,average_z});

        normalize(returnAxis);
        printAxis(returnAxis,tag + "_axis");
        return returnAxis;

    }


    //Makes the matrix's norm = 1.
    public static void normalize(SimpleMatrix matrix){

        matrix.set(matrix.divide(matrix.normF()));
    }


    //My implementation of the Cross Product.
    public static SimpleMatrix crossProduct(SimpleMatrix v1, SimpleMatrix v2){

        SimpleMatrix returnAxis = new SimpleMatrix(3,1,MatrixType.DDRM);

        double[] x = v1.getDDRM().getData();
        double[] z = v2.getDDRM().getData();

        returnAxis.setColumn(0,0, x[1]*z[2]-x[2]*z[1],x[0]*z[2]*(-1)+x[2]*z[0],x[0]*z[1]-x[1]*z[0]);
        return returnAxis;

    }


    //Shows on console the given 3-Dimensional Vector.
    public static void printAxis(SimpleMatrix axis,String tag){

        Log.d(tag,"[" + Double.toString(axis.get(0)) + ","+Double.toString(axis.get(1))+","+Double.toString(axis.get(2))+"]");

    }

}
