
import java.util.ArrayList;

public class Main {
	
	public static void main(String[] args){
		if(args.length !=1){
			System.out.println("Program takes one argument.");
			System.exit(0);
		}
		
		Graph g = new Graph(args[0]);
		Thread nodes[] = new Thread[Graph.n+1];
		NodeThread.g = g;
		NodeThread.numFinished = 0;
		ControlThread.g = g;
		for(int i=0; i<Graph.n+1; i++){
			
			if(i == Graph.n){
				nodes[i] = new ControlThread();
				nodes[i].start();
			}
			else{
				ArrayList<Integer> outNeighbors = new ArrayList<Integer>();
				
				for(int j=0; j<Graph.n; j++){// Get out neighbors
					if(Graph.AdjacencyMatrix[i][j]>0.0){
						outNeighbors.add(j);
					}
				}
				
				int outNeighborsInt[] = new int[outNeighbors.size()];
				for(int j=0; j<outNeighbors.size(); j++){
					outNeighborsInt[j] = outNeighbors.get(j);
				}
				
				nodes[i] = new NodeThread(i,outNeighborsInt);
				nodes[i].start();
			}
		}
		
		for(int i=0; i<nodes.length; i++){
			try {	
				nodes[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}