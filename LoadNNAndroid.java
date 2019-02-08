package ignalau.appauto;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import org.gitia.froog.layer.Layer;
import org.gitia.froog.util.Open;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.ejml.simple.SimpleMatrix;
import org.gitia.froog.Feedforward;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Matías Roodschild <mroodschild@gmail.com>
 *
 */

//Generates a Feed-forward NN from an Input Stream given by the Java function "getAssets.open()".
public class LoadNNAndroid {

        private static InputStream archivo = null;
        private static Feedforward net;

        @RequiresApi(api = Build.VERSION_CODES.N)
        public static Feedforward getNet(InputStream file) {
            net = new Feedforward();
            archivo = file;
            if (archivo != null) {
                agregarCapas();
            }
            return net;
        }

        private static String obtenerNodoValor(String strTag, Element eNodo) {
            Node nValor = (Node) eNodo.getElementsByTagName(strTag).item(0).getFirstChild();
            return nValor.getNodeValue();
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        private static void agregarCapas() {

            try {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(archivo);//new File(archivo.getAbsolutePath()));
                doc.getDocumentElement().normalize();

                //Creates a list of Layer Nodes.
                NodeList layers = doc.getElementsByTagName("layer");

                //Goes through every Layer.
                for (int i = 0; i < layers.getLength(); i++) {

                    //Creates a Node to get the first element.
                    Node layerNode = layers.item(i);
                    if (layerNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element layerElement = (Element) layerNode;
                        String function = obtenerNodoValor("transferFunction", layerElement);
                        String bias = obtenerNodoValor("bias", layerElement);
                        String weight = obtenerNodoValor("w", layerElement);
                        String[] biases = bias.split(",");
                        double[] b = new double[biases.length];
                        for (int j = 0; j < b.length; j++) {
                            b[j]=Double.valueOf(biases[j]);
                        }

                        String[] ws = weight.split(",");
                        double[] w = new double[ws.length];
                        for (int j = 0; j < w.length; j++) {
                            w[j]=Double.valueOf(ws[j]);
                        }
                        net.addLayer(new Layer(
                                new SimpleMatrix(b.length, w.length / b.length, true, w),
                                new SimpleMatrix(b.length, 1, true, b),
                                function));
                    }
                }
             //### Layer creation ending ###
            } catch (SAXException | IOException | ParserConfigurationException ex) {
                Logger.getLogger(Open.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }



