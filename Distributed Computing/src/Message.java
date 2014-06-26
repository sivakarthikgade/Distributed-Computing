
enum Type{update,negAck,posAck,done,startRound,doneRound,dummy,updAndNegAck};

public class Message {
	int source;
	int dest;
	int leader;
	Type type;
	int round;
	
	Message(int source, int dest,int leader,Type type,int round){
		this.source = source;
		this.dest = dest;
		this.leader = leader;
		this.type = type;
		this.round = round;
	}
	
	public String toString() {
		return (this.source+1) + "_" + (this.dest+1) + "_" + (this.leader+1) + "_" + this.type + "_" + this.round;
	}
}
