
import java.io.File;
import java.util.Scanner;

public class Graph {
	public static double AdjacencyMatrix[][];
	public static int n;
	public static int numFinished;
	public static Link[][] incQueues;
	
	Graph(String file){
		// Control thread will be represented by index n+1 which will have edges to all other nodes and from all other nodes.
		try {
			Graph.numFinished = 0;
			Scanner s = new Scanner(new File(file));
			n = Integer.parseInt(s.nextLine());
			incQueues = new Link[n][n];
			AdjacencyMatrix = new double[n][n];
			for(int i=0; i<n; i++){
				String line[] = s.nextLine().split("[,\\s]");
				for(int j=0; j<line.length; j++){
					AdjacencyMatrix[i][j] =Double.parseDouble(line[j]);
					if(AdjacencyMatrix[i][j] != 0) {
						incQueues[j][i] = new Link();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void pushMessage(int dest,Message m){
		incQueues[dest][m.source].addLast(m);
	}
	public synchronized Message popMessage(int id,int round){
		Message m = null;
		for(int i = 0; (i < incQueues[id].length) && (m == null); i++) {
			if(incQueues[id][i] != null) {
				m = incQueues[id][i].pop(round);
			}
		}
		return m;
	}
	public synchronized void finished(){
		Graph.numFinished++;
	}
	public synchronized int numberFinished(){
		return Graph.numFinished;
	}
}
