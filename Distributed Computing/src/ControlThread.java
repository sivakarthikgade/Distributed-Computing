
public class ControlThread extends Thread{
	public static Graph g;
	ControlThread(){}
	
	public void run(){
		while(NodeThread.getNumFinished() != Graph.n){
			try {
				sleep(1000);
			} catch(InterruptedException e) {
				//do Nothing
			}
		}
	}
}
