
import java.util.LinkedList;


public class NodeThread extends Thread {
	public static Graph g;
	public static int numFinished;
	private int uid;
	private int outNeighbors[];
	private LinkedList<Integer> child;
	private LinkedList<Integer> notChild;
	private int maxUidSeen;
	private int parentUid;
	private Message[] messagesToBeSent;
	private boolean acksFromOutNeighbors[];
	private boolean done;
	private int round;
	
	NodeThread(int uid,int outNeighbors[]){
		this.round = 0;
		this.uid = uid;
		this.maxUidSeen = uid;
		this.parentUid = uid;
		this.outNeighbors = outNeighbors;
		this.acksFromOutNeighbors = new boolean[this.outNeighbors.length];
		this.child = new LinkedList<Integer>();
		for(int i=0; i<this.outNeighbors.length; i++){
			this.child.add(this.outNeighbors[i]);
		}
		this.notChild = new LinkedList<Integer>();
		this.done = false;
		this.messagesToBeSent = new Message[this.outNeighbors.length];
		for(int i=0; i<this.outNeighbors.length; i++){
			this.messagesToBeSent[i] = new Message(this.uid,this.outNeighbors[i],this.maxUidSeen,Type.update,1);
		}
	}
	
	public void run(){
		while(true) {
			this.round++;
			gen_mes();
			if(this.done){
				finished();
				break;
			}
			proc_mes();
		}
	}
	private void gen_mes(){
		Message m;
		for(int i=0; i<this.outNeighbors.length; i++){
			m = this.messagesToBeSent[i];
			g.pushMessage(m.dest,m);
		}
	}
	private void proc_mes(){
		for(int i=0; i<this.outNeighbors.length; i++){	//Adding dummy messages as place holders. If they don't get overwritten, they will be sent across.
			this.messagesToBeSent[i] = new Message(this.uid,this.outNeighbors[i],this.maxUidSeen,Type.dummy,this.round+1);
		}
		Message m;
		int msgRecCnt = 0;
		while(msgRecCnt < this.outNeighbors.length){
			m = g.popMessage(this.uid,this.round);
			if(m != null) {
				msgRecCnt++;
				if(m.type == Type.dummy) {
					continue;
				} else if(m.type == Type.update) {
					if(m.leader == this.maxUidSeen) {
						for(int i=0; i<this.outNeighbors.length; i++) {
							if(m.source == this.outNeighbors[i]) {
								if(this.messagesToBeSent[i].type == Type.update) {
									for(int j=0; j<this.child.size(); j++){
										if(m.source == this.child.get(j)){
											this.child.remove(j);
											this.notChild.add(m.source);
										}
									}
								}
								this.messagesToBeSent[i] = new Message(this.uid,m.source,this.maxUidSeen,Type.negAck,this.round+1);
							}
						}
					} else if(m.leader > this.maxUidSeen) {
						this.maxUidSeen = m.leader;
						this.parentUid = m.source;
						this.acksFromOutNeighbors = new boolean[this.outNeighbors.length];
						for(int i=0; i<this.notChild.size(); i++){ //Since we have received a new leader, assume all out neighbors are children again.
							this.child.add(this.notChild.get(i));
						}
						this.notChild = new LinkedList<Integer>();
						for(int i=0; i<this.child.size(); i++){ // Remove parent if it one of your outneighbors.
							if(this.child.get(i) == this.parentUid){
								this.notChild.add(this.parentUid);
								this.child.remove(i);
							}
						}
						for(int i = 0; i < this.outNeighbors.length; i++) { // Send update to all assumed children, if they are a child they will send a posAck back to me and they will be added to child list.
							if(this.outNeighbors[i] == m.source) {
								if(this.outNeighbors.length == 1) {
									this.messagesToBeSent[i] = new Message(this.uid,this.outNeighbors[i],this.maxUidSeen,Type.posAck,this.round+1);
								}
							} else {
								this.messagesToBeSent[i] = new Message(this.uid,this.outNeighbors[i],this.maxUidSeen,Type.update,this.round+1);
							}
						}
					}
				} else if(m.type == Type.negAck) {
					for(int i=0; i<this.child.size(); i++){
						if(m.source == this.child.get(i)){
							this.child.remove(i);
							this.notChild.add(m.source);
						}
					}
				} else if(m.type == Type.posAck) {
					if(this.messagesToBeSent[0].type != Type.update) {
						int numAcks = 0;
						for(int i=0; i<this.outNeighbors.length; i++){
							if(m.source == this.outNeighbors[i]){
								this.acksFromOutNeighbors[i] = true;
							}
						}
						if(!this.child.contains(m.source)) {
							this.child.add(m.source);
						}
						for(int i = 0; i < this.outNeighbors.length; i++) {
							if(this.acksFromOutNeighbors[i] == true) {
								numAcks++;
							}
						}
						if(this.maxUidSeen == this.uid && numAcks == this.outNeighbors.length) {
							this.done = true;
							for(int i=0; i<this.outNeighbors.length; i++) {
								this.messagesToBeSent[i] = new Message(this.uid,this.outNeighbors[i],this.uid,Type.done,this.round+1);
							}
							break;
						} else if(numAcks == this.child.size()) {
							for(int i=0; i<this.outNeighbors.length; i++) {
								if(this.parentUid == this.outNeighbors[i]) {
									if(this.messagesToBeSent[i].type != Type.update) {
										this.messagesToBeSent[i] = new Message(this.uid,this.parentUid,this.maxUidSeen,Type.posAck,this.round+1);
									}
								}
							}
						}
					}
				} else if(m.type == Type.done) {
					this.done = true;
					for(int i = 0; i < this.outNeighbors.length; i++) {
						this.messagesToBeSent[i] = new Message(this.uid, this.outNeighbors[i],this.maxUidSeen,Type.done,this.round+1);
					}
					break;
				}
			}
		}
		if(this.child.size() == 0){// Check if you have no children, if so send posAck to parent
			for(int i=0; i<this.outNeighbors.length; i++) {
				if(this.parentUid == this.outNeighbors[i]) {
					this.messagesToBeSent[i] = new Message(this.uid,this.parentUid,this.maxUidSeen,Type.posAck, this.round+1);
				}
			}
		}
	}
	public static synchronized int getNumFinished(){
		return numFinished;
	}
	private synchronized void finished(){
		synchronized (System.out){
			if(this.maxUidSeen == this.uid){
				System.out.print("I am the leader with UID = "+(this.uid+1));
			}
			else{
				System.out.print("My UID = "+(this.uid+1)+", my leader UID= "+(this.maxUidSeen+1)+", my parent UID= "+(this.parentUid+1));
			}
			System.out.print(", my children are {");
			if(this.child.size() == 0){
				System.out.println("none}");
			}
			else{
				for(int i=0; i<this.child.size(); i++){
					System.out.print((this.child.get(i)+1));
					if(i < this.child.size()-1){
						System.out.print(",");
					}
					else{
						System.out.println("}");
					}
				}
			}
		}
		NodeThread.numFinished++;
	}
}
