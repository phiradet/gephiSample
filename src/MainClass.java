
import org.openide.util.Exceptions;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author PHIRADET
 */
public class MainClass {
    public static void main(String[] args)
    {
        GexfToVectorImage g = new GexfToVectorImage();
        String gexfFile = "./data/Karate.gexf";
        String outputFile = "./data/Karate.pdf";
        System.out.print("Starting..");
        try {
            g.export(gexfFile, outputFile, GexfToVectorImage.OUTPUT_PDF, GexfToVectorImage.LAYOUT_YIFANHU);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
