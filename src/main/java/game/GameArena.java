package game;

import java.util.ArrayList;
import java.util.Collections;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import game.actors.Player;

public class GameArena {
	private static final int INF = 999999999;
	
	private int[][] collision_map;
	private int w, h, tileSize;

	public GameArena(String mapJson) {
        JsonParser parser = new JsonParser();
        JsonArray layers = parser.parse(mapJson).getAsJsonObject().getAsJsonArray("layers");
        //iterate through each layer
        for (JsonElement layer : layers) {
            if(layer.getAsJsonObject().getAsJsonPrimitive("name")	//find layer with name "Collision"
                    .getAsString().equals("Collision"))
            {
                //might be useful if someone will modify the game to accept arenas with different sizes
                h = layer.getAsJsonObject().getAsJsonPrimitive("height").getAsInt();	//get height
                w  = layer.getAsJsonObject().getAsJsonPrimitive("width").getAsInt();	//get width
                tileSize = Constants.GAME_TILE_SIZE;
                JsonArray data = layer.getAsJsonObject().getAsJsonArray("data");	//contains collision map
                collision_map = new int[h][w];
                for(int i = 0; i < h; i++) {
                    for(int j = 0; j < w; j++) {
                        if (data.get(w*i + j).getAsInt() != 0)
                            collision_map[i][j] = 1;
                    }
                }
                break;
            }
        }
	}
	
	public int getEntry(int y, int x) {
		if (0 <= x && x < w && 0 <= y && y < h)
			return collision_map[y][x];
		return 1;
	}
	public int getWidth() {return w * tileSize;}
	public int getHeight() {return h * tileSize;}
	public int getTilesWidth() {return w;}
	public int getTilesHeight() {return h;}
	public int getTileSize() {return tileSize;}
	
	// Return sequence of target coordinates
	public ArrayList<TileNode> aStar(int x_target, int y_target, Player player) {
		
		int x_init = (int) player.getLowerCenter().getX() / tileSize;
		int y_init = (int) player.getLowerCenter().getY() / tileSize;
		
		ArrayList<TileNode> open = new ArrayList<TileNode>();
		ArrayList<TileNode> closed = new ArrayList<TileNode>();
		boolean toSkip = false;
		
		TileNode initTile = new TileNode(x_init, y_init, null);
		initTile.setG(0);
		initTile.setH(0);
		initTile.setF();
		open.add(initTile);
		
		while(!open.isEmpty()) {
			Collections.sort(open);
			TileNode q = open.remove(0);
			ArrayList<TileNode> successors = q.generateSuccessors(collision_map);
			
			for(TileNode i : successors) {
				
				if(i.getX() == x_target && i.getY() == y_target) {
					closed.add(i);
					return generateTargets(closed, player);
				}
				i.setG(q.getG() + 1);
				i.setH(Math.sqrt(Math.pow(Math.abs(i.getX() - x_target), 2) + Math.pow(Math.abs(i.getY() - y_target), 2)));
				i.setF();
				
				toSkip = false;
				
				for (TileNode j : open) {
					if (j.isSame(i)) {
						if (j.getF() <= i.getF()) {
							toSkip = true;
							break;
						}
					}
				}
				for (TileNode j : closed) {
					if (j.isSame(i)) {
						if (j.getF() <= i.getF()) {
							
							toSkip = true;
							break;
						}
					}
				}
				if (!toSkip) {
					open.add(i);
				}
			}
			closed.add(q);
		}
		
		return closed;
	}
/*
	private ArrayList<TileNode> generateTargets(ArrayList<TileNode> tiles) {
		ArrayList<TileNode> turns = new ArrayList<TileNode>();
		TileNode target = tiles.get(tiles.size() - 1);
		turns.add(target.convert());
		TileNode check = target.getParent().getParent();
		while (check != null) {
			// Detect turn
			if (check.getX() != target.getX() && check.getY() != target.getY()) {
				turns.add(target.getParent().convert());
			}
			target = target.getParent();
			check = check.getParent();
		}
		turns.add(target.getParent().convert());
		
		return turns;
	}
	*/
	
