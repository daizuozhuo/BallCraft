package hkust.comp3111h.ballcraft.client;

import hkust.comp3111h.MyApplication;
import hkust.comp3111h.ballcraft.BallCraft;
import hkust.comp3111h.ballcraft.data.GameData;
import hkust.comp3111h.ballcraft.graphics.skilleffects.Explosion;
import hkust.comp3111h.ballcraft.graphics.skilleffects.GrowRoot;
import hkust.comp3111h.ballcraft.graphics.skilleffects.MassOverlord;
import hkust.comp3111h.ballcraft.graphics.skilleffects.Mine;
import hkust.comp3111h.ballcraft.graphics.skilleffects.RockBump;
import hkust.comp3111h.ballcraft.server.Ball;
import hkust.comp3111h.ballcraft.server.Server;
import hkust.comp3111h.ballcraft.server.ServerAdapter;
import hkust.comp3111h.ballcraft.server.ServerGameState;
import hkust.comp3111h.ballcraft.skills.Skill;

import org.jbox2d.common.Vec2;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Vibrator;

public class Client extends IntentService {
    
    private static GameInput input;
    private static Vibrator vibrator;
    private static boolean running = false;

    private static String myself = "" + BallCraft.myself;
    private static String enemy = "" + BallCraft.enemy;
    
    /**
     * Set to true when the game init msg is sent to the Client
     */
    public static boolean gameInited = false;
    
    public static boolean remoteServerInited = false;
    
    private static boolean vibrOn;
    
    public static boolean playerDied = false;
    
    public static boolean inputStarted = false;
    
    public static int selfScore = 0;
    public static int enemyScore = 0;
    
    private static boolean active = false;
    
    public Client() {
        super("ClientService");
        vibrator = (Vibrator) MyApplication.getAppContext()
                .getSystemService(Service.VIBRATOR_SERVICE);
        vibrOn = GameData.getVibrPref();
        input = new GameInput();
    }

    public static void setInputAcceleration(float x, float y) {
        input.acceleration.x = -x;
        input.acceleration.y = -y;
        inputStarted = true;
    }

    public static void castSkill(Skill skill) {
        input.addSkill(skill);
    }

	private static void handleMessage(String string) {
		String[] parts = string.split(":");
		
		if (parts[0].equals("dead")) {
			if(parts[1].equals(myself)) {
			    playerDied = true;
			    enemyScore++;
			} else {
			    selfScore++;
			}
			
		} else if (parts[0].equals("collision")) {
			String [] collision = parts[1].split(",");
			if ((collision[0].equals(myself) || collision[1].equals(myself)) && 
				(collision[0].equals(enemy) || collision[1].equals(enemy))) {
			    if (vibrOn) {
					vibrator.vibrate(50);
			    }
			}	
			
		}		
		else if (parts[0].equals("skillInit"))
		{
			String [] str = parts[1].split("&");
			int skillID = Integer.parseInt(str[0]);
			
			switch (skillID) {
			case BallCraft.Skill.GROW_ROOT:
			    int rootPlayer = Integer.parseInt(str[1]);
			    Ball rootBall = ClientGameState.getClientGameState().balls.get(rootPlayer);
			    ClientGameState.getClientGameState().addSkillEffect(
			            skillID, new GrowRoot(rootBall));
			    break;
			    
			case BallCraft.Skill.POISON:
			    break;
			    
			case BallCraft.Skill.MASS_OVERLORD:
			    int massPlayer = Integer.parseInt(str[1]);
			    Ball massBall = ClientGameState.getClientGameState().balls.get(massPlayer);
			    ClientGameState.getClientGameState().addSkillEffect(
			            skillID, new MassOverlord(massBall));
			    break;
				
			case BallCraft.Skill.ROCK_BUMP:
				int enemyID = Integer.parseInt(str[1]);
				Ball bumpedBall = ClientGameState.getClientGameState().balls.get(enemyID);
				ClientGameState.getClientGameState().addSkillEffect(
				        skillID, new RockBump(bumpedBall));
			    break;
			    
			case BallCraft.Skill.WATER_PROPEL:
				break;
				
			case BallCraft.Skill.SLIPPERY:
			    break;
			    
			case BallCraft.Skill.IRON_WILL:
			    break;
			    
			case BallCraft.Skill.FLASHBANG:
			    break;
			    
			case BallCraft.Skill.FLAME_THROW:
			    break;
			    
			case BallCraft.Skill.LANDMINE:
				String [] position = str[1].split(",");
				float x = Float.valueOf(position[0]);
				float y = Float.valueOf(position[1]);
				int id = Integer.valueOf(position[2]);
				ClientGameState.getClientGameState().addSkillEffect(id, new Mine(new Vec2(x, y), id));
			    break;
			    
			case BallCraft.Skill.STEALTH:
			    break;
			    
			case BallCraft.Skill.MIDNIGHT:
			    break;
			}
		}
		else if (parts[0].equals("skillFinish"))
		{
			String [] str = parts[1].split("&");
			int skillID = Integer.valueOf(str[0]);
			switch (skillID) {
			
			case BallCraft.Skill.GROW_ROOT:
			    ClientGameState.getClientGameState().deleteDrawable(skillID);
			    break;
			    
			case BallCraft.Skill.WATER_PROPEL:
				break;
				
			case BallCraft.Skill.LANDMINE:
				String [] position = str[1].split(",");
				int id = Integer.valueOf(position[2]);
				Mine m = (Mine) ClientGameState.getClientGameState().getDrawable(id);
				ClientGameState.getClientGameState().addSkillEffect(-1, new Explosion(m.x, m.y, 0));
				ClientGameState.getClientGameState().deleteDrawable(id);
			    break;
			}
		}

	}

	public static void processSerializedUpdate(String serialized) {
		String[] unitStrs = serialized.split(";");
		String[] messages = unitStrs[0].split("/");
		for (String msg:messages)
		{
			handleMessage(msg);
		}
		ClientGameState.getClientGameState().applyUpdater(unitStrs[1]);
	}

    public void run() {
    	int time = 0;
    	boolean started = false;
        while (running) {
            if (active) {
	            if (Server.inited || remoteServerInited) {
	                if (inputStarted) {
		                ServerAdapter.sendToServer(input);
		                input.clearSkills();
		                started = true;
	                }
	            }
	            try {
	                Thread.sleep(20);
	            } catch (InterruptedException e) {}
	            
	            if (!started)
	            {
	            	time++;
	            	if (time > 1500)
	            	{
	                	Server.stop();
	                    Client.stop();
	                    ClientGameState.clear();
	                    if (BallCraft.isServer) {
	                        ServerGameState.clear();
	                    }
	                    return;
	            	}
	            }
            }
        }

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        running = true;
        active = true;
        this.run();
    }

    public static void stop() {
        running = false;
    } 
    
 	public static boolean isRun(){
    	return running;
 	}
 	
 	public static boolean isGameInited() {
        return gameInited;
    }
 	
 	public static void deactivate() {
 	    active = false;
 	}
 	
}
