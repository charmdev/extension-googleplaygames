package extension.gpg;

import haxe.Int64;
import haxe.Timer;

class GooglePlayGames {

	public static inline var ACHIEVEMENT_STATUS_LOCKED:Int = 0;
	public static inline var ACHIEVEMENT_STATUS_UNLOCKED:Int = 1;

	//////////////////////////////////////////////////////////////////////
	///////////// LOGIN & INIT 
	//////////////////////////////////////////////////////////////////////

	private static var javaInit(default,null) : GooglePlayGames->Void = function(callbackObject:GooglePlayGames):Void{ trace("stuB!!!");}
	public static var login(default,null) : Void->Void = function():Void{}
	public static var loginSilently(default,null) : Void->Void = function():Void{}
	public static var logout(default,null) : Void->Void = function():Void{}

	//////////////////////////////////////////////////////////////////////
	///////////// PLAYER INFO
	//////////////////////////////////////////////////////////////////////

	public static var getPlayerId(default,null) : Void->String = function():String { return null; }
	public static var getPlayerDisplayName(default,null) : Void->String = function():String { return null; }
	public static var getIdToken(default, null): Void->String = function():String { return null;}

	
	//////////////////////////////////////////////////////////////////////
	///////////// ACHIEVEMENTS
	//////////////////////////////////////////////////////////////////////

	public static var unlock(default,null) : String->Bool = function(id:String):Bool{return false;}
	public static var increment(default,null) : String->Int->Bool = function(id:String,step:Int):Bool{return false;}
	public static var reveal(default,null) : String->Bool = function(id:String):Bool{return false;}
	public static var setSteps(default,null) : String->Int->Bool = function(id:String,steps:Int):Bool{return false;}
	public static var getAchievementStatus(default,null) : String->Bool = function(id:String):Bool{return false;}
	public static var getCurrentAchievementSteps(default,null) : String->Bool = function(id:String):Bool{return false;}

	//////////////////////////////////////////////////////////////////////
	///////////// HAXE IMPLEMENTATIONS
	//////////////////////////////////////////////////////////////////////

	public static function init(){
		#if android
			if(initted){
				trace("GooglePlayGames: WONT INIT TWICE!");
				//GooglePlayGames.login();
				return;
			}
			initted=true;
			trace("GooglePlayGames: init()");
			try {
				// LINK JNI METHODS
				javaInit = openfl.utils.JNI.createStaticMethod("com/gpgex/GooglePlayGames", "init", "(Lorg/haxe/lime/HaxeObject;)V");
				login = openfl.utils.JNI.createStaticMethod("com/gpgex/GooglePlayGames", "login", "()V");
				loginSilently = openfl.utils.JNI.createStaticMethod("com/gpgex/GooglePlayGames", "loginSilently", "()V");
				logout = openfl.utils.JNI.createStaticMethod("com/gpgex/GooglePlayGames", "logout", "()V");
				unlock = openfl.utils.JNI.createStaticMethod("com/gpgex/GooglePlayGames", "unlock", "(Ljava/lang/String;)Z");
				increment = openfl.utils.JNI.createStaticMethod("com/gpgex/GooglePlayGames", "increment", "(Ljava/lang/String;I)Z");
				reveal = openfl.utils.JNI.createStaticMethod("com/gpgex/GooglePlayGames", "reveal", "(Ljava/lang/String;)Z");
				setSteps = openfl.utils.JNI.createStaticMethod("com/gpgex/GooglePlayGames", "setSteps", "(Ljava/lang/String;I)Z");
				getAchievementStatus = openfl.utils.JNI.createStaticMethod("com/gpgex/GooglePlayGames", "getAchievementStatus", "(Ljava/lang/String;)Z");
				getCurrentAchievementSteps = openfl.utils.JNI.createStaticMethod("com/gpgex/GooglePlayGames", "getCurrentAchievementSteps", "(Ljava/lang/String;)Z");
				getPlayerId = openfl.utils.JNI.createStaticMethod("com/gpgex/GooglePlayGames", "getPlayerId", "()Ljava/lang/String;");
				getPlayerDisplayName = openfl.utils.JNI.createStaticMethod("com/gpgex/GooglePlayGames", "getPlayerDisplayName", "()Ljava/lang/String;");
				getIdToken = openfl.utils.JNI.createStaticMethod("com/gpgex/GooglePlayGames", "getIdToken", "()Ljava/lang/String;");
			} catch(e:Dynamic) {
				trace("GooglePlayGames linkMethods Exception: "+e);
			}
			javaInit(getInstance());
		#end
	}

	
	//////////////////////////////////////////////////////////////////////
	///////////// EVENTS RECEPTION
	//////////////////////////////////////////////////////////////////////

	public static var onLoginResult:Int->Void=null;
	public static var onGetPlayerAchievementStatus : String->Int->Void = null;
	public static var onLoadConnectedPlayers : Array<Player>->Void = null;
	public static var onLoadInvitablePlayers : Array<Player>->Void = null;
	public static var onLoadPlayerImage : String->String->Void = null;
	public static var onGetPlayerCurrentSteps : String->Int->Void = null;
	public static var onLogOut: Void->Void = null;

	public static var initted:Bool=false;
	private static var instance:GooglePlayGames=null;

	private static function getInstance():GooglePlayGames{
		if(instance==null) instance=new GooglePlayGames();
		return instance;
	}

	private function new(){}

	//posible returns are: -1 = login failed | 0 = initiated login | 1 = login success
	//the event is fired in differents circumstances, like if you init and do not login,
	//can return -1 or 1 but if you log in, will return a series of 0 -1 0 -1 if there is no
	//connection for example. test it and adapt it to your code and logic.
	public function loginResultCallback(res:Int) {
		if(onLoginResult!=null) Timer.delay(function(){ onLoginResult(res); }, 0);
	}

	public function onLogoutCallback() {
		if (onLogOut != null)  Timer.delay(function(){ onLogOut(); }, 0);
	}

	//////////////////////////////////////////////////////////////////////
	///////////// ACHIEVEMENT STATUS
	//////////////////////////////////////////////////////////////////////
	public function onGetAchievementStatus(idAchievement:String, state:Int) {
		if (onGetPlayerAchievementStatus != null) Timer.delay(function(){ onGetPlayerAchievementStatus(idAchievement, state); },0);
	}

	//////////////////////////////////////////////////////////////////////
	///////////// ACHIEVEMENTS CURRENT STEPS
	//////////////////////////////////////////////////////////////////////

	public function onGetAchievementSteps(idAchievement:String, steps:Int) {
		if (onGetPlayerCurrentSteps != null) Timer.delay(function(){ onGetPlayerCurrentSteps(idAchievement, steps); },0);
	}

	//////////////////////////////////////////////////////////////////////
	///////////// PICTURES
	//////////////////////////////////////////////////////////////////////

	public function onGetPlayerImage(id:String, path:String) {
		if(onLoadPlayerImage!=null) Timer.delay(function(){ onLoadPlayerImage(id, path); },0);
	}
	
}