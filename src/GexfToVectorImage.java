
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.TimeUnit;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.preview.PDFExporter;
import org.gephi.io.exporter.preview.SVGExporter;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.AutoLayout;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.fruchterman.FruchtermanReingold;
import org.gephi.layout.plugin.fruchterman.FruchtermanReingoldBuilder;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author PHIRADET
 */
public class GexfToVectorImage {
    
    public final static int OUTPUT_PDF = 0;
    public final static  int OUTPUT_SVG = 1;
    
    public final static  int LAYOUT_YIFANHU = 2;
    public final static int LAYOUT_FRUCHTERMAN = 3;
    
    private ProjectController pc;
    private Workspace workspace;
    
    public GexfToVectorImage() {
        pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        workspace = pc.getCurrentWorkspace();
    }
    
    private void importFile(String filename)
    {
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        Container container = null;
        
        try{
            System.out.println("Start reading gexf file");
            File file = new File(filename);
            System.out.println("Finishing reading gexf file");
            container = importController.importFile(file);
            container.getLoader().setEdgeDefault(EdgeDefault.UNDIRECTED);
            container.setAllowAutoNode(false);
        }
        catch (Exception ex)
        {
            System.out.println("Error during reading the file:");
            ex.printStackTrace();
        }
        importController.process(container, new DefaultProcessor(), workspace);
    }
    
    private void applyLayout(GraphModel graphModel, int layoutType)
    {
        //Layout
        AutoLayout autoLayout = new AutoLayout(10, TimeUnit.SECONDS);
        autoLayout.setGraphModel(graphModel);
        
        if(layoutType == LAYOUT_FRUCHTERMAN)
        {
            FruchtermanReingold layout = new FruchtermanReingold(new FruchtermanReingoldBuilder());
            autoLayout.addLayout(layout, 1.0f);
        }
        else if(layoutType == LAYOUT_YIFANHU)
        {
            YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
            autoLayout.addLayout(layout, 1.0f);
        }
        autoLayout.execute();
    }
    
    void export(String gexfFile, String outputFile,int outputType, int layoutType) throws Exception
    {
        if(outputType != OUTPUT_PDF && outputType != OUTPUT_SVG)
        {
            throw new Exception("Only support PDF or SVG format");
        }
        
        if(layoutType != LAYOUT_YIFANHU && layoutType != LAYOUT_FRUCHTERMAN)
        {
            throw new Exception("Only support Fruchterman Reingold or Yifan Hu layout");
        }
        
        importFile(gexfFile);
        
        //See if graph is well imported
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        UndirectedGraph graph = graphModel.getUndirectedGraph();
        System.out.println("Nodes: " + graph.getNodeCount());
        System.out.println("Edges: " + graph.getEdgeCount());
        
        //Edit node's color according to a attribute
        for(Node n: graph.getNodes())
        {
            float labelNum = Float.parseFloat(n.getAttributes().getValue("label").toString());
            if(labelNum%2==0)
                n.getNodeData().setColor(1f, 0f, 0f);
            else
                n.getNodeData().setColor(0f, 0f, 1f);
            n.getNodeData().setSize(5f);
        }
        
        applyLayout(graphModel, layoutType);
        
        //Get preview object
        PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();
        PreviewProperties prop = model.getProperties();
        prop.putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.FALSE);
        prop.putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.GRAY));
        prop.putValue(PreviewProperty.EDGE_THICKNESS, new Float(0.1f));
        prop.putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
        
        
        //Export
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        if(outputType==OUTPUT_PDF)
        {
            try{
                ec.exportFile(new File(outputFile));
            }catch (IOException ex)
            {
                ex.printStackTrace();
            }
        
            System.out.println("Start printing");
            PDFExporter pdfExporter = (PDFExporter) ec.getExporter("pdf");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            ec.exportStream(baos, pdfExporter);
        }
        else if(outputType==OUTPUT_SVG)
        {
            SVGExporter svg = (SVGExporter)ec.getExporter("svg");
            svg.setWorkspace(workspace);
            try {
                svg.setWriter(new FileWriter(outputFile));
                svg.execute();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        System.out.println("Finished printing");
    }
}
