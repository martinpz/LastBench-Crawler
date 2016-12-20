/*
* Copyright (C) 2014 University of Freiburg.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package strategy;

import java.sql.Timestamp;

import runner.HF;
import de.umass.lastfm.Caller;
import de.umass.lastfm.User;
import entity.CUser;

public class MRHW {

	static int walk_id = 0;
	static int cStatus = 0;
	static boolean pausedWalk = false;
	
	static String strategyName = null;
	static String sNode = null;
	
	static int crawling_lvl = -1;
	
	public MRHW (String strategyNameF, String sNodeF, int crwlvl){
		
		strategyName = "MRHW, " + strategyNameF;
		sNode = sNodeF;
		crawling_lvl = crwlvl;
		
		try{}finally{
			run();			
		}

	}
	
	public MRHW (String strategyNameF, String sNodeF, int crwlvl, int walkId){

		try{
			walk_id = walkId;			
		}
		
		finally{
			new MRHW(strategyNameF, sNodeF, crwlvl);			
		}

	}
	
	public static void int_walk(){

		Caller.getInstance().setDebugMode(false);
		
		HF.print("############");
		HF.print("Time stamp: " + log.Helper.get_current_time());
		HF.print("Logging threshold: " + HF.threshold + " Sec");
		HF.print(String.format("## Strategy: %s", strategyName));
		
		long begin = System.currentTimeMillis();
			
		Api_Catcher.firstRun = true;
		
		if (crawling_lvl == 11 | crawling_lvl == 22){
			
			if( crawling_lvl == 11  ){
				crawling_lvl = 1;
			}
			if( crawling_lvl == 22 ){
				crawling_lvl = 2;
			}
			
			//walk_info to 0; 
			db.SQL.exe_insert(String.format("UPDATE walk_info SET status = %s WHERE walk_id = %s", 0, walk_id));
			db.SQL.exe_insert(String.format("UPDATE walk_steps SET status = %s WHERE walk_id = %s", 0, walk_id));
			
			pausedWalk = true;
			
		}else{
			String _sql_inert_walk_info = "insert into walk_info (startnode, strategy, status) values ('%s', '%s', '%s')";
			
			_sql_inert_walk_info = String.format(_sql_inert_walk_info, sNode, strategyName, 0);
			
			walk_id = db.SQL.exe_insert(_sql_inert_walk_info);			
		}

		HF.print(String.format("### Walk_Id: %s, Start node: %s", walk_id, sNode));
		
		if (pausedWalk){
			HF.print("### WALK have been paused and currently running running again");
		}
		
		// ********** walk_steps
		
			String _sql_inert_walk_steps = "insert into walk_steps (walk_id, visited_user, step, status) values ('%s', '%s', %s, '%s')";
			
			_sql_inert_walk_steps = String.format(_sql_inert_walk_steps, walk_id, sNode, 0, 0);
			
			db.SQL.exe_insert(_sql_inert_walk_steps);
		
		// ********** END OF ********** walk_steps
		
		try{}finally{
			run(sNode);			
		}

		cStatus = db.SQL.exe_select(String.format("SELECT status FROM walk_info WHERE walk_id = %s", walk_id));

		if (cStatus == 4){
			HF.print( "##" + " Crawler have recieved soft stop signal to pause this walk");
		}
		
		HF.print( "###" + HF.get_cost_pure(strategyName, begin));
		HF.print("############");
		
	}
	
	public static float gen_random(){
		
		int  min = 0, max = 1;
		
		return (float) (Math.random() * ( max - min ));
		
	}
	
	public static int calc_node_degree_db(int uId){
		
		//#TODO need to fix the bug here
		
		String _countMyFriends = "SELECT COUNT(*) FROM user_friends where user_id = %s";
		
		_countMyFriends = String.format(_countMyFriends, uId);
		
		return db.SQL.exe_select2(_countMyFriends); 
	}

	
	public static void run (String uName) {
		
		// if the walk haven't been paused
		if(!pausedWalk){
			try{}finally{
				CUser.add_new(uName, crawling_lvl);		
			}
		}

		String _getRandomFriend = "SELECT id FROM user ORDER BY RAND() LIMIT 1";
		
		//check soft_stop !!!
		// status != 4 user requested NO soft stop
		
		cStatus = db.SQL.exe_select(String.format("SELECT status FROM walk_info WHERE walk_id = %s", walk_id));

		if (cStatus != 4){
			
			boolean sucess_candidate = false;

			int _originalDegree = calc_node_degree_db(db.Lib.get_id("user", uName));
			
			int nodeWID = 0;
			
			HF.print(String.format("#### MHRW RANDOM SELECTION START"));
			
			do {
	
				cStatus = db.SQL.exe_select(String.format("SELECT status FROM walk_info WHERE walk_id = %s", walk_id));
				
				nodeWID = db.SQL.exe_select2(_getRandomFriend);
				
				float _randomP = gen_random();
				
				// change made @ 26.05, to get the number of friends via API call instead of counting explored friends at DB
					//int _candidateDegree = calc_node_degree_db(nodeWID);
					String _candidateName = db.Lib.get_name("user", nodeWID);
					int _candidateDegree = CUser.count_friends_api(_candidateName);
				
				//#TODO_ need to get the candidateDegree via the API
				
				
				//System.out.println(String.format("Random p is :: %s", _randomP));
				//System.out.println(String.format("W degree is :: %s", _originalDegree));
	
				if(_candidateDegree <= 0){
					sucess_candidate = true;
				}
				
				if (_candidateDegree > 0){
					
					double _tmp_calc = (double) _originalDegree/_candidateDegree;
					
					if(_randomP <= _tmp_calc){
						sucess_candidate = true;
					}
					else{
						sucess_candidate = false;
						
						HF.print(String.format("Next user: %s; d_v/d_w = %s/%s; %s", nodeWID, _originalDegree, _candidateDegree, "failed, choosing next user"));
					}
				}
				
				if (sucess_candidate){
	
					HF.print(String.format("Next user: %s; d_v/d_w = %s/%s; %s", nodeWID, _originalDegree, _candidateDegree, "succeed, starting ONE-Hop Walk()"));
					
					db.SQL.exe_insert(String.format("UPDATE walk_steps SET step = step +1, visited_user = %s, status = %s WHERE walk_id = %s", nodeWID, 0, walk_id));
					
					try{}finally{
						CUser.add_new(db.Lib.get_name("user", nodeWID), crawling_lvl);							
					}

				}
			//Fixing bug [06.06 at 23:00] to enable the MRHW to run infinitely many times
			//} while (cStatus != 4 & nodeWID != 0 & !sucess_candidate );
			} while (cStatus != 4 & nodeWID != 0 );
		
			HF.print(String.format("#### MHRW RANDOM END ####"));
			
		}
	}

	public static void run(){
		
		String _sql_update_walk_info = "UPDATE walk_info SET status = %s WHERE walk_id = %s";

		try {
			
			try{}finally{
				int_walk();				
			}
			
		} catch (Exception e) {
			
			log.System.add_to_log("ERROR CTACHED AT MAIN" + e);
		
			_sql_update_walk_info = String.format( _sql_update_walk_info , 3, walk_id);
			 db.SQL.exe_insert(_sql_update_walk_info);	 
		}
	
		java.util.Date date= new java.util.Date();

		String _end_ts = "" + new Timestamp(date.getTime());
		 
		cStatus = db.SQL.exe_select(String.format("SELECT status FROM walk_info WHERE walk_id = %s", walk_id));
		
		if (cStatus == 4){
			
			// update walk_info && walk_steps status = 1 (paused)
			db.SQL.exe_insert(String.format("UPDATE walk_info SET status = 1 WHERE walk_id = %s", walk_id));
			db.SQL.exe_insert(String.format("UPDATE walk_steps SET status = 1, end_ts = '%s' WHERE walk_id = %s", _end_ts, walk_id));
			
		}
		
		if (cStatus == 0){
			
			_sql_update_walk_info = String.format( _sql_update_walk_info , 2, walk_id);
			db.SQL.exe_insert(_sql_update_walk_info);

			db.SQL.exe_insert(String.format("UPDATE walk_steps SET status = %s, end_ts = '%s' WHERE walk_id = %s", 2, _end_ts,  walk_id));
			
		}
		
	}
}
