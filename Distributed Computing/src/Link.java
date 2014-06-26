
import java.util.LinkedList;

public class Link {

	private static final int unitTimeMillis = 10;
	public LinkedList<Message> msg;
	public LinkedList<Long> delay;
	
	public Link() {
		this.msg = new LinkedList<Message>();
		this.delay = new LinkedList<Long>();
	}

	public void addLast(Message m) {
		this.msg.addLast(m);
		this.delay.addLast(System.currentTimeMillis() + getMsgDelay()*unitTimeMillis);
	}
	
	public Message pop(int round) {
		Long delay = this.delay.peek();
		if((delay != null) && (delay <= System.currentTimeMillis())) {
			if((this.msg.peek().round <= round)) {
				this.delay.removeFirst();
				return this.msg.removeFirst();
			}
		}
		return null;
	}
	
	private int getMsgDelay() {
		return (int) Math.floor(Math.random()*25);
	}
}
