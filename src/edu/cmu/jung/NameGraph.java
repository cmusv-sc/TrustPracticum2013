package edu.cmu.jung;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.xml.bind.JAXBException;

import org.apache.commons.collections15.Transformer;

import edu.cmu.DBLPProcessor.Coauthorship;
import edu.cmu.DBLPProcessor.DBLPParser;
import edu.cmu.DBLPProcessor.DBLPUser;
import edu.cmu.dataset.DBLPDataSource;
import edu.cmu.dataset.DatasetInterface;
import edu.cmu.jung.Node;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

/**
 *
 * @author NASA-Trust-team
 */

public class NameGraph {

	static int edgeCount = 0;  
	Graph<Node, Edge> g;
	//DirectedGraph<Node, Edge> g;
	List<Node> nodes = new ArrayList<Node>();
	HashMap<String,DBLPUser> dblp;

	public NameGraph() {   
		DatasetInterface dblpDataset = new DBLPDataSource();
		dblp = dblpDataset.getDataset();		
	}

	/** Constructs an example directed graph with our vertex and edge classes 
	 * @throws JAXBException */
	public void constructGraph(String name) throws JAXBException {
		g = new SparseMultigraph<Node, Edge>();
		//g = new DirectedSparseMultigraph<Node, Edge>();
		createNodes(name);
		createEdges();     
	}

	private void createNodes(String name) throws JAXBException {
			String key = name;			
			DBLPUser author = dblp.get(key);
			System.out.println(author.getId());
			Node currentNode = new Node(author);
			nodes.add(currentNode);
			List<Coauthorship> c = author.getCoAuthors();
			for(int i =0;i<c.size();i++){
				for(Entry<String, Integer> entry : DBLPParser.mapUserNameId.entrySet()){
					if(entry.getValue() == c.get(i).getCoauthorid()){
						DBLPUser coauthor = dblp.get(entry.getKey());
						Node coauthorNode = new Node(coauthor);
						nodes.add(coauthorNode);
					}
				}
			}		
	}

	private void createEdges() throws JAXBException {
		int startingNodeNumber = 10;
		String key = nodes.get(0).getUser().getName();;
		DBLPUser author = dblp.get(key);
		startingNodeNumber = getNodeFromAuthor(author);		
		List<Coauthorship> c = author.getCoAuthors(); 

		for(int i =0;i<c.size();i++){
			for(Entry<String, Integer> entry : DBLPParser.mapUserNameId.entrySet()){
				if(entry.getValue() == c.get(i).getCoauthorid()){
					int endingNodeNumber;
					DBLPUser coauthor = dblp.get(entry.getKey());
					endingNodeNumber = getNodeFromAuthor(coauthor);	
					g.addEdge(new Edge(),nodes.get(startingNodeNumber), nodes.get(endingNodeNumber), EdgeType.UNDIRECTED);
				}
			}
		}
	}

	private int getNodeFromAuthor(DBLPUser author) {
		for(int i = 0; i<nodes.size(); i++)
			if(nodes.get(i).isThisPassedNode(author))
				return i;

		return 0;
	}

	/**
	 * @param args the command line arguments
	 * @throws JAXBException 
	 */
	public static void main(String[] args) throws JAXBException {
		NameGraph myApp = new NameGraph();
		myApp.constructGraph("Javier Chorro");		
		// This builds the graph
		Layout<Node, Edge> layout = new CircleLayout<Node, Edge>(myApp.g);
		layout.setSize(new Dimension(650,650));
		VisualizationViewer<Node, Edge> vv = new VisualizationViewer<Node, Edge>(layout);
		vv.setPreferredSize(new Dimension(700,700));       
		
		// Setup up a new vertex to paint transformer...
		Transformer<Integer,Paint> vertexPaint = new Transformer<Integer,Paint>() {
			public Paint transform(Integer i) {
				return Color.RED;
			}
		};  
		
		// Set up a new stroke Transformer for the edges
		float dash[] = {10.0f};
		final Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
//		Transformer<String, Stroke> edgeStrokeTransformer = new Transformer<String, Stroke>() {
//			public Stroke transform(String s) {
//				return edgeStroke;
//			}
//		};	
		
		vv.setVertexToolTipTransformer(new Transformer<Node, String>() {
			public String transform(Node e) {
				return "Name: " + e.getUser().getName() ;
			}
		});
		
		vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);        

		JFrame frame = new JFrame("Co-authorship Graph View");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(vv);
		frame.pack();
		frame.setVisible(true);     

	}

}
