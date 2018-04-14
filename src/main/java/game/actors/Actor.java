package game.actors;

import java.awt.Rectangle;

import game.Game;
import game.GameArena;
import game.Vec2;
import lobby.Protocol;

public abstract class Actor{
	public static final int TILE = -1;
	public static final int PLAYER = 0;
	public static final int FIREBALL = 1;
	
	protected Vec2 			position;
	private Rectangle 		hitbox;
	private Rectangle 		lowerBox;
	private int 			id;
	private byte 			animation; // animation row
	private int 			angle; // sprite rotation
	private boolean			destroyed;

	public abstract void update(long delta);	
	public abstract void resolve_collision(long delta, Actor a);
	
	public Actor(float x, float y, int w, int h, int lh, int id) {
		position = new Vec2(x,y);
		hitbox = new Rectangle((int)x,(int)y,w,h);
		lowerBox = new Rectangle((int)x, (int)y + (h - lh) , w, lh); // collision box dlya nog
		this.id = id;
		this.destroyed = false;
	}
	
	public Actor(float x, float y, int w, int h, int id) {
		position = new Vec2(x, y);
		hitbox = new Rectangle((int)x, (int)y, w, h);
		lowerBox = null;
		this.id = id;
		this.destroyed = false;
	}
	
	public void setPosition(int x, int y) {
		position.set(x, y);
		hitbox.setLocation(x, y);
		if(lowerBox != null)
			lowerBox.setLocation(x, y + (hitbox.height - lowerBox.height) );
	}
	
	
	public void addPosition(Vec2 p) {
		position.add(p);
		hitbox.setLocation((int)position.getX(), (int)position.getY());
		if(lowerBox != null)
			lowerBox.setLocation((int)position.getX(), (int)position.getY() + (hitbox.height - lowerBox.height) );
	}
	
	public void destroy() { destroyed = true; }
	public boolean isDestroyed() {return destroyed;}
	public Rectangle getHitbox() {return hitbox;}
	public Rectangle getLowerBox() {return lowerBox;}
	public Vec2 getPosition() {return position;}
	public int getId() {return id;}
	public Protocol.Client.Actor getState() {
		Protocol.Client.Actor state = new Protocol.Client.Actor();
		state.id = getId();
		state.type = getType();
		state.x = Math.round(getPosition().getX());
		state.y = Math.round(getPosition().getY());
		state.animation = getAnimation();
		state.angle = getAngle();

		return state;
	}
	public abstract int getType();

	protected void setAnimation(byte animation) {this.animation = animation;}
	protected byte getAnimation() {return animation;}
	protected void setAngle(int angle) {this.angle = angle;}
	protected int getAngle() {return angle;}
}
