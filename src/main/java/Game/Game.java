package Game;

import org.apache.commons.lang3.tuple.Pair;

import javax.websocket.Session;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class Game implements Runnable {
    private volatile Queue<Pair<Session, ByteBuffer>> messages;
    private Map<Session, Integer> sessions;
    private GameWorld gw;
    
    public Game(Queue<Pair<Session, ByteBuffer>> messages, Map<Session, Integer> sessions) {
        this.messages = messages;
        this.sessions = sessions;
        gw = new GameWorld();
    }

    public GameWorld getWorld() {return gw;}
    
    @Override
    public void run() {
        try {
            final int FPS = 60;
            int frameCount = 0;
            boolean running = true;

            while (running) {
                long frameStartTime = System.currentTimeMillis();

                while (!messages.isEmpty()) {
                	synchronized (messages) {
                		Pair<Session, ByteBuffer> message = messages.remove();
                		ByteBuffer buffer = message.getRight();
                        Session session = message.getLeft();
                        int id = sessions.get(session);
                        Player player = gw.getPlayer(id);
                        byte gameCmd = buffer.get(2);
                        if (gameCmd == Protocol.Server.Game.INPUT) {
                        	byte key = buffer.get(3);
                        	player.getInput().press(key);
                        	player.update();
                        	//System.out.println(player.getPosition());
                        }
                	}
                }
                if(frameCount % 1 == 0)
                	sendWorldState();
                long frameElapsedTime = System.currentTimeMillis() - frameStartTime;
                long frameRemainingTime = 1000/FPS - frameElapsedTime;
                if (frameRemainingTime > 0)
                    Thread.sleep(frameRemainingTime);
                frameCount++;
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    private void sendWorldState() {
    	if (gw.getActors().size() > 0) {
    		ByteBuffer b = ByteBuffer.allocate(gw.getActors().size()*17 + 3);
	    	b.put(Protocol.Client.GAME_MSG);
	    	b.put(Protocol.Client.Game.WORLD_STATE);
	    	b.put((byte) gw.getActors().size());
	    	for (Actor a : gw.getActors()) {
    			b.put(a.getState());	
	    	}
	    	b.flip();
	    	for(Session s : sessions.keySet()) {
	    		try{
	    			s.getBasicRemote().sendBinary(b);
	    		}catch(Exception e) {
	    			System.out.println(e.getMessage());
	    		}
	    	}
    	}
    }
    
    private void parseMessage(ByteBuffer msg) {
    	
    }
}