	private ArrayList<TileNode> generateTargets(ArrayList<TileNode> tiles, Player player) {
		ArrayList<TileNode> turns = new ArrayList<TileNode>();
		float x0;
		float y0;
		int x1;
		int y1;
		
		TileNode target = tiles.get(tiles.size() - 1);
		turns.add(target.convert());
		TileNode check = target.getParent().getParent();
		while (check != null) {
			// Detect turn
			if (check.getX() != target.getX() && check.getY() != target.getY()) {
				turns.add(target.getParent().convert());
			}
			target = target.getParent();
			check = check.getParent();
		}
		turns.add(target.getParent().convert());
		
		
		ArrayList<TileNode> path = new ArrayList<TileNode>();
		
		TileNode init = turns.get(turns.size() - 1);
		TileNode dest = turns.get(0);
		
		// Since aStar uses upper-left corner, sometimes right side of collision box can collide.
		// Thus, we need to check all four corners of hit-box for collision between first and second turns
		// TODO instead of init, this should use players current position
		x0 = (float) player.getLowerCenter().getX();
		y0 = (float )player.getLowerCenter().getY();
		
		x1 = turns.get(turns.size() - 2).getX();
		y1 = turns.get(turns.size() - 2).getY();
		if (checkCollision(x0, y0, x1, y1)) {
			TileNode start = new TileNode(init);
			path.add(start);
		}
		
		
		int i = 0;
		while (!init.getCoordinates().equals(dest.getCoordinates())) {
			x0 = (float) init.getX();
			y0 = (float) init.getY();
			x1 = dest.getX();
			y1 = dest.getY();
			if (checkCollision(x0, y0, x1, y1)) {
				++i;
				dest = turns.get(i);
			} else {
				TileNode next = new TileNode(dest);
				path.add(next);
				init = new TileNode(dest);
				i = 0;
				dest = turns.get(i);
			}
		}
		return path;
	}
	

	public boolean checkCollision(float x0, float y0, int x_target, int y_target) {
		boolean collides = false;
		float x1 = (float)x_target;
		float y1 = (float)y_target;
		// TODO make player width, height and lower height constants in Player.java ?
		//float dy1 = 20;
		//float dy2 = 40;
		//float dx = 20;
		float dy = Constants.PLAYER_LOWER_HEIGHT/2.0f;
		//float dy2 = Constants.PLAYER_HEIGHT;
		float dx = Constants.PLAYER_WIDTH/2.0f;
		ArrayList<TileNode> lineTiles = new ArrayList<TileNode>();
		// Checks all four lines from each corner of hit-box
		/*
		lineTiles.addAll(raytrace((x0)/tileSize, (y0 + dy1)/tileSize, (x1)/tileSize, (y1 + dy1)/tileSize));
		lineTiles.addAll(raytrace((x0)/tileSize, (y0 + dy2)/tileSize, (x1)/tileSize, (y1 + dy2)/tileSize));
		lineTiles.addAll(raytrace((x0 + dx)/tileSize, (y0 + dy1)/tileSize, (x1 + dx)/tileSize, (y1 + dy1)/tileSize));
		lineTiles.addAll(raytrace((x0 + dx)/tileSize, (y0 + dy2)/tileSize, (x1 + dx)/tileSize, (y1 + dy2)/tileSize));
		*/
		lineTiles.addAll(raytrace((x0 - dx)/tileSize, (y0 - dy)/tileSize, (x1 - dx)/tileSize, (y1 - dy)/tileSize));
		lineTiles.addAll(raytrace((x0 + dx)/tileSize, (y0 - dy)/tileSize, (x1 + dx)/tileSize, (y1 - dy)/tileSize));
		lineTiles.addAll(raytrace((x0 - dx)/tileSize, (y0 + dy)/tileSize, (x1 - dx)/tileSize, (y1 + dy)/tileSize));
		lineTiles.addAll(raytrace((x0 + dx)/tileSize, (y0 + dy)/tileSize, (x1 + dx)/tileSize, (y1 + dy)/tileSize));
		
		for (TileNode i : lineTiles) {
			if (collision_map[i.getY()][i.getX()] == 1) {
				collides = true;
			}
		}

		return collides;
	}
	

	public ArrayList<TileNode> raytrace(double x0, double y0, double x1, double y1)
	{
		ArrayList<TileNode> result = new ArrayList<TileNode>();
		
	    double dx = Math.abs(x1 - x0);
	    double dy = Math.abs(y1 - y0);

	    int x = (int) (Math.floor(x0));
	    int y = (int) (Math.floor(y0));

	    int n = 1;
	    int x_inc, y_inc;
	    double error;

	    if (dx == 0)
	    {
	        x_inc = 0;
	        error = INF;
	    }
	    else if (x1 > x0)
	    {
	        x_inc = 1;
	        n += (int)(Math.floor(x1)) - x;
	        error = (Math.floor(x0) + 1 - x0) * dy;
	    }
	    else
	    {
	        x_inc = -1;
	        n += x - (int)(Math.floor(x1));
	        error = (x0 - Math.floor(x0)) * dy;
	    }

	    if (dy == 0)
	    {
	        y_inc = 0;
	        error -= INF;
	    }
	    else if (y1 > y0)
	    {
	        y_inc = 1;
	        n += (int)(Math.floor(y1)) - y;
	        error -= (Math.floor(y0) + 1 - y0) * dx;
	    }
	    else
	    {
	        y_inc = -1;
	        n += y - (int)(Math.floor(y1));
	        error -= (y0 - Math.floor(y0)) * dx;
	    }

	    for (; n > 0; --n)
	    {
	    	result.add(new TileNode(x, y, null));

	        if (error > 0)
	        {
	            y += y_inc;
	            error -= dx;
	        }
	        else
	        {
	            x += x_inc;
	            error += dy;
	        }
	    }
	    
	    return result;
	}
}
